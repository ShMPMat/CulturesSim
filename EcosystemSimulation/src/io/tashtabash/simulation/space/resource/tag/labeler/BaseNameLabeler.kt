package io.tashtabash.simulation.space.resource.tag.labeler

import io.tashtabash.simulation.space.resource.Genome


data class BaseNameLabeler(private val baseName: String) : ResourceLabeler {
    override fun isSuitable(genome: Genome) = genome.baseName == baseName

    override fun toString() = "Resource is $baseName"
}
