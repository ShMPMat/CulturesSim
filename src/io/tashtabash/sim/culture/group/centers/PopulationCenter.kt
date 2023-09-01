package io.tashtabash.sim.culture.group.centers

import io.tashtabash.sim.Controller
import io.tashtabash.sim.CulturesController
import io.tashtabash.sim.culture.aspect.Aspect
import io.tashtabash.sim.culture.aspect.ConverseWrapper
import io.tashtabash.sim.culture.aspect.labeler.AspectLabeler
import io.tashtabash.sim.culture.aspect.labeler.ProducedLabeler
import io.tashtabash.sim.culture.group.GroupError
import io.tashtabash.sim.culture.group.request.ExecutedRequestResult
import io.tashtabash.sim.culture.group.request.Request
import io.tashtabash.sim.culture.group.request.RequestPool
import io.tashtabash.sim.culture.group.stratum.AspectStratum
import io.tashtabash.sim.culture.group.stratum.Person
import io.tashtabash.sim.culture.group.stratum.Stratum
import io.tashtabash.sim.culture.group.stratum.StratumPeople
import io.tashtabash.sim.event.Event
import io.tashtabash.sim.event.Type
import io.tashtabash.sim.space.resource.OwnershipMarker
import io.tashtabash.sim.space.resource.Taker
import io.tashtabash.sim.space.resource.container.MutableResourcePack
import io.tashtabash.sim.space.resource.container.ResourcePack
import io.tashtabash.sim.space.resource.tag.ResourceTag
import io.tashtabash.sim.space.resource.tag.labeler.ResourceLabeler
import io.tashtabash.sim.space.territory.Territory
import io.tashtabash.sim.space.tile.Tile
import io.tashtabash.utils.addLinePrefix
import java.util.*
import kotlin.math.ceil
import kotlin.math.min


class PopulationCenter(
        population: Int,
        private val maxPopulation: Int,
        private val minPopulation: Int,
        ownershipMarker: OwnershipMarker,
        initTile: Tile,
        initResources: ResourcePack
) {
    var population = population
        set(value) {
            if (value != actualPopulation.amount) {
                val diff = value - field

                if (diff > 0)
                    actualPopulation.addAmount(diff, 0.0)
                else if (diff < 0)
                    actualPopulation.getCleanPart(-diff, Taker.SelfTaker)
            }

            field = value
        }

    val actualPopulation = Person(ownershipMarker).copy(population)
    val taker = Taker.ResourceTaker(actualPopulation)

    init {
        initTile.addDelayedResource(actualPopulation)
    }

    val stratumCenter = StratumCenter(initTile)

    val turnResources = MutableResourcePack(initResources)

    val freePopulation: Int
        get() = population - stratumCenter.strata.sumOf { it.population }

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
        population += (fraction * population).toInt() / 5 + 1
        if (isMaxReached(territory))
            decreasePopulation(
                    population - getMaxPopulation(territory),
                    "Growth exceeded the max population"
            )
    }

    fun decreasePopulation(amount: Int, reason: String?) {
        var actualAmount = amount
        if (freePopulation < 0)
            throw GroupError("Negative population in a PopulationCenter")

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

        reason?.let {
            Controller.session.world.events.add(Event(
                    Type.PopulationDecrease,
                    "${actualPopulation.ownershipMarker} population of $population decreased by $actualAmount: $it"
            ))
        }
    }

    fun update(accessibleTerritory: Territory, group: Group) {
        if (actualPopulation.amount < population) {
            val decrease = population - actualPopulation.amount
            decreasePopulation(
                    decrease,
                    "WARNING unexpected populationCenter update, actual population is bigger than expected"
            )
        }

        stratumCenter.update(accessibleTerritory, group, turnResources)

        if (CulturesController.session.isTime(500))
            turnResources.clearEmpty()

        val additionalDanger = group.resourceCenter.pack.getTagPresence(ResourceTag("weapon")) / 1000
        val additionalResistance = group.resourceCenter.pack.getTagPresence(ResourceTag("defence")) / 1000
        actualPopulation.genome.behaviour.danger = 0.05 + additionalDanger
        actualPopulation.genome.behaviour.resistance = 0.1 + additionalResistance
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
                        { it.core.wrappedSample }
                ) { r, p -> listOf(r.getCleanPart(p, taker)) }
        var amount = evaluator.evaluate(pack)

        for (stratum in strataForRequest) {
            if (amount >= request.ceiling)
                break

            val produced = stratum.use(request.getController(ceil(amount).toInt())).resources

            val producedAmount = evaluator.evaluate(produced)
            amount += producedAmount

            if (producedAmount > 0)
                usedAspects.add(stratum.aspect)
            pack.addAll(produced)
        }

        val actualPack = request.finalFilter(pack)
        turnResources.addAll(pack)

        if (!request.isFloorSatisfied(actualPack)) {
            request.group.resourceCenter.addNeeded(request.evaluator.labeler, request.need)
            strataForRequest.filter { it.population == 0 }.forEach { it.importance += request.need }
        }

        return ExecutedRequestResult(MutableResourcePack(actualPack), usedAspects)
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

    fun getPart(fraction: Double, newTile: Tile, ownershipMarker: OwnershipMarker): PopulationCenter {
        val populationPart = (fraction * population).toInt()
        decreasePopulation(populationPart, "Part taken by $ownershipMarker")

        val pack = MutableResourcePack()
        turnResources.resources.forEach {
            pack.addAll(turnResources.getResourcePartAndRemove(it, it.amount / 2, taker))
        }

        return PopulationCenter(populationPart, maxPopulation, minPopulation, ownershipMarker, newTile, pack)
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
