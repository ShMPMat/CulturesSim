package shmp.simulation.culture.group

import shmp.simulation.culture.group.centers.Group
import shmp.simulation.culture.group.place.StaticPlace
import shmp.simulation.culture.group.process.action.pseudo.ConflictWinner
import shmp.simulation.event.Event
import shmp.simulation.event.Type
import shmp.simulation.space.tile.Tile


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
