package simulation.culture.aspect

import simulation.culture.aspect.dependency.AspectDependencies
import simulation.space.resource.Resource
import simulation.space.resource.tag.ResourceTag
import java.util.*
import java.util.function.Consumer

/**
 * Subclass of ConverseWrapper which inserts meaning in certain objects.
 */
class MeaningInserter(aspect: Aspect, resource: Resource) : ConverseWrapper(aspect, resource.fullCopy().copy(1)) {
    override val producedResources: List<Resource>
        get() = listOf(resource.copy(1))

    init {
        canInsertMeaning = true
        this.resource.setHasMeaning(true)
    }

    override fun use(controller: AspectController): AspectResult {
        val result = super.use(controller)
        val res = ArrayList(result.resources.getResourceAndRemove(resource).resources)
        res.removeIf { it.isEmpty }
        result.resources.addAll(res.map { it.insertMeaning(controller.meaning, result) })
        return result
    }

    override fun shouldPassMeaningNeed(isMeaningNeeded: Boolean) = false

    override fun copy(dependencies: AspectDependencies): MeaningInserter {
        val copy = MeaningInserter(aspect, resource)
        copy.initDependencies(dependencies)
        val unwantedTags: MutableCollection<ResourceTag> = ArrayList()
        for (resourceTag in dependencies.map.keys) {
            if (!(resourceTag.isInstrumental || resourceTag.name == "phony")) {
                unwantedTags.add(resourceTag)
            }
        }
        unwantedTags.forEach(Consumer { o: ResourceTag? -> dependencies.map.remove(o) })
        return copy
    }
}