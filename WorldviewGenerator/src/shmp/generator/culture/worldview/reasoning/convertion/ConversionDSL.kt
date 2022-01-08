package shmp.generator.culture.worldview.reasoning.convertion

import shmp.generator.culture.worldview.reasoning.ReasonComplex
import shmp.generator.culture.worldview.reasoning.Reasoning
import shmp.random.singleton.randomElementOrNull


class ConversionContext<T : Reasoning>(var state: List<T>) {
    operator fun <E> invoke(applicator: ConversionContext<T>.() -> E) = applicator()

    inline fun <reified E : T> filter() {
        state = state.filterIsInstance<E>()
    }

    inline fun <reified E : T> filterInstances(predicate: (E) -> Boolean) {
        state = state.filterIsInstance<E>()
                .filter(predicate)
    }

    inline fun <reified E : T, R : T> mapInstanceNotNull(transform: (E) -> R?) {
        state = state.filterIsInstance<E>()
                .mapNotNull(transform)
    }

    inline fun <reified E : T, R : T> flatMapInstance(transform: (E) -> Iterable<R>) {
        state = state.filterIsInstance<E>()
                .flatMap(transform)
    }

    inline fun <reified E : T> withRandomInstance(transform: (E) -> ReasonConversionResult?) =
            state.filterIsInstance<E>()
                    .randomElementOrNull()
                    ?.let { transform(it) }
                    ?: emptyReasonConversionResult()

    inline fun filter(predicate: (T) -> Boolean) {
        state = state.filter(predicate)
    }

    inline fun <reified R : T> map(transform: (T) -> R) = ConversionContext(state.map(transform))

    inline fun <reified R : T> flatMap(transform: (T) -> Iterable<R>) {
        state = state.flatMap(transform)
    }

    inline fun withRandom(transform: (T) -> ReasonConversionResult?) =
            state.randomElementOrNull()
                    ?.let { transform(it) }
                    ?: emptyReasonConversionResult()
}

fun ReasonComplex.calculate(applicator: ConversionContext<Reasoning>.() -> ReasonConversionResult) =
        calculateOn(applicator)

fun ReasonComplex.calculateAndChoose(applicator: ConversionContext<Reasoning>.() -> Unit) =
        calculateOnAndChoose(applicator)

inline fun <reified E : Reasoning> ReasonComplex.calculateOn(applicator: ConversionContext<E>.() -> ReasonConversionResult) =
        ConversionContext(reasonings.filterIsInstance<E>()).applicator()

inline fun <reified E : Reasoning> ReasonComplex.calculateOnAndChoose(applicator: ConversionContext<E>.() -> Unit): ReasonConversionResult {
    val dsl = ConversionContext(reasonings.filterIsInstance<E>())
    dsl.applicator()

    return dsl.state
            .randomElementOrNull()
            .toConversionResult()
}
