package world.gregs.voidps.world.interact.entity.player.combat.armour.barrows

import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inv.ItemChanged
import world.gregs.voidps.type.random
import world.gregs.voidps.world.interact.entity.combat.hit.HitChanceModifier
import world.gregs.voidps.world.interact.entity.combat.hit.HitDamageModifier

on<Registered>({ it.hasFullSet() }) { player: Player ->
    player["veracs_set_effect"] = true
}

on<ItemChanged>({ inventory == "worn_equipment" && BarrowsArmour.isSlot(index) && it.contains("veracs_set_effect") && !isVeracs(item) }) { player: Player ->
    player.clear("veracs_set_effect")
}

on<ItemChanged>({ inventory == "worn_equipment" && BarrowsArmour.isSlot(index) && !it.contains("veracs_set_effect") && isVeracs(item) && it.hasFullSet() }) { player: Player ->
    player["veracs_set_effect"] = true
}

fun isVeracs(item: Item) = item.id.startsWith("veracs_")

fun Player.hasFullSet() = BarrowsArmour.hasSet(this,
    "veracs_flail",
    "veracs_helm",
    "veracs_brassard",
    "veracs_plateskirt")

on<HitChanceModifier>({ type == "melee" && it.contains("veracs_set_effect") && random.nextInt(4) == 0 }, Priority.HIGHEST) { _: Character ->
    chance = 1.0
    target?.start("veracs_effect", 1)
}

on<HitDamageModifier>({ type == "melee" && it.contains("veracs_set_effect") && target?.hasClock("veracs_effect") == true }, Priority.LOW) { player: Player ->
    damage += 10
}