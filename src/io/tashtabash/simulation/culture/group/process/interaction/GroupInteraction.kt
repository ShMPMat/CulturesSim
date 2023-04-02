package io.tashtabash.simulation.culture.group.process.interaction

import io.tashtabash.simulation.culture.group.centers.Group
import io.tashtabash.simulation.culture.group.process.ProcessResult
import io.tashtabash.simulation.culture.thinking.meaning.makeMeme


interface GroupInteraction {
    val initiator: Group
    val participator: Group

    fun run(): ProcessResult
}

abstract class AbstractGroupInteraction(
        override val initiator: Group,
        override val participator: Group
) : GroupInteraction {
    protected abstract fun innerRun(): InteractionResult

    override fun run(): ProcessResult {
        val (initiatorResult, participatorResult) = innerRun()

        participator.processCenter.consumeProcessResult(
                participator,
                participatorResult + ProcessResult(makeMeme(initiator))
        )

        return initiatorResult + ProcessResult(makeMeme(participator))
    }
}

typealias InteractionResult = Pair<ProcessResult, ProcessResult>
