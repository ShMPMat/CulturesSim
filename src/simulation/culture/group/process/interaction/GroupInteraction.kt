package simulation.culture.group.process.interaction

import simulation.culture.group.centers.Group
import simulation.culture.group.process.ProcessResult
import simulation.culture.thinking.meaning.makeMeme

interface GroupInteraction {
    val initiator: Group
    val participator: Group

    fun run(): ProcessResult
}

abstract class AbstractGroupInteraction(
        override val initiator: Group,
        override val participator: Group
) : GroupInteraction {
    protected abstract fun innerRun(): ProcessResult

    override fun run() = innerRun() + ProcessResult(makeMeme(participator))
}
