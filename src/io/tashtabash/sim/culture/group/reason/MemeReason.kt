package io.tashtabash.sim.culture.group.reason

import io.tashtabash.generator.culture.worldview.Meme


data class MemeReason(val meme: Meme) : Reason {
    override fun toString() =
            meme.toString()
}
