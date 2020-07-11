package simulation

import java.util.*


//Object storing memory about something that happened in the world.
class Event(var type: Type, val description: String) {
    enum class Type {
        Death,
        ResourceDeath,
        AspectGaining,
        TileAcquisition,
        DisbandResources,

        GroupInteraction,

        Creation,
        Move,
        Change,
        Other
    }

    private var _attributes: MutableMap<String, Any> = HashMap()
    val attributes: Map<String, Any>
        get() = _attributes

    private val turn: String = Controller.session.world?.getStringTurn() ?: "Pre-historic"

    constructor(type: Type, description: String, vararg attributes: Any) : this(type, description) {
        var i = 0
        while (i < attributes.size) {
            val name = attributes[i] as String
            if (name == "") break
            this._attributes[name] = attributes[i + 1]
            i += 2
        }
    }

    constructor(type: Type, description: String, attributes: Map<String, Any>) : this(type, description) {
        this._attributes.putAll(attributes)
    }

    fun getAttribute(name: String) = _attributes[name]

    override fun toString() = "$turn. $description"
}
