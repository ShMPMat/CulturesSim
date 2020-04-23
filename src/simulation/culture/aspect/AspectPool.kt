package simulation.culture.aspect

import simulation.space.resource.Resource
import kotlin.Boolean
import kotlin.NoSuchElementException
import kotlin.Pair
import kotlin.String

open class AspectPool(initialAspects: MutableSet<Aspect>) {
    protected val aspectMap = initialAspects.map { it.name }.zip(initialAspects).toMap().toMutableMap()
    private val _converseWrappers = aspects.filterIsInstance<ConverseWrapper>().toMutableList()
    private val _converseWrappersRequirements = _converseWrappers.map { it.resource }.toMutableSet()

    protected val aspects: Set<Aspect>
        get() = aspectMap.values.toSet()

    val producedResources: List<Pair<Resource, ConverseWrapper>>
        get() = converseWrappers
                .flatMap { w ->
                    val result = w.producedResources
                    result.zip(List(result.size) { w })
                }

    val converseWrappers : List<ConverseWrapper> get() = _converseWrappers

    protected fun innerAdd(aspect: Aspect) {
        if (!aspectMap.containsKey(aspect.name) && aspect is ConverseWrapper) {
            _converseWrappers.add(aspect)
            _converseWrappersRequirements.add(aspect.resource)
        }
        aspectMap.set(aspect.name, aspect)
    }

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

    fun getResourceRequirements(): Set<Resource> = _converseWrappersRequirements
}