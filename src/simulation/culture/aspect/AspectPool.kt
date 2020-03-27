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
        get() = converseWrappers
                .flatMap { w -> w.result.zip(List(w.result.size) { w }) }

    val converseWrappers get() = aspects.filterIsInstance<ConverseWrapper>()

    fun getMeaningAspects() = aspects
            .filterIsInstance<ConverseWrapper>()
            .filter { it.canReturnMeaning() }
            .toSet()

    fun get(name: String) = aspects.firstOrNull { it.name == name }

    fun get(aspect: Aspect) = get(aspect.name)

    fun getValue(name: String) = get(name)
            ?: throw NoSuchElementException("No aspect with name $name")

    fun getValue(aspect: Aspect) = getValue(aspect.name)

    fun contains(aspectName: String) = aspectMap[aspectName] != null

    fun contains(aspect: Aspect) = contains(aspect.name)

    fun filter(predicate: (Aspect) -> Boolean) = aspects
            .filter(predicate)

    fun getAll(): Set<Aspect> = aspects

    fun getResourceRequirements() = converseWrappers
            .map { it.resource } .distinct()
}