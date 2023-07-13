package io.tashtabash.simulation.culture.group.reason

import io.tashtabash.simulation.culture.aspect.ConverseWrapper


data class BetterAspectUseReason(val converseWrapper: ConverseWrapper) : Reason {
    override fun toString() =
            "Better " + converseWrapper.name
}
