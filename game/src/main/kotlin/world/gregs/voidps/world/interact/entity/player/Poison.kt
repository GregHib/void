package world.gregs.voidps.world.interact.entity.player

import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.equipped
import world.gregs.voidps.network.visual.EquipSlot

fun Character.poisonedBy(source: Character, damage: Int) {
    if (immune(this)) {
        return
    }
    this["poison_damage", true] = damage
    this["poison_source"] = source
    source.getOrPut("poisons") { mutableSetOf<Character>() }.add(this)
    start("poison", persist = true)
}

fun Character.cure() = stop("poison")

private fun immune(character: Character): Boolean {
    if (character is Player && character.equipped(EquipSlot.Shield).id == "anti_poison_totem") {
        return true
    }
    if (character is NPC && character.def["immune_poison", false]) {
        return true
    }
    if (character.hasEffect("anti-poison")) {
        return true
    }
    return false
}
