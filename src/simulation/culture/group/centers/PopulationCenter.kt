package simulation.culture.group.centers

import extra.addLinePrefix
import simulation.Controller
import simulation.culture.aspect.Aspect
import simulation.culture.aspect.ConverseWrapper
import simulation.culture.aspect.labeler.AspectLabeler
import simulation.culture.aspect.labeler.ProducedLabeler
import simulation.culture.group.GroupError
import simulation.culture.group.request.ExecutedRequestResult
import simulation.culture.group.request.Request
import simulation.culture.group.request.RequestPool
import simulation.culture.group.stratum.*
import simulation.space.Territory
import simulation.space.resource.container.MutableResourcePack
import simulation.space.resource.container.ResourcePack
import simulation.space.resource.freeMarker
import simulation.space.resource.tag.labeler.ResourceLabeler
import simulation.space.tile.Tile
import java.util.*
import kotlin.math.ceil
import kotlin.math.min

class PopulationCenter(
        var population: Int,
        private val maxPopulation: Int,
        private val minPopulation: Int,
        initTile: Tile
) {
    val stratumCenter = StratumCenter(initTile)

    val turnResources = MutableResourcePack()

    val freePopulation: Int
        get() = population - stratumCenter.strata
                .map(Stratum::population)
                .foldRight(0, Int::plus)

    fun getMaxPopulation(controlledTerritory: Territory) = controlledTerritory.size * maxPopulation

    fun isMaxReached(controlledTerritory: Territory) = getMaxPopulation(controlledTerritory) <= population

    fun getMinPopulation(controlledTerritory: Territory) = controlledTerritory.size * minPopulation

    fun isMinPassed(controlledTerritory: Territory) = getMinPopulation(controlledTerritory) <= population

    fun changeStratumAmountByAspect(aspect: ConverseWrapper, amount: Int) =
            changeStratumAmount(stratumCenter.getByAspect(aspect), amount)

    fun changeStratumAmount(stratum: Stratum, amount: Int): WorkerBunch {
        if (!stratumCenter.strata.contains(stratum))
            throw GroupError("Stratum does not belong to this Population")

        var actualAmount = amount
        if (stratum.freePopulation < actualAmount)
            actualAmount = min(actualAmount, freePopulation + stratum.freePopulation)

        if (actualAmount == 0)
            return WorkerBunch(0, 0)

        val bunch = stratum.useAmount(actualAmount, freePopulation)
        if (freePopulation < 0)
            throw GroupError("Negative free population")

        return bunch
    }

    fun freeStratumAmountByAspect(aspect: ConverseWrapper, bunch: WorkerBunch) =
            stratumCenter.getByAspect(aspect).decreaseWorkedAmount(bunch.actualWorkers)

    fun die() {
        stratumCenter.die()
        population = 0
    }

    fun goodConditionsGrow(fraction: Double, territory: Territory) {
        population += (fraction * population).toInt() / 10 + 1
        if (isMaxReached(territory))
            decreasePopulation(population - getMaxPopulation(territory))
    }

    fun decreasePopulation(amount: Int) {
        var actualAmount = amount
        if (freePopulation < 0) throw GroupError("Negative population in a PopulationCenter")
        actualAmount = min(population, actualAmount)
        val delta = actualAmount - freePopulation
        if (delta > 0)
            for (stratum in stratumCenter.strata) {
                val part = min(
                        (actualAmount * (stratum.population.toDouble() / population) + 1).toInt(),
                        stratum.population
                )
                stratum.decreaseAmount(part)
            }

        population -= actualAmount
    }

    fun update(accessibleTerritory: Territory, group: Group) {
        stratumCenter.update(accessibleTerritory, group, turnResources)

        if (Controller.session.isTime(500))
            turnResources.clearEmpty()
    }

    fun executeRequests(requests: RequestPool) {
        for ((request, pack) in requests.requests.entries)
            pack.addAll(executeRequest(request).pack)
    }

    fun executeRequest(request: Request): ExecutedRequestResult {
        val usedAspects: MutableList<Aspect> = ArrayList()
        val evaluator = request.evaluator
        val strataForRequest = stratumCenter.getStrataForRequest(request)
        strataForRequest.sortedBy { -it.aspect.usefulness }

        val pack = evaluator.pick(
                        request.ceiling,
                        turnResources.resources,
                        { listOf(it.copy(1)) }
                ) { r, p -> listOf(r.getCleanPart(p)) }

        for (stratum in strataForRequest) {
            val amount = evaluator.evaluate(pack)
            if (amount >= request.ceiling)
                break

            val produced: ResourcePack = stratum.use(request.getController(ceil(amount).toInt()))

            if (evaluator.evaluate(produced) > 0)
                usedAspects.add(stratum.aspect)
            pack.addAll(produced)
        }

        val actualPack = request.finalFilter(pack)
        turnResources.addAll(pack)

        if (!request.isFloorSatisfied(actualPack))
            request.group.resourceCenter.addNeeded(request.evaluator.labeler, request.need)

        return ExecutedRequestResult(actualPack, usedAspects)
    }

    fun manageNewAspects(aspects: Set<Aspect?>, newTile: Tile) = aspects
            .filterIsInstance<ConverseWrapper>()
            .forEach { cw ->
                if (stratumCenter.getByAspectOrNull(cw) == null)
                    stratumCenter.addStratum(AspectStratum(0, cw, newTile))
            }

    fun finishUpdate(group: Group) = stratumCenter.finishUpdate(group)

    fun getPart(fraction: Double, newTile: Tile): PopulationCenter {
        val populationPart = (fraction * population).toInt()
        decreasePopulation(populationPart)
        return PopulationCenter(populationPart, maxPopulation, minPopulation, newTile)
    }

    fun wakeNeedStrata(need: Pair<ResourceLabeler, ResourceNeed>) {
        val labeler: AspectLabeler = ProducedLabeler(need.first)
        var options: List<AspectStratum> = stratumCenter.strata
                .filter { it.population == 0 }
                .filterIsInstance<AspectStratum>()
                .filter { labeler.isSuitable(it.aspect) }
        //TODO shuffle
        val population = freePopulation
        if (population < options.size)
            options = options.subList(0, population)
        options.forEach { it.useAmount(1, freePopulation) }
    }

    override fun toString() = """
        Free - $freePopulation
        $stratumCenter
        
        Circulating resources:
        ${turnResources.addLinePrefix()}
        """.trimIndent()
}
