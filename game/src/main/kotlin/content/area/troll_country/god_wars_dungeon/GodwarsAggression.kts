package content.area.troll_country.god_wars_dungeon

import content.entity.combat.killer
import content.entity.death.npcDeath
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.mode.move.enterArea
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.npc.hunt.huntNPC
import world.gregs.voidps.engine.entity.character.npc.hunt.huntPlayer
import world.gregs.voidps.engine.entity.character.player.PlayerOption
import world.gregs.voidps.engine.entity.npcSpawn
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.itemAdded
import world.gregs.voidps.engine.inv.itemRemoved
import world.gregs.voidps.type.random

val areas: AreaDefinitions by inject()
val dungeon = areas["godwars_dungeon_multi_area"]

enterArea("godwars_dungeon_multi_area") {
    player["gods"] = player.equipment.items.mapNotNull { it.def.getOrNull<String>("god") }.toMutableSet()
}

itemAdded(inventory = "worn_equipment") { player ->
    if (player.tile in dungeon && item.def.contains("god")) {
        player.get<MutableSet<String>>("gods")!!.add(item.def["god", ""])
    }
}

itemRemoved(inventory = "worn_equipment") { player ->
    if (player.tile in dungeon) {
        player["gods"] = player.equipment.items.mapNotNull { it.def.getOrNull<String>("god") }.toMutableSet()
    }
}

huntPlayer(mode = "godwars_aggressive") { npc ->
    npc.mode = Interact(npc, target, PlayerOption(npc, target, "Attack"))
}

huntNPC(mode = "zamorak_aggressive") { npc ->
    npc.mode = Interact(npc, target, NPCOption(npc, target, target.def, "Attack"))
}

huntNPC(mode = "anti_zamorak_aggressive") { npc ->
    npc.mode = Interact(npc, target, NPCOption(npc, target, target.def, "Attack"))
}

npcSpawn { npc ->
    randomHuntMode(npc)
}

npcDeath { npc ->
    val killer = npc.killer
    if (killer is NPC) {
        randomHuntMode(npc)
    }
}

fun randomHuntMode(npc: NPC) {
    if (npc.tile in dungeon && (npc.def["hunt_mode", ""] == "zamorak_aggressive" || npc.def["hunt_mode", ""] == "anti_zamorak_aggressive")) {
        npc["hunt_mode"] = if (random.nextBoolean()) npc.def["hunt_mode"] else "godwars_aggressive"
    }
}