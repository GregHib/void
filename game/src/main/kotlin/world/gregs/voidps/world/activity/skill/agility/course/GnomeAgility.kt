package world.gregs.voidps.world.activity.skill.agility.course

import world.gregs.voidps.engine.entity.character.forceChat
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.type.Zone
import world.gregs.voidps.type.random

internal fun NPCs.gnomeTrainer(message: String, zone: Zone) {
    val trainer = get(zone).randomOrNull(random) ?: return
    trainer.forceChat = message
}

internal fun NPCs.gnomeTrainer(message: String, zones: List<Zone>) {
    for (zone in zones) {
        val trainer = get(zone).randomOrNull(random) ?: continue
        trainer.forceChat = message
        break
    }
}