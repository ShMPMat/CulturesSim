package shmp.simulation.space.resource


data class Appearance(val colour: ResourceColour?, val texture: ResourceTexture?, val shape: ResourceShape?) {
    override fun toString() = "colour - $colour, texture - $texture, shape - $shape"
}


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

enum class ResourceTexture {
    Shiny,
    Matt,
    Rough,
    Semitransparent,
    Transparent
}

enum class ResourceShape {
    Round,
    Cylinder,
    Star,
    Peculiar
}
