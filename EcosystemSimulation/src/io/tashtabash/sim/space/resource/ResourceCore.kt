package io.tashtabash.sim.space.resource

import io.tashtabash.sim.SimulationError
import java.util.*


//Contains all general information about all Resources with the same name.
class ResourceCore(
    val genome: Genome,
    externalFeatures: List<ExternalResourceFeature> = listOf(),
    val ownershipMarker: OwnershipMarker = freeMarker,
    internal val resourceBuilder: (ResourceCore, Int) -> Resource = { c, a -> Resource(c, a) }
) {
    val externalFeatures = externalFeatures.sortedBy { it.index }

    // Interned, so that comparing the names usually stops at the identity check; remove if too much mem is used
    val fullName = (genome.baseName + externalFeatures.joinToString { "_${it.name}" })
        .intern()

    init {
        if (externalFeatures.groupBy { it.index }.map { it.value.size }.any { it != 1 })
            throw SimulationError("${genome.name} has doubled external features: $externalFeatures")
    }

    val sample by lazy { resourceBuilder(this, 1) }
    val largeSample by lazy { resourceBuilder(this, genome.defaultAmount) }
    val wrappedSample by lazy { listOf(sample) }

    internal fun fullCopy(ownershipMarker: OwnershipMarker = this.ownershipMarker) =
        resourceBuilder(ResourceCore(genome.copy(), externalFeatures, ownershipMarker), genome.defaultAmount)

    fun copyWithNewExternalFeatures(features: List<ExternalResourceFeature>) = ResourceCore(
        genome.copy(),
        features
    )

    fun copy(
        genome: Genome = this.genome,
        externalFeatures: List<ExternalResourceFeature> = this.externalFeatures,
        ownershipMarker: OwnershipMarker = this.ownershipMarker
    ) = ResourceCore(
        genome,
        externalFeatures,
        ownershipMarker
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
