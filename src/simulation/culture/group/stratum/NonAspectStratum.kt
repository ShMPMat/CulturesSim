package simulation.culture.group.stratum

import simulation.culture.group.centers.Group
import simulation.space.Territory
import simulation.space.resource.container.MutableResourcePack
import simulation.space.tile.Tile

abstract class NonAspectStratum(tile: Tile, name: String) : BaseStratum(tile, name) {
    override var population: Int = 0
        protected set

    protected var usedThisTurn = false
    protected var unusedTurns = 0
    protected var defaultImportance = 0
    override var importance: Int
        get() = defaultImportance + population - unusedTurns
        set(value) {
            defaultImportance += value - importance
            usedThisTurn = true
        }

    override fun decreaseAmount(amount: Int) {
        population -= amount
    }

    override fun update(accessibleResources: MutableResourcePack, accessibleTerritory: Territory, group: Group) {
        if (population < defaultImportance && group.populationCenter.freePopulation > 0)
            population++

        if (population == 0) return

        super.update(accessibleResources, accessibleTerritory, group)
        ego.update(accessibleResources, accessibleTerritory, group, this)
    }

    override fun finishUpdate(group: Group) {
        if (usedThisTurn) unusedTurns = 0
        else unusedTurns++
        usedThisTurn = false
    }

    override fun die() {
        population = 0
    }


}