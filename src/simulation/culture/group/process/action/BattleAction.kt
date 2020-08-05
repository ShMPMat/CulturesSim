package simulation.culture.group.process.action

import shmp.random.randomElement
import shmp.random.testProbability
import simulation.Controller
import simulation.culture.group.centers.AdministrationType
import simulation.culture.group.centers.Group
import simulation.culture.group.centers.getSubordinates
import simulation.culture.group.stratum.WorkerBunch
import simulation.space.tile.Tile
import kotlin.math.ceil
import kotlin.math.pow


class EstimateForcesA(group: Group, val toEstimate: Group): AbstractGroupAction(group) {
    override fun run(): Double =
            toEstimate.populationCenter.stratumCenter.warriorStratum.cumulativeWorkAblePopulation +
                    toEstimate.processCenter.type.getSubordinates(toEstimate)
                            .map { it.populationCenter.stratumCenter.warriorStratum.cumulativeWorkAblePopulation }
                            .foldRight(0.0, Double::plus)

    override val internalToString = "Let ${group.name} estimate the war power of ${toEstimate.name}"
}


class DecideBattleTileA(group: Group, val opponent: Group) : AbstractGroupAction(group) {
    override fun run(): Tile {
        val path = group.territoryCenter.makePath(group.territoryCenter.center, opponent.territoryCenter.center)
                ?: return opponent.territoryCenter.center

        return randomElement(path, Controller.session.random)
    }

    override val internalToString = "Let ${group.name} decide where to battle with ${opponent.name}"
}


class GatherWarriorsA(group: Group, val ceiling: Double) : AbstractGroupAction(group) {
    override fun run(): List<WorkerBunch> {
        val warriors = mutableListOf<WorkerBunch>()
        var neededWarriors = ceiling

        warriors.add(group.populationCenter.getStratumPeople(
                group.populationCenter.stratumCenter.warriorStratum,
                ceil(neededWarriors).toInt()
        ))
        neededWarriors -= warriors[0].cumulativeWorkers

        for (subordinate in group.processCenter.type.getSubordinates(group)) {
            if (neededWarriors <= 0)
                break

            val newWarriors = GatherWarriorsA(subordinate, neededWarriors).run()
            neededWarriors -= newWarriors
                    .map { it.cumulativeWorkers }
                    .foldRight(0, Int::plus)

            warriors += newWarriors
        }

        return warriors
    }

    override val internalToString = "Let ${group.name} gather army"
}
