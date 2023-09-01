package io.tashtabash.sim.interactionmodel

import io.tashtabash.sim.World
import io.tashtabash.sim.event.EventLog


/**
 * Represents general model by which World changes.
 */
interface InteractionModel<in E : World> {
    fun turn(world: E)
    fun geologicTurn(world: E)
    val eventLog: EventLog
}
