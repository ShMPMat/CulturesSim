package simulation

import extra.InputDatabase
import shmp.random.randomTile
import simulation.Controller.session
import simulation.culture.aspect.AspectInstantiation
import simulation.culture.aspect.AspectResourceTagParser
import simulation.culture.group.GROUP_TAG_TYPE
import simulation.culture.group.GroupConglomerate
import simulation.culture.group.compulsoryAspects
import simulation.culture.group.place.StrayPlacesManager
import simulation.culture.thinking.meaning.GroupMemes
import simulation.event.Event
import simulation.event.EventLog
import simulation.space.SpaceData.data
import simulation.space.WorldMap
import simulation.space.generator.MapGeneratorSupplement
import simulation.space.generator.fillResources
import simulation.space.generator.generateMap
import simulation.space.resource.container.ResourcePool
import simulation.space.resource.instantiation.ResourceInstantiation
import simulation.space.resource.material.MaterialInstantiation
import simulation.space.resource.tag.ResourceTag
import simulation.space.resource.tag.createTagMatchers
import simulation.space.tile.Tile
import java.util.*
import kotlin.random.Random


//Stores all entities in the simulation.
class World(proportionCoefficient: Int, random: Random, path: String) {
    var groups: MutableList<GroupConglomerate> = ArrayList()

    val shuffledGroups: List<GroupConglomerate>
        get() = groups.shuffled(session.random)

    val strayPlacesManager = StrayPlacesManager()

    private val memePool = GroupMemes()

    var map: WorldMap

    var events = EventLog()

    val tagMatchers = createTagMatchers("$path/ResourceTagLabelers")

    private val tags = InputDatabase("$path/ResourceTags").readLines().map { ResourceTag(it) }
            .union(tagMatchers.map { it.tag })

    val aspectPool = AspectInstantiation(tags).createPool("$path/Aspects")

    val resourcePool: ResourcePool

    init {
        val materialPool = MaterialInstantiation(tags, aspectPool.all.map { it.core.resourceAction })
            .createPool("$path/Materials")
        instantiateSpaceData(
                proportionCoefficient,
                tagMatchers,
                materialPool,
                random
        )
        resourcePool = ResourceInstantiation(
                "$path/Resources",
                aspectPool.all.map { it.core.resourceAction },
                materialPool,
                session.resourceProportionCoefficient,
                AspectResourceTagParser(tags)
        ).createPool()
        map = generateMap(
                data.mapSizeX,
                data.mapSizeY,
                data.platesAmount,
                resourcePool,
                session.random
        )
    }


    //How many turns passed from the beginning of the simulation.
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
                c.subgroups.forEach { it.cultureCenter.aspectCenter.addAspect(aspectPool.getValue(aspectName)) }
            }
        }

        groups.forEach { it.finishUpdate() }
    }

    private val tileForGroup: Tile
        private get() {
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

    fun addEvent(event: Event) {
        events.add(event)
    }

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

    override fun toString() = getStringTurn()
}
