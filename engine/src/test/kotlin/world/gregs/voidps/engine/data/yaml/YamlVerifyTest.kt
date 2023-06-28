package world.gregs.voidps.engine.data.yaml

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import java.io.File

object YamlVerifyTest {

    internal val mapper = ObjectMapper(YAMLFactory().apply {
        disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
        disable(YAMLGenerator.Feature.SPLIT_LINES)
    })

    internal inline fun <reified T : Any> load(path: String): T = mapper.readValue(File(path), T::class.java)

    @JvmStatic
    fun main(args: Array<String>) {
        val parser = YamlParser()
        val files = File("./data/definitions/").listFiles()
            .union(File("./data/map/").listFiles().toList())
            .union(File("./data/spawns/").listFiles().toList())
        val chars = CharArray(3_000_000)
        files.forEach {
            println(it.name)
            if (it.name == "gear-sets.yml" || it.name == "stairs.yml" || it.name.endsWith("spawns.yml")) {
                val one = load<List<Any>>(it.path)
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
                val one = load<Map<String, Any>>(it.path)
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