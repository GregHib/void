package content.area.kandarin.ourania

import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.npc.hunt.huntNPC
import world.gregs.voidps.engine.entity.character.npc.hunt.huntPlayer
import world.gregs.voidps.engine.entity.character.player.PlayerOption
import world.gregs.voidps.engine.entity.npcSpawn
import world.gregs.voidps.engine.timer.npcTimerTick
import world.gregs.voidps.engine.timer.timerStart
import world.gregs.voidps.type.random

npcSpawn("cave_lizard") { npc ->
    npc.softTimers.start("aggressive_hunt_mode_switch")
}

huntNPC("cave_lizard", "zamorak_*", "aggressive_npcs") { npc ->
    npc.mode = Interact(npc, target, NPCOption(npc, target, target.def, "Attack"))
}

huntPlayer("cave_lizard", "aggressive") { npc ->
    npc.mode = Interact(npc, target, PlayerOption(npc, target, "Attack"))
}

timerStart("aggressive_hunt_mode_switch") {
    interval = random.nextInt(6, 12)
}

npcTimerTick("aggressive_hunt_mode_switch") { npc ->
    npc["hunt_mode"] = if (random.nextBoolean()) "aggressive" else "aggressive_npcs"
}