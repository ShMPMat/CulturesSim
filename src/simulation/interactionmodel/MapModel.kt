package simulation.interactionmodel

import simulation.Controller
import simulation.World
import simulation.event.EventLog


//Model with a 2d map on which all interactions take place.
class MapModel : InteractionModel {
    private val eventLog = EventLog()

    override val newEvents
        get() = eventLog.newEvents
    override val allEvents
        get() = eventLog.allEvents

    override fun turn(world: World) {
        Controller.session.overallTime = System.nanoTime()
        Controller.session.groupTime = System.nanoTime()
        Controller.session.groupMainTime = 0
        Controller.session.groupOthersTime = 0
        Controller.session.groupInnerOtherTime = 0
        Controller.session.groupMigrationTime = 0

        world.map.update()
        var groups = world.shuffledGroups
        for (group in groups) {
            group.update()
        }
        Controller.session.groupTime = System.nanoTime() - Controller.session.groupTime
        Controller.session.othersTime = System.nanoTime()

        groups = world.shuffledGroups
        for (group in groups)
            group.finishUpdate()
        world.map.finishUpdate()
        world.strayPlacesManager.update()

        eventLog.joinNewEvents(world.events)
        groups.forEach {
            eventLog.joinNewEvents(it.events)
        }

        Controller.session.othersTime = System.nanoTime() - Controller.session.othersTime
        Controller.session.overallTime = System.nanoTime() - Controller.session.overallTime

        val j = allEvents.groupBy { it }.filter { it.value.size > 1 }
        val i = newEvents.groupBy { it }.filter { it.value.size > 1 }
        if (j.isNotEmpty()) {
            val k = 0
        }
    }

    override fun geologicTurn(world: World) {
        world.map.geologicUpdate()

        eventLog.joinNewEvents(world.events)
    }
}
