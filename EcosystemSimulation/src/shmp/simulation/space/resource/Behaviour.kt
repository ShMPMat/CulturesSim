package shmp.simulation.space.resource


data class Behaviour(var resistance: Double, var danger: Double, val camouflage: Double, val overflowType: OverflowType) {
    val isResisting
        get() = resistance > 0.0
}
