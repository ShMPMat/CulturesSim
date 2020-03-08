package simulation.culture.aspect

import extra.InputDatabase
import java.util.*

class AspectInstantiation {
    fun createPool(path: String): AspectPool {
        val aspects: MutableList<Aspect> = ArrayList()
        val inputDatabase = InputDatabase(path)
        var line: String?
        var tags: Array<String?>
        while (true) {
            line = inputDatabase.readLine()
            if (line == null) {
                break
            }
            tags = line.split("\\s+".toRegex()).toTypedArray()
            aspects.add(Aspect(tags, HashMap(), null))
        }
        return AspectPool(aspects)
    }
}