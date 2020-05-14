package simulation.culture.group.cultureaspect.worship

import simulation.culture.group.centers.Group
import simulation.culture.group.request.Request

interface WorshipFeature {
    fun use(group: Group, parent: Worship)
    fun adopt(group: Group): WorshipFeature?
    fun die(group: Group, parent: Worship)
    fun swapWorship(worshipObject: WorshipObject) : WorshipFeature
}