package io.tashtabash.sim.interactionmodel

import io.tashtabash.sim.CulturesWorld
import io.tashtabash.sim.event.EventLog


//Model with a 2d map on which all interactions take place.
class CulturesMapModel : InteractionModel<CulturesWorld> {
    override val eventLog = EventLog(isOblivious = false)

    var groupMainTime: Long = 0
    var groupOthersTime: Long = 0
    var groupMigrationTime: Long = 0
    var groupInnerOtherTime: Long = 0

    override fun turn(world: CulturesWorld) {
        var overallTime = System.nanoTime()
        var groupTime = System.nanoTime()
        groupMainTime = 0
        groupOthersTime = 0
        groupInnerOtherTime = 0
        groupMigrationTime = 0

        world.map.update()
        var groups = world.shuffledConglomerates
        for (group in groups)
            group.update()

        groupTime = System.nanoTime() - groupTime
        var othersTime = System.nanoTime()

        groups = world.shuffledConglomerates
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

        val nsToS = 1000_000_000.0
        println(
            buildString {
                append("Overall - ${"%.3f".format(overallTime / nsToS)}s ")
                append("Groups - ${"%.3f".format(groupTime / nsToS)}s ")
                append("Others - ${"%.3f".format(othersTime / nsToS)}s ")
                append("Groups to others - ${"%.3f".format(groupTime.toDouble() / othersTime)} ")
                append("main update to others - ${"%.3f".format(groupMainTime.toDouble() / groupOthersTime)} ")
                append("test to others - ${"%.3f".format(groupMigrationTime.toDouble() / groupInnerOtherTime)}")
            }
        )
    }

    override fun geologicTurn(world: CulturesWorld) {
        world.map.geologicUpdate()

        eventLog.joinNewEvents(world.events)

        world.incrementTurnGeology()
    }
}
