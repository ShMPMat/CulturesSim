package io.tashtabash.sim.culture.group.stratum

import io.tashtabash.sim.Controller
import io.tashtabash.sim.event.PopulationDecrease
import io.tashtabash.sim.event.of
import io.tashtabash.sim.space.SpaceData
import io.tashtabash.sim.space.resource.*
import java.lang.Integer.min


private val personGenome = Genome(
    "Person",
    ResourceType.Animal,
    Size(1.5, .4, .2) to Size(1.6, .5, .3),
    .0,
    0,
    false,
    true,
    Behaviour(.1, .05, .25, 1.0, OverflowType.Ignore),
    Appearance(null, null, null),
    false,
    Lifespan.Years(50.0),
    50,
    null,
    emptyList(),
    emptySet(),
    SpaceData.data.materialPool.get("Meat"),
)


class Person(ownershipMarker: OwnershipMarker) : Resource(
    ResourceCore(
        personGenome,
        ownershipMarker = ownershipMarker,
        resourceBuilder = { c, a ->
            Person(c.ownershipMarker).apply {
                amount = a
            }
        }
    )
) {
    override fun getPartInt(part: Int, taker: Taker) = super.getPartInt(part, taker).also {
        logGetPart(it, taker)
    }

    override fun getCleanPartInt(part: Int, taker: Taker?) = super.getCleanPartInt(part, taker).also {
        logGetPart(it, taker)
    }

    private fun logGetPart(result: Int, taker: Taker?) {
        if (result == 0 || taker == Taker.SelfTaker || taker == null)
            return

        val oldPopulation = amount + result
        Controller.session.world.events.add(
            PopulationDecrease of "$ownershipMarker population of $oldPopulation decreased by $result: taken by $taker"
        )
    }

    override fun copy(amount: Int, deathTurn: Int) = Person(ownershipMarker).apply {
        this.amount = amount
        this.deathTurn = deathTurn
    }

    override fun naturalDeath(tickSeconds: Long): List<Resource> {
        val toDie = calculateDeadAmount(tickSeconds).toInt()
        if (toDie > 0) {
            val deadAmount = min(amount, (toDie / genome.computeLifespan(tickSeconds)).toInt() + 1)

            takers += Taker.DeathTaker to deadAmount
            amount -= deadAmount
            deathTurn = 0
            deathOverhead = .0
            return applyActionOrEmpty(specialActions.getValue("_OnDeath_"), deadAmount)
        }

        return emptyList()
    }
}
