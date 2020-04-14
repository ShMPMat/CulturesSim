package simulation.culture.group.centers

import simulation.culture.group.GroupError
import simulation.culture.group.intergroup.Relation
import simulation.space.tile.Tile
import simulation.space.tile.getDistance
import java.util.*

class RelationCenter(internal val hostilityCalculator: (Group, Group) -> Double) {
    private val relationsMap: MutableMap<Group, Relation> = HashMap()
    private val evaluationFactor = 10_000

    val relatedGroups: Set<Group>
        get() = relationsMap.keys

    val relations: Collection<Relation>
        get() = relationsMap.values

    fun getNormalizedRelation(group: Group): Double {
        return if (relationsMap.containsKey(group)) relationsMap[group]?.positiveNormalized ?: 0.0
        else 2.0
    }

    fun evaluateTile(tile: Tile) = relations.map {
        (it.positive * evaluationFactor / getDistance(tile, it.other.territoryCenter.territory.center)).toInt()
    }.fold(0, Int::plus)

    fun update(groups: Collection<Group>, owner: Group) {
        updateNewConnections(groups, owner)
        updateRelations(owner)
    }

    private fun updateNewConnections(groups: Collection<Group>, owner: Group) {
        if (relations.map { it.owner }.any { it != owner }) throw GroupError("Incoherent owner for relations")
        for (group in groups) {
            if (!relationsMap.containsKey(group)) {
                val relation = Relation(owner, group)
                relation.setPair(group.relationCenter.addMirrorRelation(relation))
                relationsMap[relation.other] = relation
            }
        }
    }

    private fun updateRelations(owner: Group) {
        val dead: MutableList<Group> = ArrayList()
        for (relation in relationsMap.values) {
            if (relation.other.state == Group.State.Dead) {
                dead.add(relation.other)
            } else {
                relation.positive = hostilityCalculator(owner, relation.other)
            }
        }
        dead.forEach { relationsMap.remove(it) }
    }

    private fun addMirrorRelation(relation: Relation): Relation {
        val newRelation = Relation(relation.other, relation.owner)
        newRelation.setPair(relation)
        relationsMap[relation.owner] = newRelation
        return newRelation
    }
}