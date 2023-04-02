package io.tashtabash.simulation.space.resource.tag.labeler

import io.tashtabash.simulation.space.resource.Genome
import io.tashtabash.simulation.space.resource.dependency.AvoidTiles
import io.tashtabash.simulation.space.tile.Tile


data class HabitatLabeler(private val type: Tile.Type): ResourceLabeler {
    override fun isSuitable(genome: Genome) = genome.dependencies
            .none { it is AvoidTiles && it.badTypes.contains(type) }

    override fun toString() = "Lives on $type"
}
