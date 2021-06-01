package shmp.simulation.culture.group.reason

import shmp.random.randomElementOrNull
import shmp.simulation.culture.aspect.ConverseWrapper
import shmp.simulation.culture.group.centers.Group
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

        reason = BetterAspectUseReason(converseWrapper)
        i++
    } while (i <= tries && exceptions.contains(reason))

    return if (exceptions.contains(reason)) null else reason
}
