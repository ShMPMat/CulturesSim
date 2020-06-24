package simulation.space.resource.instantiation

import simulation.SimulationException
import simulation.space.resource.dependency.AvoidDependency
import simulation.space.resource.dependency.ConsumeDependency
import simulation.space.resource.dependency.NeedDependency
import simulation.space.resource.dependency.ResourceDependency
import simulation.space.resource.tag.labeler.makeResourceLabeler

interface DependencyParser {
    fun parse(tag: String): ResourceDependency?
}

open class DefaultDependencyParser : DependencyParser {
    override fun parse(tag: String): ResourceDependency? {
        val elements = tag.split(";".toRegex()).toTypedArray()
        return when (elements[4]) {
            "CONSUME" -> ConsumeDependency(
                    elements[2].toDouble(),
                    elements[3] == "1",
                    elements[1].toDouble(),
                    makeResourceLabeler(elements[0].split(",".toRegex()))
            )
            "AVOID" -> AvoidDependency(
                    elements[1].toDouble(),
                    elements[2].toDouble(),
                    elements[3] == "1",
                    makeResourceLabeler(elements[0].split(",".toRegex()))
            )
            "EXIST" -> NeedDependency(
                    elements[1].toDouble(),
                    elements[2].toDouble(),
                    elements[3] == "1",
                    makeResourceLabeler(elements[0].split(",".toRegex()))
            )
            else -> null
        }
    }
}