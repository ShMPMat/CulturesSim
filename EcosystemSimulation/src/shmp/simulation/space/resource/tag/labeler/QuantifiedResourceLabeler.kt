package shmp.simulation.space.resource.tag.labeler


data class QuantifiedResourceLabeler(val resourceLabeler: ResourceLabeler, val amount: Double) {
    override fun toString() = "$resourceLabeler: $amount"
}
