package io.tashtabash.simulation

import io.tashtabash.random.singleton.RandomSingleton
import io.tashtabash.simulation.event.EventLog
import io.tashtabash.simulation.space.SpaceData.data
import io.tashtabash.simulation.space.WorldMap
import io.tashtabash.simulation.space.generator.MapGeneratorSupplement
import io.tashtabash.simulation.space.generator.ResourcePlacer
import io.tashtabash.simulation.space.generator.generateMap
import io.tashtabash.simulation.space.resource.action.ActionMatcher
import io.tashtabash.simulation.space.resource.action.ActionTag
import io.tashtabash.simulation.space.resource.action.ResourceAction
import io.tashtabash.simulation.space.resource.instantiation.ResourceActionInjector
import io.tashtabash.simulation.space.resource.instantiation.ResourceInstantiation
import io.tashtabash.simulation.space.resource.instantiation.tag.TagParser
import io.tashtabash.simulation.space.resource.material.MaterialInstantiation
import io.tashtabash.simulation.space.resource.tag.ResourceTag
import io.tashtabash.simulation.space.resource.tag.createTagMatchers
import io.tashtabash.utils.InputDatabase


//Stores all entities in the io.tashtabash.simulation
open class World {
    lateinit var map: WorldMap

    var events = EventLog()

    private val tagMatchers = createTagMatchers(this::class.java.classLoader.getResources("ResourceTagLabelers"))

    val tags = InputDatabase(this::class.java.classLoader.getResources("ResourceTags"))
            .readLines()
            .map { ResourceTag(it) }
            .union(tagMatchers.map { it.tag })

    protected val actionTags = InputDatabase(this::class.java.classLoader.getResources("ActionTags"))
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
