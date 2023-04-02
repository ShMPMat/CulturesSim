package io.tashtabash.simulation.culture.group

import io.tashtabash.simulation.culture.group.centers.Group


private interface Command {
    fun execute(conglomerate: GroupConglomerate)
}

sealed class ConglomerateCommand : Command

data class Add(val group: Group) : ConglomerateCommand() {
    override fun execute(conglomerate: GroupConglomerate) {
        if (group.parentGroup == conglomerate)
            conglomerate.addGroup(group)
        else
            Transfer(group).execute(conglomerate)
    }
}

data class Transfer(val group: Group) : ConglomerateCommand() {
    override fun execute(conglomerate: GroupConglomerate) {
        group.parentGroup.removeGroup(group)
        conglomerate.addGroup(group)
    }
}