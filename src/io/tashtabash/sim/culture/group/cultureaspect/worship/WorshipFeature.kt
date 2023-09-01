package io.tashtabash.sim.culture.group.cultureaspect.worship

import io.tashtabash.sim.culture.group.centers.Group


interface WorshipFeature: WorshipObjectDependent {
    fun use(group: Group, parent: Worship)
    fun adopt(group: Group): WorshipFeature?
    fun die(group: Group, parent: Worship)
    override fun swapWorship(worshipObject: WorshipObject) : WorshipFeature

    val isFunctioning: Boolean
    val defunctTurns: Int
}


abstract class BaseWorshipFeature: WorshipFeature {
    override var isFunctioning = false
        protected set

    override var defunctTurns = 0
        protected set

    override fun use(group: Group, parent: Worship) {
        if (isFunctioning)
            defunctTurns = 0
        else defunctTurns++
    }
}
