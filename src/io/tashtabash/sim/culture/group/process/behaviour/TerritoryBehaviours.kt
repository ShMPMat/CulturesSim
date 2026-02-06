package io.tashtabash.sim.culture.group.process.behaviour

import io.tashtabash.random.singleton.chanceOfNot
import io.tashtabash.sim.culture.group.ClaimTileEvent
import io.tashtabash.sim.culture.group.centers.Group
import io.tashtabash.sim.culture.group.process.ProcessResult
import io.tashtabash.sim.event.Move
import io.tashtabash.sim.event.of


object ManageTerritoryB : AbstractGroupBehaviour() {
    override fun run(group: Group): ProcessResult {
        return if (group.populationCenter.isMinPassed(group.territoryCenter.territory))
            ExpandB.run(group)
        else
            LeaveTileB.run(group)
    }

    override val internalToString = "Expand or shrink the accessible territory"
}

object ExpandB : AbstractGroupBehaviour() {
    override fun run(group: Group): ProcessResult {
        group.territoryCenter.spreadAbility.chanceOfNot {
            return ProcessResult()
        }

        val tile = group.territoryCenter.territory.getMostUsefulTileOnOuterBrink(
            { group.territoryCenter.canSettleAndNoGroup(it) && group.territoryCenter.isTileReachable(it) },
            group.territoryCenter::tilePotentialMapper
        )
            ?: return ProcessResult()
        group.territoryCenter.claimTile(tile)

        return ProcessResult(ClaimTileEvent("Group ${group.name} claimed tile ${tile.posStr}", group, tile))
    }

    override val internalToString = "Expand the territory"
}

object LeaveTileB : AbstractGroupBehaviour() {
    override fun run(group: Group): ProcessResult {
        if (group.territoryCenter.territory.size <= 1)
            return ProcessResult()

        val tile = group.territoryCenter.territory.getMostUselessTile(group.territoryCenter::tilePotentialMapper)
            ?: return ProcessResult()
        group.territoryCenter.leaveTile(tile)

        return ProcessResult(Move of "Group ${group.name} left tile ${tile.posStr}")
    }

    override val internalToString = "Leave some territory"
}
