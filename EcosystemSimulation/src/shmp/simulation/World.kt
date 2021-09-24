package shmp.simulation

import shmp.random.singleton.RandomSingleton
import shmp.simulation.event.EventLog
import shmp.simulation.space.SpaceData.data
import shmp.simulation.space.WorldMap
import shmp.simulation.space.generator.MapGeneratorSupplement
import shmp.simulation.space.generator.fillResources
import shmp.simulation.space.generator.generateMap
import shmp.simulation.space.resource.action.ActionTag
import shmp.simulation.space.resource.action.ResourceAction
import shmp.simulation.space.resource.instantiation.ResourceActionInjector
import shmp.simulation.space.resource.instantiation.ResourceInstantiation
import shmp.simulation.space.resource.instantiation.tag.TagParser
import shmp.simulation.space.resource.material.MaterialInstantiation
import shmp.simulation.space.resource.tag.ResourceTag
import shmp.simulation.space.resource.tag.createTagMatchers
import shmp.utils.InputDatabase


//Stores all entities in the shmp.simulation
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
            actions: List<ResourceAction>,
            tagParser: TagParser,
            resourceActionInjectors: List<ResourceActionInjector>,
            proportionCoefficient: Int
    ) {
        val materialPool = MaterialInstantiation(tags, actions).createPool()

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


    //How many turns passed from the beginning of the shmp.simulation.
    var lesserTurnNumber = 0
        private set
    private var thousandTurns = 0
    private var millionTurns = 0

    fun fillResources() = fillResources(
            map,
            resourcePool,
            MapGeneratorSupplement(IntRange(data.startResourceAmountMin, data.startResourceAmountMax)),
            RandomSingleton.random
    )

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
