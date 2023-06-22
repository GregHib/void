package world.gregs.voidps.engine.data

import java.io.File

object YamlVerifyTest {
    @JvmStatic
    fun main(args: Array<String>) {
        val parser = FinalYamlParser()
        val loader = FileStorage()
        val files = File("./data/definitions/").listFiles()
            .union(File("./data/map/").listFiles().toList())
            .union(File("./data/spawns/").listFiles().toList())
            .filter { it.name != "nav-graph.yml" }
        val chars = CharArray(3_000_000)
        files.forEach {
            println(it.name)
            if (it.name == "gear-sets.yml" || it.name == "stairs.yml" || it.name.endsWith("spawns.yml")) {
                val one = loader.load<List<Any>>(it.path)
                val fr = it.reader()
                val length = fr.read(chars)
                val two = parser.parse(chars, length) as List<Any>
                if (one != two) {
                    for ((index, value) in one.withIndex()) {
                        if (value != two[index]) {
                            println(value)
                            println(two[index])
                            System.exit(0)
                        }
                    }
                }
            } else {
                val one = loader.load<Map<String, Any>>(it.path)
                val fr = it.reader()
                val length = fr.read(chars)
                val two = parser.parse(chars, length) as Map<String, Any>
                if (one != two) {
                    for ((key, value) in one) {
                        if (value != two[key]) {
                            println(value as Map<String, Any>)
                            println(two[key] as Map<String, Any>)
                            System.exit(0)
                        }
                    }
                }
            }
        }
    }
}