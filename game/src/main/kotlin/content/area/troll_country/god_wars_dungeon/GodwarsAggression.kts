package content.area.troll_country.god_wars_dungeon

import content.entity.combat.killer
import content.entity.death.npcDeath
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.event.interfaceOpen
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.mode.move.enterArea
import world.gregs.voidps.engine.entity.character.mode.move.exitArea
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.npc.hunt.huntNPC
import world.gregs.voidps.engine.entity.character.npc.hunt.huntPlayer
import world.gregs.voidps.engine.entity.character.player.Player
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
    player.open("godwars_overlay")
    player["gods"] = player.equipment.items.mapNotNull { it.def.getOrNull<String>("god") }.toMutableSet()
}

interfaceOpen("godwars_overlay") { player ->
    player.sendVariable("armadyl_killcount")
    player.sendVariable("bandos_killcount")
    player.sendVariable("saradomin_killcount")
    player.sendVariable("zamorak_killcount")
    player.sendVariable("godwars_darkness")
}

exitArea("godwars_dungeon_multi_area") {
    player.close("godwars_overlay")
    if (logout) {
        return@exitArea
    }
    player["godwars_darkness"] = false
    player.clear("armadyl_killcount")
    player.clear("bandos_killcount")
    player.clear("saradomin_killcount")
    player.clear("zamorak_killcount")
}

itemAdded(inventory = "worn_equipment") { player ->
    val god = item.def.getOrNull<String>("god") ?: return@itemAdded
    if (player.tile in dungeon) {
        player.get<MutableSet<String>>("gods")!!.add(god)
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
    } else if (killer is Player) {
        val god = npc.def["god", ""]
        if (god != "") {
            killer.inc("${god}_killcount")
        }
    }
}

fun randomHuntMode(npc: NPC) {
    if (npc.tile in dungeon && (npc.def["hunt_mode", ""] == "zamorak_aggressive" || npc.def["hunt_mode", ""] == "anti_zamorak_aggressive")) {
        npc["hunt_mode"] = if (random.nextBoolean()) npc.def["hunt_mode"] else "godwars_aggressive"
    }
}
