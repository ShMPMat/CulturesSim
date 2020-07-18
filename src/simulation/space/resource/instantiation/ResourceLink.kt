package simulation.space.resource.instantiation

import simulation.space.resource.Resource
import simulation.space.resource.action.ResourceAction
import simulation.space.resource.transformer.ResourceTransformer
import simulation.space.resource.transformer.TransformerInstantiator


data class ResourceLink(val resourceName: String, val transformer: ResourceTransformer?, val amount: Int) {
    fun transform(resource: Resource) =
            (transformer?.transform(resource) ?: resource).copy(amount)
}

fun parseLink(tag: String, actions: List<ResourceAction>): ResourceLink {
    val nameAndTrans = tag.split(":")
    val amount = tag[1].toInt()

    val name = nameAndTrans[0]
    val transformer =
            if (nameAndTrans.size > 2)
                TransformerInstantiator(actions).makeResourceTransformer(
                        nameAndTrans.drop(2).joinToString(":")
                )
            else null

    return ResourceLink(name, transformer, amount)
}
