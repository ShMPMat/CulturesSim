package io.tashtabash.sim.culture.group.process.behaviour

import io.tashtabash.sim.culture.group.centers.Group


abstract class PlanBehaviour : AbstractGroupBehaviour() {
    var isFinished = false
        protected set

    override fun update(group: Group) =
            if (isFinished) null
            else this
}
