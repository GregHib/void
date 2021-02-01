@file:OptIn(ExperimentalSerializationApi::class)

package world.gregs.voidps.cache.format.definition.internal

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.AbstractEncoder
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.modules.SerializersModule
import world.gregs.voidps.buffer.write.BufferWriter
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.format.definition.Definition
import world.gregs.voidps.cache.format.definition.Operation

internal open class DefinitionEncoder(
    private val definition: Definition,
    private val encodeDefaults: Boolean,
) : AbstractEncoder() {
    override val serializersModule: SerializersModule
        get() = definition.serializersModule

    private val writer: Writer = BufferWriter()

    override fun shouldEncodeElementDefault(descriptor: SerialDescriptor, index: Int) = encodeDefaults

    override fun encodeElement(descriptor: SerialDescriptor, index: Int): Boolean {
        val code = descriptor.getOperationOrNull(index) ?: return false
        encodeByte(code.toByte())
        return true
    }

    override fun encodeByte(value: Byte) {
        writer.writeByte(value.toInt())
    }

    override fun encodeInt(value: Int) {
        writer.writeInt(value)
    }

    override fun encodeString(value: String) {
        writer.writeString(value)
    }

    internal fun toByteArray(): ByteArray = writer.toArray()
}