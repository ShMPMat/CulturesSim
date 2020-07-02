package simulation.culture.group.centers

import extra.getTruncated
import simulation.culture.group.GroupConglomerate
import simulation.culture.group.GroupError
import simulation.culture.group.intergroup.Relation
import simulation.culture.group.request.Request
import simulation.culture.group.request.RequestPool
import simulation.space.resource.container.MutableResourcePack
import simulation.space.resource.container.ResourcePack
import simulation.space.tile.Tile
import simulation.space.tile.getDistance
import java.util.*
import kotlin.math.ceil
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
        return if (result.isNaN()) 1.0 else result
    }

    fun getMinConglomerateRelation(conglomerate: GroupConglomerate) =
            getConglomerateGroups(conglomerate).map { it.normalized }.min() ?: 1.0

    fun getMaxConglomerateRelation(conglomerate: GroupConglomerate) =
            getConglomerateGroups(conglomerate).map { it.normalized }.max() ?: 1.0

    fun getNormalizedRelation(group: Group) =
            if (relationsMap.containsKey(group)) relationsMap[group]?.normalized ?: 0.0
            else 1.0

    fun evaluateTile(tile: Tile) = relations.map {
        (it.positive * evaluationFactor / getDistance(tile, it.other.territoryCenter.territory.center)).toInt()
    }.fold(0, Int::plus)

    fun updateRelations(groups: Collection<Group>, owner: Group) {
        updateNewConnections(groups, owner)
        updateRelations()
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

    fun requestTrade(pool: RequestPool) {
        for ((request, pack) in pool.requests)
            pack.addAll(trade(request, request.amountLeft(pack)))
    }

    fun trade(request: Request, amount: Double): ResourcePack {
        if (amount <= 0) return ResourcePack()
        val pack = MutableResourcePack()
        var amountLeft = amount
        for (relation in relations.sortedByDescending { it.positive }) {
            val given = relation.other.askFor(request.reducedAmountCopy(amountLeft), relation.owner)
            relation.positiveInteractions += (request.evaluator.evaluate(given) / amount).toInt()
            pack.addAll(given)
            amountLeft = amount - request.evaluator.evaluate(pack)
            if (amountLeft <= 0) break
        }
        return pack
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
