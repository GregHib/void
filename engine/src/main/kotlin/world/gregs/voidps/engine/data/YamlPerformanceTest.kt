package world.gregs.voidps.engine.data

import java.io.File

object YamlPerformanceTest {
    @JvmStatic
    fun main(args: Array<String>) {
        val parser = FinalYamlParser()
//        val loader = FileStorage()
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
//            println(it.name)
                /*if (it.name == "gear-sets.yml" || it.name == "stairs.yml" || it.name.endsWith("spawns.yml")) {
                    val one = loader.load<List<Any>>(it.path)
                    val fr = it.reader()
                    val length = fr.read(chars)
                    val two = parser.parse(chars, length)
                    if(one != two) {
                        println(it.name)
                        println("one $one")
                        println("two $two")
                    }
                } else {
                    val one = loader.load<Map<String, Any>>(it.path)
                    val fr = it.reader()
                    val length = fr.read(chars)
                    val two = parser.parse(chars, length) as Map<String, Any>
                    if(one != two) {
                        println(it.name)
                        for ((key, value) in one) {
                            if(value != two[key]) {
                                println(key)
    //                            println("one $value")
    //                            println("two ${two[key]}")
                                fun p(m: Map<String, Any>) {
                                    println(m)
    //                                println(m["format"]!!::class.java.simpleName)
    //                                println(m["persist"]!!::class.java.simpleName)
                                }
                                p(value as Map<String, Any>)
                                p(two[key] as Map<String, Any>)
                                System.exit(0)
                            }
                        }
                    }
                }*/
                val fr = it.reader()
                val length = fr.read(chars)
                output = parser.parse(chars, length)
            }
        }
        println("Parsing took ${(System.currentTimeMillis() - start) / iterations}ms")
        println(output)
//        output.forEach(::println)
    }
}