package io.tashtabash.sim.space.resource.tag.labeler

import io.tashtabash.sim.space.resource.Genome


data class SimpleNameLabeler(private val simpleName: String) : ResourceLabeler {
    override fun isSuitable(genome: Genome) = genome.name == simpleName

    override fun toString() = "Resource with simple name $simpleName"
}
