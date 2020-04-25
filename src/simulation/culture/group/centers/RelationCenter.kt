package simulation.culture.group.centers

import simulation.culture.group.GroupError
import simulation.culture.group.intergroup.Relation
import simulation.culture.group.request.Request
import simulation.culture.group.request.RequestPool
import simulation.space.resource.MutableResourcePack
import simulation.space.resource.ResourcePack
import simulation.space.tile.Tile
import simulation.space.tile.getDistance
import java.util.*

class RelationCenter(internal val hostilityCalculator: (Relation) -> Double) {
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

    fun updateRelations(groups: Collection<Group>, owner: Group) {
        updateNewConnections(groups, owner)
        updateRelations(owner)
    }

    private fun updateNewConnections(groups: Collection<Group>, owner: Group) {
        if (relations.map { it.owner }.any { it != owner }) throw GroupError("Incoherent owner for relations")
        for (group in groups) {
            if (!relationsMap.containsKey(group)) {
                val relation = Relation(owner, group)
                relation.pair = group.relationCenter.addMirrorRelation(relation)
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
                relation.positive = hostilityCalculator(relation)
            }
        }
        dead.forEach { relationsMap.remove(it) }
    }

    private fun addMirrorRelation(relation: Relation): Relation {
        val newRelation = Relation(relation.other, relation.owner)
        newRelation.pair = relation
        relationsMap[relation.owner] = newRelation
        return newRelation
    }

    fun requestTrade(pool: RequestPool) {
        for ((request, pack) in pool.requests)
            pack.addAll(trade(request, request.amountLeft(pack)))
    }

    fun trade(request: Request, amount: Int): ResourcePack {
        if (amount <= 0) return ResourcePack()
        val pack = MutableResourcePack()
        var amountLeft = amount
        for (relation in relations.sortedByDescending { it.positive }) {
            val given = relation.other.askFor(request.reducedAmountCopy(amountLeft), relation.owner)
            relation.positiveInteractions += (request.evaluator.evaluate(given) / amount)
            pack.addAll(given)
            amountLeft = amount - request.evaluator.evaluate(pack)
            if (amountLeft <= 0) break
        }
        return pack
    }
}