package shmp.simulation.culture.group.request


sealed class RequestType {
    object Food : RequestType()
    object Warmth : RequestType()
    object Clothes : RequestType()
    object Shelter : RequestType()

    object Vital : RequestType()
    object Comfort : RequestType()
    object Improvement : RequestType()
    object Trade : RequestType()
    object Luxury : RequestType()
}
