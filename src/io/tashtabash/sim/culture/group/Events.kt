package io.tashtabash.sim.culture.group

import io.tashtabash.sim.culture.group.centers.Group
import io.tashtabash.sim.culture.group.place.StaticPlace
import io.tashtabash.sim.culture.group.process.action.pseudo.ConflictWinner
import io.tashtabash.sim.event.Event
import io.tashtabash.sim.event.Type
import io.tashtabash.sim.space.tile.Tile


class ClaimTileEvent(
        description: String,
        val group: Group,
        val tile: Tile
): Event(Type.TileAcquisition, description)

class HelpEvent(
        description: String,
        val helpValue: Double
): Event(Type.Cooperation, description)

class ConflictResultEvent(
        description: String,
        val status: ConflictWinner
): Event(Type.Conflict, description)

class RoadCreationEvent(
        description: String,
        val place: StaticPlace
): Event(Type.Creation, description)
