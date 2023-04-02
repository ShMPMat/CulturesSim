package io.tashtabash.simulation.space.resource.instantiation

import io.tashtabash.simulation.space.resource.Resources
import io.tashtabash.simulation.space.resource.action.ResourceAction


typealias ResourceActionInjector = (ResourceAction, Resources) -> List<Pair<ResourceAction, Resources>>
