package io.tashtabash.simulation.space.resource.instantiation

import io.tashtabash.simulation.space.resource.Resource
import io.tashtabash.simulation.space.resource.action.ResourceAction
import io.tashtabash.simulation.space.resource.transformer.ResourceTransformer
import io.tashtabash.simulation.space.resource.transformer.TransformerInstantiator


data class ResourceLink(val resourceName: String, val transformer: ResourceTransformer?, val amount: Int) {
    fun transform(resource: Resource) =
            (transformer?.transform(resource) ?: resource).copy(amount)
}

fun parseLink(tag: String, conversionParser: ConversionParser): ResourceLink {
    val nameAndTrans = tag.split(":")
    val amount = nameAndTrans[1].toInt()

    val name = nameAndTrans[0]
    val transformer =
            if (nameAndTrans.size > 2)
                TransformerInstantiator(conversionParser).makeResourceTransformer(
                        nameAndTrans.drop(2).joinToString(":")
                )
            else null

    return ResourceLink(name, transformer, amount)
}