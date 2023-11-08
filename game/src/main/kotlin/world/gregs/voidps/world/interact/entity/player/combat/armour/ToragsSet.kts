package world.gregs.voidps.world.interact.entity.player.combat.armour

import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inv.ItemChanged
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.world.interact.entity.combat.CombatAttack
import world.gregs.voidps.world.interact.entity.player.energy.runEnergy
import kotlin.random.Random

on<Registered>({ it.hasFullSet() }) { player: Player ->
    player["torags_set_effect"] = true
}

on<ItemChanged>({ inventory == "worn_equipment" && isSetSlot(index) && it.contains("torags_set_effect") && !isTorags(item) }) { player: Player ->
    player.clear("torags_set_effect")
}

on<ItemChanged>({ inventory == "worn_equipment" && isSetSlot(index) && !it.contains("torags_set_effect") && isTorags(item) && it.hasFullSet() }) { player: Player ->
    player["torags_set_effect"] = true
}

fun isTorags(item: Item) = item.id.startsWith("torags_")

fun isSetSlot(index: Int) = index == EquipSlot.Hat.index || index == EquipSlot.Chest.index || index == EquipSlot.Legs.index || index == EquipSlot.Weapon.index

fun Player.hasFullSet(): Boolean {
    return equipped(EquipSlot.Chest).id.startsWith("torags_platebody") &&
            equipped(EquipSlot.Legs).id.startsWith("torags_platelegs") &&
            equipped(EquipSlot.Weapon).id.startsWith("torags_hammers") &&
            equipped(EquipSlot.Hat).id.startsWith("torags_helm")
}

on<CombatAttack>({ type == "melee" && damage > 0 && target is Player && weapon?.id?.startsWith("torags_hammers") == true && it.contains("torags_set_effect") && Random.nextInt(4) == 0 }) { _: Character ->
    val target = target as Player
    if (target.runEnergy > 0) {
        target.runEnergy -= target.runEnergy / 5
        target.setGraphic("torags_effect")
    }
}