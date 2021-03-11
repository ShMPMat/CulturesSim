package shmp.simulation.culture.group.cultureaspect

import shmp.simulation.culture.group.centers.Group
import shmp.simulation.culture.group.cultureaspect.reasoning.concept.ObjectConcept
import shmp.simulation.culture.group.cultureaspect.worship.WorshipObject
import shmp.simulation.culture.group.cultureaspect.worship.WorshipObjectDependent
import shmp.simulation.culture.group.passingReward
import shmp.simulation.culture.group.request.MeaningResourceRequest
import shmp.simulation.culture.group.request.Request
import shmp.simulation.culture.group.request.RequestCore
import shmp.simulation.culture.group.resource_behaviour.ResourceBehaviour
import shmp.simulation.culture.thinking.meaning.Meme
import shmp.simulation.space.resource.container.MutableResourcePack
import shmp.simulation.space.resource.Resource

class DepictObject(
        val meme: Meme,
        val objectConcept: ObjectConcept?,
        private val resource: Resource,
        private val resourceBehaviour: ResourceBehaviour
) : CultureAspect, WorshipObjectDependent {
    override fun getRequest(group: Group): Request? = null

    override fun use(group: Group) {
        val memeInstance = group.cultureCenter.memePool.getMeme(meme.toString())
                ?: return//TODO fix this
        val result = group.populationCenter.executeRequest(MeaningResourceRequest(
                memeInstance,
                resource,
                RequestCore(
                        group,
                        1.0,
                        1.0,
                        passingReward,
                        passingReward,
                        40,
                        setOf()
                )
        ))//TODO do needs need pushing (they do)?
        val meaningful = MutableResourcePack(result.pack.resources)
        group.resourceCenter.addAll(meaningful)
        resourceBehaviour.proceedResources(meaningful, group.territoryCenter.territory)//TODO does it work?
        group.cultureCenter.memePool.strengthenMeme(meme)
    }

    override fun adopt(group: Group) = group.cultureCenter.memePool.getMeme(meme.toString())
            ?.let {
                DepictObject(
                        it,
                        objectConcept,
                        resource,
                        resourceBehaviour
                )
            }

    override fun die(group: Group) {}

    override fun swapWorship(worshipObject: WorshipObject) =
            DepictObject(worshipObject.name, objectConcept, resource, resourceBehaviour)

    override fun toString() = "Depict $meme on ${resource.fullName}, $resourceBehaviour"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DepictObject

        if (meme != other.meme) return false
        if (resource != other.resource) return false

        return true
    }

    override fun hashCode(): Int {
        var result = meme.hashCode()
        result = 31 * result + resource.hashCode()
        return result
    }
}