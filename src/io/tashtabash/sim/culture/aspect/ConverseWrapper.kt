package io.tashtabash.sim.culture.aspect

import io.tashtabash.random.singleton.chanceOf
import io.tashtabash.sim.CulturesController
import io.tashtabash.sim.SimulationError
import io.tashtabash.sim.culture.aspect.dependency.AspectDependencies
import io.tashtabash.sim.culture.aspect.dependency.Dependency
import io.tashtabash.sim.culture.aspect.dependency.LineDependency
import io.tashtabash.sim.culture.group.GroupError
import io.tashtabash.sim.culture.group.centers.AspectCenter
import io.tashtabash.sim.culture.group.request.resourceEvaluator
import io.tashtabash.sim.culture.group.stratum.StratumPeople
import io.tashtabash.sim.space.resource.ExternalResourceFeature
import io.tashtabash.sim.space.resource.Resource
import io.tashtabash.sim.space.resource.container.MutableResourcePack
import io.tashtabash.sim.space.resource.tag.ResourceTag
import io.tashtabash.sim.space.resource.tag.labeler.BaseNameLabeler
import io.tashtabash.sim.space.resource.tag.labeler.TagLabeler
import io.tashtabash.sim.space.resource.tag.phony
import java.util.*
import kotlin.math.ceil
import kotlin.math.min
import kotlin.math.pow

/**
 * Special Aspect which wraps around another aspect and resource and returns application
 * of this aspect to the resource.
 */
open class ConverseWrapper(var aspect: Aspect, val resource: Resource) : Aspect(
        aspect.core.copy(
                name = aspect.name + "On" + resource.baseName,
                tags = getReducedTags(resource, aspect),
                requirements = ArrayList(aspect.requirements),
                applyMeaning = false,
                standardComplexity = aspect.core.getPrecomputedComplexity(resource)
        ),
        AspectDependencies(mutableMapOf())
) {
    private var tooManyFailsThisTurn = false
    private var timesUsedInTurn = 0
    private var timesUsedInTurnUnsuccessfully = 0

    override fun swapDependencies(aspectCenter: AspectCenter) {
        super.swapDependencies(aspectCenter)
        aspect = aspectCenter.aspectPool.getValue(aspect)
    }

    override val producedResources = resource.applyActionUnsafe(aspect.core.resourceAction)

    open fun use(controller: AspectController) = try {
        val result = _use(controller)
        if (result.isFinished)
            aspect.markAsUsed()

        val targetResources = result.resources
                .getResourcesAndRemove { it in producedResources }
                .toMutableList()
        targetResources.removeIf { it.isEmpty }

        result.resources.addAll(targetResources.map {
            it.copyWithNewExternalFeatures(toFeatures(result.node))
        })

        result
    } catch (e: Exception) {
        throw GroupError("Error in using ConverseWrapper $name: ${e.message}")
    }

    private fun _use(controller: AspectController): AspectResult {
        //TODO put dependency resources only in node; otherwise they may merge with phony
        if (checkTermination(controller) || isBanned(controller))
            return AspectResult(isFinished = false, node = ResultNode(this))

        timesUsedInTurn++
        isCurrentlyUsed = true

        val gotWorkers = acquireWorkers(controller)
        val result = satisfyDependencies(controller)

        if (controller.isFloorExceeded(result.resources))
            markAsUsed()
        else {
            val neededAmount = ceil(controller.floor - controller.evaluate(result.resources)).toInt()
            result.neededResources += BaseNameLabeler(resource.baseName) to neededAmount
        }

        if (result.isFinished)
            controller.populationCenter.freeStratumAmountByAspect(this, gotWorkers)

        updateFailStats(result.isFinished)
        isCurrentlyUsed = false

        return result
    }

    private fun isBanned(controller: AspectController): Boolean =
        producedResources.any {
            controller.group.resourceCenter.isBanned(resource, controller.requestTypes)
        }

    private fun acquireWorkers(controller: AspectController): StratumPeople {
        val neededWorkers = calculateNeededWorkers(controller.evaluator, controller.ceiling)
        val gotWorkers = controller.populationCenter.getPeopleByAspect(this, neededWorkers)
        val allowedAmount = min(
            gotWorkers.cumulativeWorkers / core.standardComplexity
                    * controller.evaluator.evaluate(producedResources),
            controller.ceiling
        )

        controller.setMax(allowedAmount)

        return gotWorkers
    }

    private fun satisfyDependencies(controller: AspectController): AspectResult {
        val result = AspectResult(MutableResourcePack(), ResultNode(this), true, mutableListOf())
        result.node!!

        if (controller.ceiling > 0)
            for ((key, value) in dependencies.nonPhony.entries) {
                val (isOk, needs) = satisfyRegularDependency(controller, key, value, result.resources, result.node)
                result.neededResources.addAll(needs)
                if (!isOk) {
                    result.isFinished = false
                    result.neededResources += TagLabeler(key) to ceil(controller.ceiling).toInt()
                }
            }

        result.isFinished = result.isFinished && satisfyPhonyDependency(controller, result.resources)

        return result
    }

    private fun updateFailStats(isFinished: Boolean) {
        if (isFinished)
            return

        timesUsedInTurnUnsuccessfully++
        (1.0 / timesUsedInTurnUnsuccessfully.toDouble().pow(0.05)).chanceOf {
            tooManyFailsThisTurn = true
        }
    }

    private fun checkTermination(controller: AspectController) = tooManyFailsThisTurn
            || controller.depth > CulturesController.session.maxGroupDependencyDepth
            || isCurrentlyUsed
            || core.resourceExposed && producedResources.any {
        val center = controller.territory.center
            ?: throw SimulationError("Empty Territory for the Aspect use")
        !it.areNecessaryDependenciesSatisfied(center)
    }

    private fun satisfyPhonyDependency(
        controller: AspectController,
        meaningfulPack: MutableResourcePack
    ): Boolean {
        var amount = controller.evaluator.evaluate(meaningfulPack.resources)
        val resourcesPhony = getPhonyFromResources(controller)
        amount += controller.evaluator.evaluate(resourcesPhony)
        meaningfulPack.addAll(resourcesPhony)

        if (controller.isCeilingExceeded(amount))
            return true

        for (dependency in dependencies.phony) {
            val newDelta = meaningfulPack.getAmount(resource)
            val result = dependency.useDependency(controller.copy(
                depth = controller.depth + 1,
                ceiling = controller.ceiling - newDelta,
                floor = controller.floor - newDelta,
                isMeaningNeeded = shouldPassMeaningNeed(controller.isMeaningNeeded)
            ))
            if (!result.isFinished)
                continue

            amount += controller.evaluator.evaluate(result.resources.resources)
            meaningfulPack.addAll(result.resources)

            if (controller.isCeilingExceeded(amount))
                break
        }
        return true
    }

    private fun getPhonyFromResources(controller: AspectController): List<Resource> {
        val pack = resourceEvaluator(resource).pick(controller.populationCenter.turnResources)
        return controller.pickCeilingPart(
            pack.resources,
            { it.applyActionUnsafe(aspect.core.resourceAction) }
        ) { r, n ->
            r.applyActionAndConsume(
                aspect.core.resourceAction,
                n,
                true,
                controller.populationCenter.taker
            )
        }
    }

    private fun satisfyRegularDependency(
        controller: AspectController,
        requirementTag: ResourceTag,
        dependencies: Set<Dependency>,
        meaningfulPack: MutableResourcePack,
        node: ResultNode
    ): Result {
        val needs = mutableListOf<Need>()
        var isFinished = false
        val dependencyPack = MutableResourcePack()
        dependencyPack.addAll(controller.pickCeilingPart(
            controller.populationCenter.stratumCenter.getByAspect(this)
                .getInstrumentByTag(requirementTag).resources,
            { it.core.wrappedSample }
        ) { r, n -> listOf(r.getCleanPart(n, controller.populationCenter.taker)) })
        var amount = dependencyPack.getAmount(requirementTag)
        val usedForDependency = MutableResourcePack()


        for (dependency in dependencies) {
            val result = dependency.useDependency(controller.copy(
                depth = controller.depth + 1,
                ceiling = controller.ceiling - amount,
                floor = controller.floor - amount,
                isMeaningNeeded = false
            ))
            needs.addAll(result.neededResources)
            if (!result.isFinished)
                continue

            amount += result.resources.getAmount(requirementTag)
            dependencyPack.addAll(result.resources)

            if (controller.isCeilingExceeded(amount)) {
                //TODO sometimes can spend resources without getting result because other dependencies are lacking
                if (!requirementTag.isInstrumental)
                    usedForDependency.addAll(dependencyPack.getAmountOfResourcesWithTagAndErase(
                        requirementTag,
                        controller.ceiling
                    ).second)
                else
                    usedForDependency.addAll(dependencyPack.getTaggedResourcesUnpacked(requirementTag))
                meaningfulPack.addAll(dependencyPack)
                isFinished = true
                break
            }
        }
        node.resourceUsed[requirementTag] = usedForDependency
        return Result(isFinished, needs)
    }

    override fun finishUpdate() {
        timesUsedInTurn = 0
        timesUsedInTurnUnsuccessfully = 0
        tooManyFailsThisTurn = false
        super.finishUpdate()
    }

    private fun toFeatures(node: ResultNode?): List<ExternalResourceFeature> =
            if (node?.resourceUsed?.isNotEmpty() == true)
                node.resourceUsed.entries
                    .filter { it.key.name != phony.name && !it.key.isInstrumental }
                    .flatMap { p -> p.value.resources.map { it.fullName } }
                    .distinct()
                    .mapIndexed { i, n -> ElementResourceFeature(n, 1000 + i) }
            else emptyList()

    override fun checkDependencies(dependencies: AspectDependencies) =
            requirements.size + 1 == dependencies.size

    override val isValid: Boolean
        get() = resource.hasApplicationForAction(aspect.core.resourceAction)

    override fun canTakeResources() =
            aspect.name == "Take" || aspect.name == "Killing" || aspect.name == "TakeApart"

    override fun copy(dependencies: AspectDependencies): ConverseWrapper {
        val copy = ConverseWrapper(
                aspect,
                resource
        )
        copy.initDependencies(dependencies)
        return try {
            copy.canInsertMeaning = dependencies.map.getValue(phony).any {
                it is LineDependency && it.converseWrapper.canInsertMeaning
            }
            copy
        } catch (e: Exception) {
            throw GroupError("Wrong dependencies: ${e.message}")
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as ConverseWrapper

        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + name.hashCode()
        return result
    }
}

private fun getReducedTags(resource: Resource, aspect: Aspect): List<ResourceTag> {
    val result: MutableList<ResourceTag> = ArrayList()
    val allTags = resource.applyActionUnsafe(aspect.core.resourceAction).flatMap { it.tags }
    for (tag in allTags) {
        if (!result.contains(tag))
            result.add(tag)
        else if (result[result.indexOf(tag)].level < tag.level) {
            result.remove(tag)
            result.add(tag)
        }
    }
    return result
}

class ElementResourceFeature(resourceName: String, override val index: Int) : ExternalResourceFeature {
    override val name = "made_with_$resourceName"
}

private data class Result(val isFinished: Boolean = true, val need: List<Need> = emptyList())
