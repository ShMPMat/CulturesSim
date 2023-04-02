package io.tashtabash.visualizer.text

import io.tashtabash.simulation.culture.group.GroupConglomerate
import io.tashtabash.simulation.culture.group.centers.Group


fun TextCultureVisualizer.printGroupConglomerate(groupConglomerate: GroupConglomerate) {
    printMap { groupConglomerateMapper(groupConglomerate, it) }
    println(groupConglomerate)
}


fun TextCultureVisualizer.printGroup(group: Group) {
    printMap { groupMapper(group, it) }
    println(outputGroup(group))
}
