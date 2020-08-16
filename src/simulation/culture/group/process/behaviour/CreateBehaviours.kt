package simulation.culture.group.process.behaviour

import shmp.random.randomElement
import simulation.Controller.session
import simulation.event.Event
import simulation.SimulationError
import simulation.culture.aspect.hasMeaning
import simulation.culture.group.RoadCreationEvent
import simulation.culture.group.centers.Group
import simulation.culture.group.place.StaticPlace
import simulation.culture.group.process.ProcessResult
import simulation.culture.group.process.action.ProduceExactResourceA
import simulation.culture.group.process.action.ProduceSimpleResourceA
import simulation.culture.group.process.action.ReceiveGroupWideResourcesA
import simulation.culture.group.process.emptyProcessResult
import simulation.culture.thinking.meaning.flattenMemePair
import simulation.culture.thinking.meaning.makeResourceMemes
import simulation.culture.thinking.meaning.makeResourcePackMemes
import simulation.event.Type
import simulation.space.Territory
import simulation.space.tile.TileTag
import simulation.space.tile.getDistance


object RandomArtifactB : AbstractGroupBehaviour() {
    override fun run(group: Group): ProcessResult {
        if (group.cultureCenter.memePool.isEmpty)
            return emptyProcessResult

        val resourcesWithMeaning = group.cultureCenter.aspectCenter.aspectPool.producedResources
                .filter { it.hasMeaning }

        if (resourcesWithMeaning.isEmpty())
            return emptyProcessResult

        val chosen = randomElement(resourcesWithMeaning, session.random)
        val result = ProduceExactResourceA(group, chosen, 1, 5).run()

        val processResult = if (result.isNotEmpty)
            ProcessResult(Event(Type.Creation, "${group.name} created artifacts: $result")) +
                    ProcessResult(makeResourcePackMemes(result))
        else emptyProcessResult

        ReceiveGroupWideResourcesA(group, result).run()

        return processResult
    }

    override val internalToString = "Make a random Resource with some meaning"
}

class BuildRoadB(private val path: Territory, val projectName: String) : PlanBehaviour() {
    private var built: Int = 0

    override fun run(group: Group): ProcessResult {
        if (path.isEmpty)
            return emptyProcessResult

        val roadExample = session.world.resourcePool.getSimpleName("Road")
        val roadResource = ProduceSimpleResourceA(group, roadExample, 1, 50).run()
                .resources.getOrNull(0)
                ?: return emptyProcessResult

        if (roadResource.isEmpty)
            return emptyProcessResult

        val roadMemes = makeResourceMemes(roadResource).flattenMemePair()

        val tile = path.tiles.last()
        val place = StaticPlace(tile, TileTag(projectName + built, projectName))
        built++
        place.addResource(roadResource)
        path.remove(tile)

        val event = RoadCreationEvent(
                "${group.name} created a road on a tile ${tile.posStr}",
                place
        )

        return ProcessResult(roadMemes) + if (path.isEmpty) {
            isFinished = true

            ProcessResult(event, Event(Type.Creation, "${group.name} finished a road creation"))
        } else emptyProcessResult
    }

    override val internalToString
        get() = "Building a road, ${built.toDouble() / (built + path.size)} complete"
}


class ManageRoadsB : AbstractGroupBehaviour() {
    private var projectsDone = 0
    private val roadPlaces = mutableListOf<StaticPlace>()
    private var roadConstruction: BuildRoadB? = null

    override fun run(group: Group): ProcessResult {
        if (roadConstruction?.isFinished != false)
            makeNewProject(group)

        for (roadPlace in roadPlaces) {
            val needed = roadPlace.getLacking().lastOrNull()
                    ?: continue
            val lackingPack = ProduceExactResourceA(group, needed, needed.amount, 75).run()

            roadPlace.addResources(lackingPack)
        }

        val processResult = roadConstruction?.run(group)
                ?: emptyProcessResult

        roadPlaces.addAll(
                processResult.events
                        .filterIsInstance<RoadCreationEvent>()
                        .map { it.place }
        )

        return processResult
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
                ?: throw SimulationError("IMPOSSIBLE")
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
