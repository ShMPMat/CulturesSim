package io.tashtabash.sim.culture.group.process.behaviour

import io.tashtabash.generator.culture.worldview.toMeme
import io.tashtabash.sim.culture.group.centers.Group
import io.tashtabash.sim.culture.group.centers.util.toCherishedResource
import io.tashtabash.sim.culture.group.centers.util.toConcept
import io.tashtabash.sim.culture.group.process.ProcessResult
import io.tashtabash.sim.event.CultureAspectGaining
import io.tashtabash.sim.event.of


object UpdateReasoningsB : AbstractGroupBehaviour() {
    override fun run(group: Group): ProcessResult {
        val newReasonings = group.cultureCenter
            .cultureAspectCenter
            .reasonField
            .update(listOf(group.name.toMeme()))

        for (reasoning in newReasonings) {
            group.cultureCenter
                .cultureAspectCenter
                .addCultureAspect(reasoning.toConcept())
            group.cultureCenter
                .cultureAspectCenter
                .addCultureAspect(reasoning.toCherishedResource())
        }

        return ProcessResult(
            events = newReasonings.map { CultureAspectGaining of "${group.name} acquired reasoning ${it.meme}" }
        )
    }

    override val internalToString = "Update reasonings"
}
