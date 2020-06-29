package simulation.interactionmodel

import simulation.Event
import simulation.World

/**
 * Represents general model by which World changes.
 */
interface InteractionModel {
    fun turn(world: World)
    fun geologicTurn(world: World)
    val events: List<Event>
    val allEvents: List<Event>
    fun clearEvents()
}