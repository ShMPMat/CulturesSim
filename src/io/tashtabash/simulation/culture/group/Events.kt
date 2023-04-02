package io.tashtabash.simulation.culture.group

import io.tashtabash.simulation.culture.group.centers.Group
import io.tashtabash.simulation.culture.group.place.StaticPlace
import io.tashtabash.simulation.culture.group.process.action.pseudo.ConflictWinner
import io.tashtabash.simulation.event.Event
import io.tashtabash.simulation.event.Type
import io.tashtabash.simulation.space.tile.Tile


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
