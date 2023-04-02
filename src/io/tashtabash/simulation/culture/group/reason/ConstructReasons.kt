package io.tashtabash.simulation.culture.group.reason

import io.tashtabash.random.randomElementOrNull
import io.tashtabash.simulation.culture.aspect.ConverseWrapper
import kotlin.random.Random


fun constructBetterAspectUseReason(
        converseWrappers: List<ConverseWrapper>,
        exceptions: Collection<Reason>,
        random: Random,
        tries: Int = 5
): Reason? {
    var converseWrapper: ConverseWrapper?
    var reason: Reason
    var i = 0

    do {
        converseWrapper = randomElementOrNull(
                converseWrappers,
                { it.usefulness.toDouble().coerceAtLeast(1.0) },
                random
        ) ?: return null

        reason = io.tashtabash.simulation.culture.group.reason.BetterAspectUseReason(converseWrapper)
        i++
    } while (i <= tries && exceptions.contains(reason))

    return if (exceptions.contains(reason)) null else reason
}
