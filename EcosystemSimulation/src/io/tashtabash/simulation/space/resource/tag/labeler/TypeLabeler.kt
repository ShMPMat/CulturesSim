package io.tashtabash.simulation.space.resource.tag.labeler

import io.tashtabash.simulation.space.resource.Genome
import io.tashtabash.simulation.space.resource.ResourceType


data class TypeLabeler(private val type: ResourceType): ResourceLabeler {
    override fun isSuitable(genome: Genome) = genome.type == type

    override fun toString() = "Resource has the type $type"
}
