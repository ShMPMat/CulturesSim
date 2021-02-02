package shmp.simulation.culture.group.cultureaspect

import shmp.simulation.culture.group.centers.Group
import shmp.simulation.culture.group.request.Request
import shmp.simulation.culture.group.request.RequestCore
import shmp.simulation.culture.group.request.ResourceRequest
import shmp.simulation.culture.group.resource_behaviour.ResourceBehaviour
import shmp.simulation.space.resource.Resource
import java.util.*

class CherishedResource(
        val resource: Resource,
        private val resourceBehaviour: ResourceBehaviour
) : CultureAspect {
    override fun getRequest(group: Group): Request? {
        return ResourceRequest(
                resource,
                RequestCore(
                        group,
                        1.0,
                        10.0,
                        { (g, p), _: Double ->
                            g.resourceCenter.addAll(p)
                            resourceBehaviour.proceedResources(p)
                        },
                        { (g, p), _: Double ->
                            g.resourceCenter.addAll(p)
                            resourceBehaviour.proceedResources(p)
                        },
                        40
                )
        )
    }

    override fun use(group: Group) {}

    override fun adopt(group: Group) = CherishedResource(resource, resourceBehaviour)

    override fun toString() = "Aesthetically pleasing ${resource.fullName}, $resourceBehaviour"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as CherishedResource
        return resource == that.resource
    }

    override fun hashCode() = Objects.hash(resource)

    override fun die(group: Group) {}
}