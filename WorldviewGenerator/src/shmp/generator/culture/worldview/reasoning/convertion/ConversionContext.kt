package shmp.generator.culture.worldview.reasoning.convertion

import shmp.generator.culture.worldview.reasoning.ReasonComplex
import shmp.generator.culture.worldview.reasoning.Reasoning
import shmp.random.singleton.randomElementOrNull


class ConversionContext<T: Reasoning>(var state: List<T>) {
    operator fun <E> invoke(applicator: ConversionContext<T>.() -> E) = applicator()

    inline fun <reified E: T> filter(): ConversionContext<E> {//TODO divide
        val newContext = ConversionContext(state.filterIsInstance<E>())
        state = newContext.state

        return newContext
    }

    inline fun <reified E: T> filterInstances(predicate: (E) -> Boolean) {
        state = state.filterIsInstance<E>()
                .filter(predicate)
    }

    inline fun <reified E: T, R: T> mapNotNull(transform: (E) -> R?) {
        state = state.filterIsInstance<E>()
                .mapNotNull(transform)
    }

    inline fun <reified E: T, R: T> flatMap(transform: (E) -> Iterable<R>) {
        state = state.filterIsInstance<E>()
                .flatMap(transform)
    }

    inline fun <reified R: T> map(transform: (T) -> R) = ConversionContext(state.map(transform))

    inline fun <reified E: T> withRandom(transform: (E) -> ReasonConversionResult?) =
            state.filterIsInstance<E>()
                    .randomElementOrNull()
                    ?.let { transform(it) }
                    ?: emptyReasonConversionResult()
}

fun ReasonComplex.calculate(applicator: ConversionContext<Reasoning>.() -> ReasonConversionResult) = calculateOn(applicator)

inline fun <reified E: Reasoning> ReasonComplex.calculateOn(applicator: ConversionContext<E>.() -> ReasonConversionResult) =
        ConversionContext(reasonings.filterIsInstance<E>()).applicator()

fun ReasonComplex.calculateAndChoose(applicator: ConversionContext<Reasoning>.() -> Unit): ReasonConversionResult {
    val dsl = ConversionContext(reasonings.toList())
    dsl.applicator()

    return dsl.state
            .randomElementOrNull()
            .toConversionResult()
}
