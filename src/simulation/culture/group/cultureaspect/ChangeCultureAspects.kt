package simulation.culture.group.cultureaspect

import simulation.culture.group.Group

fun takeOutSimilarRituals(aspectPool: MutableCultureAspectPool, group: Group): RitualSystem? {
    val (popularReason, popularReasonRituals) = aspectPool
            .filter { it is Ritual }
            .map { it as Ritual }
            .groupBy { it.reason }
            .maxBy { it.value.size }
            ?: return null
    if (popularReasonRituals.size >= 3) {
        aspectPool.removeAll(popularReasonRituals)
        return RitualSystem(group, popularReasonRituals, popularReason)
    }
    return null
}