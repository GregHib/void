package rs.dusk.cache.definition.decoder

import rs.dusk.cache.Cache
import rs.dusk.cache.DefinitionDecoder
import rs.dusk.cache.Indices.ANIMATIONS
import rs.dusk.cache.definition.data.AnimationDefinition
import rs.dusk.core.io.read.Reader

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 08, 2020
 */
class AnimationDecoder(cache: Cache) : DefinitionDecoder<AnimationDefinition>(cache, ANIMATIONS) {

    override fun create() = AnimationDefinition()

    override fun getFile(id: Int) = id and 0x7f

    override fun getArchive(id: Int) = id ushr 7

    override fun AnimationDefinition.read(opcode: Int, buffer: Reader) {
        when (opcode) {
            1 -> {
                val length = buffer.readShort()
                durations = IntArray(length)
                repeat(length) { count ->
                    durations!![count] = buffer.readShort()
                }
                primaryFrames = IntArray(length)
                repeat(length) { count ->
                    primaryFrames!![count] = buffer.readShort()
                }
                repeat(length) { count ->
                    primaryFrames!![count] = (buffer.readShort() shl 16) + primaryFrames!![count]
                }
            }
            2 -> loopOffset = buffer.readShort()
            3 -> {
                interleaveOrder = BooleanArray(256)
                val length = buffer.readUnsignedByte()
                repeat(length) {
                    interleaveOrder!![buffer.readUnsignedByte()] = true
                }
            }
            5 -> priority = buffer.readUnsignedByte()
            6 -> leftHand = buffer.readShort()
            7 -> rightHand = buffer.readShort()
            8 -> maxLoops = buffer.readUnsignedByte()
            9 -> animatingPrecedence = buffer.readUnsignedByte()
            10 -> walkingPrecedence = buffer.readUnsignedByte()
            11 -> replayMode = buffer.readUnsignedByte()
            12 -> {
                val length = buffer.readUnsignedByte()
                secondaryFrames = IntArray(length)
                repeat(length) { count ->
                    secondaryFrames!![count] = buffer.readShort()
                }
                repeat(length) { count ->
                    secondaryFrames!![count] = (buffer.readShort() shl 16) + secondaryFrames!![count]
                }
            }
            13 -> {
                val length = buffer.readShort()
                anIntArrayArray700 = arrayOfNulls(length)
                repeat(length) { count ->
                    val size = buffer.readUnsignedByte()
                    if (size > 0) {
                        anIntArrayArray700!![count] = IntArray(size)
                        anIntArrayArray700!![count]!![0] = buffer.readUnsignedMedium()
                        for (index in 1 until size) {
                            anIntArrayArray700!![count]!![index] = buffer.readShort()
                        }
                    }
                }
            }
            14 -> aBoolean691 = true
            15 -> tweened = true
            18 -> aBoolean699 = true
            19 -> {
                if (anIntArray701 == null) {
                    anIntArray701 = IntArray(anIntArrayArray700!!.size)
                    for (index in anIntArrayArray700!!.indices) {
                        anIntArray701!![index] = 255
                    }
                }
                anIntArray701!![buffer.readUnsignedByte()] = buffer.readUnsignedByte()
            }
            20 -> {
                if (anIntArray690 == null || anIntArray692 == null) {
                    anIntArray690 = IntArray(anIntArrayArray700!!.size)
                    anIntArray692 = IntArray(anIntArrayArray700!!.size)
                    for (index in anIntArrayArray700!!.indices) {
                        anIntArray690!![index] = 256
                        anIntArray692!![index] = 256
                    }
                }
                val length = buffer.readUnsignedByte()
                anIntArray690!![length] = buffer.readShort()
                anIntArray692!![length] = buffer.readShort()
            }
        }
    }

    override fun AnimationDefinition.changeValues() {
        if (walkingPrecedence == -1) {
            walkingPrecedence = if (interleaveOrder == null) 0 else 2
        }
        if (animatingPrecedence == -1) {
            animatingPrecedence = if (interleaveOrder == null) 0 else 2
        }
    }
}