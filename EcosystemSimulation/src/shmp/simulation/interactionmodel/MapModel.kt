package shmp.simulation.interactionmodel

import shmp.simulation.World
import shmp.simulation.event.EventLog


//Model with a 2d map on which all interactions take place.
class MapModel : InteractionModel<World> {
    override val eventLog = EventLog(isOblivious = false)

    override fun turn(world: World) {
        world.map.update()
        world.map.finishUpdate()

        eventLog.joinNewEvents(world.events)
        eventLog.clearNewEvents()

        world.incrementTurn()
    }

    override fun geologicTurn(world: World) {
        world.map.geologicUpdate()

        eventLog.joinNewEvents(world.events)

        world.incrementTurnGeology()
    }
}
