package io.tashtabash.sim.culture.group.process.interaction

import io.tashtabash.sim.culture.group.centers.Group
import io.tashtabash.sim.culture.group.process.ProcessResult
import io.tashtabash.sim.culture.thinking.meaning.makeMeme


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

    // Passes the initiator result and returns the participator result
    fun reverseRun(): ProcessResult {
        val (initiatorResult, participatorResult) = innerRun()

        initiator.processCenter.consumeProcessResult(
            initiator,
            initiatorResult + ProcessResult(makeMeme(participator))
        )

        return participatorResult + ProcessResult(makeMeme(initiator))

    }
}

typealias InteractionResult = Pair<ProcessResult, ProcessResult>
