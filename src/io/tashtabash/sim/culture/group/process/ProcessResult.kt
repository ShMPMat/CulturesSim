package io.tashtabash.sim.culture.group.process

import io.tashtabash.sim.culture.group.centers.TraitChange
import io.tashtabash.generator.culture.worldview.Meme
import io.tashtabash.sim.culture.group.process.behaviour.GroupBehaviour
import io.tashtabash.sim.event.Event


class ProcessResult constructor(
        val memes: List<Meme> = listOf(),
        val events: List<Event> = listOf(),
        val traitChanges: List<TraitChange> = listOf(),
        val behaviours: List<GroupBehaviour> = listOf()
) {
    constructor(vararg events: Event) : this(events = events.toList())
    constructor(vararg memes: Meme) : this(memes.toList())
    constructor(vararg traitChanges: TraitChange) : this(traitChanges = traitChanges.toList())
    constructor(vararg behaviours: GroupBehaviour) : this(behaviours = behaviours.toList())

    operator fun plus(other: ProcessResult) = ProcessResult(
            memes + other.memes,
            events + other.events,
            traitChanges + other.traitChanges,
            behaviours + other.behaviours
    )
}

val emptyProcessResult = ProcessResult()


inline fun <E> Iterable<E>.flatMapPR(transform: (E) -> ProcessResult) = this
        .map(transform)
        .foldRight(emptyProcessResult, ProcessResult::plus)

inline fun <K, V> Map<out K, V>.flatMapPR(transform: (Map.Entry<K, V>) -> ProcessResult) = this
        .map(transform)
        .foldRight(emptyProcessResult, ProcessResult::plus)

fun List<ProcessResult>.flattenPR() = this
        .foldRight(emptyProcessResult, ProcessResult::plus)
