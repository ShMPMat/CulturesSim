package simulation.space.resource

import simulation.culture.aspect.Aspect
import simulation.culture.group.GroupError
import simulation.space.SpaceError
import simulation.space.resource.material.Material
import java.util.*
import java.util.stream.Collectors

/**
 * Class which contains all general information about all Resources with the same name.
 */
class ResourceCore(
        name: String,
        val materials: List<Material>,
        val genome: Genome,
        aspectConversion: Map<Aspect, MutableList<Pair<Resource?, Int>>>,
        internal val externalFeatures: List<ExternalResourceFeature> = listOf()
) {
    val aspectConversion: MutableMap<Aspect, MutableList<Pair<Resource?, Int>>>

    init {
        this.aspectConversion = HashMap(aspectConversion)
        setName(name)
    }

    internal fun addAspectConversion(aspect: Aspect, resourceList: List<Pair<Resource?, Int>>) {
        aspectConversion[aspect] = resourceList.toMutableList()
    }

    private fun setName(name: String) {
        genome.name = name
    }

    fun copy() = Resource(this)

    fun copy(amount: Int) = Resource(this, amount)

    fun fullCopy(): Resource {
        if (genome is GenomeTemplate)
            throw SpaceError("Cant make a full copy of a template")
        return Resource(ResourceCore(
                genome.name,
                ArrayList(materials),
                genome.copy(),
                aspectConversion,
                externalFeatures
        ))
    }

    private fun instantiateTemplateCopy(legacy: ResourceCore): ResourceCore {
        if (genome !is GenomeTemplate)
            throw SpaceError("Cant make a instantiated copy not from a template")
        return ResourceCore(
                genome.name,
                ArrayList(legacy.materials),
                genome.getInstantiatedGenome(legacy),
                aspectConversion
        )
    }

    fun copyWithExternalFeatures(features: List<ExternalResourceFeature>): ResourceCore {
        val genome = genome.copy()
        return ResourceCore(
                genome.name,
                ArrayList(materials),
                genome,
                aspectConversion,
                features
        )
    }

    fun applyAspect(aspect: Aspect): List<Resource> { //TODO throw an exception on any attempt to copy template
        return if (aspectConversion.containsKey(aspect)) {
            aspectConversion[aspect]!!.stream()
                    .map { pair: Pair<Resource?, Int> ->
                        var resource = pair.first ?: throw GroupError("Empty conversion")
                        if (resource.core.genome is GenomeTemplate) {
                            resource = resource.copy(pair.second)
                            resource.core = resource.core.instantiateTemplateCopy(this)
                            resource.computeHash()
                            return@map resource
                        } else {
                            return@map resource.copy(pair.second)
                        }
                    }.collect(Collectors.toList())
        } else listOf(applyAspectToMaterials(aspect).copy(1))
    }

    private fun applyAspectToMaterials(aspect: Aspect): ResourceCore {
        val newMaterials = materials.map { it.applyAspect(aspect) }
        val genome = genome.copy()
        genome.spreadProbability = 0.0
        return ResourceCore(
                genome.name + if (newMaterials == materials) "" else "_" + aspect.name,
                newMaterials,
                genome,
                aspectConversion
        ) //TODO dangerous stuff for genome
    }

    fun hasApplicationForAspect(aspect: Aspect) = aspectConversion.containsKey(aspect)
            || materials.any { it.hasApplicationForAspect(aspect) }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null) return false
        val resourceCore = o as ResourceCore
        return genome.baseName == resourceCore.genome.baseName
    }

    override fun hashCode(): Int {
        return Objects.hash(genome.baseName)
    }

    override fun toString() = genome.baseName
}