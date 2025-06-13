package world.gregs.voidps.cache.config.decoder

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.cache.Config.RENDER_ANIMATIONS
import world.gregs.voidps.cache.config.ConfigDecoder
import world.gregs.voidps.cache.config.data.RenderAnimationDefinition

class RenderAnimationDecoder : ConfigDecoder<RenderAnimationDefinition>(RENDER_ANIMATIONS) {

    override fun create(size: Int) = Array(size) { RenderAnimationDefinition(it) }

    override fun RenderAnimationDefinition.read(opcode: Int, buffer: Reader) {
        when (opcode) {
            1 -> {
                primaryIdle = buffer.readShort()
                primaryWalk = buffer.readShort()
                if (primaryIdle == 65535) {
                    primaryIdle = -1
                }
                if (primaryWalk == 65535) {
                    primaryWalk = -1
                }
            }
            2 -> anInt3262 = buffer.readShort()
            3 -> anInt3297 = buffer.readShort()
            4 -> anInt3269 = buffer.readShort()
            5 -> anInt3304 = buffer.readShort()
            6 -> run = buffer.readShort()
            7 -> anInt3271 = buffer.readShort()
            8 -> anInt3270 = buffer.readShort()
            9 -> anInt3293 = buffer.readShort()
            26 -> {
                anInt3261 = (4 * buffer.readUnsignedByte()).toShort().toInt()
                anInt3266 = (4 * buffer.readUnsignedByte()).toShort().toInt()
            }
            27 -> {
                if (anIntArrayArray3273 == null) {
                    anIntArrayArray3273 = arrayOfNulls(DEFAULTS_SIZE)
                }
                val length = buffer.readUnsignedByte()
                anIntArrayArray3273!![length] = IntArray(6)
                for (index in 0 until 5) {
                    anIntArrayArray3273!![length]!![index] = buffer.readUnsignedShort()
                }
            }
            28 -> {
                val length = buffer.readUnsignedByte()
                anIntArray3276 = IntArray(length)
                for (count in 0 until length) {
                    anIntArray3276!![count] = buffer.readUnsignedByte()
                    if (anIntArray3276!![count] == 255) {
                        anIntArray3276!![count] = -1
                    }
                }
            }
            29 -> anInt3258 = buffer.readUnsignedByte()
            30 -> anInt3283 = buffer.readShort()
            31 -> anInt3278 = buffer.readUnsignedByte()
            32 -> anInt3284 = buffer.readShort()
            33 -> anInt3250 = buffer.readUnsignedShort()
            34 -> anInt3272 = buffer.readUnsignedByte()
            35 -> anInt3289 = buffer.readShort()
            36 -> anInt3285 = buffer.readUnsignedShort()
            37 -> anInt3256 = buffer.readUnsignedByte()
            38 -> turning = buffer.readShort()
            39 -> secondaryWalk = buffer.readShort()
            40 -> walkBackwards = buffer.readShort()
            41 -> sideStepLeft = buffer.readShort()
            42 -> sideStepRight = buffer.readShort()
            43 -> anInt3290 = buffer.readShort()
            44 -> anInt3292 = buffer.readShort()
            45 -> anInt3303 = buffer.readShort()
            46 -> anInt3275 = buffer.readShort()
            47 -> anInt3260 = buffer.readShort()
            48 -> anInt3282 = buffer.readShort()
            49 -> anInt3253 = buffer.readShort()
            50 -> anInt3298 = buffer.readShort()
            51 -> anInt3305 = buffer.readShort()
            52 -> {
                val length = buffer.readUnsignedByte()
                anIntArray3294 = IntArray(length)
                anIntArray3302 = IntArray(length)
                for (index in 0 until length) {
                    anIntArray3294!![index] = buffer.readShort()
                    val value = buffer.readUnsignedByte()
                    anIntArray3302!![index] = value
                    anInt3281 += value
                }
            }
            53 -> aBoolean3267 = false
            54 -> {
                anInt3263 = buffer.readUnsignedByte() shl 6
                anInt3291 = buffer.readUnsignedByte() shl 6
            }
            55 -> {
                if (anIntArray3255 == null) {
                    anIntArray3255 = IntArray(DEFAULTS_SIZE)
                }
                val index = buffer.readUnsignedByte()
                anIntArray3255!![index] = buffer.readShort()
            }
            56 -> {
                if (anIntArrayArray3249 == null) {
                    anIntArrayArray3249 = arrayOfNulls(DEFAULTS_SIZE)
                }
                val length = buffer.readUnsignedByte()
                anIntArrayArray3249!![length] = IntArray(3)
                for (index in 0 until 2) {
                    anIntArrayArray3249!![length]!![index] = buffer.readUnsignedShort()
                }
            }
        }
    }

    companion object {
        const val DEFAULTS_SIZE = 15
    }
}
