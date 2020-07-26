package visualizer

import simulation.culture.group.centers.Group
import simulation.event.Event
import simulation.event.EventLog
import simulation.space.tile.Tile

fun EventLog.lastClaimedTiles(from: Int): Map<Group, Set<Tile>> = this.lastEvents
            .takeLastWhile { e -> e.turn?.let { it >= from } ?: false }
            .filter { it.type == Event.Type.TileAcquisition }
            .map { it.getAttribute("group") as Group to it.getAttribute("tile") as Tile }
            .groupBy { (group) -> group }
            .map { (group, pairs) -> group to pairs.map { (_, tile) -> tile }.toSet() }
            .toMap()
