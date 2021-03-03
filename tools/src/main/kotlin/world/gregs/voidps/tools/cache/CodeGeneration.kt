package world.gregs.voidps.tools.cache

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.definition.data.ItemDefinition2
import world.gregs.voidps.cache.format.definition.Operation
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

object CodeGeneration {

    @JvmStatic
    fun main(args: Array<String>) {

        val def = ItemDefinition2()
        val requireId = true
        val parameters = mutableMapOf<Int, MutableList<KProperty1<out ItemDefinition2, *>>>().toSortedMap()
//        println(constructor.annotations)
//        println(constructor.parameters.get(1).annotations)
//        println(def::class.typeParameters)
        for (parameter in def::class.memberProperties) {
            val opcode = parameter.annotations.filterIsInstance<Operation>().firstOrNull()?.code ?: continue
            parameters.getOrPut(opcode) { mutableListOf() }.add(parameter)
        }

        var build = FunSpec.builder("encode")
            .addParameter("writer", Writer::class)
            .addParameter("definition", ItemDefinition2::class)
        if (requireId) {
            build = build
                .beginControlFlow("if (definition.%N == %L)", "id", -1)
                .addStatement("return")
                .endControlFlow()
        }
        val default = ItemDefinition2()
        val codec = DefaultProcessor()
        for ((opcode, params) in parameters) {
            if (params.size == 1) {
                val param = params.first()
                codec.write(build, opcode, param, default)
            } else {

            }
        }

        val builder = TypeSpec.Companion.classBuilder("Encoder")
            .addFunction(
                build
                    .build()
            )
        val file = FileSpec.builder("", "HelloWorld").addType(builder.build())
        println(file.build().toString())
    }
}