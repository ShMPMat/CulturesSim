package io.tashtabash.sim.culture.group.process.behaviour

import io.tashtabash.random.SampleSpaceObject
import io.tashtabash.random.singleton.chanceOf
import io.tashtabash.random.singleton.otherwise
import io.tashtabash.random.singleton.randomElement
import io.tashtabash.random.singleton.randomElementOrNull
import io.tashtabash.sim.CulturesController
import io.tashtabash.sim.culture.aspect.Aspect
import io.tashtabash.sim.culture.aspect.ConverseWrapper
import io.tashtabash.sim.culture.group.centers.Group
import io.tashtabash.sim.culture.group.cultureaspect.util.takeOutGod
import io.tashtabash.sim.culture.group.cultureaspect.util.takeOutSimilarRituals
import io.tashtabash.sim.culture.group.cultureaspect.util.takeOutSimilarTales
import io.tashtabash.sim.culture.group.cultureaspect.util.takeOutWorship
import io.tashtabash.sim.culture.group.process.ProcessResult
import io.tashtabash.sim.culture.group.process.emptyProcessResult
import io.tashtabash.sim.event.Type
import io.tashtabash.sim.event.of


object MutateAspectsB : AbstractGroupBehaviour() {
    override fun run(group: Group): ProcessResult { //TODO separate adding of new aspects and updating old
        val aspectCenter = group.cultureCenter.aspectCenter
        val options = mutableListOf<Aspect>()

        0.1.chanceOf {
            options += CulturesController.session.world.aspectPool.all.filter { it !in aspectCenter.aspectPool.all }
        } otherwise {
            options += aspectCenter.getAllPossibleConverseWrappers(group)
        }

        options.randomElementOrNull()?.let { aspect ->
            if (aspect is ConverseWrapper && !aspectCenter.aspectPool.contains(aspect.aspect))
                return emptyProcessResult

            if (aspectCenter.addAspectTry(aspect, group))
                return ProcessResult(
                    Type.AspectGaining of "${group.name} developed aspect ${aspect.name} by itself"
                )
        }

        return emptyProcessResult
    }

    override val internalToString = "Mutate existing Aspects"
}


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
