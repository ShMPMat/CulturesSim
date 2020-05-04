package simulation.culture.group.cultureaspect

import simulation.culture.aspect.AspectController
import simulation.culture.aspect.ConverseWrapper
import simulation.culture.group.centers.Group
import simulation.culture.group.request.Request
import simulation.culture.group.request.passingEvaluator
import simulation.culture.group.resource_behaviour.ResourceBehaviour
import simulation.culture.thinking.meaning.Meme
import simulation.space.resource.MutableResourcePack
import java.util.*

class DepictObject(
        val meme: Meme,
        private val converseWrapper: ConverseWrapper,
        private val resourceBehaviour: ResourceBehaviour
) : CultureAspect {

    override fun getRequest(group: Group): Request? = null

    override fun use(group: Group) {
        if (group.cultureCenter.memePool.getMeme(meme.toString()) == null) return //TODO fix this
        val result = converseWrapper.use(AspectController(
                1,
                1,
                1,
                passingEvaluator,
                group.populationCenter,
                group.territoryCenter.accessibleTerritory,
                true,
                group,
                group.cultureCenter.memePool.getMeme(meme.toString())
        ))
        result.pushNeeds(group)
        if (result.isFinished) {
            val meaningful = MutableResourcePack(result.resources.resources.filter { it.hasMeaning() })
            result.resources.removeAll(meaningful.resources)
            group.resourceCenter.addAll(meaningful)
            resourceBehaviour.proceedResources(meaningful)
            result.resources.disbandOnTile(group.territoryCenter.disbandTile)
            group.cultureCenter.memePool.strengthenMeme(meme)
        }
    }

    override fun copy(group: Group) = DepictObject(
            meme,
            group.cultureCenter.aspectCenter.aspectPool.getValue(converseWrapper) as ConverseWrapper,
            resourceBehaviour
    )

    override fun toString() = "Depict $meme with ${converseWrapper.name} $resourceBehaviour"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as DepictObject
        return meme == that.meme && converseWrapper == that.converseWrapper
    }

    override fun hashCode() = Objects.hash(meme, converseWrapper)

    override fun die(group: Group) {}
}