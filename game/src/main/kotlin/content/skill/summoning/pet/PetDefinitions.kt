package content.skill.summoning.pet

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.timedLoad

enum class PetStage { Baby, Grown, Overgrown }

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

    fun load(): PetDefinitions {
        require(Tables.loaded) { "Tables must be loaded before pet definitions." }
        timedLoad("pet definition") {
            for (row in Tables.get("pets").rows()) {
                val id = row.rowId
                val def = PetDefinition(
                    id = id,
                    babyItem = row.item("baby_item"),
                    babyNpc = row.npc("baby_npc"),
                    grownItem = row.itemOrNull("grown_item"),
                    grownNpc = row.npcOrNull("grown_npc"),
                    overgrownItem = row.itemOrNull("overgrown_item"),
                    overgrownNpc = row.npcOrNull("overgrown_npc"),
                    growthRate = row.double("growth_rate"),
                    summoningLevel = row.int("summoning_level"),
                    food = row.itemList("food"),
                    hungryPhrase = row.stringOrNull("hungry_phrase")?.takeIf { it.isNotBlank() },
                    idlePhrases = row.stringList("idle_phrases"),
                    talkLines = row.stringList("talk_lines"),
                )
                byId[id] = def
                itemIndex[def.babyItem] = def
                def.grownItem?.let { itemIndex[it] = def }
                def.overgrownItem?.let { itemIndex[it] = def }
                npcIndex[def.babyNpc] = def
                def.grownNpc?.let { npcIndex[it] = def }
                def.overgrownNpc?.let { npcIndex[it] = def }
            }
            byId.size
        }
        return this
    }
}
