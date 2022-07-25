package shmp.simulation.interactionmodel

import shmp.simulation.World
import shmp.simulation.event.EventLog


/**
 * Represents general model by which World changes.
 */
interface InteractionModel<in E : World> {
    fun turn(world: E)
    fun geologicTurn(world: E)
    val eventLog: EventLog
}
