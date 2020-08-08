package simulation.culture.group.process.behaviour

import simulation.event.Event
import simulation.culture.group.centers.Group
import simulation.culture.group.process.action.GroupAction
import simulation.event.Type


class DelayedB(val action: GroupAction, val delay: Int) : PlanBehaviour() {
    private var timePassed = 0

    override fun run(group: Group): List<Event> {
        if (isFinished)
            return emptyList()

        val events = mutableListOf<Event>()
        if (timePassed == 0)
            events.add(Event(Type.GroupInteraction, "${group.name} started $action"))

        timePassed++

        if (timePassed < delay)
            return events

        action.run()
        isFinished = true

        return events + listOf(Event(Type.GroupInteraction, "${group.name} ended $action after $timePassed"))
    }

    override val internalToString: String
        get() = "Delayed action: $action, $timePassed out of $delay"
}
