package io.tashtabash.sim.culture.group.centers

import io.tashtabash.generator.culture.worldview.reasoning.*
import io.tashtabash.generator.culture.worldview.reasoning.concept.IdeationalConcept
import io.tashtabash.generator.culture.worldview.reasoning.concept.ObjectConcept
import io.tashtabash.random.singleton.*
import io.tashtabash.sim.CulturesController.Companion.session
import io.tashtabash.sim.culture.group.centers.util.*
import io.tashtabash.sim.culture.group.cultureaspect.*
import io.tashtabash.generator.culture.worldview.reasoning.convertion.*
import io.tashtabash.sim.culture.group.cultureaspect.worship.Worship
import io.tashtabash.sim.culture.group.reason.Reason
import io.tashtabash.generator.culture.worldview.toMeme
import io.tashtabash.sim.space.resource.Resource
import java.util.*
import kotlin.math.pow


class CultureAspectCenter(val reasonField: ReasonField) {
    val aspectPool = MutableCultureAspectPool(mutableSetOf())

    private val _aestheticallyPleasingResources: MutableSet<Resource> = HashSet()
    val aestheticallyPleasingResources: Set<Resource>
        get() = _aestheticallyPleasingResources

    val reasonsWithSystems: MutableSet<Reason> = HashSet()

    internal fun update(group: Group) {
        for (aspect in aspectPool.all)
            aspect.use(group)
    }

    fun addCultureAspect(cultureAspect: CultureAspect?): Boolean {
        cultureAspect
                ?: return false

        aspectPool.add(cultureAspect)

        if (cultureAspect is CherishedResource)
            _aestheticallyPleasingResources.add(cultureAspect.resource)

        return true
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


fun generateCultureConversions(
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

fun generateCommonReasonings(name: String) = ReasonComplex(
        COMMON_REASONS,
        setOf(
                QualityReasoning(ObjectConcept.ArbitraryObjectConcept(name.toMeme()), IdeationalConcept.Mortality)
        )
)
