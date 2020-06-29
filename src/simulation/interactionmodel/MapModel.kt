package simulation.interactionmodel

import simulation.Controller
import simulation.Event
import simulation.World
import java.util.*

/**
 * Model with 2d map on which all interactions take place.
 */
class MapModel : InteractionModel {
    override var events: MutableList<Event> = mutableListOf()
    override val allEvents: MutableList<Event> = mutableListOf()

    override fun turn(world: World) {
        Controller.session.overallTime = System.nanoTime()
        Controller.session.groupTime = System.nanoTime()
        Controller.session.groupMainTime = 0
        Controller.session.groupOthersTime = 0
        Controller.session.groupInnerOtherTime = 0
        Controller.session.groupMigrationTime = 0
        var groups = world.shuffledGroups
        for (group in groups) {
            group.update()
        }
        Controller.session.groupTime = System.nanoTime() - Controller.session.groupTime
        Controller.session.othersTime = System.nanoTime()
        var eventsNumber = world.events.size
        world.map.update()
        groups = world.shuffledGroups
        for (group in groups) {
            eventsNumber = group.events.size
            group.finishUpdate()
            events.addAll(group.events.drop(eventsNumber))
            allEvents.addAll(group.events.drop(eventsNumber))
        }
        world.map.finishUpdate()
        events.addAll(world.events.drop(eventsNumber))
        allEvents.addAll(world.events.drop(eventsNumber))
        world.strayPlacesManager.update()
        Controller.session.othersTime = System.nanoTime() - Controller.session.othersTime
        Controller.session.overallTime = System.nanoTime() - Controller.session.overallTime
    }

    override fun geologicTurn(world: World) {
        val a = world.events.size
        world.map.geologicUpdate()
        for (i in a until world.events.size) {
            events.add(world.events[i])
        }
    }

    override fun clearEvents() {
        events = ArrayList()
    }
}