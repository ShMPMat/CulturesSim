package io.tashtabash.sim.culture.group.centers

import io.tashtabash.sim.culture.group.GroupConglomerate
import io.tashtabash.sim.culture.group.intergroup.OutgoingRelation
import io.tashtabash.sim.space.tile.Tile
import io.tashtabash.sim.space.tile.getDistance
import java.util.*
import kotlin.math.max


class RelationCenter(internal val hostilityCalculator: (Group, OutgoingRelation) -> Double) {
    private val relationsMap: MutableMap<Group, OutgoingRelation> = HashMap()
    private val evaluationFactor = 10_000

    val relatedGroups: Set<Group>
        get() = relationsMap.keys

    val relations: Collection<OutgoingRelation>
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

    fun getRelationValue(group: Group) = relationsMap[group]?.value ?: 0.0

    fun getRelation(group: Group) = relationsMap[group]

    fun getRelationOrCreate(group: Group, owner: Group): OutgoingRelation {
        relationsMap[group]?.let {
            return it
        }

        val relation = OutgoingRelation(group)
        addRelation(relation, owner)
        return relation
    }

    fun evaluateTile(tile: Tile) = relations.map {
        (it.value * evaluationFactor / getDistance(tile, it.other.territoryCenter.center)).toInt()
    }.fold(0, Int::plus)

    internal fun updateRelations(groups: Collection<Group>, owner: Group) {
        updateNewConnections(groups, owner)
        updateRelations(owner)
    }

    private fun updateNewConnections(groups: Collection<Group>, owner: Group) {
        for (group in groups)
            if (!relationsMap.containsKey(group))
                addRelation(OutgoingRelation(group), owner)
    }

    fun addRelation(relation: OutgoingRelation, owner: Group) {
        relation.other.relationCenter.addMirrorRelation(relation, owner)
        relationsMap[relation.other] = relation
    }

    private fun updateRelations(owner: Group) {
        val dead = mutableListOf<Group>()
        for (relation in relationsMap.values) {
            if (relation.other.state == Group.State.Dead)
                dead += relation.other
            else
                relation.value = hostilityCalculator(owner, relation)
        }
        for (it in dead)
            relationsMap.remove(it)
    }

    private fun addMirrorRelation(relation: OutgoingRelation, group: Group) {
        val newRelation = OutgoingRelation(group, relation.value, relation.positiveInteractions)
        relationsMap[group] = newRelation
    }

    override fun toString() =
        relations.joinToString("\n")
}
