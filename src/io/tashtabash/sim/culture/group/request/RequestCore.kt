package io.tashtabash.sim.culture.group.request

import io.tashtabash.sim.culture.group.centers.Group
import io.tashtabash.sim.space.resource.container.MutableResourcePack


data class RequestCore(
        val group: Group,
        var floor: Double,
        var ceiling: Double,
        var penalty: (Pair<Group, MutableResourcePack>, Double) -> Unit,
        var reward: (Pair<Group, MutableResourcePack>, Double) -> Unit,
        val need: Int,
        val requestTypes: Set<RequestType>
)
