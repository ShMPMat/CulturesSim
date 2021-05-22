package shmp.simulation.space.resource


data class Appearance(val colour: ResourceColour?)


enum class ResourceColour {
    Black,
    Grey,
    White,
    Brown,
    Red,
    Green,
    Blue,
    Yellow,
    Orange
}
