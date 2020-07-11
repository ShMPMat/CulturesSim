package simulation.culture.group.process.action

import simulation.culture.group.centers.Group
import simulation.culture.group.process.behaviour.DelayedB


class ScheduleActionA(group: Group, private val action: GroupAction, val delay: Int) : AbstractGroupAction(group) {
    override fun run() {
        group.processCenter.addBehaviour(DelayedB(action, delay))
    }

    override val internalToString = "Schedule Action $action to the ${group.name} in $delay turns"
}
