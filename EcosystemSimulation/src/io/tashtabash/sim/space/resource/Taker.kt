package io.tashtabash.sim.space.resource

sealed class Taker {
    data class ResourceTaker(val resource: Resource): Taker() {
        override fun toString() = "Resource ${resource.fullName}"
    }

    object WindTaker: Taker()

    object DeathTaker: Taker()

    object SeparationTaker: Taker()

    object CataclysmTaker: Taker()

    object SelfTaker: Taker()

    override fun toString() = javaClass.simpleName.takeWhile { it != 'T' }
}
