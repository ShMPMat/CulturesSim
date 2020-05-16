package simulation.culture.group.centers

import shmp.random.testProbability
import simulation.Controller.*
import simulation.culture.aspect.Aspect
import simulation.culture.aspect.ConverseWrapper
import simulation.culture.aspect.labeler.AspectLabeler
import simulation.culture.aspect.labeler.ProducedLabeler
import simulation.culture.group.GroupError
import simulation.culture.group.request.ExecutedRequestResult
import simulation.culture.group.request.Request
import simulation.culture.group.request.RequestPool
import simulation.culture.group.stratum.AspectStratum
import simulation.culture.group.stratum.Stratum
import simulation.culture.group.stratum.WarriorStratum
import simulation.culture.group.stratum.WorkerBunch
import simulation.space.Territory
import simulation.space.resource.MutableResourcePack
import simulation.space.resource.ResourcePack
import simulation.space.resource.tag.labeler.ResourceLabeler
import java.util.*
import kotlin.math.ceil
import kotlin.math.min

class PopulationCenter(var population: Int, private val maxPopulation: Int, private val minPopulation: Int) {
    private val _strata: MutableList<Stratum> = ArrayList()
    val strata: List<Stratum>
        get() = _strata
    val turnResources = MutableResourcePack()

    init {
        addStratum(WarriorStratum())
    }

    fun getStratumByAspect(aspect: ConverseWrapper): AspectStratum {
        return _strata
                .filterIsInstance<AspectStratum>()
                .first { it.aspect == aspect }
    }

    val freePopulation: Int
        get() = population - _strata
                .map(Stratum::population)
                .foldRight(0, Int::plus)

    fun getMaxPopulation(controlledTerritory: Territory) = controlledTerritory.size() * maxPopulation

    fun isMaxReached(controlledTerritory: Territory) = getMaxPopulation(controlledTerritory) <= population

    fun getMinPopulation(controlledTerritory: Territory) = controlledTerritory.size() * minPopulation

    fun isMinPassed(controlledTerritory: Territory) = getMinPopulation(controlledTerritory) <= population

    fun changeStratumAmountByAspect(aspect: ConverseWrapper, amount: Int) =
            changeStratumAmount(getStratumByAspect(aspect), amount)

    fun changeStratumAmount(stratum: Stratum, amount: Int): WorkerBunch {
        var actualAmount = amount
        if (!_strata.contains(stratum)) throw GroupError("Stratum does not belong to this Population")
        if (stratum.freePopulation < actualAmount)
            actualAmount = Math.min(actualAmount, freePopulation + stratum.freePopulation)
        if (actualAmount == 0)
            return WorkerBunch(0, 0)
        val bunch = stratum.useAmount(actualAmount, freePopulation)
        if (freePopulation < 0) throw GroupError("Negative free population")
        return bunch
    }

    fun freeStratumAmountByAspect(aspect: ConverseWrapper, bunch: WorkerBunch) = try {
        getStratumByAspect(aspect).decreaseWorkedAmount(bunch.actualWorkers)
    } catch (e: NullPointerException) {
        throw RuntimeException("No stratum for Aspect")
    }

    fun die() {
        _strata.forEach { it.die() }
        population = 0
    }

    fun goodConditionsGrow(fraction: Double, territory: Territory) {
        population += (fraction * population).toInt() / 10 + 1
        if (isMaxReached(territory)) {
            decreasePopulation(population - getMaxPopulation(territory))
        }
    }

    fun decreasePopulation(amount: Int) {
        var actualAmount = amount
        if (freePopulation < 0) throw GroupError("Negative population in a PopulationCenter")
        actualAmount = min(population, actualAmount)
        val delta = actualAmount - freePopulation
        if (delta > 0) {
            for (stratum in _strata) {
                val part = min(
                        (actualAmount * (stratum.population.toDouble() / population) + 1).toInt(),
                        stratum.population
                )
                stratum.decreaseAmount(part)
            }
        }
        population -= actualAmount
    }

    fun update(accessibleTerritory: Territory, group: Group) {
        if (testProbability(session.egoRenewalProb, session.random)) {
            val mostImportantStratum = strata.maxBy { it.importance } ?: return
            if (mostImportantStratum.importance > 0)
                mostImportantStratum.ego.isActive = true
        }
        _strata.forEach { it.update(turnResources, accessibleTerritory, group) }
    }

    fun executeRequests(requests: RequestPool) {
        for ((request, pack) in requests.requests.entries)
            pack.addAll(executeRequest(request).pack)
    }

    fun executeRequest(request: Request): ExecutedRequestResult {
        val usedAspects: MutableList<Aspect> = ArrayList()
        val evaluator = request.evaluator
        val strataForRequest = getStrataForRequest(request)
        strataForRequest.sortedBy { -it.aspect.usefulness }
        val pack = MutableResourcePack(
                evaluator.pick(
                        request.ceiling,
                        turnResources.resources,
                        { listOf(it.copy(1)) }
                ) { r, p -> listOf(r.getCleanPart(p)) }
        )
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
        return ExecutedRequestResult(actualPack, usedAspects)
    }

    private fun getStrataForRequest(request: Request): List<AspectStratum> {
        return _strata
                .filter { request.isAcceptable(it) != null }
                .sortedBy { request.satisfactionLevel(it) }
                .filterIsInstance<AspectStratum>()
    }

    fun manageNewAspects(aspects: Set<Aspect?>) {
        aspects
                .filterIsInstance<ConverseWrapper>()
                .forEach { cw ->
                    if (_strata.filterIsInstance<AspectStratum>().none { it.aspect == cw }) {
                        _strata.add(AspectStratum(0, cw))
                    }
                }
    }

    fun finishUpdate(group: Group) = _strata.forEach { it.finishUpdate(group) }

    fun getPart(fraction: Double): PopulationCenter {
        val populationPart = (fraction * population).toInt()
        decreasePopulation(populationPart)
        return PopulationCenter(populationPart, maxPopulation, minPopulation)
    }

    fun wakeNeedStrata(need: Pair<ResourceLabeler, ResourceNeed>) {
        val labeler: AspectLabeler = ProducedLabeler(need.first)
        var options: List<AspectStratum> = _strata
                .filter { it.population == 0 }
                .filterIsInstance<AspectStratum>()
                .filter { labeler.isSuitable(it.aspect) }
        //TODO shuffle
        val population = freePopulation
        if (population < options.size)
            options = options.subList(0, population)
        options.forEach { it.useAmount(1, freePopulation) }
    }

    override fun toString(): String {
        val stringBuilder = StringBuilder("Free - $freePopulation\n")
        for (stratum in _strata)
            if (stratum.population != 0)
                stringBuilder.append(stratum).append("\n")
        return stringBuilder.toString()
    }

    fun addStratum(stratum: Stratum) {
        _strata.add(stratum)
    }
}