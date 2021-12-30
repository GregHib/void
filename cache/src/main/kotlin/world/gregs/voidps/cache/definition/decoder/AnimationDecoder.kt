package world.gregs.voidps.cache.definition.decoder

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.cache.DefinitionDecoder
import world.gregs.voidps.cache.Indices.ANIMATIONS
import world.gregs.voidps.cache.definition.data.AnimationDefinition

class AnimationDecoder(cache: world.gregs.voidps.cache.Cache) : DefinitionDecoder<AnimationDefinition>(cache, ANIMATIONS) {

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
                frames = IntArray(length)
                repeat(length) { count ->
                    frames!![count] = buffer.readShort()
                }
                repeat(length) { count ->
                    frames!![count] = (buffer.readShort() shl 16) + frames!![count]
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
            6 -> leftHandItem = buffer.readShort()
            7 -> rightHandItem = buffer.readShort()
            8 -> maxLoops = buffer.readUnsignedByte()
            9 -> animatingPrecedence = buffer.readUnsignedByte()
            10 -> walkingPrecedence = buffer.readUnsignedByte()
            11 -> replayMode = buffer.readUnsignedByte()
            12 -> {
                val length = buffer.readUnsignedByte()
                expressionFrames = IntArray(length)
                repeat(length) { count ->
                    expressionFrames!![count] = buffer.readShort()
                }
                repeat(length) { count ->
                    expressionFrames!![count] = (buffer.readShort() shl 16) + expressionFrames!![count]
                }
            }
            13 -> {
                val length = buffer.readShort()
                sounds = arrayOfNulls(length)
                repeat(length) { count ->
                    val size = buffer.readUnsignedByte()
                    if (size > 0) {
                        sounds!![count] = IntArray(size)
                        sounds!![count]!![0] = buffer.readUnsignedMedium()
                        for (index in 1 until size) {
                            sounds!![count]!![index] = buffer.readShort()
                        }
                    }
                }
            }
            14 -> aBoolean691 = true
            15 -> tweened = true
            18 -> useSounds = true
            19 -> {
                if (volumes == null) {
                    volumes = IntArray(sounds!!.size)
                    for (index in sounds!!.indices) {
                        volumes!![index] = 255
                    }
                }
                volumes!![buffer.readUnsignedByte()] = buffer.readUnsignedByte()
            }
            20 -> {
                if (primarySpeeds == null || secondarySpeeds == null) {
                    primarySpeeds = IntArray(sounds!!.size)
                    secondarySpeeds = IntArray(sounds!!.size)
                    for (index in sounds!!.indices) {
                        primarySpeeds!![index] = 256
                        secondarySpeeds!![index] = 256
                    }
                }
                val length = buffer.readUnsignedByte()
                primarySpeeds!![length] = buffer.readShort()
                secondarySpeeds!![length] = buffer.readShort()
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