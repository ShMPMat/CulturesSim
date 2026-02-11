package io.tashtabash.sim.culture.group.centers

import io.tashtabash.sim.culture.group.GroupConglomerate
import io.tashtabash.sim.culture.group.GroupError
import io.tashtabash.sim.culture.group.intergroup.Relation
import io.tashtabash.sim.space.tile.Tile
import io.tashtabash.sim.space.tile.getDistance
import java.util.*
import kotlin.math.max


class RelationCenter(internal val hostilityCalculator: (Relation) -> Double) {
    private val relationsMap: MutableMap<Group, Relation> = HashMap()
    private val evaluationFactor = 10_000

    val relatedGroups: Set<Group>
        get() = relationsMap.keys

    val relations: Collection<Relation>
        get() = relationsMap.values

    fun getConglomerateGroups(conglomerate: GroupConglomerate) =
            relations.filter { it.other.parentGroup == conglomerate }

    fun getAvgConglomerateRelation(conglomerate: GroupConglomerate): Double {
        val result = max(
                getConglomerateGroups(conglomerate).map { it.normalized }.average(),
                0.0000001
        )
        return if (result.isNaN()) 1.0
        else result
    }

    fun getNormalizedRelation(group: Group) = relationsMap[group]?.normalized ?: 0.5

    fun getRelation(group: Group) = relationsMap[group]

    fun evaluateTile(tile: Tile) = relations.map {
        (it.positive * evaluationFactor / getDistance(tile, it.other.territoryCenter.center)).toInt()
    }.fold(0, Int::plus)

    internal fun updateRelations(groups: Collection<Group>, owner: Group) {
        updateNewConnections(groups, owner)
        updateRelations()
    }

    private fun updateNewConnections(groups: Collection<Group>, owner: Group) {
        if (relations.map { it.owner }.any { it != owner })
            throw GroupError("Incoherent owner for relations")

        for (group in groups)
            if (!relationsMap.containsKey(group)) {
                val relation = Relation(owner, group)
                relation.pair = group.relationCenter.addMirrorRelation(relation)
                relationsMap[relation.other] = relation
            }
    }

    private fun updateRelations() {
        val dead = mutableListOf<Group>()
        for (relation in relationsMap.values) {
            if (relation.other.state == Group.State.Dead)
                dead += relation.other
            else
                relation.positive = hostilityCalculator(relation)
        }
        for (it in dead)
            relationsMap.remove(it)
    }

    private fun addMirrorRelation(relation: Relation): Relation {
        val newRelation = Relation(relation.other, relation.owner)
        newRelation.pair = relation
        relationsMap[relation.owner] = newRelation
        return newRelation
    }

    override fun toString() =
        relations.joinToString("\n")
}
