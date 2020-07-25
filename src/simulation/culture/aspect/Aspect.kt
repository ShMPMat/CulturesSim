package simulation.culture.aspect

import shmp.random.testProbability
import simulation.Controller.*
import simulation.SimulationError
import simulation.culture.aspect.AspectResult.ResultNode
import simulation.culture.aspect.dependency.AspectDependencies
import simulation.culture.aspect.dependency.Dependency
import simulation.culture.group.centers.AspectCenter
import simulation.culture.group.request.ResourceEvaluator
import simulation.culture.group.request.resourceEvaluator
import simulation.space.resource.container.MutableResourcePack
import simulation.space.resource.Resource
import simulation.space.resource.container.ResourcePack
import simulation.space.resource.tag.ResourceTag
import simulation.space.resource.tag.labeler.BaseNameLabeler
import simulation.space.resource.tag.labeler.ResourceLabeler
import simulation.space.resource.tag.labeler.TagLabeler
import java.util.*
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

open class Aspect(var core: AspectCore, dependencies: AspectDependencies) {
    /**
     * Map which stores for every requirement some Dependencies, from which
     * Aspect can get get resource for use.
     */
    var dependencies = AspectDependencies(mutableMapOf())

    init {
        initDependencies(dependencies)
    }

    var usefulness = session.defaultAspectUsefulness
        private set

    /**
     * Whether it was used on this turn.
     */
    var used = false
    private var usedThisTurn = false
    private var tooManyFailsThisTurn = false
    private var timesUsedInTurn = 0
    private var timesUsedInTurnUnsuccessfully = 0

    var canInsertMeaning = false

    fun initDependencies(dependencies: AspectDependencies) {
        for ((key, value) in dependencies.map)
            this.dependencies.map[key] = value.map { it.copy() }.toMutableSet()
    }

    open fun swapDependencies(aspectCenter: AspectCenter) =
            dependencies.map.values.forEach { set -> set.forEach { it.swapDependencies(aspectCenter) } }

    val name = core.name

    val tags = core.tags

    val requirements = core.requirements

    open val isValid = true

    fun canApplyMeaning() = core.applyMeaning

    fun canReturnMeaning() = this is ConverseWrapper && this.canInsertMeaning

    open fun isDependenciesOk(dependencies: AspectDependencies) = requirements.size == dependencies.size

    open fun canTakeResources() = false

    fun addOneDependency(newDependencies: AspectDependencies) {
        for (tag in dependencies.map.keys) try {
            for (dependency1 in newDependencies.map.getValue(tag)) {
                if (!dependencies.map.getValue(tag).contains(dependency1)) {
                    dependencies.map.getValue(tag).add(dependency1)
                    break
                }
            }
        } catch (e: Exception) {
            System.err.println(e)
            val i = 0
        }
    }

    open fun copy(dependencies: AspectDependencies) = core.makeAspect(dependencies)

    open val producedResources: List<Resource> = emptyList()

    fun calculateNeededWorkers(evaluator: ResourceEvaluator, amount: Double) = ceil(max(
            evaluator.getSatisfiableAmount(amount, producedResources) * core.standardComplexity,
            1.0
    )).toInt()

    fun calculateProducedValue(evaluator: ResourceEvaluator, workers: Int) =
            (evaluator.evaluate(producedResources) * workers) / core.standardComplexity

    private fun checkTermination(controller: AspectController) = tooManyFailsThisTurn
            || controller.depth > session.maxGroupDependencyDepth
            || used
            || core.resourceExposed && producedResources.any {
        val center = controller.territory.center
                ?: throw SimulationError("Empty Territory for the Aspect use")
        !it.isAcceptable(center)
    }

    protected fun _use(controller: AspectController): AspectResult {
        //TODO put dependency resources only in node; otherwise they may merge with phony
        if (checkTermination(controller)) return AspectResult(
                false,
                ArrayList(),
                MutableResourcePack(),
                ResultNode(this)
        )

        this as ConverseWrapper
        timesUsedInTurn++
        used = true
        var isFinished = true
        val neededResources = mutableListOf<Pair<ResourceLabeler, Int>>()
        val meaningfulPack = MutableResourcePack()

        val neededWorkers = calculateNeededWorkers(controller.evaluator, controller.ceiling)
        val gotWorkers = controller.populationCenter.getPeopleByAspect(this, neededWorkers)
        val allowedAmount = min(
                gotWorkers.cumulativeWorkers / core.standardComplexity
                        * controller.evaluator.evaluate(producedResources),
                controller.ceiling
        )

        controller.setMax(allowedAmount)
        val node = ResultNode(this)

        if (controller.ceiling > 0)
            for ((key, value) in dependencies.nonPhony.entries) {
                val (isOk, needs) = satisfyRegularDependency(controller, key, value, meaningfulPack, node)
                neededResources.addAll(needs)
                if (!isOk) {
                    isFinished = false
                    neededResources.add(TagLabeler(key) to ceil(controller.ceiling).toInt())
                }
            }

        if (isFinished)
            isFinished = satisfyPhonyDependency(controller, dependencies.phony, meaningfulPack)
        if (controller.isFloorExceeded(meaningfulPack))
            markAsUsed()
        else {
            controller.populationCenter.freeStratumAmountByAspect(this, gotWorkers)
            val neededAmount = ceil(controller.floor - controller.evaluate(meaningfulPack)).toInt()
            neededResources.add(BaseNameLabeler(resource.baseName) to neededAmount)
        }
        if (!isFinished) {
            timesUsedInTurnUnsuccessfully++
            if (!testProbability(1.0 / timesUsedInTurnUnsuccessfully.toDouble().pow(0.05), session.random))
                tooManyFailsThisTurn = true
        }
        used = false

        return AspectResult(isFinished, neededResources, meaningfulPack, node)
    }

    private fun satisfyPhonyDependency(
            controller: AspectController,
            dependencies: Set<Dependency>,
            meaningfulPack: MutableResourcePack
    ): Boolean {
        meaningfulPack.addAll(getPhonyFromResources(controller))
        if (controller.isCeilingExceeded(meaningfulPack))
            return true
        for (dependency in dependencies) {
            val newDelta = meaningfulPack.getAmount((this as ConverseWrapper).resource)
            val _p = dependency.useDependency(controller.copy(
                    depth = controller.depth + 1,
                    ceiling = controller.ceiling - newDelta,
                    floor = controller.floor - newDelta,
                    isMeaningNeeded = shouldPassMeaningNeed(controller.isMeaningNeeded)
            ))
            if (!_p.isFinished)
                continue
            meaningfulPack.addAll(_p.resources)
            if (controller.isCeilingExceeded(meaningfulPack))
                break
        }
        return true
    }

    private fun getPhonyFromResources(controller: AspectController): ResourcePack {
        val pack = resourceEvaluator((this as ConverseWrapper).resource).pick(controller.populationCenter.turnResources)
        return controller.pickCeilingPart(
                pack.resources,
                { it.applyAction(aspect.core.resourceAction) }
        ) { r, n -> r.applyActionAndConsume(aspect.core.resourceAction, n, true) }
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
        val _rp = MutableResourcePack()
        _rp.addAll(controller.pickCeilingPart(
                controller.populationCenter.stratumCenter.getByAspect(this as ConverseWrapper)
                        .getInstrumentByTag(requirementTag).resources,
                { listOf(it.copy(1)) }
        ) { r, n -> listOf(r.getCleanPart(n)) })
        val usedForDependency = MutableResourcePack()
        for (dependency in dependencies) {
            val newDelta = _rp.getAmount(requirementTag)
            val result = dependency.useDependency(controller.copy(
                    depth = controller.depth + 1,
                    ceiling = controller.ceiling - newDelta,
                    floor = controller.floor - newDelta,
                    isMeaningNeeded = false
            ))
            needs.addAll(result.neededResources)
            _rp.addAll(result.resources)
            if (!result.isFinished)
                continue
            if (_rp.getAmount(requirementTag) >= controller.ceiling) {
                //TODO sometimes can spend resources without getting result because other dependencies are lacking
                if (!requirementTag.isInstrumental)
                    usedForDependency.addAll(_rp.getAmountOfResourcesWithTagAndErase(
                            requirementTag,
                            controller.ceiling
                    ).second)
                else
                    usedForDependency.addAll(_rp.getResources(requirementTag))
                meaningfulPack.addAll(_rp)
                isFinished = true
                break
            } else {
                val j = 0
            }
        }
        node.resourceUsed[requirementTag] = usedForDependency
        return Result(isFinished, needs)
    }

    protected open fun shouldPassMeaningNeed(isMeaningNeeded: Boolean) = isMeaningNeeded

    fun markAsUsed() = gainUsefulness(1)

    fun gainUsefulness(amount: Int) {
        if (amount <= 0) return
        usefulness = max(usefulness + amount, session.defaultAspectUsefulness)
        usedThisTurn = true
    }

    fun finishUpdate() {
        timesUsedInTurn = 0
        timesUsedInTurnUnsuccessfully = 0
        tooManyFailsThisTurn = false
        if (!usedThisTurn)
            usefulness--
        usedThisTurn = false
    }

    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true

        if (other == null || javaClass != other.javaClass)
            return false

        val aspect = other as Aspect
        return core.name == aspect.core.name
    }

    override fun hashCode() = Objects.hash(core.name)

    override fun toString() = "Aspect ${core.name} usefulness - $usefulness dependencies:\n" +
            dependencies.map.entries.joinToString("\n") { (tag, deps) ->
                "**${tag.name}:\n" +
                        deps.joinToString("\n") { "****${it.name}" }
            }
}

private data class Result(val isFinished: Boolean = true, val need: List<Need> = emptyList())

typealias Need = Pair<ResourceLabeler, Int>
