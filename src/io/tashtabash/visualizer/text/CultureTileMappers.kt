package io.tashtabash.visualizer.text

import io.tashtabash.sim.culture.aspect.hasMeaning
import io.tashtabash.sim.culture.group.GROUP_TAG_TYPE
import io.tashtabash.sim.culture.group.GroupConglomerate
import io.tashtabash.sim.culture.group.GroupTileTag
import io.tashtabash.sim.culture.group.centers.Group
import io.tashtabash.sim.culture.group.getResidingGroup
import io.tashtabash.sim.space.resource.ResourceType
import io.tashtabash.sim.space.tile.Tile
import io.tashtabash.visualizer.printinfo.ConglomeratePrintInfo


fun meaningfulResourcesMapper(tile: Tile) = predicateMapper(tile) { t ->
    t.resourcePack.any { it.hasMeaning }
}

fun artificialResourcesMapper(tile: Tile): String {
    val meaningful = meaningfulResourcesMapper(tile)
    val artificialTypes = setOf(ResourceType.Building, ResourceType.Artifact)
    return when {
        meaningful != NOTHING -> meaningful
        else -> predicateMapper(tile) { t -> t.resourcePack.any { it.genome.type in artificialTypes } }
    }
}

fun aspectMapper(aspectName: String, tile: Tile) = hotnessMapper(
        100,
        tile,
        {
            val group: Group = getResidingGroup(it)
                    ?: return@hotnessMapper 0
            group.cultureCenter.aspectCenter.aspectPool.all.firstOrNull { a -> a.name.contains(aspectName) }
                    ?.usefulness
                    ?: 0
        }
)

fun cultureAspectMapper(aspectName: String, tile: Tile) = hotnessMapper(
        1,
        tile,
        { t ->
            val group: Group = getResidingGroup(t)
                    ?: return@hotnessMapper 0
            group.cultureCenter.cultureAspectCenter.aspectPool.all
                    .filter { it.toString().contains(aspectName) }
                    .size
        }
)

fun strataMapper(strataSubstr: String, tile: Tile) = hotnessMapper(
        100,
        tile,
        { t ->
            val group: Group = getResidingGroup(t)
                    ?: return@hotnessMapper 0
            group.populationCenter.stratumCenter.strata
                    .filter { it.name.contains(strataSubstr) }
                    .map { it.population }
                    .foldRight(0, Int::plus)
        }
)

fun conglomerateReachMapper(conglomerate: GroupConglomerate, tile: Tile) = predicateMapper(tile) { t ->
    conglomerate.subgroups.any {
        it.territoryCenter
                .accessibleTerritory
                .contains(t)
    }
}

fun groupConglomerateMapper(groupConglomerate: GroupConglomerate, tile: Tile) =
        if (groupConglomerate.territory.contains(tile))
            when {
                tile.resourcePack.any { it.baseName.contains("House") } -> "\u001b[31m+"
                else -> MARK
            }
        else NOTHING

fun groupMapper(group: Group, tile: Tile) =
        if (group.territoryCenter.territory.contains(tile))
            when {
                tile.resourcePack.any { it.baseName.contains("House") } -> "\u001b[31m+"
                else -> MARK
            }
        else NOTHING

fun cultureTileMapper(lastClaimedTiles: Map<Group, Set<Tile>>, groupInfo: ConglomeratePrintInfo, tile: Tile) =
        if (tile.tagPool.getByType(GROUP_TAG_TYPE).isNotEmpty()) {
            val group = (tile.tagPool.getByType("Group")[0] as GroupTileTag).group
            val start = lastClaimedTiles[group]?.let {
                if (it.contains(tile)) "\u001b[31m"
                else "\u001b[96m\u001b[1m"
            } ?: "\u001b[96m\u001b[1m"
            start + groupInfo.getConglomerateSymbol(group.parentGroup)
        } else NOTHING
