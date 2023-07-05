package world.gregs.voidps.world.interact.entity.player.combat.armour

import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.world.interact.entity.combat.HitDamageModifier
import kotlin.math.floor

val areas: AreaDefinitions by inject()
val area = areas["castle_wars"]

fun isFlagHolder(target: Character?): Boolean = target is Player && (target.equipped(EquipSlot.Weapon).id == "zamorak_flag" || target.equipped(EquipSlot.Weapon).id == "saradomin_flag")

on<HitDamageModifier>(
    { player -> player.tile in area && player.equipped(EquipSlot.Hands).id.startsWith("castle_wars_brace") && isFlagHolder(target) },
    priority = Priority.LOWER
) { _: Player ->
    damage = floor(damage * 1.2)
}