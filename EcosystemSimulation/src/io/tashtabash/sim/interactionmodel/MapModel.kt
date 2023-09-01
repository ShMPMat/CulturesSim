package io.tashtabash.sim.interactionmodel

import io.tashtabash.sim.World
import io.tashtabash.sim.event.EventLog


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
