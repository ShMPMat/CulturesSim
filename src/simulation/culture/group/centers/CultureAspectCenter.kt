package simulation.culture.group.centers

import shmp.random.SampleSpaceObject
import shmp.random.randomElement
import shmp.random.testProbability
import simulation.Controller.*
import simulation.culture.group.cultureaspect.*
import simulation.culture.group.reason.Reason
import simulation.culture.group.reason.constructBetterAspectUseReason
import simulation.culture.thinking.meaning.constructAndAddSimpleMeme
import simulation.space.resource.Resource
import java.util.*

class CultureAspectCenter(private val group: Group, cultureAspects: MutableSet<CultureAspect>) {
    val aspectPool: MutableCultureAspectPool = MutableCultureAspectPool(cultureAspects)
    private val aestheticallyPleasingResources: MutableSet<Resource> = HashSet()
    private val reasonsWithSystems: MutableSet<Reason> = HashSet()

    fun addCultureAspect(cultureAspect: CultureAspect?) {
        cultureAspect ?: return
        aspectPool.add(cultureAspect)
        if (cultureAspect is AestheticallyPleasingObject)
            aestheticallyPleasingResources.add(cultureAspect.resource)
    }

    fun useCultureAspects() = aspectPool.getAll().forEach { it.use(group) }

    fun addRandomCultureAspect(group: Group) {
        if (!testProbability(session.cultureAspectBaseProbability, session.random))
            return
        var cultureAspect: CultureAspect? = null
        when (session.random.nextInt(5)) {
            0 -> cultureAspect = createDepictObject(
                    group.cultureCenter.aspectCenter.aspectPool.getMeaningAspects(),
                    constructAndAddSimpleMeme(
                            group.cultureCenter.memePool,
                            session.random
                    ),
                    group,
                    session.random
            )
            2 -> cultureAspect = createAestheticallyPleasingObject(
                    group.cultureCenter.aspectCenter.aspectPool.producedResources
                            .map { it.first }
                            .filter { !aestheticallyPleasingResources.contains(it) }
                            .maxBy { it.genome.baseDesirability },
                    group,
                    session.random
            )
            3 -> cultureAspect = constructRitual(//TODO recursively go in dependencies;
                    constructBetterAspectUseReason(
                            group,
                            group.cultureCenter.aspectCenter.aspectPool.converseWrappers,
                            reasonsWithSystems,
                            session.random
                    ),
                    group,
                    session.random
            )
            4 -> cultureAspect = createTale(
                    group,
                    session.templateBase,
                    session.random
            )
        }
        addCultureAspect(cultureAspect)
    }

    fun mutateCultureAspects() {
        if (!testProbability(session.groupCultureAspectCollapse, session.random))
            return
        when (session.random.nextInt(3)) {
            0 -> joinSimilarRituals()
            1 -> joinSimilarTalesBy("!actor")
            2 -> addCultureAspect(
                    takeOutWorship(aspectPool, session.random)
            )
        }
    }

    private fun joinSimilarRituals() {
        val system = takeOutSimilarRituals(aspectPool, group) ?: return
        addCultureAspect(system)
        reasonsWithSystems.add(system.reason)
    }

    private fun joinSimilarTalesBy(infoTag: String) {
        val system = takeOutSimilarTalesByTag(infoTag, aspectPool)
        system?.let { addCultureAspect(it) }
    }

    private val neighbourCultureAspects: List<Pair<CultureAspect, Group>>
        get() = group.relationCenter.relatedGroups.flatMap { n ->
            n.cultureCenter.cultureAspectCenter.aspectPool.getAll().map { a -> Pair(a, n) }
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
                    { (_, second) -> group.relationCenter.getNormalizedRelation(second) },
                    session.random
            ).first.copy(group)
            addCultureAspect(aspect)
        } catch (e: NoSuchElementException) {
        }
    }
}

enum class AspectRandom : SampleSpaceObject {
    
}