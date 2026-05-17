package content.skill.summoning.pet

import world.gregs.voidps.engine.data.config.RowDefinition
import world.gregs.voidps.engine.data.definition.Tables

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

    companion object {
        fun from(row: RowDefinition) = PetDefinition(
            id = row.rowId,
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
    }
}

class PetDefinitions {

    val all: List<PetDefinition> by lazy {
        require(Tables.loaded) { "Tables must be loaded before pet definitions." }
        Tables.get("pets").rows().map { PetDefinition.from(it) }
    }

    fun get(id: String): PetDefinition? = all.firstOrNull { it.id == id }

    fun forItem(item: String): PetDefinition? = all.firstOrNull {
        it.babyItem == item || it.grownItem == item || it.overgrownItem == item
    }

    fun forNpc(npc: String): PetDefinition? = all.firstOrNull {
        it.babyNpc == npc || it.grownNpc == npc || it.overgrownNpc == npc
    }
}
