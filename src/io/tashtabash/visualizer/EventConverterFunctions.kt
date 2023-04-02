package io.tashtabash.visualizer

import io.tashtabash.simulation.culture.group.ClaimTileEvent
import io.tashtabash.simulation.culture.group.centers.Group
import io.tashtabash.simulation.event.EventLog
import io.tashtabash.simulation.space.tile.Tile


fun EventLog.lastClaimedTiles(from: Int): Map<Group, Set<Tile>> = this.lastEvents
            .takeLastWhile { e -> e.turn?.let { it >= from } ?: false }
            .filterIsInstance<ClaimTileEvent>()
            .map { it.group to it.tile }
            .groupBy { (group) -> group }
            .map { (group, pairs) -> group to pairs.map { (_, tile) -> tile }.toSet() }
            .toMap()
