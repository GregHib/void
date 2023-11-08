package world.gregs.voidps.world.interact.entity.player.combat.armour.barrows

import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inv.ItemChanged
import world.gregs.voidps.type.random
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.world.interact.entity.combat.CombatAttack

on<Registered>({ it.hasFullSet() }) { player: Player ->
    player["guthans_set_effect"] = true
}

on<ItemChanged>({ inventory == "worn_equipment" && BarrowsArmour.isSlot(index) && it.contains("guthans_set_effect") && !isGuthans(item) }) { player: Player ->
    player.clear("guthans_set_effect")
}

on<ItemChanged>({ inventory == "worn_equipment" && BarrowsArmour.isSlot(index) && !it.contains("guthans_set_effect") && isGuthans(item) && it.hasFullSet() }) { player: Player ->
    player["guthans_set_effect"] = true
}

fun isGuthans(item: Item) = item.id.startsWith("guthans_")

fun Player.hasFullSet() = BarrowsArmour.hasSet(this,
    "guthans_warspear",
    "guthans_helm",
    "guthans_platebody",
    "guthans_chainskirt")

on<CombatAttack>({ type == "melee" && it.contains("guthans_set_effect") && random.nextInt(4) == 0 }) { character: Character ->
    character.levels.boost(Skill.Constitution, damage)
    target.setGraphic("guthans_effect")
}