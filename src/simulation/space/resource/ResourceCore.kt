package simulation.space.resource

import simulation.SimulationException
import simulation.space.SpaceError
import simulation.space.resource.action.ConversionCore
import simulation.space.resource.action.ResourceAction
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
        internal val conversionCore: ConversionCore,
        externalFeatures: List<ExternalResourceFeature> = listOf()
) {
    internal val externalFeatures = externalFeatures.sortedBy { it.index }

    init {
        if (externalFeatures.groupBy { it.index }.map { it.value.size }.any { it != 1 })
            throw SimulationException("${genome.name} has doubled external features: $externalFeatures")
        setName(name)
    }

    private fun setName(name: String) {
        genome.name = name
    }

    fun copy() = Resource(this)

    fun copy(amount: Int) = Resource(this, amount)

    internal fun fullCopy(ownershipMarker: OwnershipMarker): Resource {
        if (genome is GenomeTemplate)
            throw SpaceError("Can't make a full copy of a template")
        return Resource(
                ResourceCore(
                        genome.name,
                        ArrayList(materials),
                        genome.copy(),
                        conversionCore.copy(),
                        externalFeatures
                ),
                ownershipMarker = ownershipMarker
        )
    }

    private fun instantiateTemplateCopy(legacy: ResourceCore): ResourceCore {
        if (genome !is GenomeTemplate)
            throw SpaceError("Cant make a instantiated copy not from a template")
        return ResourceCore(
                genome.name,
                ArrayList(legacy.materials),
                genome.getInstantiatedGenome(legacy),
                conversionCore.copy()
        )
    }

    fun copyWithNewExternalFeatures(features: List<ExternalResourceFeature>): ResourceCore {
        val genome = genome.copy()
        return ResourceCore(
                genome.name,
                ArrayList(materials),
                genome,
                conversionCore,
                features
        )
    }

    //TODO throw an exception on any attempt to copy template
    //TODO get rid of Templates in the conversions and move this to the ConversionCore
    fun applyAction(action: ResourceAction): List<Resource> = conversionCore.actionConversion[action]
            ?.map { (r, n) ->
                var resource = r ?: throw SimulationException("Empty conversion")
                if (resource.core.genome is GenomeTemplate) {
                    resource = resource.copy(n)
                    resource.core = resource.core.instantiateTemplateCopy(this)
                    resource.computeHash()
                    if (resource.tags.groupBy { it.name }.map { it.value.size }.any { it > 1 } || resource.toString().contains("House")) {
                        val k = 0
                    }
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
                conversionCore
        ) //TODO dangerous stuff for genome
    }

    fun hasApplication(action: ResourceAction) =
            conversionCore.hasApplication(action) || materials.any { it.hasApplication(action) }

    fun copyCore(
            name: String = this.genome.name,
            materials: List<Material> = this.materials,
            genome: Genome = this.genome,
            conversionCore: ConversionCore = this.conversionCore,
            externalFeatures: List<ExternalResourceFeature> = this.externalFeatures
    ) = ResourceCore(
            name,
            materials,
            genome,
            conversionCore,
            externalFeatures
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        val resourceCore = other as ResourceCore
        return genome.baseName == resourceCore.genome.baseName
    }

    override fun hashCode() = Objects.hash(genome.baseName)

    override fun toString() = genome.baseName
}
