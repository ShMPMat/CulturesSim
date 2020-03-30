package simulation.culture.group

import extra.SpaceProbabilityFuncs
import shmp.random.testProbability
import simulation.Controller
import simulation.Event
import simulation.space.Territory
import simulation.space.tile.Tile
import simulation.space.tile.getDistance
import java.util.function.Consumer
import java.util.function.Predicate

class TerritoryCenter(group: Group, val spreadAbility: Double, tile: Tile) {
    val territory = Territory()

    private val tileTag: GroupTileTag = GroupTileTag(group)

    private var _oldCenter: Tile? = null
    private var _oldReach: Collection<Tile> = listOf()
    private var _oldTileTypes: Collection<Tile.Type> = listOf()

    fun tilePotentialMapper(tile: Tile): Int {
        val convexPart = tile.getNeighbours { it.tagPool.contains(tileTag) }.size
        val neededResourcePart = 3 * tile.resourcePack.getAmount {
            tileTag.group.cultureCenter.aspectCenter.aspectPool.getResourceRequirements().contains(it)
        }
        val relationPart = tileTag.group.relationCenter.evaluateTile(tile)
        return convexPart + neededResourcePart + relationPart
    }

    init {
        claimTile(tile)
    }

    val accessibleTerritory: Territory
        get() = Territory(reachableTiles)

    fun getAllNearGroups(exception: Group) = territory.outerBrink
            .mapNotNull { getResidingGroup(it) }
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
        get() = reachableTiles.maxBy { tilePotentialMapper(it) }

    private val reachableTiles: Collection<Tile>
        get() {
            val tileTypes = getAccessibleTileTypes()
            if (_oldCenter != territory.center || tileTypes != _oldTileTypes) {
                _oldCenter = territory.center
                _oldReach = getReachableTiles(territory.center)
                _oldTileTypes = tileTypes
            }
            return _oldReach.filter { canSettleAndNoGroupExcept(it) }
        }

    private fun getAccessibleTileTypes(): List<Tile.Type> {
        return if (tileTag.group.cherishedResources.resources.any { it.simpleName == "Boat"})
            listOf(Tile.Type.Water)
        else listOf()
    }

    private fun getReachableTiles(tile: Tile): Collection<Tile> {
        val tiles = mutableSetOf<Tile>()
        val queue = mutableSetOf<Pair<Tile, Int>>()
        queue.add(tile to 0)
        var currentLayer = 1
        while (true) {
            tiles.addAll(queue.map { it.first })
            val currentTiles = queue
                    .filter { isTileReachableInTraverse(it) }//TODO another sequence
                    .flatMap { it.first.neighbours }
                    .asSequence()
                    .distinct()
                    .filter { !tiles.contains(it) }
                    .filter { canTraverse(it) }
                    .map { it to currentLayer }
                    .toList()
            if (currentTiles.isEmpty()) {
                break
            }
            queue.clear()
            queue.addAll(currentTiles)
            currentLayer++
        }
        return tiles
    }

    private fun isTileReachableInTraverse(pair: Pair<Tile, Int>) = when (pair.first.type) {
        Tile.Type.Water -> if (_oldTileTypes.contains(Tile.Type.Water))
            pair.second <= 30
        else false
        else -> pair.second <= 5
    }

    private fun canTraverse(tile: Tile): Boolean {
        if (tile.type == Tile.Type.Water) {
            return _oldTileTypes.contains(Tile.Type.Water)
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
                this::tilePotentialMapper
        ))
        return true
    }

    fun shrink() {
        if (territory.size() <= 1) {
            return
        }
        leaveTile(territory.getMostUselessTile(this::tilePotentialMapper))
    }

    private fun isTileReachable(tile: Tile) = getDistance(tile, territory.center) < 4

    fun claimTile(tile: Tile?) {
        if (tile == null) {
            return
        }
        if (!tile.tagPool.contains(tileTag) && hasResidingGroup(tile)) {
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
            || (tile.type == Tile.Type.Mountain//TODO set of accessible tile types
            && tileTag.group.cultureCenter.aspectCenter.aspectPool.contains("mountainLiving")))

    fun canSettleAndNoGroup(tile: Tile) =
            canSettle(tile, Predicate { hasNoResidingGroup(it) })


    fun canSettleAndNoGroupExcept(tile: Tile) =
            canSettle(tile, Predicate { hasNoResidingGroupExcept(it, tileTag.group) })

    fun canSettle(tile: Tile, additionalCondition: Predicate<Tile>) =
            canSettle(tile) && additionalCondition.test(tile)
}