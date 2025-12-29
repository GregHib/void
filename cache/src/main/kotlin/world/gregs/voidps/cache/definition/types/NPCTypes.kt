package world.gregs.voidps.cache.definition.types

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import world.gregs.config.ConfigReader
import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.buffer.write.Writer
import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.cache.definition.type.NPCType
import world.gregs.voidps.cache.definition.types.Helpers.readExtras
import world.gregs.voidps.cache.definition.types.Helpers.readIntArrays
import world.gregs.voidps.cache.definition.types.Helpers.readStrings
import world.gregs.voidps.cache.definition.types.Helpers.writeExtras
import world.gregs.voidps.cache.definition.types.Helpers.writeIntArrays
import world.gregs.voidps.cache.definition.types.Helpers.writeStrings

class NPCTypes(size: Int) : DefinitionTypes<NPCDefinition> {
    val ids = Object2IntOpenHashMap<String>()
    init {
        ids.defaultReturnValue(-1)
    }
    override val loaded = ByteArray(size)
    private val stringIds = Array(size) { "" }
    private val name = arrayOfNulls<String>(size)
    private val size = ByteArray(size) { 0 }
    private val option1 = arrayOfNulls<String>(size)
    private val option2 = arrayOfNulls<String>(size)
    private val option3 = arrayOfNulls<String>(size)
    private val option4 = arrayOfNulls<String>(size)
    private val option5 = Array<String?>(size) { EXAMINE }
    private val combat = ByteArray(size) { -1 }
    private val varbit = ByteArray(size) { -1 }
    private val varp = ByteArray(size) { -1 }
    private val transforms = arrayOfNulls<IntArray?>(size)
    private val walkMode = ByteArray(size) { 0 }
    private val renderEmote = ShortArray(size) { -1 }
    private val idleSound = ShortArray(size) { -1 }
    private val crawlSound = ShortArray(size) { -1 }
    private val walkSound = ShortArray(size) { -1 }
    private val runSound = ShortArray(size) { -1 }
    private val soundDistance = ByteArray(size) { 0 }
    private val extras = arrayOfNulls<Map<String, Any>>(size)

    override fun load(id: Int, definition: NPCDefinition) {
        ids[definition.stringId] = id
        stringIds[id] = definition.stringId
        name[id] = definition.name
        size[id] = definition.size.toByte()
        option1[id] = definition.options[0]
        option2[id] = definition.options[1]
        option3[id] = definition.options[2]
        option4[id] = definition.options[3]
        option5[id] = definition.options[4]
        combat[id] = definition.combat.toByte()
        varbit[id] = definition.varbit.toByte()
        varp[id] = definition.varp.toByte()
        walkMode[id] = definition.walkMode
        renderEmote[id] = definition.renderEmote.toShort()
        idleSound[id] = definition.idleSound.toShort()
        crawlSound[id] = definition.crawlSound.toShort()
        walkSound[id] = definition.walkSound.toShort()
        runSound[id] = definition.runSound.toShort()
        soundDistance[id] = definition.soundDistance.toByte()
        extras[id] = definition.extras
    }

    override fun save(id: Int, definition: NPCDefinition) {
        definition.stringId = stringIds[id]
        definition.name = name[id] ?: "null"
        definition.size = size[id].toInt()
        definition.options[0] = option1[id]
        definition.options[1] = option2[id]
        definition.options[2] = option3[id]
        definition.options[3] = option4[id]
        definition.options[4] = option5[id]
        definition.combat = combat[id].toInt()
        definition.varbit = varbit[id].toInt()
        definition.varp = varp[id].toInt()
        definition.walkMode = walkMode[id]
        definition.renderEmote = renderEmote[id].toInt()
        definition.idleSound = idleSound[id].toInt()
        definition.crawlSound = crawlSound[id].toInt()
        definition.walkSound = walkSound[id].toInt()
        definition.runSound = runSound[id].toInt()
        definition.soundDistance = soundDistance[id].toInt()
        definition.extras = extras[id]
    }

    fun load(reader: ConfigReader, key: String, id: Int) {
        when (key) {
            "[section]" -> stringIds[id] = reader.section()
            "name" -> name[id] = reader.string()
            "size" -> size[id] = reader.int().toByte()
        }
    }

    override fun load(reader: Reader) {
        reader.readBytes(loaded)
        reader.readBytes(size)
        reader.readBytes(combat)
        reader.readBytes(varbit)
        reader.readBytes(varp)
        reader.readBytes(walkMode)
        reader.readBytes(soundDistance)

        reader.readBytes(renderEmote)
        reader.readBytes(idleSound)
        reader.readBytes(crawlSound)
        reader.readBytes(walkSound)
        reader.readBytes(runSound)

        readStrings(reader, stringIds)
        readStrings(reader, name)
        readStrings(reader, option1)
        readStrings(reader, option2)
        readStrings(reader, option3)
        readStrings(reader, option4)
        readStrings(reader, option5)
        readIntArrays(reader, transforms)
        readExtras(reader, extras)
    }

    override fun save(writer: Writer) {
        writer.writeBytes(loaded)
        writer.writeBytes(size)
        writer.writeBytes(combat)
        writer.writeBytes(varbit)
        writer.writeBytes(varp)
        writer.writeBytes(walkMode)
        writer.writeBytes(soundDistance)

        writer.writeBytes(renderEmote)
        writer.writeBytes(idleSound)
        writer.writeBytes(crawlSound)
        writer.writeBytes(walkSound)
        writer.writeBytes(runSound)

        writeStrings(writer, stringIds)
        writeStrings(writer, name)
        writeStrings(writer, option1)
        writeStrings(writer, option2)
        writeStrings(writer, option3)
        writeStrings(writer, option4)
        writeStrings(writer, option5)
        writeIntArrays(writer, transforms)
        writeExtras(writer, extras)
    }

    companion object {

        fun load(definitions: Array<NPCDefinition>) {
            all = NPCTypes(definitions.size)
            for (id in definitions.indices) {
                val definition = definitions[id]
                if (definition == NPCDefinition.EMPTY) {
                    all.loaded[id] = 0
                    continue
                }
                all.loaded[id] = 1
                all.load(id, definition)
            }
        }

        private const val EXAMINE = "Examine"
        var all: NPCTypes = NPCTypes(0)
        fun get(id: String) = NPCType(all.ids.getInt(id))
        fun getOrNull(id: String): NPCType? {
            val index = all.ids.getInt(id)
            if (index == -1) {
                return null
            }
            return NPCType(index)
        }
        fun get(id: Int) = NPCType(id)
        fun getOrNull(id: Int) = if (contains(id)) NPCType(id) else null
        fun contains(id: Int) = all.loaded[id] == 1.toByte()
        fun id(id: Int) = all.stringIds[id]
        fun name(id: Int) = all.name[id] ?: "null"
        fun size(id: Int) = all.size[id].toInt()
        fun options(id: Int) = arrayOf(all.option1[id], all.option2[id], all.option3[id], all.option4[id], all.option5[id])
        fun option(id: Int, index: Int) = when (index) {
            0 -> all.option1[id]
            1 -> all.option2[id]
            2 -> all.option3[id]
            3 -> all.option4[id]
            4 -> all.option5[id]
            else -> throw IllegalArgumentException("Invalid option index $index")
        }

        fun combat(id: Int) = all.combat[id].toInt()
        fun varbit(id: Int) = all.varbit[id].toInt()
        fun varp(id: Int) = all.varp[id].toInt()
        fun transforms(id: Int) = all.transforms[id]
        fun walkMode(id: Int) = all.walkMode[id].toInt()
        fun renderEmote(id: Int) = all.renderEmote[id].toInt()
        fun idleSound(id: Int) = all.idleSound[id].toInt()
        fun crawlSound(id: Int) = all.crawlSound[id].toInt()
        fun walkSound(id: Int) = all.walkSound[id].toInt()
        fun runSound(id: Int) = all.runSound[id].toInt()
        fun soundDistance(id: Int) = all.soundDistance[id].toInt()
        fun extras(id: Int) = all.extras[id]
    }
}