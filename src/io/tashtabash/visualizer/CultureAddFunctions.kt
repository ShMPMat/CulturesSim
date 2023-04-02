package io.tashtabash.visualizer

import io.tashtabash.simulation.culture.aspect.Aspect
import io.tashtabash.simulation.culture.aspect.AspectPool
import io.tashtabash.simulation.culture.aspect.ConverseWrapper
import io.tashtabash.simulation.culture.aspect.MeaningInserter
import io.tashtabash.simulation.culture.group.GroupConglomerate
import io.tashtabash.simulation.culture.group.centers.Group
import io.tashtabash.simulation.space.resource.container.ResourcePool
import java.util.*


fun addGroupWant(group: Group?, wantName: String, resourcePool: ResourcePool) {
    if (group == null) {
        System.err.println("No such Group")
        return
    }
    try {
        val resource = resourcePool.getBaseName(wantName)
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
        group.cultureCenter.aspectCenter.addAspectTry(aspect.aspect, group)
        val inner = aspect.aspect
        if (!group.cultureCenter.aspectCenter.aspectPool.contains(inner)) {
            System.err.println("Can't add base aspect for the Converse Wrapper.")
            return
        }
    }
    group.cultureCenter.aspectCenter.addAspectTry(aspect, group)
}

fun addGroupAspect(group: Group?, aspectName: String, aspectPool: AspectPool) {
    if (group == null) {
        System.err.println("No such Group")
        return
    }
    val resourceNameDelimiter = "On"

    val aspect = if (aspectName.contains(resourceNameDelimiter)) {
        val (shortAspectName, resourceName) = aspectName.split(resourceNameDelimiter)
        val accessibleResource = findGroupResource(resourceName, group)
        if (accessibleResource == null) {
            System.err.println("Group has no access to needed resources")
            return
        }

        val aspect: Aspect? = aspectPool.get(shortAspectName)
        if (aspect == null) {
            System.err.println("No such Aspect")
            return
        }

        if (aspect.canApplyMeaning)
            MeaningInserter(aspect, accessibleResource)
        else
            ConverseWrapper(aspect, accessibleResource)
    } else {
        val aspect = aspectPool.get(aspectName)

        if (aspect == null) {
            System.err.println("No such Aspect")
            return
        }

        aspect
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
                .firstOrNull { it.baseName == resourceName }
        ?: group.cultureCenter.aspectCenter.aspectPool.producedResources
                .firstOrNull { it.simpleName == resourceName }
