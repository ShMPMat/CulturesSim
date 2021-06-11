package shmp.simulation.culture.group.centers

import shmp.random.singleton.chanceOfNot
import shmp.random.singleton.randomTile
import shmp.simulation.CulturesController.session
import shmp.simulation.SimulationError
import shmp.simulation.culture.group.*
import shmp.simulation.culture.group.place.StaticPlace
import shmp.simulation.space.territory.BrinkInvariantTerritory
import shmp.simulation.space.territory.MutableTerritory
import shmp.simulation.space.territory.StaticTerritory
import shmp.simulation.space.territory.Territory
import shmp.simulation.space.tile.Tile
import shmp.simulation.space.tile.TileTag
import shmp.simulation.space.tile.getDistance
import java.util.*
import kotlin.math.abs


class TerritoryCenter(group: Group, val spreadAbility: Double, tile: Tile) {
    val reachDistance = session.defaultGroupReach

    val settled: Boolean
        get() = notMoved >= 50

    val territory: MutableTerritory = BrinkInvariantTerritory()
    val center: Tile
        get() = territory.center
                ?: throw GroupError("Empty Group Territory")

    var notMoved = 0
        private set

    private val tileTag: GroupTileTag = GroupTileTag(group)

    private val _places = mutableListOf<StaticPlace>()
    val places: List<StaticPlace>
        get() = _places

    private var _oldCenter: Tile? = null
    private var _oldReach: Collection<Tile> = listOf()
    private var _oldTileTypes: Collection<Tile.Type> = listOf()

    fun tilePotentialMapper(tile: Tile): Int {
        val neededResourcePart = getReachableTilesFrom(tile).sumBy { evaluateOneTile(it) }
        val relationPart = tileTag.group.relationCenter.evaluateTile(tile)
        return neededResourcePart + relationPart
    }

    fun territoryPotentialMapper(territory: Territory) = territory.tiles.sumBy { tilePotentialMapper(it) }

    private fun evaluateOneTile(tile: Tile): Int {
        tileEvaluationHash[tile to session.world.getTurn()]?.let {
            return it
        }

        val requirements = tileTag.group.cultureCenter.aspectCenter.aspectPool.getResourceRequirements()
        val sum = requirements.sumBy { tile.resourcePack.getAmount(it) }

        tileEvaluationHash[tile to session.world.getTurn()] = 3 * sum

        return 3 * sum
    }

    internal val tileEvaluationHash = mutableMapOf<Pair<Tile, Int>, Int>()

    init {
        claimTile(tile)
    }

    val accessibleTerritory: Territory
        get() = StaticTerritory(reachableTiles)

    fun getAllNearGroups(exception: Group) = territory.outerBrink
            .mapNotNull { getResidingGroup(it) }
            .filter { it != exception }
            .toSet()

    fun update() {//TODO change Water on GoodTiles
        leaveTiles(territory.filter { !canSettle(it) })
        if (territory.isEmpty) {
            tileTag.group.die()
            return
        }
        if (!territory.contains(territory.center)) {
            territory.center = territory.randomTile()
            territory.add(center)
        }
        notMoved++
        if (settled && center.tagPool.getByType(SETTLE_TAG).isEmpty()) {
            _places.add(StaticPlace(
                    center,
                    TileTag(SETTLE_TAG + _places.count { it.tileTag.type == SETTLE_TAG }, SETTLE_TAG)
            ))
        }
    }

    fun migrate(): Boolean {
        val newCenter = migrationTile
                ?: return false

        val population = tileTag.group.populationCenter.actualPopulation
        territory.center?.removeResource(population)
        territory.center?.addDelayedResource(population)

        territory.center = newCenter
        claimTile(newCenter)
        leaveTiles(territory.filter { !isTileReachable(it) })
        return true
    }

    private val migrationTile: Tile?
        get() = reachableTiles
                .filter { canSettleAndNoGroupExcept(it) }
                .maxByOrNull { tilePotentialMapper(it) }

    private val reachableTiles: Collection<Tile>
        get() {
            val tileTypes = getAccessibleTileTypes()
            if (_oldCenter != territory.center || tileTypes != _oldTileTypes) {
                _oldCenter = territory.center
                _oldReach = getReachableTilesFrom(center)
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
            if (_oldTileTypes.contains(Tile.Type.Water))
                pair.second <= reachDistance * 6
            else false
        Tile.Type.Mountain ->
            if (tileTag.group.cultureCenter.aspectCenter.aspectPool.contains("MountainLiving"))
                pair.second <= reachDistance
            else false
        else -> pair.second <= reachDistance
    }

    fun expand(): Boolean {
        spreadAbility.chanceOfNot {
            return false
        }

        claimTile(territory.getMostUsefulTileOnOuterBrink(
                { canSettleAndNoGroup(it) && isTileReachable(it) },
                this::tilePotentialMapper
        ))
        return true
    }

    fun shrink() {
        if (territory.size <= 1)
            return
        leaveTile(territory.getMostUselessTile(this::tilePotentialMapper))
    }

    private fun isTileReachable(tile: Tile) = getDistance(tile, center) < session.defaultGroupTerritoryRadius

    fun claimTile(tile: Tile?) {
        if (tile == null) return
        if (!tile.tagPool.contains(tileTag) && hasResidingGroup(tile))
            throw RuntimeException()

        tileTag.group.parentGroup.claimTile(tile)
        tile.tagPool.add(tileTag)
        territory.add(tile)
        tileTag.group.addEvent(ClaimTileEvent(
                "Group ${tileTag.group.name} claimed tile ${tile.posStr}",
                tileTag.group,
                tile
        ))
    }

    fun die() {
        for (tile in territory.tiles)
            tile.tagPool.remove(tileTag)
    }

    private fun leaveTile(tile: Tile?) {
        if (tile == null)
            return

        tile.tagPool.remove(tileTag)
        territory.remove(tile)
        tileTag.group.parentGroup.removeTile(tile)
    }

    private fun leaveTiles(tiles: Collection<Tile>) = tiles.forEach { leaveTile(it) }

    fun canSettle(tile: Tile) = (tile.type != Tile.Type.Water && tile.type != Tile.Type.Mountain
            || (tile.type == Tile.Type.Mountain//TODO set of accessible tile types
            && tileTag.group.cultureCenter.aspectCenter.aspectPool.contains("mountainLiving")))

    fun canSettleAndNoGroup(tile: Tile) = canSettle(tile) { hasNoResidingGroup(it) }

    fun canSettleAndNoGroupExcept(tile: Tile) = canSettle(tile) { hasNoResidingGroupExcept(it, tileTag.group) }

    fun canSettle(tile: Tile, additionalCondition: (Tile) -> Boolean) = canSettle(tile) && additionalCondition(tile)

    fun makePath(start: Tile, finish: Tile): List<Tile>? {
        return makePathAStar(start, finish)
    }

    private fun makePathAStar(start: Tile, finish: Tile): List<Tile>? {
        val h = {t: Tile -> abs(t.x - finish.x) + abs(t.y - finish.y) }
        val g = mutableMapOf<Tile, Int>()
        val f = mutableMapOf<Tile, Int>()
        val prev = mutableMapOf<Tile, Tile>()
        val q = PriorityQueue<Tile>(Comparator.comparingInt { t -> f.getValue(t) })
        val u = mutableSetOf<Tile>()

        g[start] = 0
        g[start] = h(start)
        q.add(start)

        var turns = 0
        while (q.isNotEmpty() && turns < 50) {
            turns++

            val cur = q.remove()
            if (cur == finish)
                return unwind(start, finish, prev)

            u.add(cur)

            val neighbours = cur.neighbours.filter { isTileReachableInTraverse(it to 0) }
            for (v in neighbours) {
                val distance = g.getValue(cur) + abs(cur.level - v.level) + 1

                if (v in u && distance >= g.getValue(v))
                    continue

                prev[v] = cur
                g[v] = distance
                f[v] = distance + h(v)
                q.add(v)
            }
        }

        return null
    }

    private fun unwind(start: Tile, cur: Tile, map: Map<Tile, Tile>): List<Tile> =
            (if (cur == start) emptyList() else unwind(start, map.getValue(cur), map)) + listOf(cur)
}

const val SETTLE_TAG = "Settlement"

data class TileAndPrev(val tile: Tile, val prev: TileAndPrev?, val length: Int = 0) {
    fun unwind(): List<Tile> = listOf(this.tile) + if (this.prev == null) emptyList() else this.prev.unwind()
}
