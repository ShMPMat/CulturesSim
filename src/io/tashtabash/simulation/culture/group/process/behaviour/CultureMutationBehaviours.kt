package io.tashtabash.simulation.culture.group.process.behaviour

import io.tashtabash.random.SampleSpaceObject
import io.tashtabash.random.singleton.randomElement
import io.tashtabash.simulation.culture.group.centers.Group
import io.tashtabash.simulation.culture.group.cultureaspect.util.takeOutGod
import io.tashtabash.simulation.culture.group.cultureaspect.util.takeOutSimilarRituals
import io.tashtabash.simulation.culture.group.cultureaspect.util.takeOutSimilarTales
import io.tashtabash.simulation.culture.group.cultureaspect.util.takeOutWorship
import io.tashtabash.simulation.culture.group.process.ProcessResult
import io.tashtabash.simulation.culture.group.process.emptyProcessResult
import io.tashtabash.simulation.event.Type
import io.tashtabash.simulation.event.of


object MutateCultureAspectsB : AbstractGroupBehaviour() {
    override fun run(group: Group): ProcessResult =
            when (ChangeRandom.values().randomElement()) {
                ChangeRandom.RitualSystem -> joinSimilarRituals(group)
                ChangeRandom.TaleSystem -> joinSimilarTales(group)
                ChangeRandom.Worship -> makeWorship(group)
                ChangeRandom.God -> makeGod(group)
            } ?: emptyProcessResult

    private fun joinSimilarRituals(group: Group) = with(group.cultureCenter.cultureAspectCenter) {
        takeOutSimilarRituals(aspectPool)?.let { system ->
            this.addCultureAspect(system)
            this.reasonsWithSystems.add(system.reason)

            ProcessResult(Type.CultureAspectGaining of "${group.name} joined similar rituals into $system")
        }
    }

    private fun joinSimilarTales(group: Group) = with(group.cultureCenter.cultureAspectCenter) {
        takeOutSimilarTales(aspectPool)?.let { system ->
            addCultureAspect(system)

            ProcessResult(Type.CultureAspectGaining of "${group.name} joined similar tales into $system")
        }
    }

    private fun makeGod(group: Group) = with(group.cultureCenter.cultureAspectCenter) {
        takeOutGod(aspectPool, group)?.let { cult ->
            addCultureAspect(cult)

            ProcessResult(Type.CultureAspectGaining of "${group.name} formed ${cult.simpleName}")
        }
    }

    private fun makeWorship(group: Group) = with(group.cultureCenter.cultureAspectCenter) {
        takeOutWorship(reasonField, aspectPool)?.let { worship ->
            addCultureAspect(worship)

            ProcessResult(Type.CultureAspectGaining of "${group.name} formed ${worship.simpleName}")
        }
    }

    override val internalToString = "Mutate existing culture aspects"
}


private enum class ChangeRandom(override val probability: Double) : SampleSpaceObject {
    RitualSystem(3.0),
    TaleSystem(3.0),
    Worship(2.0),
    God(1.0),
}
