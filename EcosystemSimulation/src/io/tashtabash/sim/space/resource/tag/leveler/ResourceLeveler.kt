package io.tashtabash.sim.space.resource.tag.leveler

import io.tashtabash.sim.space.resource.Genome


interface ResourceLeveler {
    fun getLevel(genome: Genome): Double
}
