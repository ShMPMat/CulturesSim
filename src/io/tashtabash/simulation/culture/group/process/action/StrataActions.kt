package io.tashtabash.simulation.culture.group.process.action

import io.tashtabash.simulation.culture.group.centers.Group
import io.tashtabash.simulation.culture.group.stratum.Stratum


class IncStratumImportanceA(group: Group, val stratum: Stratum, val amount: Int) : AbstractGroupAction(group) {
    override fun run() {
        stratum.importance += amount
    }

    override val internalToString = "Increase importance of $stratum in ${group.name} on $amount"
}
