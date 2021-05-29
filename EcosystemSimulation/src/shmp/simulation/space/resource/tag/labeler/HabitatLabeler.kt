package shmp.simulation.space.resource.tag.labeler

import shmp.simulation.space.resource.Genome
import shmp.simulation.space.resource.dependency.AvoidTiles
import shmp.simulation.space.tile.Tile


data class HabitatLabeler(private val type: Tile.Type): ResourceLabeler {
    override fun isSuitable(genome: Genome) = genome.dependencies
            .none { it is AvoidTiles && it.badTypes.contains(type) }

    override fun toString() = "Lives on $type"
}
