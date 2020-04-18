package simulation.interactionmodel

import simulation.Event
import simulation.World
import java.util.*

/**
 * Model with 2d map on which all interactions take place.
 */
class MapModel : InteractionModel {
    var events: MutableList<Event> = ArrayList()

    override fun turn(world: World) {
        var groups = world.shuffledGroups
        for (group in groups) {
            val eventsNumber = group.events.size
            group.update()
            events.addAll(group.events.drop(eventsNumber))
        }
        val eventsNumber = world.events.size
        world.map.update()
        groups = world.shuffledGroups
        for (group in groups)
            group.finishUpdate()
        world.map.finishUpdate()
        events.addAll(world.events.drop(eventsNumber))
    }

    override fun geologicTurn(world: World) {
        val a = world.events.size
        world.map.geologicUpdate()
        for (i in a until world.events.size) {
            events.add(world.events[i])
        }
    }

    override fun getEvents(): Collection<Event> = events

    override fun clearEvents() {
        events = ArrayList()
    }
}