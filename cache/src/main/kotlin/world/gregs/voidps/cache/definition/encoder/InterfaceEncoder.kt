package world.gregs.voidps.cache.definition.encoder

import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.DefinitionEncoder
import world.gregs.voidps.cache.definition.data.InterfaceComponentDefinitionFull

class InterfaceEncoder : DefinitionEncoder<InterfaceComponentDefinitionFull> {

    override fun Writer.encode(definition: InterfaceComponentDefinitionFull) {
        if (definition.id == -1) {
            return
        }
        writeByte(-1)
        if (!definition.unknown.isNullOrEmpty()) {
            writeByte(definition.type or 0x80)
            writeString(definition.unknown)
        } else {
            writeByte(definition.type)
        }
        writeShort(definition.contentType)
        writeShort(definition.basePositionX)
        writeShort(definition.basePositionY)
        writeShort(definition.baseWidth)
        writeShort(definition.baseHeight)
        writeByte(definition.horizontalSizeMode.toInt())
        writeByte(definition.verticalSizeMode.toInt())
        writeByte(definition.horizontalPositionMode.toInt())
        writeByte(definition.verticalPositionMode.toInt())
        writeShort(definition.parent)

        var hiddenFlag = 0
        if (definition.hidden) {
            hiddenFlag = hiddenFlag or 0x1
        }
        writeByte(hiddenFlag)

        when (definition.type) {
            0 -> {
                writeShort(definition.scrollWidth)
                writeShort(definition.scrollHeight)
                writeByte(definition.disableHover)
            }
            3 -> {
                writeInt(definition.colour)
                writeByte(definition.filled)
                writeByte(definition.alpha)
            }
            4 -> {
                writeShort(definition.fontId)
                writeString(definition.text)
                writeByte(definition.lineHeight)
                writeByte(definition.horizontalTextAlign)
                writeByte(definition.verticalTextAlign)
                writeByte(definition.shaded)
                writeInt(definition.colour)
                writeByte(definition.alpha)
            }
            5 -> {
                writeInt(definition.defaultImage)
                writeShort(definition.imageRotation)
                var imageFlag = 0
                if (definition.aBoolean4861) {
                    imageFlag = imageFlag or 0x1
                }
                if (definition.imageRepeat) {
                    imageFlag = imageFlag or 0x2
                }
                writeByte(imageFlag)
                writeByte(definition.alpha)
                writeByte(definition.rotation)
                writeInt(definition.backgroundColour)
                writeByte(definition.flipVertical)
                writeByte(definition.flipHorizontal)
                writeInt(definition.colour)
            }
            6 -> {
                writeShort(definition.defaultMediaId)
                var flag = 0
                if (definition.animated) {
                    flag = flag or 0x4
                }
                if (definition.centreType) {
                    flag = flag or 0x2
                }
                if (definition.ignoreZBuffer) {
                    flag = flag or 0x8
                }
                if (definition.viewportZ == 0 && (definition.viewportX != 0 || definition.viewportY != 0 || definition.spritePitch != 0 || definition.spriteRoll != 0 || definition.spriteYaw != 0 || definition.spriteScale != 100)) {
                    flag = flag or 0x1
                }
                writeByte(flag)
                if (flag and 0x1 == 1) {
                    writeShort(definition.viewportX)
                    writeShort(definition.viewportY)
                    writeShort(definition.spritePitch)
                    writeShort(definition.spriteRoll)
                    writeShort(definition.spriteYaw)
                    writeShort(definition.spriteScale)
                } else if (definition.centreType) {
                    writeShort(definition.viewportX)
                    writeShort(definition.viewportY)
                    writeShort(definition.viewportZ)
                    writeShort(definition.spritePitch)
                    writeShort(definition.spriteRoll)
                    writeShort(definition.spriteYaw)
                    writeShort(definition.spriteScale)
                }
                writeShort(definition.animation)
                if (definition.horizontalSizeMode.toInt() != 0) {
                    writeShort(definition.viewportWidth)
                }
                if (definition.verticalSizeMode.toInt() != 0) {
                    writeShort(definition.viewportHeight)
                }
            }
            9 -> {
                writeByte(definition.lineWidth)
                writeInt(definition.colour)
                writeByte(definition.lineMirrored)
            }
        }

        writeMedium(definition.setting.setting)
        var startOffset: Int
        val keyRepeat = definition.keyRepeats
        val keyCodes = definition.keyCodes
        val keyModifiers = definition.keyModifiers
        if (keyRepeat != null && keyCodes != null && keyModifiers != null) {
            startOffset = 16
            for (i in keyCodes) {
                if (i.toInt() != 0) {
                    break
                }
                startOffset += 16
            }
            writeByte(startOffset)
            var counter = (startOffset shr 4) - 1
            val length = 11
            while (counter != length) {
                writeByte(keyRepeat[counter].toInt())
                writeByte(keyCodes[counter].toInt())
                writeByte(keyModifiers[counter])
                counter += 1
                while (counter != length) {
                    if (keyCodes[counter].toInt() != 0) {
                        break
                    }
                    counter++
                }
                writeByte(if (counter == length) 0 else counter + 1 shl 4)
            }
        } else {
            writeByte(0)
        }
        writeString(definition.name)
        var optionFlag = 0
        val options = definition.options
        if (options != null) {
            optionFlag = options.size
        }
        val icons = definition.mouseIcon
        if (icons != null) {
            optionFlag = optionFlag or (icons.size shl 4)
        }
        writeByte(optionFlag)
        options?.forEach {
            writeString(it)
        }
        if (icons != null && icons.isNotEmpty()) {
            writeByte(icons.lastIndex)
            for (index in icons.indices) {
                writeShort(icons[index])
            }
        }

        writeString(definition.optionOverride)
        writeByte(definition.anInt4708)
        writeByte(definition.anInt4795)
        writeByte(definition.anInt4860)
        writeString(definition.useOption)

        if (definition.setting.anInt7413 != -1) {
            writeShort(definition.setting.anInt7413)
            writeShort(definition.anInt4698)
            writeShort(definition.anInt4839)
        }

        encodeScript(definition.information)
        encodeScript(definition.mouseEnterHandler)
        encodeScript(definition.mouseExitHandler)
        encodeScript(definition.anObjectArray4771)
        encodeScript(definition.anObjectArray4768)
        encodeScript(definition.stateChangeHandler)
        encodeScript(definition.invUpdateHandler)
        encodeScript(definition.refreshHandler)
        encodeScript(definition.updateHandler)
        encodeScript(definition.anObjectArray4770)
        encodeScript(definition.mouseMotionHandler)
        encodeScript(definition.mousePressedHandler)
        encodeScript(definition.mouseDraggedHandler)
        encodeScript(definition.mouseReleasedHandler)
        encodeScript(definition.mouseDragPassHandler)
        encodeScript(definition.anObjectArray4852)
        encodeScript(definition.anObjectArray4711)
        encodeScript(definition.anObjectArray4753)
        encodeScript(definition.anObjectArray4688)
        encodeScript(definition.anObjectArray4775)
        encodeIntArray(definition.clientVarp)
        encodeIntArray(definition.inventories)
        encodeIntArray(definition.anIntArray4789)
        encodeIntArray(definition.clientVarc)
        encodeIntArray(definition.anIntArray4805)
    }

    private fun Writer.encodeScript(script: Array<Any>?) {
        val size = script?.size ?: 0
        writeByte(size)
        script?.forEach {
            writeByte(it is String)
            if (it is String) {
                writeString(it)
            } else if (it is Int) {
                writeInt(it)
            }
        }
    }

    private fun Writer.encodeIntArray(script: IntArray?) {
        val size = script?.size ?: 0
        writeByte(size)
        script?.forEach {
            writeInt(it)
        }
    }
}
