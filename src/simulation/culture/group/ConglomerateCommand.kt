package simulation.culture.group

import simulation.culture.group.centers.Group

private interface Command {
    fun execute(conglomerate: GroupConglomerate)
}

sealed class ConglomerateCommand : Command

data class Add(val group: Group) : ConglomerateCommand() {
    override fun execute(conglomerate: GroupConglomerate) = conglomerate.addGroup(group)
}

data class Transfer(val group: Group) : ConglomerateCommand() {
    override fun execute(conglomerate: GroupConglomerate) {
        group.parentGroup.removeGroup(group)
        conglomerate.addGroup(group)
    }
}