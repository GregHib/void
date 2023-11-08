package world.gregs.voidps.world.interact.entity.player.combat.armour.barrows

import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inv.ItemChanged
import world.gregs.voidps.type.random
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.world.interact.entity.combat.CombatAttack
import world.gregs.voidps.world.interact.entity.player.energy.runEnergy

on<Registered>({ it.hasFullSet() }) { player: Player ->
    player["torags_set_effect"] = true
}

on<ItemChanged>({ inventory == "worn_equipment" && BarrowsArmour.isSlot(index) && it.contains("torags_set_effect") && !isTorags(item) }) { player: Player ->
    player.clear("torags_set_effect")
}

on<ItemChanged>({ inventory == "worn_equipment" && BarrowsArmour.isSlot(index) && !it.contains("torags_set_effect") && isTorags(item) && it.hasFullSet() }) { player: Player ->
    player["torags_set_effect"] = true
}

fun isTorags(item: Item) = item.id.startsWith("torags_")

fun Player.hasFullSet() = BarrowsArmour.hasSet(this,
    "torags_hammers",
    "torags_helm",
    "torags_platebody",
    "torags_platelegs")

on<CombatAttack>({ type == "melee" && damage > 0 && target is Player && weapon?.id?.startsWith("torags_hammers") == true && it.contains("torags_set_effect") && random.nextInt(4) == 0 }) { _: Character ->
    val target = target as Player
    if (target.runEnergy > 0) {
        target.runEnergy -= target.runEnergy / 5
        target.setGraphic("torags_effect")
    }
}