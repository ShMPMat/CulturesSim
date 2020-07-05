package simulation.culture.group.process.behaviour

import shmp.random.randomElement
import simulation.Controller.session
import simulation.Event
import simulation.SimulationException
import simulation.culture.aspect.hasMeaning
import simulation.culture.group.centers.Group
import simulation.culture.group.place.StaticPlace
import simulation.culture.group.process.action.ProduceExactResourceA
import simulation.culture.group.process.action.ProduceSimpleResourceA
import simulation.culture.group.process.action.ReceiveGroupWideResourcesA
import simulation.culture.group.request.resourceToRequest
import simulation.space.Territory
import simulation.space.tile.Tile
import simulation.space.tile.getDistance


object RandomArtifactBehaviour : AbstractGroupBehaviour() {
    override fun run(group: Group): List<Event> {
        if (group.cultureCenter.memePool.isEmpty)
            return emptyList()

        val resourcesWithMeaning = group.cultureCenter.aspectCenter.aspectPool.producedResources
                .filter { it.first.hasMeaning }
                .map { it.first }
        if (resourcesWithMeaning.isEmpty())
            return emptyList()

        val chosen = randomElement(resourcesWithMeaning, session.random)
        val result = group.populationCenter.executeRequest(resourceToRequest(chosen, group, 1, 5)).pack

        val events = if (result.isNotEmpty)
            listOf(Event(Event.Type.Creation, "${group.name} created artifacts: $result"))
        else emptyList()

        ReceiveGroupWideResourcesA(group, result).run()

        return events
    }

    override fun toString() = "Make a random Resource with some meaning"
}

class BuildRoadBehaviour(private val path: Territory) : PlanBehaviour() {
    override fun run(group: Group): List<Event> {
        if (path.isEmpty)
            return emptyList()

        if (group.cultureCenter.aspectCenter.aspectPool.all.firstOrNull { it.name.contains("RoadOn")} != null) {
            val k = 0
        } else if (group.parentGroup.aspects.firstOrNull { it.name.contains("RoadOn")} != null) {
            val k = 0
        }
        val roadExample = session.world.resourcePool.get("Road")
        val roadResource = ProduceSimpleResourceA(group, roadExample, 1, 50).run()
                .resources.getOrNull(0) ?: return emptyList()

        if (roadResource.isEmpty)
            return emptyList()

        val tile = path.tiles.last()
        tile.addDelayedResource(roadResource)
        path.remove(tile)

        val event = Event(Event.Type.Creation, "${group.name} created a road on a tile ${tile.x} ${tile.y}")

        return if (path.isEmpty) {
            isFinished = true
            listOf(
                    event,
                    Event(Event.Type.Creation, "${group.name} finished a road creation")
            )
        } else listOf(event)
    }
}

class ManageRoadsBehaviour : GroupBehaviour {
    private val roadPlaces = mutableListOf<StaticPlace>()
    private var roadConstruction: BuildRoadBehaviour? = null

    override fun run(group: Group): List<Event> {
        if (roadConstruction?.isFinished != false)
            makeNewProject(group)

        return roadConstruction?.run(group)
                ?: emptyList()
    }

    private fun makeNewProject(group: Group) {
        val placeMap = getAllPlaceLocations(group)
                .groupBy { it in roadPlaces }
        val connected = placeMap[true] ?: emptyList()
        val disconnected = (placeMap[false] ?: emptyList()).toMutableList()

        if (connected.size + disconnected.size <= 1 || disconnected.isEmpty())
            return

        val finish = randomElement(disconnected, session.random).tile

        val startPlace = roadPlaces
                .minBy { getDistance(it.tile, finish) }
                ?: disconnected
                        .filter { it.tile != finish }
                        .minBy { getDistance(it.tile, finish) }
                ?: throw SimulationException("IMPOSSIBLE")
        val start = startPlace.tile

        roadConstruction = BuildRoadBehaviour(Territory(makePath(start, finish, group)))
    }

    private fun getAllPlaceLocations(group: Group): List<StaticPlace> {
        var places = mutableListOf<StaticPlace>()
        for (subgroup in group.parentGroup.subgroups) {
            places.addAll(
                    group.populationCenter.strata.flatMap { it.places }
            )
            places.addAll(
                    group.cultureCenter.cultureAspectCenter.aspectPool.worships
                            .flatMap { it.placeSystem.places }
                            .map { it.staticPlace }
            )
        }
        places = places
                .filter { it.owned.isNotEmpty }
                .toMutableList()
        places.addAll(group.parentGroup.subgroups.flatMap { it.territoryCenter.places })
        places = places
                .distinctBy { it.tile }
                .toMutableList()

        return places
    }
}

private fun makePath(start: Tile, finish: Tile, group: Group): Collection<Tile> {
    val checked = mutableSetOf<Tile>()
    val queue = mutableListOf<TileAndPrev>()
    queue.add(TileAndPrev(start, null))

    while (true) {
        checked.addAll(queue.map { it.tile })
        val currentTiles = queue
                .filter { group.territoryCenter.isTileReachableInTraverse(it.tile to 0) }
                .flatMap { it.tile.neighbours.map { n -> TileAndPrev(n, it) } }.asSequence()
                .groupBy { it.tile }
                .map { it.value.minBy { p -> p.length } ?: throw SimulationException("IMPOSSIBLE") }
                .filter { !checked.contains(it.tile) }.toList()

        val maybeFinish = currentTiles.firstOrNull { it.tile == finish }

        if (maybeFinish != null)
            return maybeFinish.unwind()

        queue.clear()
        queue.addAll(currentTiles)
    }
}

data class TileAndPrev(val tile: Tile, val prev: TileAndPrev?, val length: Int = 0) {
    fun next(next: Tile) = TileAndPrev(next, this, length + 1)
}

fun TileAndPrev.unwind(): List<Tile> =
        listOf(this.tile) + if (this.prev == null) emptyList() else this.prev.unwind()
