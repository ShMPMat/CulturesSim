package shmp.visualizer.text

import shmp.simulation.culture.group.GroupConglomerate
import shmp.simulation.culture.group.centers.Group
import shmp.simulation.space.resource.Resource
import shmp.simulation.space.tile.Tile
import shmp.visualizer.outputGroup
import shmp.visualizer.outputResource


fun TextCultureVisualizer.printGroupConglomerate(groupConglomerate: GroupConglomerate) {
    printMap { groupConglomerateMapper(groupConglomerate, it) }
    println(groupConglomerate)
}


fun TextCultureVisualizer.printGroup(group: Group) {
    printMap { groupMapper(group, it) }
    println(outputGroup(group))
}

fun TextEcosystemVisualizer.printTile(tile: Tile) {
    printMap { if (it == tile) "\u001b[31m\u001b[41mX" else "" }
    println(tile)
}

fun TextEcosystemVisualizer.printResource(resource: Resource) {
    printMap { tile: Tile ->
        if (tile.resourcePack.any { it.simpleName == resource.simpleName && it.isNotEmpty })
            "\u001b[30m\u001b[41m" + tile.resourcePack.getAmount(resource) % 10
        else ""
    }
    println(outputResource(resource))
}