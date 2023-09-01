package io.tashtabash.sim.culture.group

import io.tashtabash.random.SampleSpaceObject
import io.tashtabash.sim.culture.aspect.Aspect
import io.tashtabash.sim.culture.group.centers.Group


data class NeighbourAspectBox(
        val aspect: Aspect,
        val group: Group,
        override var probability: Double = 1.0
) : SampleSpaceObject

fun convert(aspects: Collection<Pair<Aspect, Group>>) = aspects
        .groupBy { it.first }
        .map { NeighbourAspectBox(it.key, it.value[0].second, it.value.size.toDouble()) }
