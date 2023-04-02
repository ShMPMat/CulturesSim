package io.tashtabash.visualizer.printinfo

import io.tashtabash.simulation.culture.group.GroupConglomerate


class ConglomeratePrintInfo(private val symbolsPool: List<String>) {
    val populations = mutableMapOf<GroupConglomerate, Int>()

    private val symbols = mutableMapOf<String, String>()
    private var nextSymbol = 0

    fun getConglomerateSymbol(conglomerate: GroupConglomerate) = symbols[conglomerate.name]
            ?: symbolsPool[nextSymbol].also {
                nextSymbol = (nextSymbol + 1) % symbolsPool.size
                symbols[conglomerate.name] = it
            }
}
