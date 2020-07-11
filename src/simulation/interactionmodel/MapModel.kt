package simulation.interactionmodel

import simulation.Controller
import simulation.Event
import simulation.World

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

        val worldEventsNumber = world.events.size
        val groupEventSize = world.groups
                .map { it to it.events.size }
                .toMap()

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

        events.addAll(world.events.drop(worldEventsNumber))
        allEvents.addAll(world.events.drop(worldEventsNumber))
        groups.forEach {
            events.addAll(it.events.drop(groupEventSize[it] ?: 0))
            allEvents.addAll(it.events.drop(groupEventSize[it] ?: 0))
        }

        Controller.session.othersTime = System.nanoTime() - Controller.session.othersTime
        Controller.session.overallTime = System.nanoTime() - Controller.session.overallTime

        val j = allEvents.groupBy { it }.filter { it.value.size > 1 }
        val i = events.groupBy { it }.filter { it.value.size > 1 }
        if (j.isNotEmpty()) {
            val k = 0
        }
        clearEvents()
    }

    override fun geologicTurn(world: World) {
        val worldEventsSize = world.events.size

        world.map.geologicUpdate()

        events.addAll(world.events.drop(worldEventsSize))
    }

    override fun clearEvents() = events.clear()
}
