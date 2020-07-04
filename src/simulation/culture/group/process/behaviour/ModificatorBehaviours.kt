package simulation.culture.group.process.behaviour

import shmp.random.testProbability
import simulation.Controller
import simulation.Event
import simulation.culture.group.centers.Group

class ChanceWrapperBehaviour(
        val behaviour: GroupBehaviour,
        val probability: Double,
        private val probabilityUpdate: (Group) -> Double = { probability }
) : AbstractGroupBehaviour() {
    override fun run(group: Group) =
            if (testProbability(probability, Controller.session.random))
                behaviour.run(group)
            else emptyList()

    override fun update(group: Group) = ChanceWrapperBehaviour(
            behaviour.update(group),
            probabilityUpdate(group),
            probabilityUpdate
    )

    override fun toString() = "With probability $probability do:\n" +
            "  $behaviour"
}

fun GroupBehaviour.withProbability(probability: Double, probabilityUpdate: (Group) -> Double = { probability })
        = ChanceWrapperBehaviour(this, probability, probabilityUpdate)


class TimesWrapperBehaviour(
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

    override fun update(group: Group) = TimesWrapperBehaviour(
            behaviour.update(group),
            minUpdate(group),
            maxUpdate(group),
            minUpdate,
            maxUpdate
    )

    override fun toString() = "From $min to ${max - 1} times do:\n" +
            "  $behaviour"
}

fun GroupBehaviour.times(
        min: Int,
        max: Int = min + 1,
        minUpdate: (Group) -> Int = { min },
        maxUpdate: (Group) -> Int = { if (max != min + 1) max else minUpdate(it) + 1 }
)
        = TimesWrapperBehaviour(this, min, max, minUpdate, maxUpdate)
