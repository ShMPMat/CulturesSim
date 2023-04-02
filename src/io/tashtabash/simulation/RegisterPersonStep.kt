package io.tashtabash.simulation

import io.tashtabash.simulation.culture.group.stratum.Person
import io.tashtabash.simulation.interactionmodel.InteractionModel
import io.tashtabash.simulation.space.SpaceData
import io.tashtabash.simulation.space.resource.container.ResourcePool
import io.tashtabash.simulation.space.resource.freeMarker


class RegisterPersonStep<E : CulturesWorld> : ControllerInitStep<E> {
    override fun run(world: E, interactionModel: InteractionModel<E>) {
        val resources = (SpaceData.data.resourcePool.cores + Person(freeMarker).core)
                .sortedBy { it.genome.baseName }
        SpaceData.data.resourcePool = ResourcePool(resources)
    }
}
