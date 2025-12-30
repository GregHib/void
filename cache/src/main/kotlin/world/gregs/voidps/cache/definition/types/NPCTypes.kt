package world.gregs.voidps.cache.definition.types

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.cache.definition.type.NPCType
import kotlin.text.isNotBlank

object NPCTypes : DefinitionTypes<NPCType, NPCDefinition>() {

    override val ids = Object2IntOpenHashMap<String>()
    override lateinit var loaded: ByteArray
    private lateinit var stringIds: Array<String>
    private lateinit var name: Array<String?>
    private lateinit var size: ByteArray
    private lateinit var option1: Array<String?>
    private lateinit var option2: Array<String?>
    private lateinit var option3: Array<String?>
    private lateinit var option4: Array<String?>
    private lateinit var option5: Array<String?>
    private lateinit var combat: ByteArray
    private lateinit var varbit: ByteArray
    private lateinit var varp: ByteArray
    private lateinit var transforms: Array<IntArray?>
    private lateinit var walkMode: ByteArray
    private lateinit var renderEmote: ShortArray
    private lateinit var idleSound: ShortArray
    private lateinit var crawlSound: ShortArray
    private lateinit var walkSound: ShortArray
    private lateinit var runSound: ShortArray
    private lateinit var soundDistance: ByteArray
    private lateinit var categories: Array<Set<String>?>
    override lateinit var extras: Array<Map<String, Any>?>

    init {
        set(0)
    }

    fun id(id: Int) = stringIds[id]
    fun name(id: Int) = name[id] ?: "null"
    fun size(id: Int) = size[id].toInt()
    fun options(id: Int) = arrayOf(option1[id], option2[id], option3[id], option4[id], option5[id])
    fun option(id: Int, index: Int) = when (index) {
        0 -> option1[id]
        1 -> option2[id]
        2 -> option3[id]
        3 -> option4[id]
        4 -> option5[id]
        else -> throw IllegalArgumentException("Invalid option index $index")
    }

    fun combat(id: Int) = combat[id].toInt()
    fun varbit(id: Int) = varbit[id].toInt()
    fun varp(id: Int) = varp[id].toInt()
    fun transforms(id: Int) = transforms[id]
    fun walkMode(id: Int) = walkMode[id].toInt()
    fun renderEmote(id: Int) = renderEmote[id].toInt()
    fun idleSound(id: Int) = idleSound[id].toInt()
    fun crawlSound(id: Int) = crawlSound[id].toInt()
    fun walkSound(id: Int) = walkSound[id].toInt()
    fun runSound(id: Int) = runSound[id].toInt()
    fun soundDistance(id: Int) = soundDistance[id].toInt()
    fun categories(id: Int) = categories[id] ?: emptySet()
    fun extras(id: Int) = extras[id]

    override fun get(id: Int) = NPCType(id)

    private const val DEFAULT_SIZE = 0.toByte()
    private const val DEFAULT_OPTION_5 = "Examine"
    private const val DEFAULT_COMBAT = (-1).toByte()
    private const val DEFAULT_VARBIT = (-1).toByte()
    private const val DEFAULT_VARP = (-1).toByte()
    private const val DEFAULT_WALK_MODE = 0.toByte()
    private const val DEFAULT_RENDER_EMOTE = (-1).toShort()
    private const val DEFAULT_IDLE_SOUND = (-1).toShort()
    private const val DEFAULT_CRAWL_SOUND = (-1).toShort()
    private const val DEFAULT_WALK_SOUND = (-1).toShort()
    private const val DEFAULT_RUN_SOUND = (-1).toShort()
    private const val DEFAULT_SOUND_DISTANCE = (-1).toByte()

    override fun set(size: Int) {
        ids.clear()
        loaded = ByteArray(size)
        stringIds = Array(size) { it.toString() }
        name = arrayOfNulls(size)
        this.size = ByteArray(size) { DEFAULT_SIZE }
        option1 = arrayOfNulls(size)
        option2 = arrayOfNulls(size)
        option3 = arrayOfNulls(size)
        option4 = arrayOfNulls(size)
        option5 = Array(size) { DEFAULT_OPTION_5 }
        combat = ByteArray(size) { DEFAULT_COMBAT }
        varbit = ByteArray(size) { DEFAULT_VARBIT }
        varp = ByteArray(size) { DEFAULT_VARP }
        transforms = arrayOfNulls(size)
        walkMode = ByteArray(size) { DEFAULT_WALK_MODE }
        renderEmote = ShortArray(size) { DEFAULT_RENDER_EMOTE }
        idleSound = ShortArray(size) { DEFAULT_IDLE_SOUND }
        crawlSound = ShortArray(size) { DEFAULT_CRAWL_SOUND }
        walkSound = ShortArray(size) { DEFAULT_WALK_SOUND }
        runSound = ShortArray(size) { DEFAULT_RUN_SOUND }
        soundDistance = ByteArray(size) { DEFAULT_SOUND_DISTANCE }
        categories = arrayOfNulls(size)
        extras = arrayOfNulls(size)
    }

    override fun bytes() = listOf(loaded, this.size, combat, varbit, varp, walkMode, soundDistance)
    override fun shorts() = listOf(renderEmote, idleSound, crawlSound, walkSound, runSound)
    override fun strings() = listOf(stringIds)
    override fun nullStrings() = listOf(name, option1, option2, option3, option4, option5)
    override fun nullIntArrays() = listOf(transforms)
    override fun nullStringSets() = listOf(categories)
    override fun nullMaps() = listOf(extras)

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

    var animationDefinitions: Map<String, Int>? = null
    var soundDefinitions: Map<String, Int>? = null
    val abbreviations = Object2ObjectOpenHashMap<String, String>()

    @Suppress("UNCHECKED_CAST")
    override fun load(key: String, value: Any, id: Int, section: String): Boolean {
        ids.put(section, id)
        when (key) {
            "clone" -> {
                val name = value as String
                val npc = ids.getOrDefault(name, -1)
                require(npc >= 0) { "Cannot find npc id to clone '$name'" }
                val extras = extras(npc) ?: return true
                getOrPutExtras(npc).putAll(extras)
            }
            "categories" -> categories[id] = (value as List<String>).toSet()
            "pickpocket" -> {
                PickpocketTypes.load(key, value, id, section)
                getOrPutExtras(id)["pickpocket_id"] = PickpocketTypes.index(id)
            }
            "fishing_net", "fishing_cage", "fishing_lure", "fishing_harpoon", "fishing_bait" -> {
                FishingSpotTypes.load(key, value, id, section)
                getOrPutExtras(id)[key] = FishingSpotTypes.index(id)
            }
            "drop_table" -> {
                value as String
                require(DropTableTypes.index == 0 || value.isBlank() || DropTableTypes.get("${value}_drop_table") != -1) { "Drop table '$value' not found for npc $section" }
                getOrPutExtras(id)[key] = value
            }
            "combat_anims" -> {
                value as String
                if (animationDefinitions != null && value.isNotBlank()) {
                    // Attack isn't always required because of weapon style
                    require(animationDefinitions!!.containsKey("${value}_defend")) { "No combat animation ${value}_defend found for npc $section" }
                    require(animationDefinitions!!.containsKey("${value}_death")) { "No combat animation ${value}_death found for npc $section" }
                }
                getOrPutExtras(id)[key] = value
            }
            "combat_sounds" -> {
                value as String
                if (soundDefinitions != null && value.isNotBlank()) {
                    require(soundDefinitions!!.containsKey("${value}_attack") || soundDefinitions!!.containsKey("${value}_defend") || soundDefinitions!!.containsKey("${value}_death")) { "No combat sounds '$value' found for npc $section" }
                }
                getOrPutExtras(id)[key] = value
            }
            "aka" -> for (abbr in value as List<String>) {
                abbreviations[abbr] = section
            }
            else -> return false
        }
        return true
    }
}