package content.skill.summoning.pet

import world.gregs.voidps.engine.data.config.RowDefinition
import world.gregs.voidps.engine.data.definition.Tables

enum class PetStage { Baby, Grown, Overgrown }

fun RowDefinition.isCatLike(): Boolean = rowId == "hellcat" || rowId == "cat" || rowId.startsWith("cat_")

fun RowDefinition.itemFor(stage: PetStage): String? = when (stage) {
    PetStage.Baby -> itemOrNull("baby_item")
    PetStage.Grown -> itemOrNull("grown_item")
    PetStage.Overgrown -> itemOrNull("overgrown_item")
}

fun RowDefinition.npcFor(stage: PetStage): String? = when (stage) {
    PetStage.Baby -> npcOrNull("baby_npc")
    PetStage.Grown -> npcOrNull("grown_npc")
    PetStage.Overgrown -> npcOrNull("overgrown_npc")
}

fun RowDefinition.stageForItem(item: String): PetStage? = when (item) {
    itemOrNull("baby_item") -> PetStage.Baby
    itemOrNull("grown_item") -> PetStage.Grown
    itemOrNull("overgrown_item") -> PetStage.Overgrown
    else -> null
}

fun RowDefinition.stageForNpc(npc: String): PetStage? = when (npc) {
    npcOrNull("baby_npc") -> PetStage.Baby
    npcOrNull("grown_npc") -> PetStage.Grown
    npcOrNull("overgrown_npc") -> PetStage.Overgrown
    else -> null
}

fun RowDefinition.nextStageItem(item: String): String? = when (item) {
    itemOrNull("baby_item") -> itemOrNull("grown_item")
    itemOrNull("grown_item") -> itemOrNull("overgrown_item")
    else -> null
}

fun RowDefinition.nextStageNpc(npc: String): String? = when (npc) {
    npcOrNull("baby_npc") -> npcOrNull("grown_npc")
    npcOrNull("grown_npc") -> npcOrNull("overgrown_npc")
    else -> null
}

fun RowDefinition.isFinalStage(item: String): Boolean {
    val stage = stageForItem(item) ?: return true
    return nextStageItem(item) == null || (isCatLike() && stage != PetStage.Baby)
}

fun RowDefinition.ambientPhrases(): List<String> {
    val idle = stringList("idle_phrases")
    if (idle.isNotEmpty()) return idle
    val hungry = stringOrNull("hungry_phrase")?.takeIf { it.isNotBlank() } ?: return emptyList()
    return listOf(hungry)
}

fun petRowForItem(itemId: String): RowDefinition? = Tables.get("pets").rows().firstOrNull {
    it.itemOrNull("baby_item") == itemId ||
        it.itemOrNull("grown_item") == itemId ||
        it.itemOrNull("overgrown_item") == itemId
}

fun petRowForNpc(npcId: String): RowDefinition? = Tables.get("pets").rows().firstOrNull {
    it.npcOrNull("baby_npc") == npcId ||
        it.npcOrNull("grown_npc") == npcId ||
        it.npcOrNull("overgrown_npc") == npcId
}

fun allPetRows(): List<RowDefinition> = Tables.get("pets").rows()
