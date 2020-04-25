package simulation.culture.group

import simulation.culture.group.centers.Group
import simulation.culture.group.centers.PopulationCenter
import simulation.space.Territory
import simulation.space.resource.MutableResourcePack

class CultStratum(val cultName: String) : Stratum {
    override var population: Int = 1

    override fun decreaseAmount(amount: Int) {
        population -= amount
    }

    override fun update(
            accessibleResources: MutableResourcePack,
            accessibleTerritory: Territory,
            group: Group
    ) {

    }

    override fun finishUpdate(group: Group) {}

    override fun toString(): String {
        return "Stratum for $cultName, population - $population"
    }
}
