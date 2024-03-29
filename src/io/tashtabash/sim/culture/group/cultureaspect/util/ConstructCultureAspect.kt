package io.tashtabash.sim.culture.group.cultureaspect.util

import io.tashtabash.random.randomElement
import io.tashtabash.random.singleton.randomElement
import io.tashtabash.random.singleton.testProbability
import io.tashtabash.random.testProbability
import io.tashtabash.sim.culture.aspect.AspectPool
import io.tashtabash.sim.culture.aspect.ConverseWrapper
import io.tashtabash.sim.culture.group.place.StaticPlace
import io.tashtabash.sim.culture.group.centers.Group
import io.tashtabash.sim.culture.group.centers.Trait
import io.tashtabash.sim.culture.group.centers.toNegativeChange
import io.tashtabash.sim.culture.group.centers.toPositiveChange
import io.tashtabash.sim.culture.group.cultureaspect.*
import io.tashtabash.generator.culture.worldview.reasoning.concept.ObjectConcept
import io.tashtabash.sim.culture.group.cultureaspect.worship.Worship
import io.tashtabash.sim.culture.group.reason.BetterAspectUseReason
import io.tashtabash.sim.culture.group.reason.Reason
import io.tashtabash.sim.culture.group.resource_behaviour.getRandom
import io.tashtabash.sim.culture.thinking.language.templates.TemplateBase
import io.tashtabash.sim.culture.thinking.language.templates.constructTextInfo
import io.tashtabash.generator.culture.worldview.Meme
import io.tashtabash.sim.culture.thinking.meaning.makeAspectMemes
import io.tashtabash.sim.space.resource.Resource
import io.tashtabash.sim.space.tile.TileTag
import kotlin.math.absoluteValue
import kotlin.math.pow
import kotlin.math.sign
import kotlin.random.Random


fun createDepictObject(
        meaningAspects: Collection<ConverseWrapper>,
        meme: Meme?,
        objectConcept: ObjectConcept?
): DepictObject? =
        if (meaningAspects.isNotEmpty() && meme != null)
            DepictObject(
                    meme,
                    objectConcept,
                    meaningAspects.flatMap { it.producedResources }.randomElement(),
                    getRandom()
            )
        else null

fun createAestheticallyPleasingObject(resource: Resource?): CherishedResource? = resource?.let {
    CherishedResource(resource, getRandom())
}

fun createTale(group: Group, templateBase: TemplateBase): Tale? {
    val template = templateBase.randomSentenceTemplate
            ?: return null

    val info = constructTextInfo(group.cultureCenter, templateBase)
            ?: return null

    return Tale(template, info)
}

fun createSimpleConcept(group: Group, random: Random): Concept? {
    val traitCenter = group.cultureCenter.traitCenter

    val trait = randomElement(Trait.values(), { traitCenter.processedValue(it).absoluteValue + 0.001 }, random)
    val magnitude = random.nextDouble(0.1, 10.0)

    val agitateForChance = traitCenter.processedValue(trait).let { (it.absoluteValue.pow(0.3) * it.sign + 1) / 2 }
    val agitateFor = testProbability(agitateForChance, random)

    return if (agitateFor)
        Concept(Meme("$trait is good"), listOf(trait.toPositiveChange() * magnitude))
    else
        Concept(Meme("$trait is bad"), listOf(trait.toNegativeChange() * magnitude))
}

fun createRitual(reason: Reason?, group: Group, random: Random): Ritual? {
    return when (reason) {
        is BetterAspectUseReason -> {
            createBetterAspectUseReasonRitual(
                    reason,
                    group.cultureCenter.aspectCenter.aspectPool,
                    random
            )
        }
        else -> null
    }
}

fun createBetterAspectUseReasonRitual(
        reason: BetterAspectUseReason,
        aspectPool: AspectPool,
        random: Random
): Ritual? {
    val converseWrapper = reason.converseWrapper
    val (aspectMemes, second) = makeAspectMemes(converseWrapper)

    aspectMemes.addAll(second)
    aspectMemes.shuffle(random)

    for (meme in aspectMemes) { //TODO maybe depth check
        if (meme.observerWord == converseWrapper.name)
            continue

        if (aspectPool.contains(meme.observerWord)) {
            val myAspect = aspectPool.getValue(meme.observerWord)

            if (myAspect is ConverseWrapper)
                return AspectRitual(myAspect, getRandom(), reason)
        } else {
            val options: List<ConverseWrapper> = aspectPool.converseWrappers
                    .filter { meme.observerWord in it.producedResources.map { r -> r.baseName } }

            return if (options.isNotEmpty())
                AspectRitual(randomElement(options, random), getRandom(), reason)
            else {
                val meaningAspects = aspectPool.getMeaningAspects()
                val aspect = createDepictObject(
                        meaningAspects,
                        aspectMemes.randomElement(),
                        null
                ) ?: continue

                CultureAspectRitual(aspect, reason) //TODO make complex tales;
            }
        }
    }

    return null
}

fun createSpecialPlaceForWorship(worship: Worship, group: Group): SpecialPlace? {
    val tag = worship.toString().replace(' ', '_')
    val tilesWithoutPlaces = group.territoryCenter.territory.tiles.filter { t ->
        t.getTilesInRadius(2).all { it.tagPool.getByType(tag).isEmpty() }
    }
    if (tilesWithoutPlaces.isEmpty())
        return null

    val number = group.territoryCenter.territory.size - tilesWithoutPlaces.size
    val existsInCenter = !tilesWithoutPlaces.contains(group.territoryCenter.territory.center)
    val tile =
            if (!existsInCenter && 0.5.testProbability())
                group.territoryCenter.center
            else tilesWithoutPlaces.randomElement()

    return SpecialPlace(StaticPlace(tile, TileTag(tag + number, tag)))
}
