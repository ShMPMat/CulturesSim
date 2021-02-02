package shmp.simulation.culture.group.centers

import shmp.random.SampleSpaceObject
import shmp.random.randomElement
import shmp.random.randomElementOrNull
import shmp.random.testProbability
import shmp.simulation.Controller.*
import shmp.simulation.culture.group.cultureaspect.*
import shmp.simulation.culture.group.cultureaspect.worship.Worship
import shmp.simulation.culture.group.reason.Reason
import shmp.simulation.culture.group.reason.constructBetterAspectUseReason
import shmp.simulation.culture.thinking.meaning.constructAndAddSimpleMeme
import shmp.simulation.space.resource.Resource
import java.util.*
import kotlin.math.pow


class CultureAspectCenter(private val group: Group) {
    val aspectPool = MutableCultureAspectPool(mutableSetOf())
    private val aestheticallyPleasingResources: MutableSet<Resource> = HashSet()
    private val reasonsWithSystems: MutableSet<Reason> = HashSet()

    fun addCultureAspect(cultureAspect: CultureAspect?) {
        cultureAspect ?: return

        aspectPool.add(cultureAspect)

        if (cultureAspect is CherishedResource)
            aestheticallyPleasingResources.add(cultureAspect.resource)
    }

    fun useCultureAspects() = aspectPool.all.forEach { it.use(group) }

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
                            .maxBy { it.genome.baseDesirability },
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

    private val neighbourCultureAspects: List<Pair<CultureAspect, Group>>
        get() = group.relationCenter.relatedGroups.flatMap { n ->
            n.cultureCenter.cultureAspectCenter.aspectPool.all.map { a -> Pair(a, n) }
        }

    private fun getNeighbourCultureAspects(predicate: (CultureAspect) -> Boolean): List<Pair<CultureAspect, Group>> =
            neighbourCultureAspects.filter { (f) -> predicate(f) }

    fun adoptCultureAspects(group: Group) {
        if (!session.isTime(session.groupTurnsBetweenAdopts))
            return

        val aspect = randomElementOrNull(
                getNeighbourCultureAspects { !aspectPool.contains(it) },
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
