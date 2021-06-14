package shmp.simulation.culture.group.cultureaspect

import shmp.simulation.culture.group.centers.Group
import shmp.simulation.culture.group.request.Request
import shmp.simulation.culture.thinking.language.templates.TextInfo
import shmp.simulation.culture.thinking.meaning.Meme
import java.util.*


class Tale(internal val template: Meme, val info: TextInfo) : CultureAspect {
    private val meme = info.substitute(template)

    override fun getRequest(group: Group): Request? = null

    override fun use(group: Group) {
        group.cultureCenter.memePool.strengthenMeme(meme)
        info.map.values.forEach { group.cultureCenter.memePool.strengthenMeme(info.substitute(it)) }
    }

    override fun adopt(group: Group) = Tale(template, info)

    override fun die(group: Group) {}

    override fun toString() = "Tale about $meme"

    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true

        if (other == null || javaClass != other.javaClass)
            return false

        val that = other as Tale

        return meme == that.meme
    }

    override fun hashCode() = Objects.hash(meme)
}
