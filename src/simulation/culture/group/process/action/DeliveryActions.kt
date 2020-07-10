package simulation.culture.group.process.action

import simulation.culture.group.centers.Group
import simulation.culture.group.process.behaviour.DeliveryBehaviour
import simulation.culture.group.process.behaviour.TimedResourceDeliveryB
import simulation.space.resource.container.ResourcePack
import simulation.space.tile.getDistance


class ScheduleDefaultDelivery(val from: Group, val to: Group, val pack: ResourcePack) : AbstractGroupAction(from) {
    override fun run() = ScheduleDelivery(
            from,
            TimedResourceDeliveryB(pack, to, ComputeTravelTime(from, to).run())
    ).run()
}

class ScheduleDelivery(group: Group, private val deliveryB: DeliveryBehaviour) : AbstractGroupAction(group) {
    override fun run() {
        group.processCenter.addBehaviour(deliveryB)
    }
}

class ComputeTravelTime(group: Group, private val target: Group): AbstractGroupAction(group) {
    override fun run() = getDistance(group.territoryCenter.center, target.territoryCenter.center) /
            group.territoryCenter.reachDistance
}
