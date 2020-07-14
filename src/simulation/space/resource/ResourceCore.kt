package simulation.space.resource

import simulation.SimulationException
import simulation.space.SpaceError
import simulation.space.resource.action.ResourceAction
import simulation.space.resource.instantiation.GenomeTemplate
import java.util.*


//Contains all general information about all Resources with the same name.
class ResourceCore(
        val genome: Genome,
        externalFeatures: List<ExternalResourceFeature> = listOf()
) {
    internal val externalFeatures = externalFeatures.sortedBy { it.index }

    init {
        if (externalFeatures.groupBy { it.index }.map { it.value.size }.any { it != 1 })
            throw SimulationException("${genome.name} has doubled external features: $externalFeatures")
    }

    fun copy(amount: Int = genome.defaultAmount) = Resource(this, amount)

    internal fun fullCopy(ownershipMarker: OwnershipMarker) =
            if (genome is GenomeTemplate)
                throw SpaceError("Can't make a full copy of a template")
            else Resource(
                    ResourceCore(genome.copy(), externalFeatures),
                    ownershipMarker = ownershipMarker
            )

    fun copyWithNewExternalFeatures(features: List<ExternalResourceFeature>) = ResourceCore(
            genome.copy(),
            features
    )

    //TODO get rid of Templates in the conversions and move this to the ConversionCore
    fun applyAction(action: ResourceAction): List<Resource> = genome.conversionCore.actionConversion[action]
            ?.map { (r, n) ->
                val resource = r?.copy(n)
                        ?: throw SimulationException("Empty conversion")
                return@map if (resource.core.genome is GenomeTemplate)
                    throw SimulationException("No GenomeTemplates allowed")
                else resource
            } ?: listOf(copy(1))

    fun copyCore(
            genome: Genome = this.genome,
            externalFeatures: List<ExternalResourceFeature> = this.externalFeatures
    ) = ResourceCore(
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
