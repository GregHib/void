package world.gregs.voidps.world.map

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.player.equip.InventoryOption

on<InterfaceOption>({ id == "equipment_bonuses" && option == "Examine" }) { player: Player ->
    player.message(item.def.getOrNull("examine") ?: return@on, ChatType.ItemExamine)
}

on<InventoryOption>({ option == "Examine" }) { player: Player ->
    player.message(item.def.getOrNull("examine") ?: return@on, ChatType.ItemExamine)
}

on<ObjectOption>({ approach && option == "Examine" }) { player: Player ->
    player.message(def.getOrNull("examine") ?: return@on, ChatType.ObjectExamine)
}

on<NPCOption>({ approach && option == "Examine" }) { player: Player ->
    player.message(def.getOrNull("examine") ?: return@on, ChatType.NPCExamine)
}