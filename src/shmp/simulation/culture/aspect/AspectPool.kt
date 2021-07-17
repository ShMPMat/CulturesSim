package shmp.simulation.culture.aspect

import shmp.simulation.culture.aspect.dependency.AspectDependency
import shmp.simulation.culture.aspect.dependency.LineDependency
import shmp.simulation.culture.group.GroupError
import shmp.simulation.space.resource.Resource
import kotlin.Boolean
import kotlin.NoSuchElementException
import kotlin.String

open class AspectPool(initialAspects: MutableSet<Aspect>) {
    protected val aspectMap = initialAspects.map { it.name to it }.toMap().toMutableMap()
    private val _cws = aspects.filterIsInstance<ConverseWrapper>().toMutableSet()
    private val _cwRequirements = _cws
            .map { it.resource }
            .groupBy { it }
            .map { it.key to it.value.size }
            .toMap().toMutableMap()

    protected val aspects: Set<Aspect>
        get() = aspectMap.values.toSet()

    val producedResources: List<Resource>
        get() = converseWrappers.flatMap { it.producedResources }

    val converseWrappers: Set<ConverseWrapper> get() = _cws

    protected fun innerAdd(aspect: Aspect) {
        if (!aspectMap.containsKey(aspect.name) && aspect is ConverseWrapper) {
            _cws.add(aspect)
            _cwRequirements[aspect.resource] = (_cwRequirements[aspect.resource] ?: 0) + 1
        }
        aspectMap[aspect.name] = aspect
    }

    protected fun innerRemove(aspect: Aspect) : Boolean {
        if (aspects.any { it is ConverseWrapper && it.aspect == aspect })
            throw GroupError("Cannot remove aspect ${aspect.name} while there are ConverseWrappers with it")
        val innerAspect = aspectMap.remove(aspect.name)
                ?: return false

        if (innerAspect is ConverseWrapper) {
            _cws.remove(innerAspect)
            if (_cwRequirements[innerAspect.resource] == null) {
                val h = 0
            }
            if (_cwRequirements[innerAspect.resource] == 1)
                _cwRequirements.remove(innerAspect.resource)
            else
                _cwRequirements[innerAspect.resource] =
                        _cwRequirements.getValue(innerAspect.resource) - 1
        }
        deleteDependencyOnAspect(innerAspect)
        return true
    }

    fun deleteDependencyOnAspect(aspect: Aspect) {
        aspects.forEach {a ->
            a.dependencies.removeIf { it is LineDependency && it.converseWrapper == aspect
                    || it is AspectDependency && it.aspect == aspect}
        }
    }

    fun getMeaningAspects() = aspects
            .filterIsInstance<ConverseWrapper>()
            .filter { it.canReturnMeaning() }
            .distinct()

    fun get(name: String) = aspects.firstOrNull { it.name == name }

    fun get(aspect: Aspect) = get(aspect.name)

    fun getValue(name: String) = get(name)
            ?: throw NoSuchElementException("No aspect with name $name")

    fun getValue(aspect: Aspect) = getValue(aspect.name)

    fun contains(aspectName: String) = aspectMap[aspectName] != null

    fun contains(aspect: Aspect) = contains(aspect.name)

    fun filter(predicate: (Aspect) -> Boolean) = aspects
            .filter(predicate)

    val all: Set<Aspect>
        get() = aspects

    fun getResourceRequirements(): Set<Resource> = _cwRequirements.keys
}
