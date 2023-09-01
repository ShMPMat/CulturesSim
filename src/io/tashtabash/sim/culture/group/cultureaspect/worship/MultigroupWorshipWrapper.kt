package io.tashtabash.sim.culture.group.cultureaspect.worship

import io.tashtabash.sim.culture.group.centers.Group


class MultigroupWorshipWrapper(val worship: Worship): Worship(
        worship.worshipObject,
        worship.taleSystem,
        worship.depictSystem,
        worship.placeSystem,
        worship.reasonComplex,
        mutableListOf()
) {
    override fun getRequest(group: Group) = worship.getRequest(group)

    override fun use(group: Group) = worship.use(group)

//    override fun adopt(group: Group) = worship.adopt(group)
    override fun adopt(group: Group) = MultigroupWorshipWrapper(worship)

    override fun die(group: Group) = worship.die(group)

    override fun swapWorship(worshipObject: WorshipObject) = worship.swapWorship(worshipObject)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MultigroupWorshipWrapper) return false

        return worship == other.worship
    }

    override fun hashCode() = worship.hashCode()

    override fun toString() = worship.toString()
}
