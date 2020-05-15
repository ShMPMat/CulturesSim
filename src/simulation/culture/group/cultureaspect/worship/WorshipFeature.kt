package simulation.culture.group.cultureaspect.worship

import simulation.culture.group.centers.Group
import simulation.culture.group.request.Request

interface WorshipFeature: WorshipObjectDependent {
    fun use(group: Group, parent: Worship)
    fun adopt(group: Group): WorshipFeature?
    fun die(group: Group, parent: Worship)
    override fun swapWorship(worshipObject: WorshipObject) : WorshipFeature
}