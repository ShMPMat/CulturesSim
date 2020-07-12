package simulation.space.resource

import simulation.SimulationException
import simulation.space.SpaceError
import simulation.space.resource.action.ResourceAction
import simulation.space.resource.instantiation.GenomeTemplate
import simulation.space.resource.material.Material
import java.util.*


//Contains all general information about all Resources with the same name.
class ResourceCore(
        name: String,
        val materials: List<Material>,
        val genome: Genome,
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
                        externalFeatures
                ),
                ownershipMarker = ownershipMarker
        )
    }

    fun copyWithNewExternalFeatures(features: List<ExternalResourceFeature>): ResourceCore {
        val genome = genome.copy()
        return ResourceCore(
                genome.name,
                ArrayList(materials),
                genome,
                features
        )
    }

    //TODO throw an exception on any attempt to copy template
    //TODO get rid of Templates in the conversions and move this to the ConversionCore
    fun applyAction(action: ResourceAction): List<Resource> = genome.conversionCore.actionConversion[action]
            ?.map { (r, n) ->
                val resource = r?.copy(n)
                        ?: throw SimulationException("Empty conversion")

                return@map if (resource.core.genome is GenomeTemplate)
                    throw SimulationException("No GenomeTemplates allowed")
                else resource
            } ?: listOf(applyActionToMaterials(action).copy(1))

    private fun applyActionToMaterials(action: ResourceAction): ResourceCore {
        val newMaterials = materials.map { it.applyAction(action) }
        val genome = genome.copy()
        genome.spreadProbability = 0.0
        return ResourceCore(
                genome.name + if (newMaterials == materials) "" else "_" + action.name,
                newMaterials,
                genome
        ) //TODO dangerous stuff for genome
    }

    fun hasApplication(action: ResourceAction) =
            genome.conversionCore.hasApplication(action) || materials.any { it.hasApplication(action) }

    fun copyCore(
            name: String = this.genome.name,
            materials: List<Material> = this.materials,
            genome: Genome = this.genome,
            externalFeatures: List<ExternalResourceFeature> = this.externalFeatures
    ) = ResourceCore(
            name,
            materials,
            genome,
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
