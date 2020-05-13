package simulation.culture.group.centers

import shmp.random.randomTile
import shmp.random.testProbability
import simulation.Controller.*
import simulation.Event
import simulation.culture.group.*
import simulation.space.Territory
import simulation.space.tile.Tile
import simulation.space.tile.TileTag
import simulation.space.tile.getDistance

class TerritoryCenter(group: Group, val spreadAbility: Double, tile: Tile) {
    val settled: Boolean
     get() = notMoved >= 50

    val territory = Territory()
    var notMoved = 0
        private set

    private val tileTag: GroupTileTag = GroupTileTag(group)
    private val places = mutableListOf<Place>()

    private var _oldCenter: Tile? = null
    private var _oldReach: Collection<Tile> = listOf()
    private var _oldTileTypes: Collection<Tile.Type> = listOf()

    fun tilePotentialMapper(tile: Tile): Int {
        val neededResourcePart = getReachableTilesFrom(tile).sumBy { evaluateOneTile(it) }
        val relationPart = tileTag.group.relationCenter.evaluateTile(tile)
        return neededResourcePart + relationPart
    }

    private fun evaluateOneTile(tile: Tile) = 3 * tile.resourcePack.getAmount {
        tileTag.group.cultureCenter.aspectCenter.aspectPool.getResourceRequirements().contains(it)
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

    fun update() {//TODO change Water on GoodTiles
        leaveTiles(territory.getTiles { !canSettle(it) })
        if (territory.isEmpty) {
            tileTag.group.die()
            return
        }
        if (!territory.contains(territory.center))
            territory.center = randomTile(territory, session.random)
        notMoved++
        if (settled && territory.center.tagPool.getByType(SETTLE_TAG).isEmpty()) {
            places.add(Place(
                    territory.center,
                    TileTag(SETTLE_TAG + places.count { it.tileTag.type == SETTLE_TAG }, SETTLE_TAG)
            ))
        }
    }

    fun migrate(): Boolean {
        val newCenter = migrationTile ?: return false
        territory.center = newCenter
        claimTile(newCenter)
        leaveTiles(territory.getTiles { !isTileReachable(it) })
        return true
    }

    private val migrationTile: Tile?
        get() = reachableTiles
                .filter { canSettleAndNoGroupExcept(it) }
                .maxBy { tilePotentialMapper(it) }

    private val reachableTiles: Collection<Tile>
        get() {
            val tileTypes = getAccessibleTileTypes()
            if (_oldCenter != territory.center || tileTypes != _oldTileTypes) {
                _oldCenter = territory.center
                _oldReach = getReachableTilesFrom(territory.center)
                _oldTileTypes = tileTypes
            }
            return _oldReach
        }

    private fun getAccessibleTileTypes(): List<Tile.Type> {
        return if (tileTag.group.resourceCenter.pack.any { it.simpleName == "Boat" })
            listOf(Tile.Type.Water)
        else listOf()
    }

    private fun getReachableTilesFrom(tile: Tile): Collection<Tile> {
        val tiles = mutableSetOf<Tile>()
        val queue = mutableListOf<Pair<Tile, Int>>()
        queue.add(tile to 0)
        var currentLayer = 1
        while (true) {
            tiles.addAll(queue.map { it.first })
            val currentTiles = queue
                    .filter { isTileReachableInTraverse(it) }
                    .flatMap { it.first.neighbours }.asSequence()
                    .distinct()
                    .filter { !tiles.contains(it) }
                    .map { it to currentLayer }.toList()
            if (currentTiles.isEmpty())
                break
            queue.clear()
            queue.addAll(currentTiles)
            currentLayer++
        }
        return tiles
    }

    private fun isTileReachableInTraverse(pair: Pair<Tile, Int>) = when (pair.first.type) {
        Tile.Type.Water ->
            if (_oldTileTypes.contains(Tile.Type.Water)) pair.second <= session.defaultGroupReach * 6
            else false
        Tile.Type.Mountain ->
            if (tileTag.group.cultureCenter.aspectCenter.aspectPool.contains("MountainLiving")) pair.second <=
                    session.defaultGroupReach
            else false
        else -> pair.second <= session.defaultGroupReach
    }

    fun expand(): Boolean {
        if (!testProbability(spreadAbility, session.random))
            return false
        claimTile(territory.getMostUsefulTileOnOuterBrink(
                { canSettleAndNoGroup(it) && isTileReachable(it) },
                this::tilePotentialMapper
        ))
        return true
    }

    fun shrink() {
        if (territory.size() <= 1)
            return
        leaveTile(territory.getMostUselessTile(this::tilePotentialMapper))
    }

    private fun isTileReachable(tile: Tile) = getDistance(tile, territory.center) < session.defaultGroupTerritoryRadius

    fun claimTile(tile: Tile?) {
        if (tile == null) return
        if (!tile.tagPool.contains(tileTag) && hasResidingGroup(tile))
            throw RuntimeException()
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
        territory.remove(tile)
        tileTag.group.parentGroup.removeTile(tile)
    }

    private fun leaveTiles(tiles: Collection<Tile>) {
        tiles.forEach { leaveTile(it) }
    }

    fun canSettle(tile: Tile) = (tile.type != Tile.Type.Water && tile.type != Tile.Type.Mountain
            || (tile.type == Tile.Type.Mountain//TODO set of accessible tile types
            && tileTag.group.cultureCenter.aspectCenter.aspectPool.contains("mountainLiving")))

    fun canSettleAndNoGroup(tile: Tile) = canSettle(tile) { hasNoResidingGroup(it) }

    fun canSettleAndNoGroupExcept(tile: Tile) = canSettle(tile) { hasNoResidingGroupExcept(it, tileTag.group) }

    fun canSettle(tile: Tile, additionalCondition: (Tile) -> Boolean) = canSettle(tile) && additionalCondition(tile)
}

const val SETTLE_TAG = "Settlement"