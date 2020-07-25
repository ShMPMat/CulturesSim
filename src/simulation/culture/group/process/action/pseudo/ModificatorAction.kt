package simulation.culture.group.process.action.pseudo

import simulation.culture.group.process.action.GroupAction


class ActionSequence(val sequence: List<GroupAction>) : AbstractGroupPseudoAction() {
    override fun run() = sequence.forEach { it.run() }

    override val internalToString = "Following happened: " +
            if (sequence.isNotEmpty())
                sequence.joinToString()
            else "nothing"
}
