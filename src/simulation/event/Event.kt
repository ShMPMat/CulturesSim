package simulation.event

import simulation.Controller
import java.util.*


//Object storing memory about something that happened in the world.
open class Event(var type: Type, val description: String) {
    enum class Type(val colourString: String) {
        Death(("\u001B[31m")),
        ResourceDeath(("\u001B[31m")),
        AspectGaining("\u001B[32m"),
        TileAcquisition("\u001B[32m"),
        DisbandResources("\u001B[32m"),

        GroupInteraction("\u001B[35m"),
        Conflict("\u001B[31m"),
        Cooperation("\u001B[32m"),

        Creation("\u001B[34m"),
        Move("\u001B[33m"),
        Change("\u001B[33m"),
        Other("")
    }

    private var _attributes: MutableMap<String, Any> = HashMap()

    val turnString = Controller.session.world?.getStringTurn() ?: "Pre-historic"
    val turn = turnString.toIntOrNull()

    constructor(type: Type, description: String, vararg attributes: Any) : this(type, description) {
        var i = 0
        while (i < attributes.size) {
            val name = attributes[i] as String
            if (name == "")
                break
            this._attributes[name] = attributes[i + 1]
            i += 2
        }
    }

    constructor(type: Type, description: String, attributes: Map<String, Any>) : this(type, description) {
        this._attributes.putAll(attributes)
    }

    fun getAttribute(name: String) = _attributes[name]

    override fun toString() = "${type.colourString}$turnString. $description\u001B[37m"
}
