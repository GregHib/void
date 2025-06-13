package world.gregs.voidps.cache.definition.decoder

import world.gregs.voidps.buffer.read.BufferReader
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.DefinitionDecoder
import world.gregs.voidps.cache.Index.INTERFACES
import world.gregs.voidps.cache.definition.data.InterfaceComponentDefinitionFull
import world.gregs.voidps.cache.definition.data.InterfaceComponentSetting
import world.gregs.voidps.cache.definition.data.InterfaceDefinitionFull

class InterfaceDecoderFull : DefinitionDecoder<InterfaceDefinitionFull>(INTERFACES) {

    override fun create(size: Int) = Array(size) { InterfaceDefinitionFull(it) }

    override fun size(cache: Cache): Int = cache.lastArchiveId(index)

    override fun load(definitions: Array<InterfaceDefinitionFull>, reader: Reader) {
        val packed = readId(reader)
        val id = InterfaceDefinitionFull.id(packed)
        val definition = definitions[id]
        if (definition.components == null) {
            definition.components = Array(InterfaceDefinitionFull.componentId(packed) + 1) { InterfaceComponentDefinitionFull(id = it + (id shl 16)) }
        }
        definition.components!![InterfaceDefinitionFull.componentId(packed)].read(reader)
    }

    override fun load(definitions: Array<InterfaceDefinitionFull>, cache: Cache, id: Int) {
        val archiveId = getArchive(id)
        val lastArchive = cache.lastFileId(index, archiveId)
        if (lastArchive == -1) {
            return
        }
        val definition = definitions[id]
        val components = Array(lastArchive + 1) { InterfaceComponentDefinitionFull(id = it + (id shl 16)) }
        for (i in 0..lastArchive) {
            val data = cache.data(index, archiveId, i)
            if (data != null) {
                components[i].read(BufferReader(data))
            }
        }
        definition.components = components
    }

    fun InterfaceComponentDefinitionFull.read(buffer: Reader) {
        buffer.readUnsignedByte()
        type = buffer.readUnsignedByte()
        if (type and 0x80 != 0) {
            type = type and 0x7f
            unknown = buffer.readString()
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
        if (parent == 65535) {
            parent = -1
        }
        val flag = buffer.readUnsignedByte()
        hidden = 0x1 and flag != 0
        if (type == 0) {
            scrollWidth = buffer.readShort()
            scrollHeight = buffer.readShort()
            disableHover = buffer.readUnsignedByte() == 1
        } else if (type == 3) {
            colour = buffer.readInt()
            filled = buffer.readUnsignedByte() == 1
            alpha = buffer.readUnsignedByte()
        } else if (type == 4) {
            fontId = buffer.readShort()
            if (fontId == 65535) {
                fontId = -1
            }
            text = buffer.readString()
            lineHeight = buffer.readUnsignedByte()
            horizontalTextAlign = buffer.readUnsignedByte()
            verticalTextAlign = buffer.readUnsignedByte()
            shaded = buffer.readUnsignedByte() == 1
            colour = buffer.readInt()
            alpha = buffer.readUnsignedByte()
        } else if (type == 5) {
            defaultImage = buffer.readInt()
            imageRotation = buffer.readShort()
            val type = buffer.readUnsignedByte()
            aBoolean4861 = type and 0x1 != 0
            imageRepeat = type and 0x2 != 0
            alpha = buffer.readUnsignedByte()
            rotation = buffer.readUnsignedByte()
            backgroundColour = buffer.readInt()
            flipVertical = buffer.readUnsignedByte() == 1
            flipHorizontal = buffer.readUnsignedByte() == 1
            colour = buffer.readInt()
        } else if (type == 6) {
            defaultMediaType = 1
            defaultMediaId = buffer.readShort()
            if (defaultMediaId == 65535) {
                defaultMediaId = -1
            }
            val modelFlag = buffer.readUnsignedByte()
            animated = 0x4 and modelFlag == 4
            val bool = 0x1 and modelFlag == 1
            centreType = modelFlag and 0x2 == 2
            ignoreZBuffer = 0x8 and modelFlag == 8
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
        } else if (type == 9) {
            lineWidth = buffer.readUnsignedByte()
            colour = buffer.readInt()
            lineMirrored = buffer.readUnsignedByte() == 1
        }
        val setting = buffer.readUnsignedMedium()
        var mod = buffer.readUnsignedByte()
        if (mod != 0) {
            keyRepeats = ByteArray(11)
            keyCodes = ByteArray(11)
            keyModifiers = IntArray(11)
            while (mod != 0) {
                val index = (mod shr 4) - 1
                mod = buffer.readUnsignedByte() or mod shl 8
                mod = mod and 0xfff
                val repeat = buffer.readByte().toByte()
                if (repeat.toInt() != 0) {
                    clickable = true
                }
                val code = buffer.readByte().toByte()
                keyModifiers!![index] = mod
                keyRepeats!![index] = repeat
                keyCodes!![index] = code
                mod = buffer.readUnsignedByte()
            }
        }
        name = buffer.readString()
        val mouseFlag = buffer.readUnsignedByte()
        val optionCount = mouseFlag and 0xf
        if (optionCount > 0) {
            options = Array(optionCount) { buffer.readString() }
        }
        val iconCount = mouseFlag shr 4
        if (iconCount > 0) {
            val size = buffer.readUnsignedByte()
            mouseIcon = IntArray(size + 1) { if (size + 1 > it) -1 else 0 }
            mouseIcon!![size] = buffer.readShort()
        }
        if (iconCount > 1) {
            mouseIcon!![buffer.readUnsignedByte()] = buffer.readShort()
        }
        optionOverride = buffer.readString()
        if (optionOverride == "") {
            optionOverride = null
        }
        anInt4708 = buffer.readUnsignedByte()
        anInt4795 = buffer.readUnsignedByte()
        anInt4860 = buffer.readUnsignedByte()
        useOption = buffer.readString()
        var settingData = -1
        if (setting and 0x3fda8 shr 11 != 0) {
            settingData = buffer.readShort()
            if (settingData == 65535) {
                settingData = -1
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
        this.setting = InterfaceComponentSetting(setting, settingData)
        information = decodeScript(buffer)
        mouseEnterHandler = decodeScript(buffer)
        mouseExitHandler = decodeScript(buffer)
        anObjectArray4771 = decodeScript(buffer)
        anObjectArray4768 = decodeScript(buffer)
        stateChangeHandler = decodeScript(buffer)
        invUpdateHandler = decodeScript(buffer)
        refreshHandler = decodeScript(buffer)
        updateHandler = decodeScript(buffer)
        anObjectArray4770 = decodeScript(buffer)
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
        inventories = decodeIntArray(buffer)
        anIntArray4789 = decodeIntArray(buffer)
        clientVarc = decodeIntArray(buffer)
        anIntArray4805 = decodeIntArray(buffer)
    }

    override fun InterfaceDefinitionFull.read(opcode: Int, buffer: Reader) = throw IllegalStateException("Shouldn't be used.")

    companion object {
        private fun InterfaceComponentDefinitionFull.decodeScript(buffer: Reader): Array<Any>? {
            val length = buffer.readUnsignedByte()
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

        private fun decodeIntArray(buffer: Reader): IntArray? {
            val length = buffer.readUnsignedByte()
            if (length == 0) {
                return null
            }
            return IntArray(length) { buffer.readInt() }
        }
    }
}
