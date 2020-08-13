package simulation.culture.group.process.behaviour

import simulation.event.Event
import simulation.culture.group.centers.Group
import simulation.culture.group.process.ProcessResult
import simulation.culture.group.process.action.GroupAction
import simulation.culture.group.process.emptyProcessResult
import simulation.event.Type


class DelayedB(val action: GroupAction, val delay: Int) : PlanBehaviour() {
    private var timePassed = 0

    override fun run(group: Group): ProcessResult {
        if (isFinished)
            return emptyProcessResult

        var processResult = emptyProcessResult
        if (timePassed == 0)
            processResult += ProcessResult(Event(Type.GroupInteraction, "${group.name} started $action"))

        timePassed++

        if (timePassed < delay)
            return emptyProcessResult

        action.run()
        isFinished = true

        return processResult +
                ProcessResult(Event(Type.GroupInteraction, "${group.name} ended $action after $timePassed"))
    }

    override val internalToString: String
        get() = "Delayed action: $action, $timePassed out of $delay"
}
