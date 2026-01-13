package io.tashtabash.sim

import io.tashtabash.random.singleton.RandomSingleton
import io.tashtabash.sim.event.EventLog
import io.tashtabash.sim.space.SpaceData.data
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


//Stores all entities in the io.tashtabash.simulation
open class World {
    lateinit var map: WorldMap

    var events = EventLog()

    private val classLoader = Thread.currentThread().contextClassLoader

    private val tagMatchers = createTagMatchers(classLoader.getResources("ResourceTagLabelers"))

    val tags = InputDatabase(Collections.enumeration(getResourcePaths(classLoader.getResources("ResourceTags/").toList())))
            .readLines()
            .map { ResourceTag(it) }
            .union(tagMatchers.map { it.tag })

    protected val actionTags = InputDatabase(classLoader.getResources("ActionTags"))
            .readLines()
            .map { ActionTag(it) }

    val resourcePool
        get() = data.resourcePool

    fun initializeMap(
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
                data.resourceProportionCoefficient,
                tagParser,
                resourceActionInjectors
        ).createPool()

        data.resourcePool = initialResources

        map = generateMap(data.mapSizeX, data.mapSizeY, data.platesAmount, initialResources, RandomSingleton.random)
    }


    //How many turns passed from the beginning of the io.tashtabash.simulation.
    var lesserTurnNumber = 0
        private set
    private var thousandTurns = 0
    private var millionTurns = 0

    fun placeResources() = ResourcePlacer(
            map,
            resourcePool,
            MapGeneratorSupplement(IntRange(data.startResourceAmountMin, data.startResourceAmountMax)),
            RandomSingleton.random
    ).placeResources()

    fun getStringTurn() = (lesserTurnNumber + thousandTurns * 1000 + millionTurns * 1000000).toString()
    fun getTurn() = lesserTurnNumber + thousandTurns * 1000 + millionTurns * 1000000

    fun incrementTurn() {
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

    fun incrementTurnGeology() {
        millionTurns++
    }

    override fun toString() = getStringTurn()
}
