package shmp.visualizer

import shmp.simulation.culture.group.ClaimTileEvent
import shmp.simulation.culture.group.centers.Group
import shmp.simulation.event.EventLog
import shmp.simulation.space.tile.Tile


fun EventLog.lastClaimedTiles(from: Int): Map<Group, Set<Tile>> = this.lastEvents
            .takeLastWhile { e -> e.turn?.let { it >= from } ?: false }
            .filterIsInstance<ClaimTileEvent>()
            .map { it.group to it.tile }
            .groupBy { (group) -> group }
            .map { (group, pairs) -> group to pairs.map { (_, tile) -> tile }.toSet() }
            .toMap()
