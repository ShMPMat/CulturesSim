package shmp.simulation

import shmp.random.singleton.RandomSingleton
import shmp.random.singleton.randomTile
import shmp.simulation.CulturesController.session
import shmp.simulation.culture.aspect.AspectInstantiation
import shmp.simulation.culture.aspect.AspectPool
import shmp.simulation.culture.aspect.AspectResourceTagParser
import shmp.simulation.culture.group.GROUP_TAG_TYPE
import shmp.simulation.culture.group.GroupConglomerate
import shmp.simulation.culture.group.compulsoryAspects
import shmp.simulation.culture.group.place.StrayPlacesManager
import shmp.simulation.space.tile.Tile
import java.util.*


class CulturesWorld(private val path: String): World(path) {
    var groups: MutableList<GroupConglomerate> = ArrayList()

    val shuffledGroups: List<GroupConglomerate>
        get() = groups.shuffled(RandomSingleton.random)

    val strayPlacesManager = StrayPlacesManager()

    lateinit var aspectPool: AspectPool

    fun initializeMap(proportionCoefficient: Int) {
        aspectPool = AspectInstantiation(tags, actionTags).createPool("$path/Aspects")

        val actions = aspectPool.all.map { it.core.resourceAction }

        initializeMap(actions, AspectResourceTagParser(tags), proportionCoefficient)
    }

    fun initializeGroups() {
        for (i in 0 until session.startGroupAmount)
            groups.add(GroupConglomerate(1, tileForGroup))

        for (aspectName in compulsoryAspects)
            groups.forEach { c ->
                c.subgroups.forEach { it.cultureCenter.aspectCenter.addAspect(aspectPool.getValue(aspectName), it) }
            }

        groups.forEach { it.finishUpdate() }
    }

    private val tileForGroup: Tile
        get() {
            while (true) {
                val tile = map.randomTile()
                if (tile.tagPool.getByType(GROUP_TAG_TYPE).isEmpty()
                        && tile.type != Tile.Type.Water && tile.type != Tile.Type.Mountain) {
                    return tile
                }
            }
        }

    fun addGroupConglomerate(groupConglomerate: GroupConglomerate) {
        groups.add(groupConglomerate)
    }

    fun clearDeadConglomerates() {
        groups.removeIf { it.state == GroupConglomerate.State.Dead }
    }
}
