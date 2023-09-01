package io.tashtabash.sim.space.resource.dependency


abstract class Temperature(
        var threshold: Int,
        deprivationCoefficient: Double
) : CoefficientDependency(deprivationCoefficient) {
    override val isNecessary = true

    override val isPositive = true

    override val isResourceNeeded = false
}
