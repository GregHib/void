package rs.dusk.cache.definition.decoder

import rs.dusk.cache.Cache
import rs.dusk.cache.DefinitionDecoder
import rs.dusk.cache.Indices.INTERFACES
import rs.dusk.cache.definition.data.InterfaceComponentDefinition
import rs.dusk.cache.definition.data.InterfaceComponentSetting
import rs.dusk.cache.definition.data.InterfaceDefinition
import rs.dusk.core.io.read.BufferReader
import rs.dusk.core.io.read.Reader

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 08, 2020
 */
class InterfaceDecoder(cache: Cache) : DefinitionDecoder<InterfaceDefinition>(cache, INTERFACES) {

    override fun create() = InterfaceDefinition()

    override val size: Int
        get() = cache.lastIndexId(index)

    override fun readData(id: Int): InterfaceDefinition? {
        val archive = getArchive(id)
        val lastArchive = cache.lastFileId(index, archive)
        if (lastArchive == -1) {
            return null
        }
        val definition = create()
        definition.components = (0..lastArchive).map { file ->
            val component = InterfaceComponentDefinition(id = file + (id shl 16))
            val data = cache.getFile(index, archive, file)
            if (data != null) {
                component.read(BufferReader(data))
            }
            file to component
        }.toMap()
        return definition
    }

    fun InterfaceComponentDefinition.read(buffer: Reader) {
        var i = buffer.readUnsignedByte()
        if (i == 255) {
            i = -1
        }
        type = buffer.readUnsignedByte()
        if (type and 0x80 != 0) {
            type = type and 0x7f
            aString4765 = buffer.readString()
        }
        contentType = buffer.readShort()
        basePositionX = buffer.readUnsignedShort()
        basePositionY = buffer.readUnsignedShort()
        baseWidth = buffer.readShort()
        baseHeight = buffer.readShort()
        horizontalSizeMode = buffer.readByte().toByte()
        verticalSizeMode = buffer.readByte().toByte()
        horizontalPositionMode = buffer.readByte().toByte()
        verticalPositionMode = buffer.readByte().toByte()
        parent = buffer.readShort()
        parent = if (parent == 65535) {
            -1
        } else {
            parent + (id and -65536)
        }
        val i_17_ = buffer.readUnsignedByte()
        hidden = 0x1 and i_17_ != 0
        if (i >= 0) {
            disableHover = i_17_ and 0x2 != 0
        }
        if (type == 0) {
            scrollWidth = buffer.readShort()
            scrollHeight = buffer.readShort()
            if (i < 0) {
                disableHover = buffer.readUnsignedByte() == 1
            }
        }
        if (type == 3) {
            colour = buffer.readInt()
            filled = buffer.readUnsignedByte() == 1
            alpha = buffer.readUnsignedByte()
        }
        if (type == 4) {
            fontId = buffer.readShort()
            if (fontId == 65535) {
                fontId = -1
            }
            if (i >= 2) {
                monochrome = buffer.readUnsignedByte() == 1
            }
            text = buffer.readString()
            lineHeight = buffer.readUnsignedByte()
            horizontalTextAlign = buffer.readUnsignedByte()
            verticalTextAlign = buffer.readUnsignedByte()
            shaded = buffer.readUnsignedByte() == 1
            colour = buffer.readInt()
            alpha = buffer.readUnsignedByte()
            if (i >= 0) {
                lineCount = buffer.readUnsignedByte()
            }
        }
        if (type == 5) {
            defaultImage = buffer.readInt()
            imageRotation = buffer.readShort()
            val i_18_ = buffer.readUnsignedByte()
            aBoolean4861 = i_18_ and 0x1 != 0
            imageRepeat = i_18_ and 0x2 != 0
            alpha = buffer.readUnsignedByte()
            rotation = buffer.readUnsignedByte()
            backgroundColour = buffer.readInt()
            flipVertical = buffer.readUnsignedByte() == 1
            flipHorizontal = buffer.readUnsignedByte() == 1
            colour = buffer.readInt()
            if (i >= 3) {
                aBoolean4782 = buffer.readUnsignedByte() == 1
            }
        }
        if (type == 6) {
            defaultMediaType = 1
            defaultMediaId = buffer.readShort()
            if (defaultMediaId == 65535) {
                defaultMediaId = -1
            }
            val i_19_ = buffer.readUnsignedByte()
            aBoolean4707 = 0x4 and i_19_ == 4
            val bool = 0x1 and i_19_ == 1
            centreType = i_19_ and 0x2 == 2
            ignoreZBuffer = 0x8 and i_19_ == 8
            if (bool) {
                viewportX = buffer.readUnsignedShort()
                viewportY = buffer.readUnsignedShort()
                spritePitch = buffer.readShort()
                spriteRoll = buffer.readShort()
                spriteYaw = buffer.readShort()
                spriteScale = buffer.readShort()
            } else if (centreType) {
                viewportX = buffer.readUnsignedShort()
                viewportY = buffer.readUnsignedShort()
                viewportZ = buffer.readUnsignedShort()
                spritePitch = buffer.readShort()
                spriteRoll = buffer.readShort()
                spriteYaw = buffer.readShort()
                spriteScale = buffer.readUnsignedShort()
            }
            animation = buffer.readShort()
            if (animation == 65535) {
                animation = -1
            }
            if (horizontalSizeMode.toInt() != 0) {
                viewportWidth = buffer.readShort()
            }
            if (verticalSizeMode.toInt() != 0) {
                viewportHeight = buffer.readShort()
            }
        }
        if (type == 9) {
            lineWidth = buffer.readUnsignedByte()
            colour = buffer.readInt()
            lineMirrored = buffer.readUnsignedByte() == 1
        }
        val setting = buffer.readMedium()
        var i_21_ = buffer.readUnsignedByte()
        if (i_21_ != 0) {
            keyRepeat = ByteArray(11)
            keyCodes = ByteArray(11)
            keyModifiers = IntArray(11)
            while ( /**/i_21_ != 0) {
                val i_22_ = (i_21_ shr 4) - 1
                i_21_ = buffer.readUnsignedByte() or i_21_ shl 8
                i_21_ = i_21_ and 0xfff
                if (i_21_ == 4095) {
                    i_21_ = -1
                }
                val b_23_ = buffer.readByte().toByte()
                if (b_23_.toInt() != 0) {
                    aBoolean4802 = true
                }
                val b_24_ = buffer.readByte().toByte()
                keyModifiers!![i_22_] = i_21_
                keyRepeat!![i_22_] = b_23_
                keyCodes!![i_22_] = b_24_
                i_21_ = buffer.readUnsignedByte()
            }
        }
        applyText = buffer.readString()
        val i_25_ = buffer.readUnsignedByte()
        val optionCount = i_25_ and 0xf
        if (optionCount > 0) {
            options = (0 until optionCount).map { buffer.readString() }.toTypedArray()
        }
        val i_28_ = i_25_ shr 4
        if (i_28_ > 0) {
            val i_29_ = buffer.readUnsignedByte()
            anIntArray4863 = IntArray(i_29_ + 1)
            var i_30_ = 0
            while (anIntArray4863!!.size > i_30_) {
                anIntArray4863!![i_30_] = -1
                i_30_++
            }
            anIntArray4863!![i_29_] = buffer.readShort()
        }
        if (i_28_ > 1) {
            val i_31_ = buffer.readUnsignedByte()
            anIntArray4863!![i_31_] = buffer.readShort()
        }
        aString4784 = buffer.readString()
        if (aString4784 == "") {
            aString4784 = null
        }
        anInt4708 = buffer.readUnsignedByte()
        anInt4795 = buffer.readUnsignedByte()
        anInt4860 = buffer.readUnsignedByte()
        aString4786 = buffer.readString()
        var i_32_ = -1
        if (setting and 0x3fda8 shr 11 != 0) {
            i_32_ = buffer.readShort()
            if (i_32_ == 65535) {
                i_32_ = -1
            }
            anInt4698 = buffer.readShort()
            if (anInt4698 == 65535) {
                anInt4698 = -1
            }
            anInt4839 = buffer.readShort()
            if (anInt4839 == 65535) {
                anInt4839 = -1
            }
        }
        if (i >= 0) {
            anInt4761 = buffer.readShort()
            if (anInt4761 == 65535) {
                anInt4761 = -1
            }
        }
        this.setting = InterfaceComponentSetting(setting, i_32_)
        if (i >= 0) {
            val i_33_ = buffer.readUnsignedByte()
            var i_34_ = 0
            while (i_33_ > i_34_) {
                val i_35_ = buffer.readMedium()
                val i_36_ = buffer.readInt()
                params!![i_35_.toLong()] = i_36_
                i_34_++
            }
            val i_37_ = buffer.readUnsignedByte()
            for (i_38_ in 0 until i_37_) {
                val i_39_ = buffer.readMedium()
                val string = buffer.readString()
                params!![i_39_.toLong()] = string
            }
        }
        anObjectArray4758 = decodeScript(buffer)
        mouseEnterHandler = decodeScript(buffer)
        mouseExitHandler = decodeScript(buffer)
        anObjectArray4771 = decodeScript(buffer)
        anObjectArray4768 = decodeScript(buffer)
        stateChangeHandler = decodeScript(buffer)
        invUpdateHandler = decodeScript(buffer)
        refreshHandler = decodeScript(buffer)
        updateHandler = decodeScript(buffer)
        anObjectArray4770 = decodeScript(buffer)
        if (i >= 0) {
            anObjectArray4751 = decodeScript(buffer)
        }
        mouseMotionHandler = decodeScript(buffer)
        mousePressedHandler = decodeScript(buffer)
        mouseDraggedHandler = decodeScript(buffer)
        mouseReleasedHandler = decodeScript(buffer)
        mouseDragPassHandler = decodeScript(buffer)
        anObjectArray4852 = decodeScript(buffer)
        anObjectArray4711 = decodeScript(buffer)
        anObjectArray4753 = decodeScript(buffer)
        anObjectArray4688 = decodeScript(buffer)
        anObjectArray4775 = decodeScript(buffer)
        clientVarp = decodeIntArray(buffer)
        containers = decodeIntArray(buffer)
        anIntArray4789 = decodeIntArray(buffer)
        clientVarc = decodeIntArray(buffer)
        anIntArray4805 = decodeIntArray(buffer)
    }

    override fun InterfaceDefinition.read(opcode: Int, buffer: Reader) {
        throw IllegalStateException("Shouldn't be used.")
    }

    companion object {

        private fun InterfaceComponentDefinition.decodeScript(buffer: Reader): Array<Any?>? {
            val length = buffer.readUnsignedByte()
            if (length == 0) {
                return null
            }
            val objects = arrayOfNulls<Any>(length)
            repeat(length) {
                val string = buffer.readUnsignedBoolean()
                objects[it] = if (string) buffer.readString() else buffer.readInt()
            }
            hasScript = true
            return objects
        }

        private fun decodeIntArray(buffer: Reader): IntArray? {
            val length = buffer.readUnsignedByte()
            if (length == 0) {
                return null
            }
            val data = IntArray(length)
            for (index in 0 until length) {
                data[index] = buffer.readInt()
            }
            return data
        }
    }
}