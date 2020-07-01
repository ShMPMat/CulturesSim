package simulation.culture.group.stratum

import simulation.Controller
import simulation.SimulationException
import simulation.culture.aspect.getAspectImprovement
import simulation.culture.group.centers.Group
import simulation.culture.group.cultureaspect.SpecialPlace
import simulation.space.tile.Tile
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

class TraderStratum(tile: Tile) : NonAspectStratum(tile, "Stratum of traders") {
    private val tradeAspect = Controller.session.world.aspectPool.get("Trade")
            ?: throw SimulationException("No aspect Trade exists for the $name")

    var _effectiveness = 1.0
        private set

    private var workedPopulation = 0
    override val freePopulation: Int
        get() = population - workedPopulation

    private val effectiveness: Double
        get() {
            if (_effectiveness == -1.0)
                _effectiveness = 1.0 + places
                        .flatMap { it.owned.resources }
                        .map { it.getAspectImprovement(tradeAspect) }
                        .foldRight(0.0, Double::plus)
            return _effectiveness
        }

    override fun useAmount(amount: Int, maxOverhead: Int): WorkerBunch {
        if (amount <= 0)
            return WorkerBunch(0, 0)

        usedThisTurn = true

        val additional = max(0, min(amount - population, maxOverhead))
        population += additional
        val resultAmount = min(population, amount)
        return WorkerBunch(resultAmount, resultAmount)
    }

    override fun finishUpdate(group: Group) {
        _effectiveness = -1.0
        super.finishUpdate(group)
    }

    override fun toString() =
            "$name, population - $population, effectiveness - $effectiveness, importance - $importance" +
                    super.toString()
}
