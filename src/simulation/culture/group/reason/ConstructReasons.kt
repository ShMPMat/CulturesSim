package simulation.culture.group.reason

import shmp.random.randomElement
import shmp.random.randomElementWithProbability
import simulation.culture.aspect.ConverseWrapper
import simulation.culture.group.Group
import kotlin.random.Random

fun randomReason(group: Group, random: Random): Reason? {
    var reason: Reason? = null
    when (random.nextInt(1)) {
        0 -> {
            val wrappers = group.cultureCenter.aspectCenter.aspectPool.getConverseWrappers()
            if (wrappers.isNotEmpty()) {
                val converseWrapper = randomElement(
                        wrappers,
                        random
                )
                reason = BetterAspectUseReason(
                        group,
                        converseWrapper
                )
            }
        }
    }
    return reason
}

fun constructBetterAspectUseReason(
        group: Group,
        converseWrappers: Collection<ConverseWrapper>,
        exceptions: Collection<Reason>,
        random: Random,
        tries: Int = 5
): Reason? {
    var converseWrapper: ConverseWrapper?
    var reason: Reason
    var i = 0
    do {
        if (converseWrappers.isEmpty()) return null
        converseWrapper = randomElementWithProbability(
                converseWrappers,
                { it.usefulness.toDouble().coerceAtLeast(1.0) },
                random
        )
        reason = BetterAspectUseReason(group, converseWrapper)
        i++
    } while (i <= tries && exceptions.contains(reason))
    return if (exceptions.contains(reason)) null else reason
}