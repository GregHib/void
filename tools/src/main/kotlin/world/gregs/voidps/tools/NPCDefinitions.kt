package world.gregs.voidps.tools

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap
import world.gregs.config.ConfigLoader
import world.gregs.config.file.FileChangeDetector
import world.gregs.config.param.NpcParams
import world.gregs.voidps.buffer.read.ArrayReader
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.ArrayWriter
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.FileCache
import world.gregs.voidps.cache.Index
import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.cache.definition.data.NPCDefinitionFull
import world.gregs.voidps.engine.data.Settings
import java.io.File
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.last
import kotlin.io.path.Path

object NPCDefinitions {
    @JvmStatic
    fun main(args: Array<String>) {
        Settings.load()
        val cache: Cache = FileCache(Settings["storage.cache.path"])
//        writer.writeInt(NpcDefinitionCodec.size(cache))
//        val r = ArrayReader()
//        for (i in 0 until NpcDefinitionCodec.size(cache)) {
//            val def = NPCDefinition(i)
//            val data = NpcDefinitionCodec.data(cache, i)
//            if (data != null) {
//                r.set(data)
//                NpcDefinitionCodec.read(r, def)
//                NpcDefinitionCodec.write(writer, def)
//            } else {
//                writer.writeInt(0)
//            }
//        }
        val temp = File("./temp-partial.dat")
//        temp.writeBytes(writer.toArray())
        val reader = ArrayReader(temp.readBytes())
        val size = reader.readInt()

        // 55-63ms -> 30ms
        val definitions = Array(size) {
            val def = NPCDefinition(it)
//            val data = NpcDefinitionCodec.data(cache, it)
//            if (data != null) {
//                reader.set(data)
//                NpcDefinitionCodec.decode(reader, def)
            NpcDefinitionCodec.read(reader, def)
//            }
            def
        }
        val base = File("C:\\Users\\Greg\\IdeaProjects\\void\\data\\.temp\\")
        base.mkdirs()
        val file = base.resolve("npc_configs.dat")
        if (!file.exists()) {
            val start = System.currentTimeMillis()
            ConfigLoader.load(base, FileChangeDetector().detect(base.parentFile, base, forceClear = true))
            println("Detection took ${System.currentTimeMillis() - start}ms")
        }
        val start = System.currentTimeMillis()
        NpcParams.read(file, definitions)
        println("Took ${System.currentTimeMillis() - start}ms")
//        val files = configFiles()
//        val categories = CategoryDefinitions().load(files.find(Settings["definitions.categories"]))
//        val ammo = AmmoDefinitions().load(files.find(Settings["definitions.ammoGroups"]))
//        val parameters = ParameterDefinitions(categories, ammo).load(files.find(Settings["definitions.parameters"]))
//        val definitions = NPCDecoder(true, parameters).load(cache)
//        NPCDefinitions.init(definitions).load(files.getValue(Settings["definitions.npcs"]))
//        val renderAnimations = RenderAnimationDecoder().load(cache)
//        for (i in NPCDefinitions.definitions.indices) {
//            val def = NPCDefinitions.getOrNull(i) ?: continue
//            if (def.name.contains("wizard", ignoreCase = true)) {
//                println(def)
//                val att = def["att", 0]
//                val str = def["str", 0]
//                val defence = def["def", 0]
//                val hp = def["hitpoints", 0]
//                val stabDef = def["stab_defence", 0]
//                val slashDef = def["slash_defence", 0]
//                val crushDef = def["crush_defence", 0]
//                val strengthBonus = def["strength", 0.0]
//                val attackBonus = def["attack_bonus", 0]
//
//                val averageLevel = floor((att + str + defence + min(hp, 20_000)) / 4.0).toInt()// 650
//                val averageDefBonus = floor((stabDef + slashDef + crushDef) / 3.0).toInt() // 80
//                val xpBonus = 1 + 0.025 * floor((39 * averageLevel * (averageDefBonus + strengthBonus + attackBonus))/ 200000.0).toInt()
////                println("Bonus: ${xpBonus}")
////                println("Actual: ${2.0 - xpBonus}")
//            }
//        }
    }
}


object NpcDefinitionCodec {
    val index: Int = Index.NPCS

    fun size(cache: Cache): Int = cache.lastArchiveId(index) * 256 + (cache.fileCount(index, cache.lastArchiveId(index)))

    fun create(size: Int, block: (Int) -> NPCDefinition) = Array(size, block)

    fun create(index: Int) = NPCDefinition(index)

    fun data(cache: Cache, index: Int): ByteArray? {
        return cache.data(Index.NPCS, index ushr 7, index and 0x7f)
    }

    fun decode(reader: Reader, definition: NPCDefinitionFull) = with(definition) {
        while (reader.position() < reader.length) {
            when (val opcode = reader.readUnsignedByte()) {
                0 -> break
                1 -> {
                    val length = reader.readUnsignedByte()
                    modelIds = IntArray(length)
                    for (count in 0 until length) {
                        modelIds!![count] = reader.readUnsignedShort()
                        if (modelIds!![count] == 65535) {
                            modelIds!![count] = -1
                        }
                    }
                }
                2 -> name = reader.readString()
                12 -> size = reader.readUnsignedByte()
                in 30..34 -> options[opcode - 30] = reader.readString()
                40 -> readColours(reader)
                41 -> readTextures(reader)
                42 -> readColourPalette(reader)
                60 -> dialogueModels = IntArray(reader.readUnsignedByte()) { reader.readUnsignedShort() }
                93 -> drawMinimapDot = false
                95 -> combat = reader.readShort()
                97 -> scaleXY = reader.readShort()
                98 -> scaleZ = reader.readShort()
                99 -> priorityRender = true
                100 -> lightModifier = reader.readByte()
                101 -> shadowModifier = 5 * reader.readByte()
                102 -> headIcon = reader.readShort()
                103 -> rotation = reader.readShort()
                106, 118 -> readTransforms(reader, opcode == 118)
                107 -> clickable = false
                109 -> slowWalk = false
                111 -> animateIdle = false
                113 -> {
                    primaryShadowColour = reader.readShort().toShort()
                    secondaryShadowColour = reader.readShort().toShort()
                }
                114 -> {
                    primaryShadowModifier = reader.readByte().toByte()
                    secondaryShadowModifier = reader.readByte().toByte()
                }
                119 -> walkMode = reader.readByte().toByte()
                121 -> {
                    translations = arrayOfNulls(modelIds!!.size)
                    val length = reader.readUnsignedByte()
                    for (count in 0 until length) {
                        val index = reader.readUnsignedByte()
                        translations!![index] = intArrayOf(
                            reader.readByte(),
                            reader.readByte(),
                            reader.readByte(),
                        )
                    }
                }
                122 -> hitbarSprite = reader.readShort()
                123 -> height = reader.readShort()
                125 -> respawnDirection = reader.readByte().toByte()
                127 -> renderEmote = reader.readShort()
                128 -> reader.readUnsignedByte()
                134 -> {
                    idleSound = reader.readShort()
                    if (idleSound == 65535) {
                        idleSound = -1
                    }
                    crawlSound = reader.readShort()
                    if (crawlSound == 65535) {
                        crawlSound = -1
                    }
                    walkSound = reader.readShort()
                    if (walkSound == 65535) {
                        walkSound = -1
                    }
                    runSound = reader.readShort()
                    if (runSound == 65535) {
                        runSound = -1
                    }
                    soundDistance = reader.readUnsignedByte()
                }
                135 -> {
                    primaryCursorOp = reader.readUnsignedByte()
                    primaryCursor = reader.readShort()
                }
                136 -> {
                    secondaryCursorOp = reader.readUnsignedByte()
                    secondaryCursor = reader.readShort()
                }
                137 -> attackCursor = reader.readShort()
                138 -> armyIcon = reader.readShort()
                139 -> spriteId = reader.readShort()
                140 -> ambientSoundVolume = reader.readUnsignedByte()
                141 -> visiblePriority = true
                142 -> mapFunction = reader.readShort()
                143 -> invisiblePriority = true
                in 150..154 -> {
                    options[opcode - 150] = reader.readString()
                }
                155 -> {
                    hue = reader.readByte().toByte()
                    saturation = reader.readByte().toByte()
                    lightness = reader.readByte().toByte()
                    opacity = reader.readByte().toByte()
                }
                158 -> mainOptionIndex = 1.toByte()
                159 -> mainOptionIndex = 0.toByte()
                160 -> {
                    val length = reader.readUnsignedByte()
                    campaigns = IntArray(length) { reader.readShort() }
                }
                162 -> vorbis = true
                163 -> slayerType = reader.readUnsignedByte()
                164 -> {
                    soundRateMin = reader.readShort()
                    soundRateMax = reader.readShort()
                }
                165 -> pickSizeShift = reader.readUnsignedByte()
                168 -> soundRangeMin = reader.readUnsignedByte()
                249 -> readParameters(reader)
            }
        }
    }

    fun read(reader: Reader, type: NPCDefinition) = with(type) {
        while (reader.position() < reader.length) {
            when (val opcode = reader.readUnsignedByte()) {
                1, 60, 160 -> reader.skip(reader.readUnsignedByte() * 2)
                2 -> name = reader.readString()
                12 -> size = reader.readUnsignedByte()
                in 30..34 -> options[-30 + opcode] = reader.readString()
                40, 41, 121 -> reader.skip(reader.readUnsignedByte() * 4)
                42 -> reader.skip(reader.readUnsignedByte())
                93, 99, 107, 109, 111, 141, 143, 158, 159, 162 -> continue
                95 -> combat = reader.readShort()
                97, 98, 102, 103, 114, 122, 123, 137, 138, 139, 142 -> reader.skip(2)
                100, 101, 125, 128, 140, 163, 165, 168 -> reader.skip(1)
                106, 118 -> readTransforms(reader, opcode == 118)
                113, 155, 164 -> reader.skip(4)
                119 -> walkMode = reader.readByte().toByte()
                127 -> renderEmote = reader.readShort()
                134 -> {
                    idleSound = reader.readShort()
                    if (idleSound == 65535) {
                        idleSound = -1
                    }
                    crawlSound = reader.readShort()
                    if (crawlSound == 65535) {
                        crawlSound = -1
                    }
                    walkSound = reader.readShort()
                    if (walkSound == 65535) {
                        walkSound = -1
                    }
                    runSound = reader.readShort()
                    if (runSound == 65535) {
                        runSound = -1
                    }
                    soundDistance = reader.readUnsignedByte()
                }
                135, 136 -> reader.skip(3)
                in 150..154 -> {
                    options[opcode - 150] = reader.readString()
//                    if (!member) {
//                        options[opcode - 150] = null
//                    }
                }
                249 -> readParameters(reader, type)
            }
        }
    }


    fun write(writer: Writer, definition: NPCDefinition) {
        if (definition.id == -1) {
            return
        }

        val name = definition.name
        if (name != "null") {
            writer.writeByte(2)
            writer.writeString(name)
        }

        if (definition.size != 1) {
            writer.writeByte(12)
            writer.writeByte(definition.size)
        }

        val options = definition.options
        for (index in 0 until 5) {
            val option = options[index] ?: continue
            writer.writeByte(30 + index)
            writer.writeString(option)
        }

        if (definition.combat != -1) {
            writer.writeByte(95)
            writer.writeShort(definition.combat)
        }

        writeTransforms(writer, definition.transforms, definition.varbit, definition.varp, 106, 118)

        if (definition.walkMode.toInt() != 0) {
            writer.writeByte(119)
            writer.writeByte(definition.walkMode.toInt())
        }

        if (definition.renderEmote != -1) {
            writer.writeByte(127)
            writer.writeShort(definition.renderEmote)
        }

        if (definition.idleSound != -1 || definition.crawlSound != -1 || definition.walkSound != -1 || definition.runSound != -1 || definition.soundDistance != 0) {
            writer.writeByte(134)
            writer.writeShort(definition.idleSound)
            writer.writeShort(definition.crawlSound)
            writer.writeShort(definition.walkSound)
            writer.writeShort(definition.runSound)
            writer.writeByte(definition.soundDistance)
        }

        writeParameters(writer, definition.params)

        writer.writeByte(0)
    }

    fun writeParameters(writer: Writer, params: Map<Int, Any>?) {
        if (params == null) {
            return
        }
        writer.writeByte(249)
        writer.writeByte(params.size)
        for ((id, value) in params) {
            writer.writeByte(value is String)
            writer.writeMedium(id)
            if (value is String) {
                writer.writeString(value)
            } else if (value is Int) {
                writer.writeInt(value)
            }
        }
    }

    fun writeTransforms(writer: Writer, transforms: IntArray?, varbit: Int, varp: Int, smaller: Int, larger: Int) {
        if (transforms == null || !(varbit != -1 || varp != -1)) {
            return
        }
        val last = transforms.last()
        val extended = last != -1
        writer.writeByte(if (extended) larger else smaller)
        writer.writeShort(varbit)
        writer.writeShort(varp)

        if (extended) {
            writer.writeShort(last)
        }
        writer.writeByte(transforms.size - 2)
        for (i in 0 until transforms.size - 1) {
            writer.writeShort(transforms[i])
        }
    }

    fun readParameters(buffer: Reader, definition: NPCDefinition) {
        val length = buffer.readUnsignedByte()
        if (length == 0) {
            return
        }
        val params = Int2ObjectArrayMap<Any>()
        for (i in 0 until length) {
            val string = buffer.readUnsignedBoolean()
            val id = buffer.readUnsignedMedium()
            params[id] = if (string) buffer.readString() else buffer.readInt()
        }
        definition.params = params
    }


    fun readTransforms(definition: NPCDefinition, buffer: Reader, isLast: Boolean) {
        definition.varbit = buffer.readShort()
        if (definition.varbit == 65535) {
            definition.varbit = -1
        }
        definition.varp = buffer.readShort()
        if (definition.varp == 65535) {
            definition.varp = -1
        }
        var last = -1
        if (isLast) {
            last = buffer.readUnsignedShort()
            if (last == 65535) {
                last = -1
            }
        }
        val length = buffer.readUnsignedByte()
        definition.transforms = IntArray(length + 2)
        for (count in 0..length) {
            definition.transforms!![count] = buffer.readUnsignedShort()
            if (definition.transforms!![count] == 65535) {
                definition.transforms!![count] = -1
            }
        }
        definition.transforms!![length + 1] = last
    }
}