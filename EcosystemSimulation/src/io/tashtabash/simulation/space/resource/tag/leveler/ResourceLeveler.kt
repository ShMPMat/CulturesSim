package io.tashtabash.simulation.space.resource.tag.leveler

import io.tashtabash.simulation.space.resource.Genome


interface ResourceLeveler {
    fun getLevel(genome: Genome): Double
}
