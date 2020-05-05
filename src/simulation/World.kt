package simulation

import extra.InputDatabase
import extra.SpaceProbabilityFuncs
import simulation.Controller.session
import simulation.culture.aspect.AspectInstantiation
import simulation.culture.group.GROUP_TAG_TYPE
import simulation.culture.group.GroupConglomerate
import simulation.culture.group.StrayPlacesManager
import simulation.culture.thinking.meaning.GroupMemes
import simulation.culture.thinking.meaning.Meme
import simulation.space.SpaceData.data
import simulation.space.WorldMap
import simulation.space.generator.MapGeneratorSupplement
import simulation.space.generator.fillResources
import simulation.space.generator.generateMap
import simulation.space.resource.ResourceInstantiation
import simulation.space.resource.ResourcePool
import simulation.space.resource.material.MaterialInstantiation
import simulation.space.resource.tag.ResourceTag
import simulation.space.resource.tag.createTagMatchers
import simulation.space.tile.Tile
import java.io.FileReader
import java.util.*

/**
 * Class which stores all entities in the simulation.
 */
class World {
    var groups: MutableList<GroupConglomerate> = ArrayList()
    val shuffledGroups: List<GroupConglomerate>
        get() = groups.shuffled(session.random)
    private val tags = InputDatabase("SupplementFiles/ResourceTags").readLines().map { ResourceTag(it) }
            .union(createTagMatchers("SupplementFiles/ResourceTagLabelers").map { it.tag })
    val aspectPool = AspectInstantiation(tags).createPool("SupplementFiles/Aspects")
    val materialPool = MaterialInstantiation(tags, aspectPool)
            .createPool("SupplementFiles/Materials")
    val resourcePool: ResourcePool

    val strayPlacesManager = StrayPlacesManager()

    private val memePool = GroupMemes()

    var map: WorldMap
    var events: MutableList<Event> = ArrayList()

    init {
        resourcePool = ResourceInstantiation(
                "SupplementFiles/Resources",
                aspectPool,
                materialPool,
                tags
        ).createPool()
        map = generateMap(
                data.mapSizeX,
                data.mapSizeY,
                data.platesAmount,
                session.random
        )
    }

    /**
     * How many turns passed from the beginning of the simulation.
     */
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
        groups.forEach { c ->
            c.subgroups.forEach { it.cultureCenter.aspectCenter.addAspect(aspectPool.getValue("TakeApart")) }
        }
        groups.forEach { c ->
            c.subgroups.forEach { it.cultureCenter.aspectCenter.addAspect(aspectPool.getValue("Take")) }
        }
        groups.forEach { it.finishUpdate() }
    }

    private val tileForGroup: Tile
        private get() {
            while (true) {
                val tile = SpaceProbabilityFuncs.randomTile(map)
                if (tile.tagPool.getByType(GROUP_TAG_TYPE).isEmpty()
                        && tile.type != Tile.Type.Water && tile.type != Tile.Type.Mountain) {
                    return tile
                }
            }
        }

    /**
     * Getter for turn.
     *
     * @return how many turns passed since the beginning of the simulation.
     */
    fun getTurn() = (lesserTurnNumber + thousandTurns * 1000 + millionTurns * 1000000).toString()

    /**
     * Returns Meme by name.
     *
     * @param name name of the Meme.
     * @return Meme with this name.
     */
    fun getPoolMeme(name: String?): Meme? {
        try {
            return memePool.getMemeCopy(name)
        } catch (e: Exception) {
            val i = 0
        }
        return null
    }

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

    override fun toString() = getTurn()
}