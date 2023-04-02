package io.tashtabash.simulation.interactionmodel

import io.tashtabash.simulation.World
import io.tashtabash.simulation.event.EventLog


/**
 * Represents general model by which World changes.
 */
interface InteractionModel<in E : World> {
    fun turn(world: E)
    fun geologicTurn(world: E)
    val eventLog: EventLog
}
