package io.tashtabash.sim

import io.tashtabash.sim.culture.group.stratum.Person
import io.tashtabash.sim.interactionmodel.InteractionModel
import io.tashtabash.sim.space.SpaceData
import io.tashtabash.sim.space.resource.container.ResourcePool
import io.tashtabash.sim.space.resource.freeMarker


class RegisterPersonStep<E : CulturesWorld> : ControllerInitStep<E> {
    override fun run(world: E, interactionModel: InteractionModel<E>) {
        val resources = (SpaceData.data.resourcePool.cores + Person(freeMarker).core)
                .sortedBy { it.genome.baseName }
        SpaceData.data.resourcePool = ResourcePool(resources)
    }
}
