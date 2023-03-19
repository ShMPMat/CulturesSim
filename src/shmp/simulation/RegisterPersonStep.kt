package shmp.simulation

import shmp.simulation.culture.group.stratum.Person
import shmp.simulation.interactionmodel.InteractionModel
import shmp.simulation.space.SpaceData
import shmp.simulation.space.resource.container.ResourcePool
import shmp.simulation.space.resource.freeMarker


class RegisterPersonStep<E : CulturesWorld> : ControllerInitStep<E> {
    override fun run(world: E, interactionModel: InteractionModel<E>) {
        val resources = (SpaceData.data.resourcePool.cores + Person(freeMarker).core)
                .sortedBy { it.genome.baseName }
        SpaceData.data.resourcePool = ResourcePool(resources)
    }
}
