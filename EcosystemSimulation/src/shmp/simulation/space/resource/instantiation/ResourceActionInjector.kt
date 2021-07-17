package shmp.simulation.space.resource.instantiation

import shmp.simulation.space.resource.Resources
import shmp.simulation.space.resource.action.ResourceAction


typealias ResourceActionInjector = (ResourceAction, Resources) -> List<Pair<ResourceAction, Resources>>
