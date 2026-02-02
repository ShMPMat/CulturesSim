package io.tashtabash.sim.culture.group.process.behaviour

import io.tashtabash.sim.culture.group.centers.Group
import io.tashtabash.sim.culture.group.process.ProcessResult
import io.tashtabash.sim.culture.group.process.action.GroupAction
import io.tashtabash.sim.culture.group.process.emptyProcessResult
import io.tashtabash.sim.event.IntergroupInteraction
import io.tashtabash.sim.event.of


class DelayedB(val action: GroupAction, val delay: Int) : PlanBehaviour() {
    private var timePassed = 0

    override fun run(group: Group): ProcessResult {
        if (isFinished)
            return emptyProcessResult

        var processResult = emptyProcessResult
        if (timePassed == 0)
            processResult += ProcessResult(IntergroupInteraction of "${group.name} started $action")

        timePassed++

        if (timePassed < delay)
            return emptyProcessResult

        action.run()
        isFinished = true

        return processResult +
                ProcessResult(IntergroupInteraction of "${group.name} ended $action after $timePassed turns")
    }

    override val internalToString: String
        get() = "Delayed action: $action, $timePassed out of $delay"
}
