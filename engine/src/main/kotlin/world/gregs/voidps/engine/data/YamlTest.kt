package world.gregs.voidps.engine.data

import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.module.kotlin.readValue

object YamlTest {
    @JvmStatic
    fun main(args: Array<String>) {
        val loader = FileStorage.yamlMapper(false)
            .disable(MapperFeature.ALLOW_COERCION_OF_SCALARS)
            .disable(MapperFeature.ALLOW_IS_GETTERS_FOR_NON_BOOLEAN)
        println(loader.readValue<List<Any>>("""
            - list
            - of stuff
        """.trimIndent()))
    }

}