package shmp.simulation.space.resource


interface ExternalResourceFeature {
    val name: String

    //Resource must contain only one instance of a Feature with a particular index.
    //Features will be shown in the Resource name in order of their index.
    val index: Int
}
