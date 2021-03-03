package world.gregs.voidps.tools.cache

import com.squareup.kotlinpoet.FunSpec
import world.gregs.voidps.cache.Definition
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1


@Target(AnnotationTarget.PROPERTY)
annotation class Process(val with: KClass<Scribe>)

interface Scribe {
    fun write(builder: FunSpec.Builder, opcode: Int, param: KProperty1<out Definition, *>, default: Definition)

    fun read(builder: FunSpec.Builder, opcode: Int, param: KProperty1<out Definition, *>)
}