package simulation.culture.aspect

import extra.InputDatabase
import java.util.*

class AspectInstantiation {
    fun createPool(path: String): AspectPool {
        val aspects: MutableList<Aspect> = ArrayList()
        val inputDatabase = InputDatabase(path)
        while (true) {
            val line = inputDatabase.readLine() ?: break
            val tags = line.split("\\s+".toRegex()).toTypedArray()
            aspects.add(createAspect(tags))
        }
        return AspectPool(aspects)
    }

    private fun createAspect(tags: Array<String>): Aspect {
        return Aspect(tags, HashMap(), null)
    }

    fun postResourceInstantiation() { //TODO all the creation must be moved after the resources

    }
}