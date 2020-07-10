package simulation.culture.group.centers

import shmp.random.SampleSpaceObject
import shmp.random.randomElement
import shmp.random.testProbability
import simulation.Controller.*
import simulation.culture.group.cultureaspect.*
import simulation.culture.group.cultureaspect.worship.Worship
import simulation.culture.group.reason.Reason
import simulation.culture.group.reason.constructBetterAspectUseReason
import simulation.culture.thinking.meaning.constructAndAddSimpleMeme
import simulation.space.resource.Resource
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
        var cultureAspect: CultureAspect? = null
        when (randomElement(AspectRandom.values(), session.random)) {
            AspectRandom.Depict -> cultureAspect = createDepictObject(
                    group.cultureCenter.aspectCenter.aspectPool.getMeaningAspects(),
                    constructAndAddSimpleMeme(
                            group.cultureCenter.memePool,
                            session.random
                    ),
                    group,
                    session.random
            )
            AspectRandom.AestheticallyPleasing -> cultureAspect = createAestheticallyPleasingObject(
                    group.cultureCenter.aspectCenter.aspectPool.producedResources
                            .filter { it.genome.isDesirable }
                            .filter { !aestheticallyPleasingResources.contains(it) }
                            .maxBy { it.genome.baseDesirability },
                    group,
                    session.random
            )
            AspectRandom.Ritual -> cultureAspect = constructRitual(//TODO recursively go in dependencies;
                    constructBetterAspectUseReason(
                            group,
                            group.cultureCenter.aspectCenter.aspectPool.converseWrappers,
                            reasonsWithSystems,
                            session.random
                    ),
                    group,
                    session.random
            )
            AspectRandom.Tale -> cultureAspect = createTale(
                    group,
                    session.templateBase,
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

    private fun joinSimilarRituals() {
        val system = takeOutSimilarRituals(aspectPool) ?: return
        addCultureAspect(system)
        reasonsWithSystems.add(system.reason)
    }

    private fun makeGod(group: Group) {
        val cult = takeOutGod(aspectPool, group, session.random) ?: return
        addCultureAspect(cult)
    }

    private fun joinSimilarTalesBy(infoTag: String) {
        val system = takeOutSimilarTalesByTag(infoTag, aspectPool)
        system?.let { addCultureAspect(it) }
    }

    private val neighbourCultureAspects: List<Pair<CultureAspect, Group>>
        get() = group.relationCenter.relatedGroups.flatMap { n ->
            n.cultureCenter.cultureAspectCenter.aspectPool.all.map { a -> Pair(a, n) }
        }

    private fun getNeighbourCultureAspects(predicate: (CultureAspect) -> Boolean): List<Pair<CultureAspect, Group>> =
            neighbourCultureAspects.filter { (f) -> predicate(f) }

    fun adoptCultureAspects(group: Group) {
        if (!session.isTime(session.groupTurnsBetweenAdopts)) return
        val cultureAspects = getNeighbourCultureAspects { !aspectPool.contains(it) }
        //TODO mb some more smart check?
        if (cultureAspects.isNotEmpty()) try {
            val aspect = randomElement(
                    cultureAspects,
                    { (_, g) -> group.relationCenter.getNormalizedRelation(g) },
                    session.random
            ).first.adopt(group)
                    ?: return
            if (shouldAdopt(aspect))
                addCultureAspect(aspect)
        } catch (e: NoSuchElementException) {
        }
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
}

private enum class AspectRandom(override val probability: Double) : SampleSpaceObject {
    Depict(1.0),
    AestheticallyPleasing(1.0),
    Ritual(1.0),
    Tale(3.0)
}

private enum class ChangeRandom(override val probability: Double) : SampleSpaceObject {
    RitualSystem(3.0),
    TaleSystem(3.0),
    Worship(2.0),
    God(1.0),
}