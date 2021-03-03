package world.gregs.voidps.tools.cache

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.MemberName
import world.gregs.voidps.cache.Definition
import kotlin.reflect.KProperty1

class DefaultProcessor : Scribe {
    override fun write(builder: FunSpec.Builder, opcode: Int, param: KProperty1<out Definition, *>, default: Definition) {
        val defaultValue = param.getter.call(default) ?: return
        builder.beginControlFlow("if (definition.%N != %L)", param.name, defaultValue)
        builder.addStatement("writer.%M(%L)", MemberName("", "writeByte"), opcode)
        val name = MemberName("", "write${param.returnType.toString().split(".").last()}")
        builder.addStatement("writer.%M(definition.%N)", name, param.name)
        builder.endControlFlow()
    }

    override fun read(builder: FunSpec.Builder, opcode: Int, param: KProperty1<out Definition, *>) {
        TODO("Not yet implemented")
    }
}