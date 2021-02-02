package shmp.simulation.interactionmodel

import shmp.simulation.World
import shmp.simulation.event.EventLog

/**
 * Represents general model by which World changes.
 */
interface InteractionModel {
    fun turn(world: World)
    fun geologicTurn(world: World)
    val eventLog: EventLog
}