package io.tashtabash.sim.culture.aspect

import io.tashtabash.sim.space.resource.tag.ResourceTag


class InstrumentTag(val shortName: String, level: Double = 1.0) : ResourceTag(shortName, level) {
    override fun copy(level: Double) = InstrumentTag(shortName, level)
}


val ResourceTag.isInstrumental get() = this is InstrumentTag
