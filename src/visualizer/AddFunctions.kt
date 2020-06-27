package visualizer

import simulation.culture.aspect.Aspect
import simulation.culture.aspect.AspectPool
import simulation.culture.aspect.ConverseWrapper
import simulation.culture.aspect.MeaningInserter
import simulation.culture.group.centers.Group
import simulation.culture.group.GroupConglomerate
import simulation.space.resource.container.ResourcePool
import simulation.space.tile.Tile
import java.util.*

fun addResourceOnTile(tile: Tile?, resourceName: String, resourcePool: ResourcePool) {
    if (tile == null) {
        System.err.println("No such Tile")
        return
    }
    try {
        val resource = resourcePool.get(resourceName)
        tile.addDelayedResource(resource.copy())
    } catch (e: NoSuchElementException) {
        System.err.println("No such Resource")
    }
}


fun addGroupWant(group: Group?, wantName: String, resourcePool: ResourcePool) {
    if (group == null) {
        System.err.println("No such Group")
        return
    }
    try {
        val resource = resourcePool.get(wantName)
        group.cultureCenter.addResourceWant(resource)
    } catch (e: NoSuchElementException) {
        System.err.println("Cannot addAll want to the group")
    }
}

fun addGroupConglomerateWant(group: GroupConglomerate?, wantName: String, resourcePool: ResourcePool) {
    if (group == null) {
        System.err.println("No such GroupConglomerate")
        return
    }
    group.subgroups.forEach {
        addGroupWant(it, wantName, resourcePool)
    }
}

fun addGroupAspect(group: Group?, aspect: Aspect) {
    if (group == null) {
        System.err.println("No such Group")
        return
    }
    if (aspect is ConverseWrapper) {
        group.cultureCenter.aspectCenter.addAspect(aspect.aspect)
        val inner = aspect.aspect
        if (!group.cultureCenter.aspectCenter.aspectPool.contains(inner)) {
            System.err.println("Can't add base aspect for the Converse Wrapper.")
            return
        }
    }
    group.cultureCenter.aspectCenter.addAspect(aspect)
}

fun addGroupAspect(group: Group?, aspectName: String, aspectPool: AspectPool) {
    if (group == null) {
        System.err.println("No such Group")
        return
    }
    val aspect = if (aspectName.contains("On")) {
        val resourceName = aspectName.split("On".toRegex()).toTypedArray()[1]
        val accessibleResource = findGroupResource(resourceName, group)
        if (accessibleResource == null) {
            System.err.println("Group has no access to needed resources")
            return
        }
        try {
            val a: Aspect = aspectPool.getValue(aspectName.split("On".toRegex()).toTypedArray()[0])
            if (a.canApplyMeaning()) {
                MeaningInserter(a, accessibleResource)
            } else {
                ConverseWrapper(a, accessibleResource)
            }
        } catch (e: NoSuchElementException) {
            System.err.println("No such Aspect")
            return
        }
    } else {
        try {
            aspectPool.getValue(aspectName)
        } catch (e: NoSuchElementException) {
            System.err.println("No such Aspect")
            return
        }
    }
    addGroupAspect(group, aspect)
}

fun addGroupConglomerateAspect(group: GroupConglomerate?, aspectName: String, aspectPool: AspectPool) {
    if (group == null) {
        System.err.println("No such GroupConglomerate")
        return
    }
    group.subgroups.forEach {
        addGroupAspect(it, aspectName, aspectPool)
    }
}

private fun findGroupResource(resourceName: String, group: Group) = group.overallTerritory.differentResources
        .firstOrNull { it.baseName == resourceName }
        ?: group.overallTerritory.differentResources
                .firstOrNull { it.simpleName == resourceName }
        ?: group.cultureCenter.aspectCenter.aspectPool.producedResources
                .map { it.first }
                .firstOrNull { it.baseName == resourceName }
        ?: group.cultureCenter.aspectCenter.aspectPool.producedResources
                .map { it.first }
                .firstOrNull { it.simpleName == resourceName }