package world.gregs.voidps.world.interact.entity.player.combat.armour.barrows

import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inv.ItemChanged
import world.gregs.voidps.type.random
import world.gregs.voidps.world.interact.entity.combat.hit.CombatAttack

on<Registered>({ it.hasFullSet() }) { player: Player ->
    player["ahrims_set_effect"] = true
}

on<ItemChanged>({ inventory == "worn_equipment" && BarrowsArmour.isSlot(index) && it.contains("ahrims_set_effect") && !isAhrims(item) }) { player: Player ->
    player.clear("ahrims_set_effect")
}

on<ItemChanged>({ inventory == "worn_equipment" && BarrowsArmour.isSlot(index) && !it.contains("ahrims_set_effect") && isAhrims(item) && it.hasFullSet() }) { player: Player ->
    player["ahrims_set_effect"] = true
}

fun isAhrims(item: Item) = item.id.startsWith("ahrims_")

fun Player.hasFullSet() = BarrowsArmour.hasSet(this,
    "ahrims_staff",
    "ahrims_hood",
    "ahrims_robe_top",
    "ahrims_robe_skirt")

on<CombatAttack>({ type == "magic" && it.contains("ahrims_set_effect") && damage > 0 && random.nextInt(4) == 0 }) { _: Character ->
    val drain = target.levels.drain(Skill.Strength, 5)
    if (drain < 0) {
        target.setGraphic("ahrims_effect")
    }
}