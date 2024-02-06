package world.gregs.voidps.world.map

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.entity.character.npc.npcApproach
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.obj.objectApproach
import world.gregs.voidps.world.interact.entity.player.equip.inventoryOption

interfaceOption("equipment_bonuses", option = "Examine") {
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