package simulation.culture.group

import extra.SpaceProbabilityFuncs
import shmp.random.testProbability
import simulation.Controller
import simulation.Event
import simulation.space.Territory
import simulation.space.tile.Tile
import simulation.space.tile.getDistance
import java.util.*
import java.util.function.Consumer
import java.util.function.Predicate

class TerritoryCenter(group: Group, val spreadAbility: Double, tile: Tile?) {
    val territory = Territory()

    private val tileTag: GroupTileTag = GroupTileTag(group)

    val tilePotentialMapper = { t: Tile ->
        t.getNeighbours { it.tagPool.contains(tileTag) }.size
                + 3 * t.resourcePack.getAmount {
            tileTag.group.cultureCenter.aspectCenter.aspectPool.getResourceRequirements().contains(it)
        }
        + tileTag.group.relationCenter.evaluateTile(t)
    }

    init {
        claimTile(tile)
    }

    fun getAllNearGroups(exception: Group) = territory.outerBrink
            .flatMap { it.tagPool.getByType(tileTag.type) }
            .map { (it as GroupTileTag).group }
            .filter { it != exception }
            .toSet()

    val disbandTile: Tile
        get() = SpaceProbabilityFuncs.randomTile(territory)

    fun migrate(): Boolean {
        val newCenter = migrationTile ?: return false
        territory.center = newCenter
        claimTile(newCenter) //TODO move claim and leave here
        leaveTiles(territory.getTiles { !isTileReachable(it) })
        return true
    }

    private val migrationTile: Tile?
        get() = migrationTiles.maxBy { tilePotentialMapper(it) }

    private val migrationTiles: Collection<Tile>
        get() {
            val tiles: MutableSet<Tile> = HashSet()
            val queue: MutableSet<Tile> = HashSet()
            queue.add(territory.center)
            while (true) {
                tiles.addAll(queue)
                val currentTiles = queue
                        .flatMap { it.neighbours }
                        .distinct()
                        .filter { !tiles.contains(it) }
                        .filter { isTileReachable(it) }
                        .filter { canTraverse(it) }
                if (currentTiles.isEmpty()) {
                    break
                }
                queue.clear()
                queue.addAll(currentTiles)
            }
            return tiles.filter { canSettleAndNoGroup(it) }
        }

    private fun canTraverse(tile: Tile): Boolean {
        if (tile.type == Tile.Type.Water) {
            return false
        } else if (tile.type == Tile.Type.Mountain) {
            if (!tileTag.group.cultureCenter.aspectCenter.aspectPool.contains("MountainLiving")) {
                return false
            }
        }
        return true
    }

    fun expand(): Boolean {
        if (!testProbability(spreadAbility, Controller.session.random)) {
            return false
        }
        claimTile(territory.getMostUsefulTileOnOuterBrink(
                { canSettleAndNoGroup(it) && isTileReachable(it) },
                tilePotentialMapper
        ))
        return true
    }

    fun shrink() {
        if (territory.size() <= 1) {
            return
        }
        leaveTile(territory.getMostUselessTile(tilePotentialMapper))
    }

    private fun isTileReachable(tile: Tile) = getDistance(tile, territory.center) < 4

    fun claimTile(tile: Tile?) {
        if (tile == null) {
            return
        }
        if (!tile.tagPool.contains(tileTag) && tile.tagPool.getByType(tileTag.type).isNotEmpty()) {
            throw RuntimeException()
        }
        tileTag.group.parentGroup.claimTile(tile)
        tile.tagPool.add(tileTag)
        territory.add(tile)
        tileTag.group.addEvent(Event(
                Event.Type.TileAcquisition,
                "Group " + tileTag.group.name + " claimed tile " + tile.x + " " + tile.y,
                "group", this, "tile", tile
        ))
    }

    fun die() {
        for (tile in territory.tiles) {
            tile.tagPool.remove(tileTag)
        }
    }

    private fun leaveTile(tile: Tile?) {
        if (tile == null) {
            return
        }
        tile.tagPool.remove(tileTag)
        territory.removeTile(tile)
        tileTag.group.parentGroup.removeTile(tile)
    }

    private fun leaveTiles(tiles: Collection<Tile>) {
        tiles.forEach(Consumer { tile: Tile? -> leaveTile(tile) })
    }

    fun canSettle(tile: Tile) = (tile.type != Tile.Type.Water && tile.type != Tile.Type.Mountain
            || (tile.type == Tile.Type.Mountain
            && tileTag.group.cultureCenter.aspectCenter.aspectPool.contains("mountainLiving")))//TODO set of accessible tiles

    fun canSettleAndNoGroup(tile: Tile) =
            canSettle(tile, Predicate { t: Tile -> t.tagPool.getByType(tileTag.type).isEmpty() })

    fun canSettle(tile: Tile, additionalCondition: Predicate<Tile>) =
            canSettle(tile) && additionalCondition.test(tile)
}