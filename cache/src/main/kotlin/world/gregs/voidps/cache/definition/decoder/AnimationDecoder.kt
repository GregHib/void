package world.gregs.voidps.cache.definition.decoder

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.cache.DefinitionDecoder
import world.gregs.voidps.cache.Indices.ANIMATIONS
import world.gregs.voidps.cache.definition.data.AnimationDefinition

/**
 * @author GregHib <greg@gregs.world>
 * @since April 08, 2020
 */
class AnimationDecoder(cache: world.gregs.voidps.cache.Cache) : DefinitionDecoder<AnimationDefinition>(cache, ANIMATIONS) {

    override fun create() = AnimationDefinition()

    override fun getFile(id: Int) = id and 0x7f

    override fun getArchive(id: Int) = id ushr 7

    override fun AnimationDefinition.read(opcode: Int, buffer: Reader) {
        when (opcode) {
            1 -> {
                val length = buffer.readShort()
                durations = IntArray(length) { buffer.readShort() }
                val frames = IntArray(length) { buffer.readShort() }
                repeat(length) { count ->
                    frames[count] = (buffer.readShort() shl 16) + frames[count]
                }
                primaryFrames = frames
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
                secondaryFrames = IntArray(length) { buffer.readShort() }
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
                    anIntArray701 = IntArray(anIntArrayArray700!!.size) { 255 }
                }
                anIntArray701!![buffer.readUnsignedByte()] = buffer.readUnsignedByte()
            }
            20 -> {
                if (anIntArray690 == null || anIntArray692 == null) {
                    anIntArray690 = IntArray(anIntArrayArray700!!.size) { 256 }
                    anIntArray692 = IntArray(anIntArrayArray700!!.size) { 256 }
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