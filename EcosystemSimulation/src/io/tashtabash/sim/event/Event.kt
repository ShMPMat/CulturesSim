package io.tashtabash.sim.event

import io.tashtabash.sim.Controller


//Object storing memory about something that happened in the world.
open class Event(var type: Type, val description: String) {
    private val turnString = Controller.session.world?.getStringTurn() ?: "Pre-historic"
    val turn = turnString.toIntOrNull()

    override fun toString() = "${type.colourString}$turnString. ${type.javaClass.simpleName}: $description\u001B[37m"
}


open class Type(val colourString: String)

object Death : Type(("\u001B[31m"))
object ResourceDeath : Type(("\u001B[31m"))
object Fail : Type(("\u001B[31m"))
object DisbandResources : Type("\u001B[32m")
object TileAcquisition : Type("\u001B[32m")
object Conflict : Type("\u001B[31m")
object Cooperation : Type("\u001B[32m")
object Cataclysm : Type(("\u001B[31m"))
object Creation : Type("\u001B[34m")
object Move : Type("\u001B[33m")
object Change : Type("\u001B[33m")
object PopulationDecrease : Type("")
object Other : Type("")

infix fun Type.of(description: String) =
        Event(this, description)
