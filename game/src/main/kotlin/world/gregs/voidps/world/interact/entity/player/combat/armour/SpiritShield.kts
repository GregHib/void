package world.gregs.voidps.world.interact.entity.player.combat.armour

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.EquipSlot
import world.gregs.voidps.engine.entity.item.equipped
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.HitDamageModifier
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.random.Random

on<HitDamageModifier>({ it.equipped(EquipSlot.Shield).name == "divine_spirit_shield" }, Priority.HIGHISH) { player: Player ->
    val points = player.levels.get(Skill.Prayer)
    val drain = ceil((damage * 0.3) / 20.0).toInt()
    if (points > drain) {
        player.levels.drain(Skill.Prayer, drain)
        damage = floor(damage * 0.7)
    }
}

on<HitDamageModifier>({ it.equipped(EquipSlot.Shield).name == "elysian_spirit_shield" }, Priority.HIGHISH) { player: Player ->
    if (Random.nextDouble() >= 0.7) {
        return@on
    }
    damage = floor(damage * 0.75)
}