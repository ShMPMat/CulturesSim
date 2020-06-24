package simulation.space.resource

import simulation.SimulationException
import simulation.space.SpaceError
import simulation.space.resource.instantiation.GenomeTemplate
import simulation.space.resource.material.Material
import java.util.*

/**
 * Class which contains all general information about all Resources with the same name.
 */
class ResourceCore(
        name: String,
        val materials: List<Material>,
        val genome: Genome,
        aspectConversion: Map<ResourceAction, MutableList<Pair<Resource?, Int>>>,
        internal val externalFeatures: List<ExternalResourceFeature> = listOf()
) {
    val aspectConversion: MutableMap<ResourceAction, MutableList<Pair<Resource?, Int>>>

    init {
        this.aspectConversion = HashMap(aspectConversion)
        setName(name)
    }

    internal fun addAspectConversion(action: ResourceAction, resourceList: List<Pair<Resource?, Int>>) {
        aspectConversion[action] = resourceList.toMutableList()
    }

    private fun setName(name: String) {
        genome.name = name
    }

    fun copy() = Resource(this)

    fun copy(amount: Int) = Resource(this, amount)

    fun fullCopy(): Resource {
        if (genome is GenomeTemplate)
            throw SpaceError("Can't make a full copy of a template")
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

    //TODO throw an exception on any attempt to copy template
    fun applyAction(action: ResourceAction): List<Resource> = aspectConversion[action]
            ?.map { (r, n) ->
                var resource = r ?: throw SimulationException("Empty conversion")
                if (resource.core.genome is GenomeTemplate) {
                    resource = resource.copy(n)
                    resource.core = resource.core.instantiateTemplateCopy(this)
                    resource.computeHash()
                    return@map resource
                } else {
                    return@map resource.copy(n)
                }
            } ?: listOf(applyActionToMaterials(action).copy(1))

    private fun applyActionToMaterials(action: ResourceAction): ResourceCore {
        val newMaterials = materials.map { it.applyAction(action) }
        val genome = genome.copy()
        genome.spreadProbability = 0.0
        return ResourceCore(
                genome.name + if (newMaterials == materials) "" else "_" + action.name,
                newMaterials,
                genome,
                aspectConversion
        ) //TODO dangerous stuff for genome
    }

    fun hasApplicationForAction(action: ResourceAction) =
            aspectConversion.containsKey(action) || materials.any { it.hasApplicationForAspect(action) }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        val resourceCore = other as ResourceCore
        return genome.baseName == resourceCore.genome.baseName
    }

    override fun hashCode() = Objects.hash(genome.baseName)

    override fun toString() = genome.baseName
}