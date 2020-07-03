package simulation.space.resource.tag.labeler

import simulation.space.resource.Genome

object PassingLabeler : ResourceLabeler {
    override fun isSuitable(genome: Genome) = true

    override fun toString() = "Any Resource"
}
