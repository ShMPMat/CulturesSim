package io.tashtabash.simulation.space.resource.tag.labeler

import io.tashtabash.simulation.space.resource.Genome


data class SimpleNameLabeler(private val simpleName: String) : ResourceLabeler {
    override fun isSuitable(genome: Genome) = genome.name == simpleName

    override fun toString() = "Resource with simple name $simpleName"
}
