package simulation.culture.group.process

import simulation.event.Event


class ProcessResult(val events: List<Event>) {
    constructor(vararg events: Event) : this(events.toList())

    operator fun plus(other: ProcessResult) =
            ProcessResult(events + other.events)
}

val emptyProcessResult = ProcessResult(listOf())


inline fun <E> Iterable<E>.flatMapPR(transform: (E) -> ProcessResult) = this
        .map(transform)
        .foldRight(emptyProcessResult, ProcessResult::plus)

inline fun <K, V> Map<out K, V>.flatMapPR(transform: (Map.Entry<K, V>) -> ProcessResult) = this
        .map(transform)
        .foldRight(emptyProcessResult, ProcessResult::plus)

fun List<ProcessResult>.flattenPR() = this
        .foldRight(emptyProcessResult, ProcessResult::plus)
