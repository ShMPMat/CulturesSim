package shmp.simulation.culture.group.cultureaspect

import shmp.random.randomElement
import shmp.random.testProbability
import shmp.simulation.culture.aspect.AspectPool
import shmp.simulation.culture.aspect.ConverseWrapper
import shmp.simulation.culture.group.place.StaticPlace
import shmp.simulation.culture.group.centers.Group
import shmp.simulation.culture.group.centers.Trait
import shmp.simulation.culture.group.centers.makeNegativeChange
import shmp.simulation.culture.group.centers.makePositiveChange
import shmp.simulation.culture.group.cultureaspect.worship.Worship
import shmp.simulation.culture.group.reason.BetterAspectUseReason
import shmp.simulation.culture.group.reason.Reason
import shmp.simulation.culture.group.resource_behaviour.getRandom
import shmp.simulation.culture.thinking.language.templates.TemplateBase
import shmp.simulation.culture.thinking.language.templates.constructTextInfo
import shmp.simulation.culture.thinking.meaning.Meme
import shmp.simulation.culture.thinking.meaning.MemeSubject
import shmp.simulation.culture.thinking.meaning.makeAspectMemes
import shmp.simulation.space.resource.Resource
import shmp.simulation.space.tile.TileTag
import kotlin.math.absoluteValue
import kotlin.math.pow
import kotlin.math.sign
import kotlin.random.Random


fun createDepictObject(
        meaningAspects: Collection<ConverseWrapper>,
        meme: Meme?,
        group: Group,
        random: Random
): DepictObject? =
        if (meaningAspects.isNotEmpty() && meme != null)
            DepictObject(
                    meme,
                    randomElement(meaningAspects.flatMap { it.producedResources }, random),
                    getRandom(group, random)
            )
        else null

fun createAestheticallyPleasingObject(resource: Resource?, group: Group, random: Random): CherishedResource? =
        resource?.let {
            CherishedResource(resource, getRandom(group, random))
        }

fun createTale(group: Group, templateBase: TemplateBase, random: Random): Tale? {
    val template = templateBase.randomSentenceTemplate
            ?: return null

    val info = constructTextInfo(
            group.cultureCenter,
            templateBase,
            random
    ) ?: return null

    return Tale(template, info)
}

fun createSimpleConcept(group: Group, random: Random): Concept? {
    val traitCenter = group.cultureCenter.traitCenter

    val trait = randomElement(Trait.values(), { traitCenter.value(it).absoluteValue + 0.001 }, random)
    val magnitude = random.nextDouble(0.1, 10.0)

    val agitateForChance = traitCenter.value(trait).let { (it.absoluteValue.pow(0.3) * it.sign + 1) / 2 }
    val agitateFor = testProbability(agitateForChance, random)

    return if (agitateFor)
        Concept(MemeSubject("$trait is good"), listOf(makePositiveChange(trait) * magnitude))
    else
        Concept(MemeSubject("$trait is bad"), listOf(makeNegativeChange(trait) * magnitude))
}

fun createRitual(reason: Reason?, group: Group, random: Random): Ritual? {
    return when (reason) {
        is BetterAspectUseReason -> {
            createBetterAspectUseReasonRitual(
                    reason,
                    group.cultureCenter.aspectCenter.aspectPool,
                    group,
                    random
            )
        }
        else -> null
    }
}

fun createBetterAspectUseReasonRitual(
        reason: BetterAspectUseReason,
        aspectPool: AspectPool,
        group: Group,
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
                return AspectRitual(myAspect, getRandom(group, random), reason)
        } else {
            val options: List<ConverseWrapper> = aspectPool.converseWrappers
                    .filter { meme.observerWord in it.producedResources.map { r -> r.baseName } }

            return if (options.isNotEmpty())
                AspectRitual(randomElement(options, random), getRandom(group, random), reason)
            else {
                val meaningAspects = aspectPool.getMeaningAspects()
                val aspect = createDepictObject(
                        meaningAspects,
                        randomElement(aspectMemes, random),
                        group,
                        random
                ) ?: continue

                CultureAspectRitual(aspect, reason) //TODO make complex tales;
            }
        }
    }

    return null
}

fun createSpecialPlaceForWorship(worship: Worship, group: Group, random: Random): SpecialPlace? {
    val tag = worship.toString().replace(' ', '_')
    val tilesWithoutPlaces = group.territoryCenter.territory.tiles.filter { t ->
        t.getTilesInRadius(2).all { it.tagPool.getByType(tag).isEmpty() }
    }
    if (tilesWithoutPlaces.isEmpty())
        return null

    val number = group.territoryCenter.territory.size - tilesWithoutPlaces.size
    val existsInCenter = !tilesWithoutPlaces.contains(group.territoryCenter.territory.center)
    val tile =
            if (!existsInCenter && testProbability(0.5, random))
                group.territoryCenter.center
            else randomElement(tilesWithoutPlaces, random)

    return SpecialPlace(StaticPlace(tile, TileTag(tag + number, tag)))
}
