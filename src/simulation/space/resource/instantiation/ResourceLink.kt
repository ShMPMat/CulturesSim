package simulation.space.resource.instantiation

import simulation.space.resource.Resource
import simulation.space.resource.transformer.ResourceTransformer
import simulation.space.resource.transformer.makeResourceTransformer


data class ResourceLink(val resourceName: String, val transformer: ResourceTransformer?, val amount: Int) {
    fun transform(resource: Resource) =
            (transformer?.transform(resource) ?: resource).copy(amount)
}

fun parseLink(tag: String): ResourceLink {
    val nameAndTrans = tag.split(":")
    val amount = tag[1].toInt()

    val name = nameAndTrans[0]
    val transformer = if (nameAndTrans.size > 2)
        makeResourceTransformer(nameAndTrans[2].split("#"))
    else null

    return ResourceLink(name, transformer, amount)
}
