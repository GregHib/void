package world.gregs.voidps.world.interact.entity.player

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.has
import world.gregs.voidps.engine.entity.item.EquipSlot
import world.gregs.voidps.engine.entity.item.equipped
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.entity.start
import world.gregs.voidps.engine.entity.stop

fun Character.poison(damage: Int) {
    if (immune(this)) {
        return
    }
    this["poison_damage", true] = damage
    start("poison")
}

fun Character.cure() = stop("poison")

private fun immune(character: Character): Boolean {
    if (character is Player && character.equipped(EquipSlot.Shield).name == "anti-poison_totem") {
        return true
    }
    if (character is NPC && character.def["immune_poison", false]) {
        return true
    }
    if (character.has("anti-poison")) {
        return true
    }
    return false
}
