package world.gregs.yaml

import java.io.File

object YamlPerformanceTest {
    @JvmStatic
    fun main(args: Array<String>) {
        val yaml = Yaml()
        val files = File("./data/definitions/").listFiles()
            .union(File("./data/map/").listFiles().toList())
            .union(File("./data/spawns/").listFiles().toList())
            .map { it.readText().toCharArray() }
        var output: Any = ""
        repeat(10) {
            files.forEach {
                output = yaml.read(it, it.size)
            }
        }
        val iterations = 1000
        val start = System.currentTimeMillis()
        repeat(iterations) {
            files.forEach {
                output = yaml.read(it, it.size)
            }
        }
        println("Parsing took ${(System.currentTimeMillis() - start) / iterations}ms")
        println(output)
    }
}