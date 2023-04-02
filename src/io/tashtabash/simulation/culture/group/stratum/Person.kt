package io.tashtabash.simulation.culture.group.stratum

import io.tashtabash.simulation.space.SpaceData
import io.tashtabash.simulation.space.resource.*
import io.tashtabash.simulation.space.resource.action.ConversionCore


class Person(ownershipMarker: OwnershipMarker) : Resource(
        ResourceCore(
                Genome(
                        "Person",
                        ResourceType.Animal,
                        1.6 to 1.6,
                        0.0,
                        0,
                        false,
                        true,
                        Behaviour(0.1, 0.05, 0.25, OverflowType.Ignore),
                        Appearance(null, null, null),
                        false,
                        false,
                        50.0,
                        50,
                        null,
                        emptyList(),
                        emptySet(),
                        SpaceData.data.materialPool.get("Meat"),
                        emptyList(),
                        ConversionCore(mapOf())
                ),
                ownershipMarker = ownershipMarker,
                resourceBuilder = { c, a ->
                    Person(c.ownershipMarker).apply {
                        amount = a
                    }
                }
        )
) {
    private var toDie = 0

    override fun naturalDeath(): List<Resource> {
        if (toDie > 0) {
            val deadAmount = (toDie / genome.lifespan).toInt() + 1
            toDie -= deadAmount

            takers.add(Taker.DeathTaker to deadAmount)
            amount -= deadAmount
            deathTurn = 0
            deathOverhead = 0
            deathPart = 1.0
            return applyActionOrEmpty(specialActions.getValue("_OnDeath_"), deadAmount)
        }

        if (deathTurn + deathOverhead < genome.lifespan)
            return emptyList()

        toDie = (deathPart * amount).toInt()
        return if (toDie != 0) naturalDeath() else emptyList()
    }
}
