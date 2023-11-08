package world.gregs.voidps.world.interact.entity.player.combat.armour

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.type.random
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.world.interact.entity.combat.HitDamageModifier
import kotlin.math.ceil
import kotlin.math.floor

on<HitDamageModifier>({ target is Player && target.equipped(EquipSlot.Shield).id == "divine_spirit_shield" }, Priority.HIGHISH) { _: Character ->
    val player = target as Player
    val points = player.levels.get(Skill.Prayer)
    val drain = ceil((damage * 0.3) / 20.0).toInt()
    if (points > drain) {
        player.levels.drain(Skill.Prayer, drain)
        damage = floor(damage * 0.7)
    }
}

on<HitDamageModifier>({ target is Player && target.equipped(EquipSlot.Shield).id == "elysian_spirit_shield" }, Priority.HIGHISH) { _: Character ->
    if (random.nextDouble() >= 0.7) {
        return@on
    }
    damage = floor(damage * 0.75)
}