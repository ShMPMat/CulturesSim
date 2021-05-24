package shmp.simulation.culture.group.stratum

import shmp.simulation.space.SpaceData
import shmp.simulation.space.resource.*
import shmp.simulation.space.resource.action.ConversionCore


class Person(ownershipMarker: OwnershipMarker): Resource(
        ResourceCore(
                Genome(
                        "Person",
                        ResourceType.Animal,
                        1.6,
                        0.0,
                        0,
                        false,
                        true,
                        Behaviour(true, OverflowType.Ignore),
                        Appearance(null),
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
                ownershipMarker = ownershipMarker
        )
)
