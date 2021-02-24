package shmp.simulation.culture.group.centers

import shmp.random.SampleSpaceObject
import shmp.random.randomElement
import shmp.random.randomElementOrNull
import shmp.random.testProbability
import shmp.simulation.Controller.*
import shmp.simulation.culture.group.centers.util.MemoryConversion
import shmp.simulation.culture.group.centers.util.takeOutCommonReasonings
import shmp.simulation.culture.group.centers.util.toConcept
import shmp.simulation.culture.group.cultureaspect.*
import shmp.simulation.culture.group.cultureaspect.reasoning.*
import shmp.simulation.culture.group.cultureaspect.reasoning.convertion.ReasonConversion
import shmp.simulation.culture.group.cultureaspect.worship.Worship
import shmp.simulation.culture.group.reason.Reason
import shmp.simulation.culture.group.reason.constructBetterAspectUseReason
import shmp.simulation.culture.thinking.meaning.MemeSubject
import shmp.simulation.culture.thinking.meaning.constructAndAddSimpleMeme
import shmp.simulation.space.resource.Resource
import java.util.*
import kotlin.math.pow


class CultureAspectCenter(val reasonField: ReasonField, private val reasonConversions: List<ReasonConversion>) {
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
        reasonField.reasonComplexes.forEach { complex ->
            if (!testProbability(session.reasoningUpdate, session.random))
                return@forEach

            val newReasonings = if (complex.isEmpty)
                listOf(generateBaseReasoning(listOf(MemeSubject(group.name)), session.random))
            else
                generateNewReasonings(reasonField, complex)
            addReasonings(complex, newReasonings)
        }

        if (testProbability(session.reasoningUpdate, session.random)) {
            val conversion = randomElement(reasonConversions, session.random)

            conversion.enrichComplex(reasonField.commonReasonings, reasonField, session.random)
        }
    }

    private fun addReasonings(complex: ReasonComplex, reasonings: List<Reasoning>) {
        val acceptedReasonings = complex.addReasonings(reasonings)
        acceptedReasonings.forEach { addCultureAspect(it.toConcept()) }
    }

    fun addCultureAspect(cultureAspect: CultureAspect?) {
        cultureAspect ?: return

        aspectPool.add(cultureAspect)

        if (cultureAspect is CherishedResource)
            aestheticallyPleasingResources.add(cultureAspect.resource)
    }

    private fun useCultureAspects(group: Group) = aspectPool.all.forEach { it.use(group) }

    fun addRandomCultureAspect(group: Group) {
        if (!testProbability(session.cultureAspectBaseProbability, session.random))
            return

        val cultureAspect = when (randomElement(AspectRandom.values(), session.random)) {
            AspectRandom.Depict -> createDepictObject(
                    group.cultureCenter.aspectCenter.aspectPool.getMeaningAspects(),
                    constructAndAddSimpleMeme(
                            group.cultureCenter.memePool,
                            session.random
                    ),
                    group,
                    session.random
            )
            AspectRandom.AestheticallyPleasing -> createAestheticallyPleasingObject(
                    group.cultureCenter.aspectCenter.aspectPool.producedResources
                            .filter { it.genome.isDesirable }
                            .filter { !aestheticallyPleasingResources.contains(it) }
                            .maxByOrNull { it.genome.baseDesirability },
                    group,
                    session.random
            )
            AspectRandom.Ritual -> createRitual(//TODO recursively go in dependencies;
                    constructBetterAspectUseReason(
                            group,
                            group.cultureCenter.aspectCenter.aspectPool.converseWrappers,
                            reasonsWithSystems,
                            session.random
                    ),
                    group,
                    session.random
            )
            AspectRandom.Tale -> createTale(
                    group,
                    session.templateBase,
                    session.random
            )
            AspectRandom.Concept -> createSimpleConcept(
                    group,
                    session.random
            )
        }

        addCultureAspect(cultureAspect)
    }

    fun mutateCultureAspects(group: Group) {
        if (!testProbability(session.groupCultureAspectCollapse, session.random))
            return

        when (randomElement(ChangeRandom.values(), session.random)) {
            ChangeRandom.RitualSystem -> joinSimilarRituals()
            ChangeRandom.TaleSystem -> joinSimilarTalesBy("!actor")
            ChangeRandom.Worship -> addCultureAspect(takeOutWorship(aspectPool, session.random))
            ChangeRandom.God -> makeGod(group)
        }
    }

    private fun joinSimilarRituals() = takeOutSimilarRituals(aspectPool)?.let { system ->
        addCultureAspect(system)
        reasonsWithSystems.add(system.reason)
    }

    private fun makeGod(group: Group) = takeOutGod(aspectPool, group, session.random)?.let { cult ->
        addCultureAspect(cult)
    }

    private fun joinSimilarTalesBy(infoTag: String) = takeOutSimilarTalesBy(infoTag, aspectPool)?.let { system ->
        addCultureAspect(system)
    }

    private fun getNeighbourCultureAspects(group: Group) =
            group.relationCenter.relatedGroups.flatMap { n ->
                n.cultureCenter.cultureAspectCenter.aspectPool.all.map { a -> a to n }
            }

    private fun getNeighbourCultureAspects(group: Group, predicate: (CultureAspect) -> Boolean) =
            getNeighbourCultureAspects(group).filter { (f) -> predicate(f) }

    fun adoptCultureAspects(group: Group) {
        if (!session.isTime(session.groupTurnsBetweenAdopts))
            return

        val aspect = randomElementOrNull(
                getNeighbourCultureAspects(group) { !aspectPool.contains(it) },
                { (_, g) -> group.relationCenter.getNormalizedRelation(g) },
                session.random
        )?.first?.adopt(group)
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
            return testProbability(probability.pow(2), session.random)
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
    Depict(1.0),
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
