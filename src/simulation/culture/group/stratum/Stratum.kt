package simulation.culture.group.stratum

import simulation.culture.group.centers.Group
import simulation.space.Territory
import simulation.space.resource.MutableResourcePack
import simulation.space.tile.Tile
import simulation.space.tile.TileTag

interface Stratum {
    val population: Int
    val freePopulation: Int
    val importance: Int
    val ego: Ego

    fun decreaseAmount(amount: Int)

    fun useAmount(amount: Int, maxOverhead: Int): WorkerBunch

    fun update(
            accessibleResources: MutableResourcePack,
            accessibleTerritory: Territory,
            group: Group
    )

    fun finishUpdate(group: Group)

    fun die()
}

abstract class BaseStratum(tile: Tile, name: String): Stratum {
    override val ego = Ego(tile, name)

    override fun toString() = "\nEgo resources: ${ego.place.current.owned}"
}

data class WorkerBunch(val cumulativeWorkers: Int, val actualWorkers: Int)