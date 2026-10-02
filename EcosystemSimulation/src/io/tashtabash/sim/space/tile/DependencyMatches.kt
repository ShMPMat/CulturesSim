package io.tashtabash.sim.space.tile

import io.tashtabash.sim.space.resource.Resource


// Tile Resources matching a LabelerDependency, with the worth of one unit of each.
class DependencyMatches(keysVersion: Int) {
    // The ResourcePack.keysVersion these matches are valid for
    var keysVersion = keysVersion
        internal set

    var size = 0
        private set

    // Slots from size on are unused
    var resources = arrayOfNulls<Resource>(4)
        private set
    var worths = IntArray(4)
        private set

    inline fun any(action: (Resource, Int) -> Boolean): Boolean {
        for (i in 0 until size)
            if (action(resources[i]!!, worths[i]))
                return true

        return false
    }

    internal fun add(resource: Resource, worth: Int) {
        if (size == resources.size) {
            resources = resources.copyOf(size * 2)
            worths = worths.copyOf(size * 2)
        }

        resources[size] = resource
        worths[size] = worth
        size++
    }

    internal fun removeAll(removed: List<Resource>) {
        var kept = 0
        for (i in 0 until size) {
            val resource = resources[i]
            if (isRemoved(resource, removed))
                continue

            resources[kept] = resource
            worths[kept] = worths[i]
            kept++
        }

        for (i in kept until size)
            resources[i] = null
        size = kept
    }

    private fun isRemoved(resource: Resource?, removed: List<Resource>): Boolean {
        for (i in 0 until removed.size) // Check manually to allocate nothing
            if (removed[i] === resource)
                return true

        return false
    }
}
