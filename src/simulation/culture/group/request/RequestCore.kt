package simulation.culture.group.request

import simulation.culture.group.centers.Group
import simulation.space.resource.container.MutableResourcePack

data class RequestCore(
        val group: Group,
        var floor: Double,
        var ceiling: Double,
        var penalty: (Pair<Group, MutableResourcePack>, Double) -> Unit,
        var reward: (Pair<Group, MutableResourcePack>, Double) -> Unit,
        val need: Int
)