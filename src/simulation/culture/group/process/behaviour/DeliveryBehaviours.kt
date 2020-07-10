package simulation.culture.group.process.behaviour

import simulation.Event
import simulation.culture.group.centers.Group
import simulation.culture.group.process.action.ReceivePopulationResourcesA
import simulation.space.resource.container.ResourcePack


abstract class DeliveryBehaviour(val pack: ResourcePack, val target: Group, val deliveryTime: Int): PlanBehaviour()

class TimedResourceDeliveryB(
        pack: ResourcePack,
        target: Group,
        deliveryTime: Int
): DeliveryBehaviour(pack, target, deliveryTime) {
    private var timePassed = 0

    override fun run(group: Group): List<Event> {
        val events = mutableListOf<Event>()
        if (timePassed == 0)
            events.add(Event(
                    Event.Type.GroupInteraction,
                    "${group.name} started delivery of ${pack.listResources()} to ${target.name}"
            ))

        timePassed++

        if (timePassed < deliveryTime)
            return events

        ReceivePopulationResourcesA(target, pack).run()

        return events + listOf(Event(
                Event.Type.GroupInteraction,
                "${group.name} delivered ${pack.listResources()} to ${target.name}"
        ))
    }

    override val internalToString =
            "Delivery of ${pack.listResources()} to ${target.name}, $timePassed out of $deliveryTime"
}
