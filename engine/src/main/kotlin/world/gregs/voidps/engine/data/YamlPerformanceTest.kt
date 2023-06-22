package world.gregs.voidps.engine.data

import java.io.File

object YamlPerformanceTest {
    @JvmStatic
    fun main(args: Array<String>) {
        val parser = YamlParser()
        val files = File("./data/definitions/").listFiles()
            .union(File("./data/map/").listFiles().toList())
            .union(File("./data/spawns/").listFiles().toList())
            .filter { it.name != "nav-graph.yml" }
        val chars = CharArray(3_000_000)
        var output: Any = ""
//        repeat(10) {
//            files.forEach {
//                val fr = it.reader()
//                val length = fr.read(chars)
//                output = parser.parse(chars, length)
//            }
//        }
        val iterations = 1000
        val start = System.currentTimeMillis()
        repeat(iterations) {
            files.forEach {
                val fr = it.reader()
                val length = fr.read(chars)
                output = parser.parse(chars, length)
            }
        }
        println("Parsing took ${(System.currentTimeMillis() - start) / iterations}ms")
        println(output)
    }
}