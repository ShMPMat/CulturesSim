package simulation.culture.group.process.behaviour

import shmp.random.testProbability
import simulation.Controller
import simulation.event.Event
import simulation.culture.group.centers.Group
import kotlin.math.min

class ChanceWrapperB(
        val behaviour: GroupBehaviour,
        val probability: Double,
        private val probabilityUpdate: (Group) -> Double = { probability }
) : AbstractGroupBehaviour() {
    override fun run(group: Group) =
            if (testProbability(probability, Controller.session.random))
                behaviour.run(group)
            else emptyList()

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


class TimesWrapperB(
        val behaviour: GroupBehaviour,
        val min: Int,
        val max: Int = min + 1,
        private val minUpdate: (Group) -> Int = { min },
        private val maxUpdate: (Group) -> Int = { if (max != min + 1) max else minUpdate(it) + 1 }
) : AbstractGroupBehaviour() {
    override fun run(group: Group): List<Event> {
        val times = Controller.session.random.nextInt(min, max)
        return (0 until times).flatMap { behaviour.run(group) }
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
