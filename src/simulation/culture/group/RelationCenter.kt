package simulation.culture.group

import simulation.culture.group.intergroup.Relation
import java.util.*

class RelationCenter(internal val hostilityCalculator: (Group) -> Double) {
    private val relationsMap: MutableMap<Group, Relation> = HashMap()

    val relatedGroups: Set<Group>
        get() = relationsMap.keys

    val relations: Collection<Relation>
        get() = relationsMap.values

    fun getNormalizedRelation(group: Group): Double {
        return if (relationsMap.containsKey(group)) relationsMap[group]?.positiveNormalized ?: 0.0
        else 2.0
    }

    fun addMirrorRelation(relation: Relation): Relation {
        val newRelation = Relation(relation.other, relation.owner)
        newRelation.setPair(relation)
        relationsMap[relation.owner] = newRelation
        return newRelation
    }

    fun updateNewConnections(groups: Collection<Group>, owner: Group) {
        if (relations.map { it.owner }.any { it != owner })  throw GroupException("Incoherent owner for relations")
        for (group in groups) {
            if (!relationsMap.containsKey(group)) {
                val relation = Relation(owner, group)
                relation.setPair(group.relationCenter.addMirrorRelation(relation))
                relationsMap[relation.other] = relation
            }
        }
    }

    fun updateRelations() {
        val dead: MutableList<Group> = ArrayList()
        for (relation in relationsMap.values) {
            if (relation.other.state == Group.State.Dead) {
                dead.add(relation.other)
            } else {
                relation.positive = hostilityCalculator(relation.other)
            }
        }
        dead.forEach { relationsMap.remove(it) }
    }

}