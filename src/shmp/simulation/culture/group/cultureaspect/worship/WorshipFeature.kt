package shmp.simulation.culture.group.cultureaspect.worship

import shmp.simulation.culture.group.centers.Group


interface WorshipFeature: WorshipObjectDependent {
    fun use(group: Group, parent: Worship)
    fun adopt(group: Group): WorshipFeature?
    fun die(group: Group, parent: Worship)
    override fun swapWorship(worshipObject: WorshipObject) : WorshipFeature
}
