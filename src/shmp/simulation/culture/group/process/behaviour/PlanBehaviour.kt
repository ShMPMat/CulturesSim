package shmp.simulation.culture.group.process.behaviour

import shmp.simulation.culture.group.centers.Group


abstract class PlanBehaviour : AbstractGroupBehaviour() {
    var isFinished = false
        protected set

    override fun update(group: Group) =
            if (isFinished) null
            else this
}
