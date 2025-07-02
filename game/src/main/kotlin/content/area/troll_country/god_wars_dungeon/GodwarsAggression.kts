package content.area.troll_country.god_wars_dungeon

import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.mode.move.enterArea
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.npc.hunt.huntNPC
import world.gregs.voidps.engine.entity.character.npc.hunt.huntPlayer
import world.gregs.voidps.engine.entity.character.player.PlayerOption
import world.gregs.voidps.engine.entity.npcSpawn
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.itemAdded
import world.gregs.voidps.engine.inv.itemRemoved

val areas: AreaDefinitions by inject()
val area = areas["godwars_dungeon_multi_area"]

enterArea("godwars_dungeon_multi_area") {
    player["gods"] = player.equipment.items.mapNotNull { it.def.getOrNull<String>("god") }.toMutableSet()
}

itemAdded(inventory = "worn_equipment") { player ->
    if (player.tile in area && item.def.contains("god")) {
        player.get<MutableSet<String>>("gods")!!.add(item.def["god", ""])
    }
}

itemRemoved(inventory = "worn_equipment") { player ->
    player["gods"] = player.equipment.items.mapNotNull { it.def.getOrNull<String>("god") }.toMutableSet()
}

huntPlayer(mode = "godwars_aggressive") { npc ->
    npc.mode = Interact(npc, target, PlayerOption(npc, target, "Attack"))
}

huntNPC(mode = "zamorak_aggressive_npcs") { npc ->
    npc.mode = Interact(npc, target, NPCOption(npc, target, target.def, "Attack"))
}

huntNPC(mode = "zamorak_aggressive_npcs") { npc ->
    npc.mode = Interact(npc, target, NPCOption(npc, target, target.def, "Attack"))
}