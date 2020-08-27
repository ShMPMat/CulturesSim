package simulation.culture.group.centers

import extra.getTruncated
import simulation.culture.group.GroupConglomerate
import simulation.culture.group.GroupError
import simulation.culture.group.intergroup.Relation
import simulation.space.tile.Tile
import simulation.space.tile.getDistance
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

    fun getMinConglomerateRelation(conglomerate: GroupConglomerate) =
            getConglomerateGroups(conglomerate).map { it.normalized }.min() ?: 1.0

    fun getMaxConglomerateRelation(conglomerate: GroupConglomerate) =
            getConglomerateGroups(conglomerate).map { it.normalized }.max() ?: 1.0

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
        val dead: MutableList<Group> = ArrayList()
        for (relation in relationsMap.values) {
            if (relation.other.state == Group.State.Dead)
                dead.add(relation.other)
            else
                relation.positive = hostilityCalculator(relation)
        }
        dead.forEach { relationsMap.remove(it) }
    }

    private fun addMirrorRelation(relation: Relation): Relation {
        val newRelation = Relation(relation.other, relation.owner)
        newRelation.pair = relation
        relationsMap[relation.owner] = newRelation
        return newRelation
    }

    override fun toString() = relations
            .map { it.other.parentGroup }
            .distinct()
            .joinToString("\n") {
                "${it.name} average - ${getTruncated(getAvgConglomerateRelation(it), 4)}, " +
                        "min - ${getTruncated(getMinConglomerateRelation(it), 4)}, " +
                        "max - ${getTruncated(getMaxConglomerateRelation(it), 4)}\n"
            }
}
