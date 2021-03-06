package shmp.simulation.culture.group.centers

import shmp.random.singleton.randomElementOrNull
import shmp.simulation.CulturesController.session
import shmp.simulation.culture.aspect.Aspect
import shmp.simulation.culture.aspect.hasMeaning
import shmp.simulation.culture.group.GROUP_TAG_TYPE
import shmp.simulation.culture.group.cultureaspect.CherishedResource
import shmp.simulation.culture.group.resource_behaviour.getRandom
import shmp.simulation.culture.thinking.meaning.GroupMemes
import shmp.simulation.culture.thinking.meaning.Meme
import shmp.simulation.event.Event
import shmp.simulation.event.EventLog
import shmp.simulation.event.Type
import shmp.simulation.space.resource.Resource
import shmp.simulation.space.resource.tag.labeler.ResourceLabeler
import shmp.simulation.space.territory.Territory
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.log


class CultureCenter(
        private val group: Group,
        val memePool: GroupMemes,
        val traitCenter: TraitCenter,
        val memoryCenter: MemoryCenter,
        val cultureAspectCenter: CultureAspectCenter,
        val aspectCenter: AspectCenter
) {

    val requestCenter = RequestCenter()

    val events = EventLog()

    private val evaluatedMap = mutableMapOf<Resource, ValueEntry>()

    fun addAspiration(labeler: ResourceLabeler) = group.resourceCenter.addNeeded(labeler, 100)

    fun addResourceWant(resource: Resource) = cultureAspectCenter.addCultureAspect(CherishedResource(
            resource,
            getRandom()
    ))

    fun update(group: Group) {
        val aspectEvents = aspectCenter.mutateAspects(group).map {
            Event(Type.AspectGaining, "${group.name} got aspect ${it.name} by itself")
        }
        events.addAll(aspectEvents)
        aspectCenter.update(cultureAspectCenter.aspectPool.cwDependencies, group)
        cultureAspectCenter.update(group)
        lookOnTerritory(group.territoryCenter.accessibleTerritory)
        memoryCenter.update(group)
    }

    private fun lookOnTerritory(accessibleTerritory: Territory) {
        val tags = accessibleTerritory.tiles.flatMap { it.tagPool.all }

        for (tag in tags) {
            if (tag.type == GROUP_TAG_TYPE)
                continue

            memePool.add(Meme(tag.name))
            memePool.strengthenMeme(Meme(tag.name))
        }
    }

    fun intergroupUpdate(group: Group) {
        events.addAll(aspectCenter.adoptAspects(group))
        cultureAspectCenter.adoptCultureAspects(group)
    }

    val meaning: Meme
        get() = memePool.valuableMeme

    fun addNeedAspect(need: Pair<ResourceLabeler, ResourceNeed>) {
        val (first, second) = aspectCenter.findOptions(need.first, group)
                .randomElementOrNull()
                ?: return

        aspectCenter.addAspectTry(first, group)

        events.add(
                if (second == null)
                    Event(Type.AspectGaining, "Group ${group.name} developed aspect ${first.name}")
                else
                    Event(
                            Type.AspectGaining,
                            "Group ${group.name} took aspect ${first.name} from group ${second.name}"
                    )
        )
    }

    fun finishAspectUpdate(): Set<Aspect> {
        val aspects = aspectCenter.finishUpdate()
        aspects.forEach {
            memePool.addAspectMemes(it)
            memePool.addMemeCombination(Meme(group.name).addPredicate(
                    Meme("acquireAspect").addPredicate(Meme(it.name))
            ))
        }
        return aspects
    }

    fun consumeAllTraitChanges(changes: List<TraitChange>) {
        traitCenter.changeOnAll(changes)

        for (change in changes.filter { it.delta != 0.0 }) {
            val meme = if (change.delta > 0.0)
                change.trait.positiveMeme
            else
                change.trait.negativeMeme

            memePool.strengthenMeme(meme, ceil(abs(change.delta / 0.001)).toInt())
        }
    }

    fun die(group: Group) = cultureAspectCenter.die(group)

    fun finishUpdate() {
        requestCenter.finishUpdate()
        memoryCenter.turnRequests = requestCenter.turnRequests
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
        val cultureValue = if (isCherished) 3
        else 0

        return (base + 1) * (cultureValue + 1)
    }

    private fun getResourceAccessibility(resource: Resource): Int {
        val conglomerate = group.parentGroup.subgroups
        val others = group.relationCenter.relations
                .filter { it.other.parentGroup != group.parentGroup }

        return when {
            !resource.genome.isDesirable -> return 1
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
        |
        |
        |$traitCenter
        |
        |
        |$memoryCenter        |
    """.trimMargin()
}


private data class ValueEntry(val value: Int) {
    val creationTime = session.world.getTurn()
}
