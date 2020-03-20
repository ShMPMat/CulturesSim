package simulation.culture.aspect

import simulation.space.resource.Resource
import kotlin.Boolean
import kotlin.NoSuchElementException
import kotlin.Pair
import kotlin.String

open class AspectPool(initialAspects: MutableSet<Aspect>) {
    protected val aspectMap = initialAspects.map {it.name}.zip(initialAspects).toMap().toMutableMap()

    protected val aspects: Set<Aspect>
        get() = aspectMap.values.toSet()

    val producedResources: List<Pair<Resource, ConverseWrapper>>
        get() = getConverseWrappers()
                .flatMap { w -> w.result.zip(List(w.result.size) { w }) }

    fun getConverseWrappers() = aspects.filterIsInstance<ConverseWrapper>()

    fun getMeaningAspects() = aspects
            .filterIsInstance<ConverseWrapper>()
            .filter { it.canReturnMeaning() }
            .toSet()

    fun get(name: String) = aspects.firstOrNull { it.name == name }
            ?: throw NoSuchElementException("No aspect with name $name")

    fun get(aspect: Aspect) = get(aspect.name)

    fun contains(aspect: Aspect) = aspectMap[aspect.name] != null

    fun filter(predicate: (Aspect) -> Boolean) = aspects
            .filter(predicate)

    fun getAll(): Set<Aspect> = aspects

    fun getResourceRequirements() = getConverseWrappers()
            .map { it.resource } .distinct()
}