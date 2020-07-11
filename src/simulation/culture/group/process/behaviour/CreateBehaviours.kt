package simulation.culture.group.process.behaviour

import shmp.random.randomElement
import simulation.Controller.session
import simulation.event.Event
import simulation.SimulationException
import simulation.culture.aspect.hasMeaning
import simulation.culture.group.centers.Group
import simulation.culture.group.place.StaticPlace
import simulation.culture.group.process.action.ProduceExactResourceA
import simulation.culture.group.process.action.ProduceSimpleResourceA
import simulation.culture.group.process.action.ReceiveGroupWideResourcesA
import simulation.culture.group.request.resourceToRequest
import simulation.space.Territory
import simulation.space.tile.TileTag
import simulation.space.tile.getDistance


object RandomArtifactB : AbstractGroupBehaviour() {
    override fun run(group: Group): List<Event> {
        if (group.cultureCenter.memePool.isEmpty) {
            return emptyList()
        }

        val resourcesWithMeaning = group.cultureCenter.aspectCenter.aspectPool.producedResources
                .filter { it.hasMeaning }
        if (resourcesWithMeaning.isEmpty()) {
            return emptyList()
        }

        val chosen = randomElement(resourcesWithMeaning, session.random)
        val result = group.populationCenter.executeRequest(resourceToRequest(chosen, group, 1, 5)).pack

        val events = if (result.isNotEmpty)
            listOf(Event(Event.Type.Creation, "${group.name} created artifacts: $result"))
        else emptyList()

        ReceiveGroupWideResourcesA(group, result).run()

        return events
    }

    override val internalToString = "Make a random Resource with some meaning"
}

class BuildRoadB(private val path: Territory, val projectName: String) : PlanBehaviour() {
    private var built: Int = 0

    override fun run(group: Group): List<Event> {
        if (path.isEmpty)
            return emptyList()

        val roadExample = session.world.resourcePool.get("Road")
        val roadResource = ProduceSimpleResourceA(group, roadExample, 1, 50).run()
                .resources.getOrNull(0) ?: return emptyList()

        if (roadResource.isEmpty)
            return emptyList()

        val tile = path.tiles.last()
        val place = StaticPlace(tile, TileTag(projectName + built, projectName))
        built++
        place.addResource(roadResource)
        path.remove(tile)

        val event = Event(
                Event.Type.Creation,
                "${group.name} created a road on a tile ${tile.x} ${tile.y}",
                "place", place
        )

        return if (path.isEmpty) {
            isFinished = true
            listOf(
                    event,
                    Event(Event.Type.Creation, "${group.name} finished a road creation")
            )
        } else listOf(event)
    }

    override val internalToString
        get() = "Building a road, ${built.toDouble() / (built + path.size)} complete"
}

class ManageRoadsB : AbstractGroupBehaviour() {
    private var projectsDone = 0
    private val roadPlaces = mutableListOf<StaticPlace>()
    private var roadConstruction: BuildRoadB? = null

    override fun run(group: Group): List<Event> {
        if (roadConstruction?.isFinished != false)
            makeNewProject(group)

        for (roadPlace in roadPlaces) {
            val needed = roadPlace.getLacking().lastOrNull()
                    ?: continue
            val lackingPack = ProduceExactResourceA(group, needed, needed.amount, 75).run()

            roadPlace.addResources(lackingPack)
        }

        val events = roadConstruction?.run(group)
                ?: emptyList()

        roadPlaces.addAll(
                events
                        .mapNotNull { it.getAttribute("place") }
                        .filterIsInstance<StaticPlace>()
        )

        return events
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

        val path = group.territoryCenter.makePath(start, finish)
                ?: return
        roadConstruction = BuildRoadB(
                Territory(path),
                "${group.name} road $projectsDone"
        )
        projectsDone++
    }

    private fun getAllPlaceLocations(group: Group): List<StaticPlace> {
        var places = mutableListOf<StaticPlace>()
        for (subgroup in group.parentGroup.subgroups) {
            places.addAll(
                    group.populationCenter.stratumCenter.strata.flatMap { it.places }
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

    override val internalToString
        get() = "RoadManager, $projectsDone roads complete,  current project: " +
                if (roadConstruction == null) "none" else roadConstruction.toString()
}
