package content.entity

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.entity.character.npc.npcApproach
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.obj.objectApproach
import content.entity.player.inv.inventoryOption
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.data.definition.ObjectDefinitions
import world.gregs.voidps.engine.client.instruction.onInstruction
import world.gregs.voidps.engine.inject
import world.gregs.voidps.network.client.instruction.ExamineItem
import world.gregs.voidps.network.client.instruction.ExamineNpc
import world.gregs.voidps.network.client.instruction.ExamineObject

interfaceOption("Examine", id = "equipment_bonuses") {
    player.message(item.def.getOrNull("examine") ?: return@interfaceOption, ChatType.ItemExamine)
}

inventoryOption("Examine") {
    player.message(item.def.getOrNull("examine") ?: return@inventoryOption, ChatType.ItemExamine)
}

objectApproach("Examine") {
    player.message(def.getOrNull("examine") ?: return@objectApproach, ChatType.ObjectExamine)
}

npcApproach("Examine") {
    player.message(def.getOrNull("examine") ?: return@npcApproach, ChatType.NPCExamine)
}

val itemDefinitions: ItemDefinitions by inject()
val npcDefinitions: NPCDefinitions by inject()
val objectDefinitions: ObjectDefinitions by inject()

onInstruction<ExamineItem> { player ->
    val definition = itemDefinitions.get(itemId)
    if (definition.contains("examine")) {
        player.message(definition["examine"], ChatType.Game)
    }
}

onInstruction<ExamineNpc> { player ->
    val definition = npcDefinitions.get(npcId)
    if (definition.contains("examine")) {
        player.message(definition["examine"], ChatType.Game)
    }
}

onInstruction<ExamineObject> { player ->
    val definition = objectDefinitions.get(objectId)
    if (definition.contains("examine")) {
        player.message(definition["examine"], ChatType.Game)
    }
}