package world.gregs.voidps.world.interact.entity.player

import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.timer.softTimer
import world.gregs.voidps.engine.timer.stopSoftTimer
import world.gregs.voidps.engine.timer.stopTimer
import world.gregs.voidps.engine.timer.timer
import world.gregs.voidps.network.visual.update.player.EquipSlot

fun Character.poisonedBy(source: Character, damage: Int) {
    if (immune(this)) {
        return
    }
    this["poison_damage", true] = damage
    this["poison_source"] = source
    source.getOrPut("poisons") { mutableSetOf<Character>() }.add(this)
    if (this is Player) {
        timer("poison", 30, persist = true)
    } else if (this is NPC) {
        softTimer("poison", 30, persist = true)
    }
}

fun Character.poisoned(): Boolean {
    return when (this) {
        is Player -> timers.contains("poison")
        is NPC -> softTimers.contains("poison")
        else -> false
    }
}

fun Character.cure(): Boolean {
    if (this is Player) {
        stopTimer("poison")
    } else if (this is NPC) {
        stopSoftTimer("poison")
    }
    return true
}

private fun immune(character: Character): Boolean {
    if (character is Player && character.equipped(EquipSlot.Shield).id == "anti_poison_totem") {
        return true
    }
    if (character is NPC && character.def["immune_poison", false]) {
        return true
    }
    if (character is Player && character.timers.contains("anti-poison")) {
        return true
    }
    return false
}
