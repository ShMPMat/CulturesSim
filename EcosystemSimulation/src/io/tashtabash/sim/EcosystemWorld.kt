package io.tashtabash.sim

import io.tashtabash.random.singleton.RandomSingleton
import io.tashtabash.sim.event.EventLog
import io.tashtabash.sim.init.instantiateSpaceData
import io.tashtabash.sim.space.SpaceData
import io.tashtabash.sim.space.WorldMap
import io.tashtabash.sim.space.generator.MapGeneratorSupplement
import io.tashtabash.sim.space.generator.ResourcePlacer
import io.tashtabash.sim.space.generator.generateMap
import io.tashtabash.sim.space.resource.action.ActionMatcher
import io.tashtabash.sim.space.resource.action.ActionTag
import io.tashtabash.sim.space.resource.action.ResourceAction
import io.tashtabash.sim.space.resource.instantiation.ResourceActionInjector
import io.tashtabash.sim.space.resource.instantiation.ResourceInstantiation
import io.tashtabash.sim.space.resource.instantiation.getResourcePaths
import io.tashtabash.sim.space.resource.instantiation.tag.TagParser
import io.tashtabash.sim.space.resource.material.MaterialInstantiation
import io.tashtabash.sim.space.resource.tag.ResourceTag
import io.tashtabash.sim.space.resource.tag.createTagMatchers
import io.tashtabash.utils.InputDatabase
import java.util.Collections


class EcosystemWorld : World {
    override lateinit var map: WorldMap

    override var events = EventLog()

    private val classLoader = Thread.currentThread().contextClassLoader

    private val tagMatchers = createTagMatchers(classLoader.getResources("ResourceTagLabelers"))

    override val tags = InputDatabase(
        Collections.enumeration(
            getResourcePaths(
                classLoader.getResources("ResourceTags/").toList()
            )
        )
    )
            .readLines()
            .map { ResourceTag(it) }
            .union(tagMatchers.map { it.tag })

    override val actionTags = InputDatabase(classLoader.getResources("ActionTags"))
            .readLines()
            .map { ActionTag(it) }

    override val resourcePool
        get() = SpaceData.data.resourcePool

    override fun initializeMap(
        actions: Map<ResourceAction, List<ActionMatcher>>,
        tagParser: TagParser,
        resourceActionInjectors: List<ResourceActionInjector>,
        proportionCoefficient: Double
    ) {
        val materialPool = MaterialInstantiation(tags, actions.keys.toList()).createPool()

        instantiateSpaceData(proportionCoefficient, tagMatchers, materialPool)

        val initialResources = ResourceInstantiation(
            "Resources/",
            actions,
            materialPool,
            SpaceData.data.resourceProportionCoefficient,
            tagParser,
            resourceActionInjectors
        ).createPool()

        SpaceData.data.resourcePool = initialResources

        map = generateMap(
            SpaceData.data.mapSizeX,
            SpaceData.data.mapSizeY,
            SpaceData.data.platesAmount,
            initialResources,
            RandomSingleton.random
        )
    }


    //How many turns passed from the beginning of the io.tashtabash.simulation.
    override var lesserTurnNumber = 0
        private set
    private var thousandTurns = 0
    private var millionTurns = 0

    override fun placeResources() = ResourcePlacer(
        map,
        resourcePool,
        MapGeneratorSupplement(IntRange(SpaceData.data.startResourceAmountMin, SpaceData.data.startResourceAmountMax)),
        RandomSingleton.random
    ).placeResources()

    override val turn
        get() = lesserTurnNumber + thousandTurns * 1000 + millionTurns * 1000000

    override fun incrementTurn() {
        lesserTurnNumber++
        if (lesserTurnNumber == 1000) {
            lesserTurnNumber = 0
            incrementTurnEvolution()
        }
    }

    fun incrementTurnEvolution() {
        thousandTurns++
        if (thousandTurns == 1000) {
            thousandTurns = 0
            incrementTurnGeology()
        }
    }

    override fun incrementTurnGeology() {
        millionTurns++
    }

    override fun toString() =
        turn.toString()
}
