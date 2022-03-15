import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.item.floor.FloorItemOption
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.player.equip.ContainerOption

on<ContainerOption>({ option == "Examine" }) { player: Player ->
    player.message(item.def.getOrNull("examine") ?: return@on, ChatType.ItemExamine)
}

on<ObjectOption>({ option == "Examine" }) { player: Player ->
    player.message(obj.def.getOrNull("examine") ?: return@on, ChatType.ObjectExamine)
}

on<NPCOption>({ option == "Examine" }) { player: Player ->
    player.message(npc.def.getOrNull("examine") ?: return@on, ChatType.NPCExamine)
}

on<FloorItemOption>({ option == "Examine" }) { player: Player ->
    player.message(floorItem.def.getOrNull("examine") ?: return@on, ChatType.ItemExamine)
}