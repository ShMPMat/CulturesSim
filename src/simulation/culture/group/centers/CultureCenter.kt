package simulation.culture.group.centers

import shmp.random.randomElement
import shmp.random.testProbability
import simulation.Controller.session
import simulation.Event
import simulation.culture.aspect.*
import simulation.culture.group.GROUP_TAG_TYPE
import simulation.culture.group.cultureaspect.AestheticallyPleasingObject
import simulation.culture.group.request.passingEvaluator
import simulation.culture.group.resource_behaviour.getRandom
import simulation.culture.thinking.meaning.GroupMemes
import simulation.culture.thinking.meaning.Meme
import simulation.culture.thinking.meaning.MemePredicate
import simulation.culture.thinking.meaning.MemeSubject
import simulation.space.Territory
import simulation.space.resource.Resource
import simulation.space.resource.tag.labeler.ResourceLabeler
import java.util.*

class CultureCenter(private val group: Group, val memePool: GroupMemes, aspects: List<Aspect>) {
    val aspectCenter: AspectCenter = AspectCenter(group, aspects)
    val cultureAspectCenter: CultureAspectCenter = CultureAspectCenter(group)
    val requestCenter = RequestCenter()
    val events: MutableList<Event> = ArrayList()

    fun addAspiration(labeler: ResourceLabeler) = group.resourceCenter.addNeeded(labeler, 100)

    fun addResourceWant(resource: Resource) = cultureAspectCenter.addCultureAspect(AestheticallyPleasingObject(
            resource,
            getRandom(group, session.random)
    ))

    fun update() {
        events.addAll(aspectCenter.mutateAspects())
        aspectCenter.update(cultureAspectCenter.aspectPool.cwDependencies)
        createArtifact()
        cultureAspectCenter.useCultureAspects()
        cultureAspectCenter.addRandomCultureAspect(group)
        cultureAspectCenter.mutateCultureAspects(group)
        lookOnTerritory(group.territoryCenter.accessibleTerritory)
    }

    private fun lookOnTerritory(accessibleTerritory: Territory) {
        val tags = accessibleTerritory.tiles
                .flatMap { it.tagPool.all }
        for (tag in tags) {
            if (tag.type == GROUP_TAG_TYPE)
                continue
            memePool.add(MemeSubject(tag.name))
            memePool.strengthenMeme(MemeSubject(tag.name))
        }
    }

    fun intergroupUpdate() {
        events.addAll(aspectCenter.adoptAspects(group))
        cultureAspectCenter.adoptCultureAspects(group)
    }

    private fun createArtifact() {
        if (testProbability(0.1, session.random)) {
            if (memePool.isEmpty)
                return
            val wrappers: List<ConverseWrapper> = ArrayList(aspectCenter.aspectPool.getMeaningAspects())
            if (wrappers.isEmpty()) {
                if (aspectCenter.aspectPool.converseWrappers.filterIsInstance<MeaningInserter>().isNotEmpty()) {
                    val k = 0
                }
                return
            }
            val chosen = randomElement(wrappers, session.random)
            val result = chosen.use(AspectController(
                    1,
                    1,
                    1,
                    passingEvaluator,
                    group.populationCenter,
                    group.territoryCenter.accessibleTerritory,
                    true,
                    group,
                    memePool.valuableMeme
            ))
            group.resourceCenter.addAll(
                    result.resources.getResources { it.hasMeaning }
            )
        }
    }

    val meaning: Meme
        get() = memePool.valuableMeme

    fun addNeedAspect(need: Pair<ResourceLabeler, ResourceNeed>) {
        val options = aspectCenter.findOptions(need.first)
        if (options.isEmpty())
            return
        val (first, second) = randomElement(options, session.random)
        aspectCenter.addAspect(first)
        if (second == null)
            events.add(Event(
                    Event.Type.AspectGaining,
                    "Group " + group.name + " developed aspect " + first.name,
                    "group", this
            ))
        else events.add(Event(
                Event.Type.AspectGaining, String.format(
                "Group %s took aspect %s from group %s",
                group.name,
                first.name,
                second.name
        ),
                "group", this
        ))
    }

    fun finishAspectUpdate(): Set<Aspect> {
        val aspects = aspectCenter.finishUpdate()
        aspects.forEach {
            memePool.addAspectMemes(it)
            memePool.addMemeCombination(MemeSubject(group.name).addPredicate(
                    MemePredicate("acquireAspect").addPredicate(MemeSubject(it.name))
            ))
        }
        return aspects
    }

    fun pushAspects() {
        aspectCenter.pushAspects()
    }

    fun die() = cultureAspectCenter.die(group)

    fun finishUpdate() {
        requestCenter.finishUpdate()
    }

    fun evaluateResource(resource: Resource): Int {
        val base = resource.genome.baseDesirability + 1
        val meaningPart = if (resource.hasMeaning) 3 else 1
        val conglomerate = group.relationCenter.relations
                .filter { it.other.parentGroup == group.parentGroup }
        val desire = group.resourceCenter.needLevel(resource) + 1

        val others = group.relationCenter.relations
                .filter { it.other.parentGroup != group.parentGroup }
        val accessibility = when {
            group.resourceCenter.pack.contains(resource) -> 1
            aspectCenter.aspectPool.producedResources.map { it.first }.contains(resource) -> 2
            conglomerate.any { it.other.resourceCenter.pack.contains(resource) } -> 3
            conglomerate.any {
                it.other.cultureCenter.aspectCenter.aspectPool.producedResources
                        .map { p -> p.first }.contains(resource)
            } -> 4
            others.any { it.other.resourceCenter.pack.contains(resource) } -> 5
            others.any {
                        it.other.cultureCenter.aspectCenter.aspectPool.producedResources
                                .map { p -> p.first }.contains(resource)
                    } -> 6
            else -> 10
        }
        return base * meaningPart * accessibility * desire
    }
}