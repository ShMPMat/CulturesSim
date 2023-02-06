package shmp.simulation.interactionmodel

import shmp.simulation.CulturesController
import shmp.simulation.CulturesWorld
import shmp.simulation.event.EventLog


//Model with a 2d map on which all interactions take place.
class CulturesMapModel : InteractionModel<CulturesWorld> {
    override val eventLog = EventLog(isOblivious = false)

    override fun turn(world: CulturesWorld) {
        CulturesController.session.overallTime = System.nanoTime()
        CulturesController.session.groupTime = System.nanoTime()
        CulturesController.session.groupMainTime = 0
        CulturesController.session.groupOthersTime = 0
        CulturesController.session.groupInnerOtherTime = 0
        CulturesController.session.groupMigrationTime = 0

        world.map.update()
        var groups = world.shuffledGroups
        for (group in groups) {
            group.update()
        }

        CulturesController.session.groupTime = System.nanoTime() - CulturesController.session.groupTime
        CulturesController.session.othersTime = System.nanoTime()

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

        CulturesController.session.othersTime = System.nanoTime() - CulturesController.session.othersTime
        CulturesController.session.overallTime = System.nanoTime() - CulturesController.session.overallTime
    }

    override fun geologicTurn(world: CulturesWorld) {
        world.map.geologicUpdate()

        eventLog.joinNewEvents(world.events)

        world.incrementTurnGeology()
    }
}
