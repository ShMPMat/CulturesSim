package io.tashtabash.sim.event

import io.tashtabash.sim.Controller


//Object storing memory about something that happened in the world.
open class Event(var type: Type, val description: String) {
    private val turnString = Controller.session.world?.getStringTurn() ?: "Pre-historic"
    val turn = turnString.toIntOrNull()

    override fun toString() = "${type.colourString}$turnString. $type: $description\u001B[37m"
}

enum class Type(val colourString: String) {
    Death(("\u001B[31m")),
    ResourceDeath(("\u001B[31m")),
    AspectGaining("\u001B[32m"),
    CultureAspectGaining("\u001B[32m"),
    TileAcquisition("\u001B[32m"),
    DisbandResources("\u001B[32m"),

    GroupInteraction("\u001B[35m"),
    Conflict("\u001B[31m"),
    Cooperation("\u001B[32m"),

    Cataclysm(("\u001B[31m")),

    Creation("\u001B[34m"),
    Move("\u001B[33m"),
    Change("\u001B[33m"),
    PopulationDecrease(""),
    Other("")
}

infix fun Type.of(description: String) =
        Event(this, description)
