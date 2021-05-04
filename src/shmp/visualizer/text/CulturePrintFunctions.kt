package shmp.visualizer.text

import shmp.simulation.culture.group.GroupConglomerate
import shmp.simulation.culture.group.centers.Group


fun TextCultureVisualizer.printGroupConglomerate(groupConglomerate: GroupConglomerate) {
    printMap { groupConglomerateMapper(groupConglomerate, it) }
    println(groupConglomerate)
}


fun TextCultureVisualizer.printGroup(group: Group) {
    printMap { groupMapper(group, it) }
    println(outputGroup(group))
}
