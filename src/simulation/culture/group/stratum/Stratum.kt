package simulation.culture.group.stratum

import simulation.culture.group.centers.Group
import simulation.culture.group.place.StaticPlace
import simulation.space.Territory
import simulation.space.resource.container.MutableResourcePack
import simulation.space.tile.Tile

interface Stratum {
    val population: Int
    val freePopulation: Int
    var importance: Int
    val ego: Ego

    val places: List<StaticPlace>

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


abstract class BaseStratum(tile: Tile, protected val name: String) : Stratum {
    override val ego = Ego(tile, name)


    protected val innerPlaces = mutableListOf<StaticPlace>()
    override val places: List<StaticPlace>
        get() = innerPlaces

    override fun toString() = "\nEgo resources: ${ego.place.current.owned}"
}


data class WorkerBunch(val cumulativeWorkers: Int, val actualWorkers: Int)
