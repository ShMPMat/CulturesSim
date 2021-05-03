package shmp.utils.labeler


class EqualityLabeler<T>(val reference: T) : AbstractLabeler<T>() {
    override fun matches(sample: T) = reference == sample

    override fun explanation() = "equals to $reference"
}
