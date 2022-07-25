package shmp.simulation.culture.group.cultureaspect.worship

import shmp.simulation.CulturesController.Companion.session
import shmp.simulation.culture.group.centers.Group
import shmp.simulation.culture.group.cultureaspect.worship.OfferingType.CultPractitioners
import shmp.simulation.culture.group.cultureaspect.worship.OfferingType.LocalTemple
import shmp.simulation.space.resource.Resource
import shmp.simulation.space.resource.container.ResourcePack


class Offering(resource: Resource, val type: OfferingType, val interval: Int) : WorshipResourceFeature(resource) {
    init {
        isFunctioning = true
    }

    private var turnsPassed = 0;

    override fun doUse(group: Group, parent: Worship): Boolean {
        turnsPassed++

        return if (turnsPassed == interval) {
            turnsPassed = 0

            true
        } else false
    }

    override fun getNeededAmount(group: Group, parent: Worship) = group.populationCenter.population

    override fun processResources(pack: ResourcePack, group: Group, parent: Worship) {
        when(type) {
            LocalTemple -> {
                val templeResource = session.world.resourcePool.getSimpleName("Temple")
                val place = parent.placeSystem.places
                        .firstOrNull { it.staticPlace.getResource(templeResource).amount > 0 }

                place?.staticPlace?.addResources(pack)
                isFunctioning = if (place == null) {
                    group.resourceCenter.addAll(pack)

                    false
                } else true
            }
            CultPractitioners -> {
                parent.cult?.findStratum(group, parent)?.ego?.place?.current?.addResources(pack)

                isFunctioning = if (parent.cult == null) {
                    group.resourceCenter.addAll(pack)

                    false
                } else true
            }
        }
    }

    override fun adopt(group: Group) = Offering(resource, type, interval)

    override fun swapWorship(worshipObject: WorshipObject) = Offering(resource, type, interval)

    override fun toString() = "Offering of ${resource.fullName} " + when(type) {
        LocalTemple -> "to local temple"
        CultPractitioners -> "to cult practitioners"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Offering) return false

        if (resource != other.resource) return false

        return true
    }

    override fun hashCode() = resource.hashCode()
}

enum class OfferingType {
    LocalTemple,
    CultPractitioners
}
