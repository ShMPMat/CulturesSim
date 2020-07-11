package simulation.interactionmodel

import simulation.event.Event
import simulation.World
import simulation.event.EventLog

/**
 * Represents general model by which World changes.
 */
interface InteractionModel {
    fun turn(world: World)
    fun geologicTurn(world: World)
    val eventLog: EventLog
}