package shmp.simulation.culture.group.stratum

import shmp.simulation.culture.aspect.Aspect
import shmp.simulation.culture.group.centers.Group
import shmp.simulation.space.territory.Territory
import shmp.simulation.space.resource.container.MutableResourcePack
import shmp.simulation.space.tile.Tile


abstract class NonAspectStratum(tile: Tile, baseName: String, postfix: String) : BaseStratum(tile, baseName, postfix) {
    override var population: Int = 0
        internal set

    protected var aspect: Aspect? = null

    private var gainedImportance = 0

    protected var usedThisTurn = false
    protected var unusedTurns = 0
    protected var defaultImportance = 0
    override var importance: Int
        get() = defaultImportance + population - unusedTurns
        set(value) {
            val diff = value - importance
            defaultImportance += diff
            usedThisTurn = true

            gainedImportance += diff
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

        manageAspect(group)
    }

    private fun manageAspect(group: Group) {
        aspect?.let {
            if (gainedImportance > 0) {
                val actualAspect = group.cultureCenter.aspectCenter.aspectPool.get(it)

                if (actualAspect == null)
                    group.cultureCenter.aspectCenter.addAspect(it, group)
                else
                    aspect = actualAspect

                aspect?.gainUsefulness(gainedImportance)
            }
        }

        gainedImportance = 0
    }

    override fun die() {
        population = 0
    }
}
