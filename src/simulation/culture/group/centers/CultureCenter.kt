package simulation.culture.group.centers

import shmp.random.randomElement
import simulation.Controller.session
import simulation.Event
import simulation.culture.aspect.Aspect
import simulation.culture.aspect.hasMeaning
import simulation.culture.group.GROUP_TAG_TYPE
import simulation.culture.group.cultureaspect.CherishedResource
import simulation.culture.group.resource_behaviour.getRandom
import simulation.culture.thinking.meaning.GroupMemes
import simulation.culture.thinking.meaning.Meme
import simulation.culture.thinking.meaning.MemePredicate
import simulation.culture.thinking.meaning.MemeSubject
import simulation.space.Territory
import simulation.space.resource.Resource
import simulation.space.resource.tag.labeler.ResourceLabeler
import java.util.*
import kotlin.math.log

class CultureCenter(private val group: Group, val memePool: GroupMemes, aspects: List<Aspect>) {
    val aspectCenter: AspectCenter = AspectCenter(group, aspects)
    val cultureAspectCenter: CultureAspectCenter = CultureAspectCenter(group)
    val requestCenter = RequestCenter()

    val events: MutableList<Event> = ArrayList()

    private val evaluatedMap = mutableMapOf<Resource, ValueEntry>()

    fun addAspiration(labeler: ResourceLabeler) = group.resourceCenter.addNeeded(labeler, 100)

    fun addResourceWant(resource: Resource) = cultureAspectCenter.addCultureAspect(CherishedResource(
            resource,
            getRandom(group, session.random)
    ))

    fun update() {
        events.addAll(
                aspectCenter.mutateAspects()
                        .map { Event(it.type, group.name + it.description, it.attributes) }
        )
        aspectCenter.update(cultureAspectCenter.aspectPool.cwDependencies, group)
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
        val entry = evaluatedMap[resource]
        if (entry != null)
            if (session.world.getTurn() - entry.creationTime < session.resourceValueRefreshTime)
                return entry.value

        val base = resource.genome.baseDesirability + 1
        val meaningPart = if (resource.hasMeaning) 3 else 1
        val desire = getResourceDesirability(resource)
        val uniqueness = resource.externalFeatures.size + 1
        val durability =
                if (resource.genome.lifespan < 1_000_000_000)
                    log(resource.genome.lifespan.toDouble(), 10.0).toInt() + 1
                else 1
        val accessibility = getResourceAccessibility(resource)

        val value = base * meaningPart * desire * uniqueness * durability * accessibility
        evaluatedMap[resource] = ValueEntry(value)
        return value
    }

    private fun getResourceDesirability(resource: Resource): Int {
        val base = group.resourceCenter.needLevel(resource)

        val isCherished = cultureAspectCenter.aspectPool.cherishedResources.any { it.resource == resource }
        val cultureValue = if (isCherished)
            3
        else
            0

        return (base + 1) * (cultureValue + 1)
    }

    private fun getResourceAccessibility(resource: Resource): Int {
        val conglomerate = group.parentGroup.subgroups
        val others = group.relationCenter.relations
                .filter { it.other.parentGroup != group.parentGroup }

        return when {
            !resource.genome.isDesirable -> {
                return 1
            }
            !isResourceDesirable(resource) -> 1
            aspectCenter.aspectPool.producedResources.contains(resource) -> 2
            conglomerate.any { it.resourceCenter.pack.contains(resource) } -> 3
            conglomerate.any {
                it.cultureCenter.aspectCenter.aspectPool.producedResources.contains(resource)
            } -> 4
            others.any { it.other.resourceCenter.pack.contains(resource) } -> 5
            others.any {
                it.other.cultureCenter.aspectCenter.aspectPool.producedResources.contains(resource)
            } -> 6
            else -> 10
        }
    }

    private fun isResourceDesirable(resource: Resource) =
            !group.resourceCenter.pack.contains(resource) && !group.populationCenter.turnResources.contains(resource)

    override fun toString() = """
        |$aspectCenter
        |
        |
        |$cultureAspectCenter
        |
        |
        |$requestCenter
    """.trimMargin()
}

private data class ValueEntry(val value: Int) {
    val creationTime = session.world.getTurn()
}
