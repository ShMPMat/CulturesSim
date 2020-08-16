package simulation.culture.group.process

import simulation.culture.thinking.meaning.Meme
import simulation.event.Event


class ProcessResult constructor(val memes: List<Meme> = listOf(), val events: List<Event> = listOf()) {
    constructor(vararg events: Event) : this(events = events.toList())
    constructor(vararg memes: Meme) : this(memes.toList())

    operator fun plus(other: ProcessResult) =
            ProcessResult(memes + other.memes, events + other.events)
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
