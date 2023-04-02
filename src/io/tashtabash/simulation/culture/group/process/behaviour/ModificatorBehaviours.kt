package io.tashtabash.simulation.culture.group.process.behaviour

import io.tashtabash.random.singleton.RandomSingleton
import io.tashtabash.random.singleton.chanceOf
import io.tashtabash.simulation.culture.group.centers.Group
import io.tashtabash.simulation.culture.group.process.ProcessResult
import io.tashtabash.simulation.culture.group.process.TraitExtractor
import io.tashtabash.simulation.culture.group.process.emptyProcessResult
import io.tashtabash.simulation.culture.group.process.flatMapPR
import kotlin.math.min


class ChanceWrapperB(
        val behaviour: GroupBehaviour,
        val probability: Double,
        private val probabilityUpdate: (Group) -> Double = { probability }
) : AbstractGroupBehaviour() {
    override fun run(group: Group) =
            probability.chanceOf<ProcessResult> {
                behaviour.run(group)
            } ?: emptyProcessResult

    override fun update(group: Group): ChanceWrapperB? {
        return ChanceWrapperB(
                behaviour.update(group) ?: return null,
                probabilityUpdate(group),
                probabilityUpdate
        )
    }

    override val internalToString
        get() = """
            |With probability $probability do:
            |    $behaviour
            """.trimMargin()
}

fun GroupBehaviour.withProbability(probability: Double, probabilityUpdate: (Group) -> Double = { probability }) =
        ChanceWrapperB(this, probability, probabilityUpdate)


class TraitChanceWrapperB(
        val behaviour: GroupBehaviour,
        val traitExtractor: TraitExtractor
): AbstractGroupBehaviour() {
    override fun run(group: Group) =
            traitExtractor.extract(group.cultureCenter.traitCenter).chanceOf<ProcessResult> {
                behaviour.run(group)
            } ?: emptyProcessResult

    override val internalToString: String
        get() = """
            |Depending on $traitExtractor, do:
            |    $behaviour
            """.trimMargin()
}

fun GroupBehaviour.withTrait(extractor: TraitExtractor) =
        TraitChanceWrapperB(this, extractor)


class TimesWrapperB(
        val behaviour: GroupBehaviour,
        val min: Int,
        val max: Int = min + 1,
        private val minUpdate: (Group) -> Int = { min },
        private val maxUpdate: (Group) -> Int = { if (max != min + 1) max else minUpdate(it) + 1 }
) : AbstractGroupBehaviour() {
    override fun run(group: Group): ProcessResult {
        val times = RandomSingleton.random.nextInt(min, max)
        return (0 until times).flatMapPR { behaviour.run(group) }
    }

    override fun update(group: Group): TimesWrapperB? {
        return TimesWrapperB(
                behaviour.update(group)
                        ?: return null,
                min(minUpdate(group), 10),
                min(maxUpdate(group), 11),
                minUpdate,
                maxUpdate
        )
    }

    override val internalToString
        get() = """
            |From $min to ${max - 1} times do:
            |    $behaviour
            """.trimMargin()
}

fun GroupBehaviour.times(
        min: Int,
        max: Int = min + 1,
        minUpdate: (Group) -> Int = { min },
        maxUpdate: (Group) -> Int = { if (max != min + 1) max else minUpdate(it) + 1 }
) = TimesWrapperB(this, min, max, minUpdate, maxUpdate)
