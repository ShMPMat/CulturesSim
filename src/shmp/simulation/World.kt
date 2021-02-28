package shmp.simulation

import shmp.utils.InputDatabase
import shmp.random.randomTile
import shmp.simulation.Controller.session
import shmp.simulation.culture.aspect.AspectInstantiation
import shmp.simulation.culture.aspect.AspectResourceTagParser
import shmp.simulation.culture.group.GROUP_TAG_TYPE
import shmp.simulation.culture.group.GroupConglomerate
import shmp.simulation.culture.group.compulsoryAspects
import shmp.simulation.culture.group.place.StrayPlacesManager
import shmp.simulation.event.EventLog
import shmp.simulation.space.SpaceData.data
import shmp.simulation.space.WorldMap
import shmp.simulation.space.generator.MapGeneratorSupplement
import shmp.simulation.space.generator.fillResources
import shmp.simulation.space.generator.generateMap
import shmp.simulation.space.resource.action.ActionTag
import shmp.simulation.space.resource.instantiation.ResourceInstantiation
import shmp.simulation.space.resource.material.MaterialInstantiation
import shmp.simulation.space.resource.tag.ResourceTag
import shmp.simulation.space.resource.tag.createTagMatchers
import shmp.simulation.space.tile.Tile
import java.util.*
import kotlin.random.Random


//Stores all entities in the shmp.simulation.
class World(proportionCoefficient: Int, random: Random, path: String) {
    var groups: MutableList<GroupConglomerate> = ArrayList()

    val shuffledGroups: List<GroupConglomerate>
        get() = groups.shuffled(session.random)

    val strayPlacesManager = StrayPlacesManager()

    var map: WorldMap

    var events = EventLog()

    val tagMatchers = createTagMatchers("$path/ResourceTagLabelers")

    private val tags = InputDatabase("$path/ResourceTags")
            .readLines()
            .map { ResourceTag(it) }
            .union(tagMatchers.map { it.tag })

    private val actionTags = InputDatabase("$path/ActionTags")
            .readLines()
            .map { ActionTag(it) }

    val aspectPool = AspectInstantiation(tags, actionTags).createPool("$path/Aspects")

    val resourcePool
        get() = data.resourcePool

    init {
        val materialPool = MaterialInstantiation(tags, aspectPool.all.map { it.core.resourceAction })
            .createPool("$path/Materials")
        instantiateSpaceData(
                proportionCoefficient,
                tagMatchers,
                materialPool,
                random
        )
        val initialResourcePool = ResourceInstantiation(
                "$path/Resources",
                aspectPool.all.map { it.core.resourceAction },
                materialPool,
                session.resourceProportionCoefficient,
                AspectResourceTagParser(tags)
        ).createPool()

        data.resourcePool = initialResourcePool

        map = generateMap(
                data.mapSizeX,
                data.mapSizeY,
                data.platesAmount,
                initialResourcePool,
                session.random
        )
    }


    //How many turns passed from the beginning of the shmp.simulation.
    var lesserTurnNumber = 0
        private set
    private var thousandTurns = 0
    private var millionTurns = 0

    fun fillResources() = fillResources(
            map,
            resourcePool,
            MapGeneratorSupplement(IntRange(session.startResourceAmountMin, session.startResourceAmountMax)),
            session.random
    )

    fun initializeFirst() {
        for (i in 0 until session.startGroupAmount) {
            groups.add(GroupConglomerate(1, tileForGroup))
        }

        for (aspectName in compulsoryAspects) {
            groups.forEach { c ->
                c.subgroups.forEach { it.cultureCenter.aspectCenter.addAspect(aspectPool.getValue(aspectName), it) }
            }
        }

        groups.forEach { it.finishUpdate() }
    }

    private val tileForGroup: Tile
        get() {
            while (true) {
                val tile = randomTile(map, session.random)
                if (tile.tagPool.getByType(GROUP_TAG_TYPE).isEmpty()
                        && tile.type != Tile.Type.Water && tile.type != Tile.Type.Mountain) {
                    return tile
                }
            }
        }


    fun getStringTurn() = (lesserTurnNumber + thousandTurns * 1000 + millionTurns * 1000000).toString()
    fun getTurn() = lesserTurnNumber + thousandTurns * 1000 + millionTurns * 1000000

    fun addGroupConglomerate(groupConglomerate: GroupConglomerate) {
        groups.add(groupConglomerate)
    }

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

    fun clearDeadConglomerates() {
        groups.removeIf { it.state == GroupConglomerate.State.Dead }
    }

    override fun toString() = getStringTurn()
}
