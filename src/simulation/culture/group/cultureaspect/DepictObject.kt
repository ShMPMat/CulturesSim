package simulation.culture.group.cultureaspect

import simulation.culture.group.centers.Group
import simulation.culture.group.cultureaspect.worship.WorshipObject
import simulation.culture.group.cultureaspect.worship.WorshipObjectDependent
import simulation.culture.group.passingReward
import simulation.culture.group.request.MeaningResourceRequest
import simulation.culture.group.request.Request
import simulation.culture.group.resource_behaviour.ResourceBehaviour
import simulation.culture.thinking.meaning.Meme
import simulation.space.resource.MutableResourcePack
import simulation.space.resource.Resource

class DepictObject(
        val meme: Meme,
        private val resource: Resource,
        private val resourceBehaviour: ResourceBehaviour
) : WorshipObjectDependent {

    override fun getRequest(group: Group): Request? = null

    override fun use(group: Group) {
        if (group.cultureCenter.memePool.getMeme(meme.toString()) == null) return //TODO fix this
        val result = group.populationCenter.executeRequest(MeaningResourceRequest(
                group,
                group.cultureCenter.memePool.getMeme(meme.toString()),
                resource,
                1,
                1,
                passingReward,
                passingReward
        ))//TODO do needs need pushing (they do)?
        val meaningful = MutableResourcePack(result.pack.resources)
        group.resourceCenter.addAll(meaningful)
        resourceBehaviour.proceedResources(meaningful)//TODO does it work?
        group.cultureCenter.memePool.strengthenMeme(meme)
    }

    override fun adopt(group: Group) =
            if (group.cultureCenter.memePool.getMeme(meme.toString()) != null)
                DepictObject(
                        group.cultureCenter.memePool.getMeme(meme.toString()),
                        resource,
                        resourceBehaviour
                )
            else null

    override fun die(group: Group) {}

    override fun swapWorship(worshipObject: WorshipObject) = DepictObject(worshipObject.name, resource, resourceBehaviour)

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