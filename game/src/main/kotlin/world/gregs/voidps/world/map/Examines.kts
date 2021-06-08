import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.FloorItemOption
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.encode.message
import world.gregs.voidps.world.interact.entity.player.equip.ContainerOption

on<ContainerOption>({ option == "Examine" }) { player: Player ->
    player.message(item.def.getOrNull("examine") as? String ?: return@on)
}

on<ObjectOption>({ option == "Examine" }) { player: Player ->
    player.message(obj.def.getOrNull("examine") as? String ?: return@on)
}

on<NPCOption>({ option == "Examine" }) { player: Player ->
    player.message(npc.def.getOrNull("examine") as? String ?: return@on)
}

on<FloorItemOption>({ option == "Examine" }) { player: Player ->
    player.message(floorItem.def.getOrNull("examine") as? String ?: return@on)
}