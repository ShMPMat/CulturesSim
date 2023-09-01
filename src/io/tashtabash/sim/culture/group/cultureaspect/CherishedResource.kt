package io.tashtabash.sim.culture.group.cultureaspect

import io.tashtabash.sim.culture.group.centers.Group
import io.tashtabash.sim.culture.group.request.Request
import io.tashtabash.sim.culture.group.request.RequestCore
import io.tashtabash.sim.culture.group.request.RequestType
import io.tashtabash.sim.culture.group.request.ResourceRequest
import io.tashtabash.sim.culture.group.resource_behaviour.ResourceBehaviour
import io.tashtabash.sim.space.resource.Resource
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
                            resourceBehaviour.proceedResources(p, group.territoryCenter.territory)
                        },
                        { (g, p), _: Double ->
                            g.resourceCenter.addAll(p)
                            resourceBehaviour.proceedResources(p, group.territoryCenter.territory)
                        },
                        40,
                        setOf(RequestType.Luxury)
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