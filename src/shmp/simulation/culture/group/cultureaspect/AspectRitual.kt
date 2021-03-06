package shmp.simulation.culture.group.cultureaspect

import shmp.simulation.culture.aspect.AspectController
import shmp.simulation.culture.aspect.ConverseWrapper
import shmp.simulation.culture.group.centers.Group
import shmp.simulation.culture.group.reason.Reason
import shmp.simulation.culture.group.request.Request
import shmp.simulation.culture.group.request.RequestType
import shmp.simulation.culture.group.request.passingEvaluator
import shmp.simulation.culture.group.resource_behaviour.ResourceBehaviour
import shmp.simulation.culture.group.resource_behaviour.getRandom
import java.util.*

class AspectRitual(
        val converseWrapper: ConverseWrapper,
        private val resourceBehaviour: ResourceBehaviour,
        reason: Reason
) : Ritual(reason) {
    override fun getRequest(group: Group): Request? = null

    override fun use(group: Group) {
        val result = converseWrapper.use(AspectController(
                1,
                1.0,
                1.0,
                passingEvaluator,
                group.populationCenter,
                group.territoryCenter.accessibleTerritory,
                true,
                group,
                setOf(RequestType.Spiritual)
        ))
        result.pushNeeds(group)
        if (result.isFinished) {
            group.resourceCenter.addAll(result.resources)
            resourceBehaviour.proceedResources(result.resources, group.territoryCenter.territory)
        }
    }

    override fun adopt(group: Group): AspectRitual? {
        if (!group.cultureCenter.aspectCenter.aspectPool.contains(converseWrapper)) return null
        return AspectRitual(
                group.cultureCenter.aspectCenter.aspectPool.getValue(converseWrapper) as ConverseWrapper,
                getRandom(),
                reason
        )
    }

    override fun toString() = "Ritual with ${converseWrapper.name}, $resourceBehaviour because $reason"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as AspectRitual
        return converseWrapper == that.converseWrapper
    }

    override fun hashCode() = Objects.hash(converseWrapper)

    override fun die(group: Group) {}
}