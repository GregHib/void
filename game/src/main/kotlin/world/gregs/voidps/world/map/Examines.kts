import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.mode.interact.onApproach
import world.gregs.voidps.engine.entity.character.mode.interact.option.def
import world.gregs.voidps.engine.entity.character.mode.interact.option.option
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.player.equip.ContainerOption

on<ContainerOption>({ option == "Examine" }) { player: Player ->
    player.message(item.def.getOrNull("examine") ?: return@on, ChatType.ItemExamine)
}

onApproach({ option == "Examine" }) { player: Player, obj: GameObject ->
    player.message(def.getOrNull("examine") ?: return@onApproach, ChatType.ObjectExamine)
}

on<NPCOption>({ option == "Examine" }) { player: Player ->
    player.message(def.getOrNull("examine") ?: return@on, ChatType.NPCExamine)
}