package io.tashtabash.sim.space.resource.instantiation

import io.tashtabash.sim.space.resource.Resources
import io.tashtabash.sim.space.resource.action.ResourceAction


typealias ResourceActionInjector = (ResourceAction, Resources) -> List<Pair<ResourceAction, Resources>>
