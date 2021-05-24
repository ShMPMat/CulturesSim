package shmp.simulation.space.resource

sealed class Taker {
    data class ResourceTaker(val resource: Resource): Taker()

    object WindTaker: Taker()

    object DeathTaker: Taker()

    object SeparationTaker: Taker()

    object SelfTaker: Taker()
}
