package io.tashtabash.sim.space.resource.tag.labeler

import io.tashtabash.sim.space.resource.Genome
import io.tashtabash.sim.space.resource.dependency.AvoidTiles
import io.tashtabash.sim.space.tile.Tile


data class HabitatLabeler(private val type: Tile.Type): ResourceLabeler {
    override fun isSuitable(genome: Genome) = genome.dependencies
            .none { it is AvoidTiles && it.badTypes.contains(type) }

    override fun toString() = "Lives on $type"
}
