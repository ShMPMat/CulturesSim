package shmp.simulation.space.resource.tag.leveler

import shmp.simulation.space.resource.Genome


interface ResourceLeveler {
    fun getLevel(genome: Genome): Int
}
