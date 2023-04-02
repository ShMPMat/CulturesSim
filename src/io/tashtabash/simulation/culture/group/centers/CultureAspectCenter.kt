package io.tashtabash.simulation.culture.group.centers

import io.tashtabash.random.SampleSpaceObject
import io.tashtabash.random.singleton.*
import io.tashtabash.simulation.CulturesController.Companion.session
import io.tashtabash.simulation.culture.group.centers.util.*
import io.tashtabash.simulation.culture.group.cultureaspect.*
import io.tashtabash.generator.culture.worldview.reasoning.ReasonField
import io.tashtabash.generator.culture.worldview.reasoning.Reasoning
import io.tashtabash.generator.culture.worldview.reasoning.convertion.*
import io.tashtabash.simulation.culture.group.cultureaspect.util.*
import io.tashtabash.simulation.culture.group.cultureaspect.worship.Worship
import io.tashtabash.simulation.culture.group.reason.Reason
import io.tashtabash.simulation.culture.group.reason.constructBetterAspectUseReason
import io.tashtabash.generator.culture.worldview.Meme
import io.tashtabash.generator.culture.worldview.toMeme
import io.tashtabash.simulation.space.resource.Resource
import java.util.*
import kotlin.math.pow


class CultureAspectCenter(val reasonField: ReasonField) {
    val aspectPool = MutableCultureAspectPool(mutableSetOf())
    private val aestheticallyPleasingResources: MutableSet<Resource> = HashSet()
    private val reasonsWithSystems: MutableSet<Reason> = HashSet()

    internal fun update(group: Group) {
        useCultureAspects(group)
        addRandomCultureAspect(group)
        mutateCultureAspects(group)
        updateReasonings(group)
    }

    private fun updateReasonings(group: Group) {
        session.reasoningUpdate.chanceOf {
            val newReasonings = reasonField.update(listOf(group.name.toMeme()))
            processReasonings(newReasonings)
        }
    }

    private fun processReasonings(reasonings: List<Reasoning>) {
        reasonings.forEach { addCultureAspect(it.toConcept()) }
        reasonings.forEach { addCultureAspect(it.toCherishedResource()) }
    }

    fun addCultureAspect(cultureAspect: CultureAspect?) {
        cultureAspect ?: return

        aspectPool.add(cultureAspect)

        if (cultureAspect is CherishedResource)
            aestheticallyPleasingResources.add(cultureAspect.resource)
    }

    private fun useCultureAspects(group: Group) = aspectPool.all.forEach { it.use(group) }

    fun addRandomCultureAspect(group: Group) {
        session.cultureAspectBaseProbability.chanceOfNot {
            return
        }

        val cultureAspect = when (AspectRandom.values().randomElement()) {
            AspectRandom.AestheticallyPleasing -> createAestheticallyPleasingObject(
                    group.cultureCenter.aspectCenter.aspectPool.producedResources
                            .filter { it.genome.isDesirable }
                            .filter { !aestheticallyPleasingResources.contains(it) }
                            .maxByOrNull { it.genome.baseDesirability }
            )
            AspectRandom.Ritual -> createRitual(//TODO recursively go in dependencies;
                    constructBetterAspectUseReason(
                            group.cultureCenter.aspectCenter.aspectPool.converseWrappers.sortedBy { it.name },
                            reasonsWithSystems,
                            session.random
                    ),
                    group,
                    session.random
            )
            AspectRandom.Tale -> createTale(
                    group,
                    session.templateBase
            )
            AspectRandom.Concept -> createSimpleConcept(
                    group,
                    session.random
            )
        }

        addCultureAspect(cultureAspect)
    }

    fun mutateCultureAspects(group: Group) {
        session.groupCultureAspectCollapse.chanceOfNot {
            return
        }

        when (ChangeRandom.values().randomElement()) {
            ChangeRandom.RitualSystem -> joinSimilarRituals()
            ChangeRandom.TaleSystem -> joinSimilarTales()
            ChangeRandom.Worship -> addCultureAspect(takeOutWorship(reasonField, aspectPool))
            ChangeRandom.God -> makeGod(group)
        }
    }

    private fun joinSimilarRituals() = takeOutSimilarRituals(aspectPool)?.let { system ->
        addCultureAspect(system)
        reasonsWithSystems.add(system.reason)
    }

    private fun makeGod(group: Group) = takeOutGod(aspectPool, group)?.let { cult ->
        addCultureAspect(cult)
    }

    private fun joinSimilarTales() = takeOutSimilarTales(aspectPool)?.let { system ->
        addCultureAspect(system)
    }

    private fun getNeighbourCultureAspects(group: Group) =
            group.relationCenter.relatedGroups.flatMap { n ->
                n.cultureCenter.cultureAspectCenter.aspectPool.all.map { a -> a to n }
            }

    private fun getNeighbourCultureAspects(group: Group, predicate: (CultureAspect) -> Boolean) =
            getNeighbourCultureAspects(group).filter { (f) -> predicate(f) }

    fun adoptCultureAspects(group: Group) {
        session.groupAspectAdoptionProb.chanceOfNot {
            return
        }

        val aspect = getNeighbourCultureAspects(group) { !aspectPool.contains(it) }
                .randomElementOrNull { (_, g) -> group.relationCenter.getNormalizedRelation(g) }
                ?.first
                ?.adopt(group)
                ?: return

        if (shouldAdopt(aspect))
            addCultureAspect(aspect)
    }

    private fun shouldAdopt(aspect: CultureAspect): Boolean {
        if (aspect is Worship) {
            val similarGodsAmount = aspectPool.worships.count { w ->
                w.worshipObject.memes.any { it in aspect.worshipObject.memes }
            }
            val probability = 1.0 / (similarGodsAmount + 1)
            return probability.pow(2).testProbability()
        }
        return true
    }

    fun die(group: Group) = aspectPool.all.forEach { it.die(group) }

    override fun toString() = """
        |Culture Aspects:
        |${aspectPool.all.joinToString()}
        |
        |Reasons:
        |$reasonField
    """.trimMargin()
}


private enum class AspectRandom(override val probability: Double) : SampleSpaceObject {
    Tale(3.0),
    AestheticallyPleasing(1.0),
    Ritual(1.0),
    Concept(1.0)
}

private enum class ChangeRandom(override val probability: Double) : SampleSpaceObject {
    RitualSystem(3.0),
    TaleSystem(3.0),
    Worship(2.0),
    God(1.0),
}


fun cultureConversions(
        memoryCenter: MemoryCenter,
        aspectCenter: AspectCenter,
        stratumCenter: StratumCenter
) = listOf(
        MemoryConversion(memoryCenter),
        AspectConversion(aspectCenter),
        StratumConversion(stratumCenter),
) + groupBaseConversions()

fun groupBaseConversions() = baseConversions() +
        listOf(AspectResourcesConversion, StratumResourcesConversion)
