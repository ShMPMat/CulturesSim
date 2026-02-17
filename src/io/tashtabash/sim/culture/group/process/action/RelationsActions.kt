package io.tashtabash.sim.culture.group.process.action

import io.tashtabash.random.singleton.testProbability
import io.tashtabash.sim.culture.group.centers.Group
import io.tashtabash.sim.culture.group.centers.Trait
import io.tashtabash.sim.culture.group.process.get
import io.tashtabash.sim.culture.group.process.interaction.ChangeRelationsI
import io.tashtabash.sim.culture.group.process.plus


class ChangeRelationsA(
    group: Group,
    private val target: Group,
    private val delta: Double
) : AbstractGroupAction(group) {
    override fun run() {
        changeRelations(group, delta)

        if (target.parentGroup != group.parentGroup)
            group.parentGroup.subgroups
                .filter { it != group }
                .forEach { changeRelations(it, delta / 3) }
    }

    private fun changeRelations(currentGroup: Group, currentDelta: Double) {
        currentGroup.relationCenter.getRelationOrCreate(target, currentGroup)
            .positiveInteractions += currentDelta
    }

    override val internalToString = "Change relations of ${group.name} and ${target.name} on $delta"
}

class CooperateA(
    group: Group,
    private val target: Group,
    private val helpAmount: Double //range - 0-1
) : AbstractGroupAction(group) {
    override fun run(): Boolean {
        val relations = group.relationCenter.getRelationValue(target) * 0.95
        // + is used to make the help almost guaranteed on good relations, but * 0.95 leaves some probability to reject
        val probability = (1 - helpAmount) + relations
        val answer = probability.testProbability() && Trait.Peace.get() + relations testOn group

        val relationsChange = 1.0 * if (answer) 1 else -1
        ChangeRelationsI(group, target, relationsChange).run()

        return answer
    }

    override val internalToString =
        "Let ${group.name} decide whether to cooperate with ${target.name}, help amount - $helpAmount"
}
