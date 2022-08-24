package shmp.simulation.space.resource.instantiation

import shmp.simulation.space.resource.dependency.AvoidDependency
import shmp.simulation.space.resource.dependency.ConsumeDependency
import shmp.simulation.space.resource.dependency.NeedDependency
import shmp.simulation.space.resource.dependency.ResourceDependency
import shmp.simulation.space.resource.tag.labeler.QuantifiedResourceLabeler
import shmp.simulation.space.resource.tag.labeler.makeResourceLabeler


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
                    QuantifiedResourceLabeler(makeResourceLabeler(elements[0]), elements[1].toDouble()),
                    elements.getOrNull(5)?.toInt() ?: 1
            )
            "AVOID" -> AvoidDependency(
                    elements[2].toDouble(),
                    elements[3] == "1",
                    QuantifiedResourceLabeler(makeResourceLabeler(elements[0]), elements[1].toDouble())
            )
            "EXIST" -> NeedDependency(
                    elements[2].toDouble(),
                    elements[3] == "1",
                    QuantifiedResourceLabeler(makeResourceLabeler(elements[0]), elements[1].toDouble())
            )
            else -> null
        }
    }
}
