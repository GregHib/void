package world.gregs.voidps.tools.convert.osrs

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.cache.config.ConfigDecoder
import world.gregs.voidps.cache.definition.data.AnimationDefinitionFull

/**
 * Revision 215
 */
class AnimationDecoderFullOSRS : ConfigDecoder<AnimationDefinitionFull>(12) {

    override fun create(size: Int) = Array(size) { AnimationDefinitionFull(it) }

    override fun AnimationDefinitionFull.read(opcode: Int, buffer: Reader) {
        when (opcode) {
            1 -> {
                val length = buffer.readShort()
                durations = IntArray(length) { buffer.readShort() }
                frames = IntArray(length) { buffer.readShort() }
                for (count in 0 until length) {
                    frames!![count] = (buffer.readShort() shl 16) + frames!![count]
                }
            }
            2 -> loopOffset = buffer.readShort()
            3 -> {
                interleaveOrder = BooleanArray(256)
                val length = buffer.readUnsignedByte()
                for (count in 0 until length) {
                    interleaveOrder!![buffer.readUnsignedByte()] = true
                }
            }
            4 -> {
                // Stretches
            }
            5 -> priority = buffer.readUnsignedByte()
            6 -> leftHandItem = buffer.readShort()
            7 -> rightHandItem = buffer.readShort()
            8 -> maxLoops = buffer.readUnsignedByte()
            9 -> animatingPrecedence = buffer.readUnsignedByte()
            10 -> walkingPrecedence = buffer.readUnsignedByte()
            11 -> replayMode = buffer.readUnsignedByte()
            12 -> {
                val length = buffer.readUnsignedByte()
                expressionFrames = IntArray(length) { buffer.readShort() }
                for (count in 0 until length) {
                    expressionFrames!![count] = (buffer.readShort() shl 16) + expressionFrames!![count]
                }
            }
            13 -> {
                val length = buffer.readUnsignedByte()
                sounds = arrayOf(IntArray(length) { buffer.readUnsignedMedium() })
            }
            14 -> {
                val skeletalId = buffer.readInt()
            }
            15 -> {
                val count = buffer.readUnsignedShort()
                val sounds = mutableMapOf<Int, Int>()
                val skeletalSounds = sounds
                repeat(count) {
                    val id = buffer.readUnsignedShort()
                    val value = buffer.readMedium()
                    sounds[id] = value
                }
            }
            16 -> {
                val skeletalRangeBegin = buffer.readUnsignedShort()
                val skeletalRangeEnd = buffer.readUnsignedShort()
            }
            17 -> {
                val skeletalAttributes = BooleanArray(256) { false }
                val count = buffer.readUnsignedByte()
                repeat(count) {
                    skeletalAttributes[buffer.readUnsignedByte()] = true
                }
            }
        }
    }

    override fun changeValues(definitions: Array<AnimationDefinitionFull>, definition: AnimationDefinitionFull) {
    }
}