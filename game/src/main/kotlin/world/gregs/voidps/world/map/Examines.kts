package world.gregs.voidps.world.map

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.entity.character.npc.npcApproach
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.obj.objectApproach
import world.gregs.voidps.world.interact.entity.player.equip.inventory

interfaceOption({ id == "equipment_bonuses" && option == "Examine" }) { player: Player ->
    player.message(item.def.getOrNull("examine") ?: return@interfaceOption, ChatType.ItemExamine)
}

inventory({ option == "Examine" }) { player: Player ->
    player.message(item.def.getOrNull("examine") ?: return@inventory, ChatType.ItemExamine)
}

objectApproach({ option == "Examine" }) { player: Player ->
    player.message(def.getOrNull("examine") ?: return@objectApproach, ChatType.ObjectExamine)
}

npcApproach({ option == "Examine" }) { player: Player ->
    player.message(def.getOrNull("examine") ?: return@npcApproach, ChatType.NPCExamine)
}