package shmp.simulation.interactionmodel

import shmp.simulation.Controller
import shmp.simulation.World
import shmp.simulation.event.EventLog


//Model with a 2d map on which all interactions take place.
class MapModel : InteractionModel {
    override val eventLog = EventLog(isOblivious = false)

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
        eventLog.clearNewEvents()

        Controller.session.othersTime = System.nanoTime() - Controller.session.othersTime
        Controller.session.overallTime = System.nanoTime() - Controller.session.overallTime
    }

    override fun geologicTurn(world: World) {
        world.map.geologicUpdate()

        eventLog.joinNewEvents(world.events)
    }
}
