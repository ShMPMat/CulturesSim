package shmp.utils


fun <E> Collection<E>.without(element: E) = filter { it != element }
