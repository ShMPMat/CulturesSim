package io.tashtabash.sim.culture.group.process.behaviour

import io.tashtabash.random.singleton.randomElement
import io.tashtabash.random.singleton.randomElementOrNull
import io.tashtabash.sim.CulturesController.Companion.session
import io.tashtabash.sim.SimulationError
import io.tashtabash.sim.culture.aspect.hasMeaning
import io.tashtabash.sim.culture.group.RoadCreationEvent
import io.tashtabash.sim.culture.group.centers.Group
import io.tashtabash.sim.culture.group.centers.Trait
import io.tashtabash.sim.culture.group.centers.toPositiveChange
import io.tashtabash.sim.culture.group.cultureaspect.util.createDepictObject
import io.tashtabash.sim.culture.group.place.StaticPlace
import io.tashtabash.sim.culture.group.process.ProcessResult
import io.tashtabash.sim.culture.group.process.action.ProduceResourceA
import io.tashtabash.sim.culture.group.process.action.ProduceSimpleResourceA
import io.tashtabash.sim.culture.group.process.action.ReceiveGroupWideResourcesA
import io.tashtabash.sim.culture.group.process.emptyProcessResult
import io.tashtabash.sim.culture.group.request.RequestType
import io.tashtabash.sim.culture.thinking.meaning.constructAndAddSimpleMeme
import io.tashtabash.sim.culture.thinking.meaning.flattenMemePair
import io.tashtabash.sim.culture.thinking.meaning.makeResourceMemes
import io.tashtabash.sim.culture.thinking.meaning.makeResourcePackMemes
import io.tashtabash.sim.event.Creation
import io.tashtabash.sim.event.Event
import io.tashtabash.sim.space.territory.StaticTerritory
import io.tashtabash.sim.space.territory.Territory
import io.tashtabash.sim.space.tile.TileTag
import io.tashtabash.sim.space.tile.getDistance


object RandomArtifactB : AbstractGroupBehaviour() {
    override fun run(group: Group): ProcessResult {
        if (group.cultureCenter.memePool.isEmpty)
            return emptyProcessResult

        val resourcesWithMeaning = group.cultureCenter.aspectCenter.aspectPool.producedResources
            .filter { it.hasMeaning }
        val chosen = resourcesWithMeaning.randomElementOrNull()
            ?: return emptyProcessResult

        val result = ProduceResourceA(group, chosen, 1, 5, setOf(RequestType.Luxury)).run()
        val processResult =
            if (result.isNotEmpty)
                ProcessResult(Event(Creation, "${group.name} created artifacts: $result")) +
                        ProcessResult(makeResourcePackMemes(result))
            else emptyProcessResult

        ReceiveGroupWideResourcesA(group, result).run()

        return processResult + ProcessResult(Trait.Creation.toPositiveChange())
    }

    override val internalToString = "Make a random Resource with some meaning"
}

object RandomDepictCaB : AbstractGroupBehaviour() {
    override fun run(group: Group): ProcessResult {
        if (group.cultureCenter.memePool.isEmpty)
            return emptyProcessResult

        val meaningAspects = group.cultureCenter.aspectCenter.aspectPool.getMeaningAspects()

        if (meaningAspects.isEmpty()) {
            val aspect = session.world.aspectPool.filter { it.canApplyMeaning }.randomElementOrNull()
                ?: return emptyProcessResult
            group.cultureCenter.aspectCenter.tryAddingAspect(aspect, group)
            return emptyProcessResult
        }

        val depict = createDepictObject(
            meaningAspects,
            constructAndAddSimpleMeme(group.cultureCenter.memePool),
            null
        )

        group.cultureCenter.cultureAspectCenter.addCultureAspect(depict)

        val processResult =
            if (depict == null) emptyProcessResult
            else ProcessResult(Event(Creation, "${group.name} now has: $depict"))

        return processResult + ProcessResult(Trait.Creation.toPositiveChange() * 10)
    }

    override val internalToString = "Make a random Depiction of something"
}

class BuildRoadB(path: Territory, val projectName: String) : PlanBehaviour() {
    private var built: Int = 0
    private val path = path.tiles.toMutableList()

    override fun run(group: Group): ProcessResult {
        if (path.isEmpty())
            return emptyProcessResult

        val roadExample = session.world.resourcePool.getSimpleName("Road")
        val roadResource = ProduceSimpleResourceA(group, roadExample, 1, 50).run()
            .resources.getOrNull(0)
            ?: return emptyProcessResult

        if (roadResource.isEmpty)
            return emptyProcessResult

        val roadMemes = makeResourceMemes(roadResource).flattenMemePair()

        val tile = path.last()
        val place = StaticPlace(tile, TileTag(projectName + built, projectName))
        built++
        place.addResource(roadResource)
        path.removeAt(path.lastIndex)

        val event = RoadCreationEvent(
            "${group.name} created a road on a tile ${tile.posStr}",
            place
        )

        return ProcessResult(roadMemes) +
                ProcessResult(Trait.Creation.toPositiveChange() * 0.05) +
                if (path.isEmpty()) {
                    isFinished = true

                    ProcessResult(event, Event(Creation, "${group.name} finished a road creation"))
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
            val types = setOf(RequestType.Improvement)
            val lackingPack = ProduceResourceA(group, needed, needed.amount, 75, types).run()

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

        val finish = disconnected.randomElement().tile

        val startPlace = roadPlaces
            .minByOrNull { getDistance(it.tile, finish) }
            ?: disconnected
                .filter { it.tile != finish }
                .minByOrNull { getDistance(it.tile, finish) }
            ?: throw SimulationError("IMPOSSIBLE")
        val start = startPlace.tile

        val path = group.territoryCenter.makePath(start, finish)
            ?: return
        roadConstruction = BuildRoadB(
            StaticTerritory(path.toSet()),
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
