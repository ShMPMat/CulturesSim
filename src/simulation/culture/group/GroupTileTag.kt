package simulation.culture.group

import simulation.culture.group.centers.Group
import simulation.space.tile.Tile
import simulation.space.tile.TileTag

const val GROUP_TAG_TYPE = "Group"

class GroupTileTag(val group: Group) : TileTag(group.name, GROUP_TAG_TYPE)


fun getResidingGroup(tile: Tile): Group? = (tile.tagPool.getByType(GROUP_TAG_TYPE)
        .firstOrNull() as GroupTileTag?)?.group

fun hasResidingGroup(tile: Tile) = getResidingGroup(tile) != null

fun hasNoResidingGroup(tile: Tile) = !hasResidingGroup(tile)

fun hasNoResidingGroupExcept(tile: Tile, exceptionGroup: Group) =
        !hasResidingGroup(tile) || getResidingGroup(tile) == exceptionGroup