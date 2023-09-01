package io.tashtabash.sim.culture.group.reason

import io.tashtabash.sim.culture.aspect.ConverseWrapper


data class BetterAspectUseReason(val converseWrapper: ConverseWrapper) : Reason {
    override fun toString() =
            "Better " + converseWrapper.name
}
