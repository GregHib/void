package world.gregs.voidps.world.interact.entity.player.combat.armour

import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.ItemChanged
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.world.interact.entity.combat.HitDamageModifier
import kotlin.math.floor

val areas: AreaDefinitions by inject()
val area = areas["castle_wars"]

fun isFlagHolder(target: Character?): Boolean = target is Player && (target.equipped(EquipSlot.Weapon).id == "zamorak_flag" || target.equipped(EquipSlot.Weapon).id == "saradomin_flag")

on<HitDamageModifier>({ player -> player["castle_wars_brace", false] && isFlagHolder(target) }, Priority.LOWER) { _: Player ->
    damage = floor(damage * 1.2)
}

// TODO should be activated on game start not equip.

on<ItemChanged>({ inventory == "worn_equipment" && index == EquipSlot.Hands.index && item.id.startsWith("castle_wars_brace") && it.tile in area }) { player: Player ->
    player["castle_wars_brace"] = true
}

on<ItemChanged>({ inventory == "worn_equipment" && index == EquipSlot.Hat.index && !item.id.startsWith("castle_wars_brace") && it.tile in area }) { player: Player ->
    player.clear("castle_wars_brace")
}