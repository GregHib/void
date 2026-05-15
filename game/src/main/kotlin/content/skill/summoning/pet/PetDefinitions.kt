package content.skill.summoning.pet

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.config.Config
import world.gregs.voidps.engine.timedLoad

enum class PetStage { Baby, Grown, Overgrown }

/**
 * One pet variant (kitten/cat/overgrown cat is a single entry with three stages).
 *
 * [id] is the persistence key; per-pet attributes are keyed `pet_hunger_<id>`,
 * `pet_growth_<id>`, `pet_unlocked_<id>` so growth state survives metamorphosis.
 */
data class PetDefinition(
    val id: String,
    val babyItem: String,
    val babyNpc: String,
    val grownItem: String?,
    val grownNpc: String?,
    val overgrownItem: String?,
    val overgrownNpc: String?,
    val growthRate: Double,
    val summoningLevel: Int,
    val food: List<String>,
    val hungryPhrase: String? = null,
    val idlePhrases: List<String> = emptyList(),
    val talkLines: List<String> = emptyList(),
) {
    /** Phrases the pet randomly says while idling; falls back to [hungryPhrase] for silent variety. */
    val ambientPhrases: List<String>
        get() = idlePhrases.ifEmpty { listOfNotNull(hungryPhrase) }

    fun itemFor(stage: PetStage): String? = when (stage) {
        PetStage.Baby -> babyItem
        PetStage.Grown -> grownItem
        PetStage.Overgrown -> overgrownItem
    }

    fun npcFor(stage: PetStage): String? = when (stage) {
        PetStage.Baby -> babyNpc
        PetStage.Grown -> grownNpc
        PetStage.Overgrown -> overgrownNpc
    }

    fun stageForItem(item: String): PetStage? = when (item) {
        babyItem -> PetStage.Baby
        grownItem -> PetStage.Grown
        overgrownItem -> PetStage.Overgrown
        else -> null
    }

    fun stageForNpc(npc: String): PetStage? = when (npc) {
        babyNpc -> PetStage.Baby
        grownNpc -> PetStage.Grown
        overgrownNpc -> PetStage.Overgrown
        else -> null
    }

    fun nextStageItem(item: String): String? = when (item) {
        babyItem -> grownItem
        grownItem -> overgrownItem
        else -> null
    }

    fun nextStageNpc(npc: String): String? = when (npc) {
        babyNpc -> grownNpc
        grownNpc -> overgrownNpc
        else -> null
    }

    val isCatLike: Boolean
        get() = id == "hellcat" || id == "cat" || id.startsWith("cat_")
}

class PetDefinitions {

    private val byId = Object2ObjectOpenHashMap<String, PetDefinition>()
    private val itemIndex = Object2ObjectOpenHashMap<String, PetDefinition>()
    private val npcIndex = Object2ObjectOpenHashMap<String, PetDefinition>()

    val all: Collection<PetDefinition> get() = byId.values

    fun get(id: String): PetDefinition? = byId[id]
    fun forItem(item: String): PetDefinition? = itemIndex[item]
    fun forNpc(npc: String): PetDefinition? = npcIndex[npc]

    fun load(path: String): PetDefinitions {
        timedLoad("pet definition") {
            Config.fileReader(path) {
                while (nextSection()) {
                    val id = section()
                    var babyItem = ""
                    var babyNpc = ""
                    var grownItem: String? = null
                    var grownNpc: String? = null
                    var overgrownItem: String? = null
                    var overgrownNpc: String? = null
                    var growthRate = 0.0
                    var summoningLevel = 0
                    var food: List<String> = emptyList()
                    var hungryPhrase: String? = null
                    var idlePhrases: List<String> = emptyList()
                    var talkLines: List<String> = emptyList()
                    while (nextPair()) {
                        when (key()) {
                            "baby_item" -> babyItem = string()
                            "baby_npc" -> babyNpc = string()
                            "grown_item" -> grownItem = string()
                            "grown_npc" -> grownNpc = string()
                            "overgrown_item" -> overgrownItem = string()
                            "overgrown_npc" -> overgrownNpc = string()
                            "growth_rate" -> growthRate = double()
                            "summoning_level" -> summoningLevel = int()
                            "food" -> {
                                val list = mutableListOf<String>()
                                while (nextElement()) list.add(string())
                                food = list
                            }
                            "hungry_phrase" -> hungryPhrase = string()
                            "idle_phrases" -> {
                                val list = mutableListOf<String>()
                                while (nextElement()) list.add(string())
                                idlePhrases = list
                            }
                            "talk_lines" -> {
                                val list = mutableListOf<String>()
                                while (nextElement()) list.add(string())
                                talkLines = list
                            }
                        }
                    }
                    require(babyItem.isNotBlank() && babyNpc.isNotBlank()) {
                        "Pet '$id' requires baby_item and baby_npc."
                    }
                    val def = PetDefinition(
                        id = id,
                        babyItem = babyItem,
                        babyNpc = babyNpc,
                        grownItem = grownItem,
                        grownNpc = grownNpc,
                        overgrownItem = overgrownItem,
                        overgrownNpc = overgrownNpc,
                        growthRate = growthRate,
                        summoningLevel = summoningLevel,
                        food = food,
                        hungryPhrase = hungryPhrase,
                        idlePhrases = idlePhrases,
                        talkLines = talkLines,
                    )
                    byId[id] = def
                    itemIndex[babyItem] = def
                    grownItem?.let { itemIndex[it] = def }
                    overgrownItem?.let { itemIndex[it] = def }
                    npcIndex[babyNpc] = def
                    grownNpc?.let { npcIndex[it] = def }
                    overgrownNpc?.let { npcIndex[it] = def }
                }
            }
            byId.size
        }
        return this
    }
}
