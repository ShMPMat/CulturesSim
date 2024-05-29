package io.tashtabash.sim.culture.group.centers

import io.tashtabash.sim.CulturesController.Companion.session
import io.tashtabash.sim.culture.aspect.Aspect
import io.tashtabash.sim.culture.aspect.hasMeaning
import io.tashtabash.sim.culture.group.cultureaspect.CherishedResource
import io.tashtabash.sim.culture.group.resource_behaviour.getRandom
import io.tashtabash.sim.culture.thinking.meaning.GroupMemes
import io.tashtabash.generator.culture.worldview.Meme
import io.tashtabash.sim.culture.desirableTag
import io.tashtabash.sim.culture.thinking.meaning.makeMeme
import io.tashtabash.sim.event.*
import io.tashtabash.sim.space.resource.Resource
import io.tashtabash.sim.space.resource.tag.labeler.ResourceLabeler
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

    private val importanceToDepthCoefficient = 100

    fun addAspiration(labeler: ResourceLabeler) = group.resourceCenter.addNeeded(labeler, 100)

    fun addResourceWant(resource: Resource) = cultureAspectCenter.addCultureAspect(CherishedResource(
            resource,
            getRandom()
    ))

    fun update(group: Group) {
        aspectCenter.update(cultureAspectCenter.aspectPool.cwDependencies, group)
        cultureAspectCenter.update(group)
        memoryCenter.update(group)
    }

    fun intergroupUpdate(group: Group) {
        events.addAll(aspectCenter.adoptAspects(group))
        cultureAspectCenter.adoptCultureAspects(group)
    }

    val meaning: Meme
        get() = memePool.valuableMeme

    fun addNeedAspect(labeler: ResourceLabeler, need: ResourceNeed) {
        val searchDepth = need.importance / importanceToDepthCoefficient + 1
        val option = aspectCenter.findRandomOption(labeler, group, searchDepth)

        if (option.isEmpty()) {
            events += Fail of "Group ${group.name} couldn't develop an aspect for a need $labeler"
            return
        }

        var success = true
        for ((aspect) in option.reversed()) {
            success = aspectCenter.tryAddingAspect(aspect, group)
            if (!success)
                break
        }
        val (aspect, sourceGroup) = option.first()

        events +=
            if (success)
                if (sourceGroup == null)
                    AspectGaining of "Group ${group.name} developed an aspect ${aspect.name} for a need $labeler"
                else
                    AspectGaining of "Group ${group.name} took an aspect ${aspect.name} from group ${sourceGroup.name}" +
                            " for a need $labeler"
            else
                Fail of "Group ${group.name} invented an aspect ${aspect.name} for a need $labeler but couldn't add it"
    }

    fun finishAspectUpdate(): Set<Aspect> {
        val aspects = aspectCenter.finishUpdate()
        aspects.forEach {
            memePool.addAspectMemes(it)
            memePool.addMemeCombination(makeMeme(group).addPredicate(
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
                    log(resource.genome.lifespan, 10.0).toInt() + 1
                else 1
        val accessibility = getResourceAccessibility(resource)

        val value = base * meaningPart * desire * uniqueness * durability * accessibility
        evaluatedMap[resource] = ValueEntry(value)
        return value
    }

    private fun getResourceDesirability(resource: Resource): Int {
        val base = group.resourceCenter.needLevel(resource)

        val isCherished = cultureAspectCenter.aspectPool.cherishedResources.any { it.resource == resource }
        val cultureValue =
            if (isCherished) 3
            else 0

        return (base + 1) * (cultureValue + 1)
    }

    private fun getResourceAccessibility(resource: Resource): Int {
        val conglomerate = group.parentGroup.subgroups
        val others = group.relationCenter.relations
                .filter { it.other.parentGroup != group.parentGroup }

        return when {
            resource.genome.getTagLevel(desirableTag) == 0.0 -> return 1
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
        |$memoryCenter
    """.trimMargin()
}


private data class ValueEntry(val value: Int) {
    val creationTime = session.world.getTurn()
}
