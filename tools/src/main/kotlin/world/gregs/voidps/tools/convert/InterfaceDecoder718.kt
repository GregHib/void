package world.gregs.voidps.tools.convert

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.cache.definition.data.InterfaceComponentDefinitionFull

class InterfaceDecoder718 {

    fun InterfaceComponentDefinitionFull.read(reader: Reader) {
        var flag0: Int = reader.readUnsignedByte()
        if (flag0 == 255) {
            flag0 = -1
        }
        type = reader.readUnsignedByte()
        if (type and 0x80 != 0) {
            type = type and 0x7f
            unknown = reader.readString()
        }
        contentType = reader.readUnsignedShort()
        basePositionX = reader.readShort()
        basePositionY = reader.readShort()
        baseWidth = reader.readUnsignedShort()
        baseHeight = reader.readUnsignedShort()
        horizontalSizeMode = reader.readByte().toByte()
        verticalSizeMode = reader.readByte().toByte()
        horizontalPositionMode = reader.readByte().toByte()
        verticalPositionMode = reader.readByte().toByte()
        parent = reader.readUnsignedShort()
        if (parent == 65535) {
            parent = -1
        }
        val flag1: Int = reader.readUnsignedByte()
        hidden = flag1 and 0x1 != 0
        if (flag0 >= 0) {
            disableHover = flag1 and 0x2 != 0
        }
        if (type == 0) {
            scrollWidth = reader.readUnsignedShort()
            scrollHeight = reader.readUnsignedShort()
            if (flag0 < 0) {
                disableHover = reader.readUnsignedByte() == 1
            }
        }
        if (type == 5) {
            defaultImage = reader.readInt()
            imageRotation = reader.readUnsignedShort()
            val flag3: Int = reader.readUnsignedByte()
            val aBoolean1196 = flag3 and 0x1 != 0
            imageRepeat = flag3 and 0x2 != 0
            alpha = reader.readUnsignedByte()
            rotation = reader.readUnsignedByte()
            backgroundColour = reader.readInt()
            flipVertical = reader.readUnsignedByte() == 1
            flipHorizontal = reader.readUnsignedByte() == 1
            colour = reader.readInt()
            if (flag0 >= 3) {
                val aBoolean1183 = reader.readUnsignedByte() == 1
            }
        }
        if (type == 6) {
            defaultMediaType = 1
            defaultMediaId = reader.readBigSmart()
            val flag: Int = reader.readUnsignedByte()
            val bool = flag and 0x1 == 1
            centreType = flag and 0x2 == 2
            animated = flag and 0x4 == 4
            ignoreZBuffer = flag and 0x8 == 8
            if (bool) {
                viewportX = reader.readShort()
                viewportY = reader.readShort()
                spritePitch = reader.readUnsignedShort()
                spriteRoll = reader.readUnsignedShort()
                spriteYaw = reader.readUnsignedShort()
                spriteScale = reader.readUnsignedShort()
            } else if (centreType) {
                viewportX = reader.readShort()
                viewportY = reader.readShort()
                viewportZ = reader.readShort()
                spritePitch = reader.readUnsignedShort()
                spriteRoll = reader.readUnsignedShort()
                spriteYaw = reader.readUnsignedShort()
                spriteScale = reader.readShort()
            }
            animation = reader.readBigSmart()
            if (horizontalSizeMode.toInt() != 0) viewportWidth = reader.readUnsignedShort()
            if (verticalSizeMode.toInt() != 0) viewportHeight = reader.readUnsignedShort()
        }
        if (type == 4) {
            fontId = reader.readBigSmart()
            if (flag0 >= 2) {
                val aBoolean1211 = reader.readUnsignedByte() == 1
            }
            text = reader.readString()
            lineHeight = reader.readUnsignedByte()
            horizontalTextAlign = reader.readUnsignedByte()
            verticalTextAlign = reader.readUnsignedByte()
            shaded = reader.readUnsignedByte() == 1
            colour = reader.readInt()
            alpha = reader.readUnsignedByte()
            if (flag0 >= 0) {
                val anInt1217 = reader.readUnsignedByte()
            }
        }
        if (type == 3) {
            colour = reader.readInt()
            filled = reader.readUnsignedByte() == 1
            alpha = reader.readUnsignedByte()
        }
        if (type == 9) {
            lineWidth = reader.readUnsignedByte()
            colour = reader.readInt()
            lineMirrored = reader.readUnsignedByte() == 1
        }
        val setting: Int = reader.readUnsignedMedium()
        var modifier: Int = reader.readUnsignedByte()
        if (modifier != 0) {
            keyRepeats = ByteArray(11)
            keyCodes = ByteArray(11)
            keyModifiers = IntArray(11)
            while (modifier != 0) {
                val index = (modifier shr 4) - 1
                modifier = modifier shl 8 or reader.readUnsignedByte()
                modifier = modifier and 0xfff
                if (modifier == 4095) {
                    modifier = -1
                }
                val repeat: Byte = reader.readByte().toByte()
                if (repeat.toInt() != 0) {
                    val aBoolean1220 = true
                }
                val code: Byte = reader.readByte().toByte()
                keyModifiers!![index] = modifier
                keyRepeats!![index] = repeat
                keyCodes!![index] = code
                modifier = reader.readUnsignedByte()
            }
        }
        name = reader.readString()
        val flag4: Int = reader.readUnsignedByte()
        val optionCount = flag4 and 0xf
        val iconCount = flag4 shr 4
        if (optionCount > 0) {
            options = Array(optionCount) {
                reader.readString()
            }
        }
        if (iconCount > 0) {
            val size: Int = reader.readUnsignedByte()
            mouseIcon = IntArray(size + 1)
            for (index in mouseIcon!!.indices) mouseIcon!![index] = -1
            mouseIcon!![size] = reader.readUnsignedShort()
        }
        if (iconCount > 1) {
            val index: Int = reader.readUnsignedByte()
            mouseIcon!![index] = reader.readUnsignedShort()
        }
        optionOverride = reader.readString()
        if (optionOverride == "") optionOverride = null
        anInt4708 = reader.readUnsignedByte()
        anInt4795 = reader.readUnsignedByte()
        anInt4860 = reader.readUnsignedByte()
        useOption = reader.readString()
        var i17 = -1
        if (setting shr 11 and 0x7f != 0) {
            i17 = reader.readUnsignedShort()
            if (i17 == 65535) {
                i17 = -1
            }
            anInt4698 = reader.readUnsignedShort()
            if (anInt4698 == 65535) {
                anInt4698 = -1
            }
            anInt4839 = reader.readUnsignedShort()
            if (anInt4839 == 65535) {
                anInt4839 = -1
            }
        }
        if (flag0 >= 0) {
            var anInt1272 = reader.readUnsignedShort()
            if (anInt1272 == 65535) {
                anInt1272 = -1
            }
        }
//            aClass298_Sub38_1219 = Class298_Sub38(i_5_, i_17_)
        if (flag0 >= 0) {
            val size: Int = reader.readUnsignedByte()
            for (i19 in 0 until size) {
                val i20: Int = reader.readUnsignedMedium()
                val i21: Int = reader.readInt()
//                    this.aClass437_1279.method5817(Class298_Sub35(i_21_), i_20_.toLong())
            }
            val i22: Int = reader.readUnsignedByte()
            for (index in 0 until i22) {
                val i24: Int = reader.readUnsignedMedium()
                val string: String = reader.readString()
//                    this.aClass437_1279.method5817(string, i_24_.toLong())
            }
        }
        information = decodeScript(reader)
        mouseEnterHandler = decodeScript(reader)
        mouseExitHandler = decodeScript(reader)
        anObjectArray4771 = decodeScript(reader)
        anObjectArray4768 = decodeScript(reader)
        stateChangeHandler = decodeScript(reader)
        invUpdateHandler = decodeScript(reader)
        refreshHandler = decodeScript(reader)
        updateHandler = decodeScript(reader)
        anObjectArray4770 = decodeScript(reader)
        if (flag0 >= 0) {
            val anObjectArray1247 = decodeScript(reader)
        }
        mouseMotionHandler = decodeScript(reader)
        mousePressedHandler = decodeScript(reader)
        mouseDraggedHandler = decodeScript(reader)
        mouseReleasedHandler = decodeScript(reader)
        mouseDragPassHandler = decodeScript(reader)
        anObjectArray4852 = decodeScript(reader)
        anObjectArray4711 = decodeScript(reader)
        anObjectArray4753 = decodeScript(reader)
        anObjectArray4688 = decodeScript(reader)
        anObjectArray4775 = decodeScript(reader)
        clientVarp = decodeIntArray(reader)
        inventories = decodeIntArray(reader)
        anIntArray4789 = decodeIntArray(reader)
        clientVarc = decodeIntArray(reader)
        anIntArray4805 = decodeIntArray(reader)
    }

    private fun InterfaceComponentDefinitionFull.decodeScript(buffer: Reader): Array<Any>? {
        val length = if (buffer.remaining > 0) buffer.readUnsignedByte() else 0
        if (length == 0) {
            return null
        }
        val objects = Array<Any>(length) {
            val string = buffer.readUnsignedBoolean()
            if (string) buffer.readString() else buffer.readInt()
        }
        hasScript = true
        return objects
    }

    private fun decodeIntArray(reader: Reader): IntArray? {
        return try {
            val length: Int = if (reader.remaining <= 0) 0 else reader.readUnsignedByte()
            if (0 == length) return null
            val array = IntArray(length)
            for (i in 0 until length) array[i] = reader.readInt()
            array
        } catch (exception: RuntimeException) {
            exception.printStackTrace()
            null
        }
    }
}
