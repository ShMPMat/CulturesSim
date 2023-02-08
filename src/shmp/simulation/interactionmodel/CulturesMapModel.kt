package shmp.simulation.interactionmodel

import shmp.simulation.CulturesWorld
import shmp.simulation.event.EventLog


//Model with a 2d map on which all interactions take place.
class CulturesMapModel : InteractionModel<CulturesWorld> {
    override val eventLog = EventLog(isOblivious = false)

    var overallTime: Long = 0
    var groupTime: Long = 0
    var othersTime: Long = 0
    var groupMainTime: Long = 0
    var groupOthersTime: Long = 0
    var groupMigrationTime: Long = 0
    var groupInnerOtherTime: Long = 0

    override fun turn(world: CulturesWorld) {
        overallTime = System.nanoTime()
        groupTime = System.nanoTime()
        groupMainTime = 0
        groupOthersTime = 0
        groupInnerOtherTime = 0
        groupMigrationTime = 0

        world.map.update()
        var groups = world.shuffledGroups
        for (group in groups) {
            group.update()
        }

        groupTime = System.nanoTime() - groupTime
        othersTime = System.nanoTime()

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

        othersTime = System.nanoTime() - othersTime
        overallTime = System.nanoTime() - overallTime

        world.incrementTurn()

        if (world.lesserTurnNumber % 100 == 0)
            world.clearDeadConglomerates()

        println(
                "Overall - $overallTime Groups - $groupTime Others - $othersTime Groups to others - "
                        + groupTime.toDouble() / othersTime.toDouble() +
                        " main update to others - " + groupMainTime.toDouble() / groupOthersTime.toDouble() +
                        " current test to others - " + groupMigrationTime.toDouble() / groupInnerOtherTime.toDouble()
        )
    }

    override fun geologicTurn(world: CulturesWorld) {
        world.map.geologicUpdate()

        eventLog.joinNewEvents(world.events)

        world.incrementTurnGeology()
    }
}
