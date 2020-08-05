package simulation.culture.group

import simulation.culture.group.centers.Group
import simulation.culture.group.place.StaticPlace
import simulation.culture.group.process.action.pseudo.BattlePA
import simulation.culture.group.process.action.pseudo.ConflictWinner
import simulation.event.Event
import simulation.space.tile.Tile


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
