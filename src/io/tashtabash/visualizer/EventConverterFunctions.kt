package io.tashtabash.visualizer

import io.tashtabash.sim.culture.group.ClaimTileEvent
import io.tashtabash.sim.culture.group.centers.Group
import io.tashtabash.sim.event.EventLog
import io.tashtabash.sim.space.tile.Tile


fun EventLog.lastClaimedTiles(from: Int): Map<Group, Set<Tile>> = this.lastEvents
            .takeLastWhile { e -> e.turn?.let { it >= from } ?: false }
            .filterIsInstance<ClaimTileEvent>()
            .map { it.group to it.tile }
            .groupBy { (group) -> group }
            .map { (group, pairs) -> group to pairs.map { (_, tile) -> tile }.toSet() }
            .toMap()
