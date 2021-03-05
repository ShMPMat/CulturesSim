package shmp.simulation.culture.group.centers

import shmp.utils.addLinePrefix
import shmp.simulation.Controller
import shmp.simulation.culture.aspect.Aspect
import shmp.simulation.culture.aspect.ConverseWrapper
import shmp.simulation.culture.aspect.labeler.AspectLabeler
import shmp.simulation.culture.aspect.labeler.ProducedLabeler
import shmp.simulation.culture.group.GroupError
import shmp.simulation.culture.group.request.ExecutedRequestResult
import shmp.simulation.culture.group.request.Request
import shmp.simulation.culture.group.request.RequestPool
import shmp.simulation.culture.group.stratum.*
import shmp.simulation.space.territory.Territory
import shmp.simulation.space.resource.container.MutableResourcePack
import shmp.simulation.space.resource.container.ResourcePack
import shmp.simulation.space.resource.tag.labeler.ResourceLabeler
import shmp.simulation.space.tile.Tile
import java.util.*
import kotlin.math.ceil
import kotlin.math.min

class PopulationCenter(
        var population: Int,
        private val maxPopulation: Int,
        private val minPopulation: Int,
        initTile: Tile,
        initResources: ResourcePack
) {
    val stratumCenter = StratumCenter(initTile)

    val turnResources = MutableResourcePack(initResources)

    val freePopulation: Int
        get() = population - stratumCenter.strata
                .map(Stratum::population)
                .foldRight(0, Int::plus)

    fun getMaxPopulation(controlledTerritory: Territory) = controlledTerritory.size * maxPopulation

    fun isMaxReached(controlledTerritory: Territory) = getMaxPopulation(controlledTerritory) <= population

    fun maxPopulationPart(controlledTerritory: Territory) = population.toDouble() / getMaxPopulation(controlledTerritory)

    fun getMinPopulation(controlledTerritory: Territory) = controlledTerritory.size * minPopulation

    fun isMinPassed(controlledTerritory: Territory) = getMinPopulation(controlledTerritory) <= population

    fun getPeopleByAspect(aspect: ConverseWrapper, amount: Int) =
            getStratumPeople(stratumCenter.getByAspect(aspect), amount)

    fun getStratumPeople(stratum: Stratum, amount: Int): StratumPeople {
        if (!stratumCenter.strata.contains(stratum))
            throw GroupError("Stratum does not belong to this Population")

        var actualAmount = amount
        if (stratum.freePopulation < actualAmount)
            actualAmount = min(actualAmount, freePopulation + stratum.freePopulation)

        if (actualAmount == 0)
            return StratumPeople(0, stratum)

        val bunch = stratum.useAmount(actualAmount, freePopulation)
        if (freePopulation < 0)
            throw GroupError("Negative free population")

        return bunch
    }

    fun freeStratumAmountByAspect(aspect: ConverseWrapper, bunch: StratumPeople) =
            stratumCenter.getByAspect(aspect).decreaseWorkedAmount(bunch.workers)

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
        strataForRequest.sortedByDescending { it.aspect.usefulness }

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

    fun movePopulation(tile: Tile) {
        stratumCenter.movePopulation(tile)
    }

    fun getPart(fraction: Double, newTile: Tile): PopulationCenter {
        val populationPart = (fraction * population).toInt()
        decreasePopulation(populationPart)

        val pack = MutableResourcePack()
        turnResources.resources.forEach {
            pack.addAll(turnResources.getResourcePartAndRemove(it, it.amount / 2))
        }

        return PopulationCenter(populationPart, maxPopulation, minPopulation, newTile, pack)
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
        |Free - $freePopulation
        |$stratumCenter
        |
        |Circulating resources:
        |${turnResources.addLinePrefix()}
        """.trimMargin()
}