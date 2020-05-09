package simulation.culture.group.cultureaspect

import simulation.Controller
import simulation.culture.aspect.AspectController
import simulation.culture.aspect.ConverseWrapper
import simulation.culture.group.centers.Group
import simulation.culture.group.reason.Reason
import simulation.culture.group.request.Request
import simulation.culture.group.request.passingEvaluator
import simulation.culture.group.resource_behaviour.ResourceBehaviour
import simulation.culture.group.resource_behaviour.getRandom
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
                1,
                1,
                passingEvaluator,
                group.populationCenter,
                group.territoryCenter.accessibleTerritory,
                true,
                group,
                group.cultureCenter.meaning
        ))
        result.pushNeeds(group)
        if (result.isFinished) {
            group.resourceCenter.addAll(result.resources)
            resourceBehaviour.proceedResources(result.resources)
        }
    }

    override fun adopt(group: Group): AspectRitual? {
        if (!group.cultureCenter.aspectCenter.aspectPool.contains(converseWrapper)) return null
        return AspectRitual(
                group.cultureCenter.aspectCenter.aspectPool.getValue(converseWrapper) as ConverseWrapper,
                getRandom(group, Controller.session.random),
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