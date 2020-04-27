package simulation.culture.group

import simulation.culture.group.centers.Group

private interface Command {
    fun execute(conglomerate: GroupConglomerate)
}

sealed class ConglomerateCommand : Command

data class Add(val group: Group) : ConglomerateCommand() {
    override fun execute(conglomerate: GroupConglomerate) = conglomerate.addGroup(group)
}