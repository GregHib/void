package world.gregs.voidps.world.interact.entity.player.combat.range.special

import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.type.random
import world.gregs.voidps.world.activity.skill.slayer.undead
import world.gregs.voidps.world.interact.entity.combat.hit.CombatAttack
import world.gregs.voidps.world.interact.entity.combat.hit.HitDamageModifier
import world.gregs.voidps.world.interact.entity.combat.hit.directHit
import world.gregs.voidps.world.interact.entity.player.combat.consume.drink.antifire
import world.gregs.voidps.world.interact.entity.player.combat.consume.drink.superAntifire
import world.gregs.voidps.world.interact.entity.player.combat.prayer.protectMagic
import world.gregs.voidps.world.interact.entity.player.combat.range.ammo
import world.gregs.voidps.world.interact.entity.player.effect.freeze
import world.gregs.voidps.world.interact.entity.player.toxin.poison
import world.gregs.voidps.world.interact.entity.sound.playSound
import java.util.concurrent.TimeUnit
import kotlin.math.floor

on<HitDamageModifier>({ type == "range" && damage > 0 && it.ammo == "opal_bolts_e" && random.nextDouble() < 0.05 }, Priority.LOW) { player: Player ->
    damage += floor(player.levels.get(Skill.Ranged) * 0.1)
    target.setGraphic("lucky_lightning")
    player.playSound("lucky_lightning", delay = 40)
}

on<CombatAttack>({ type == "range" && damage > 0 && it.ammo == "jade_bolts_e" && random.nextDouble() < 0.05 }) { player: Player ->
    val duration = TimeUnit.SECONDS.toTicks(5)
    target.freeze(duration)
    player.start("delay", duration)
    target.setGraphic("earths_fury")
    player.playSound("earths_fury", delay = 40)
}

// TODO other staves and npcs
fun isFirey(target: Character): Boolean = target is Player && target.equipped(EquipSlot.Weapon).id == "staff_of_fire"
fun isWatery(target: Character): Boolean = target is Player && target.equipped(EquipSlot.Weapon).id == "staff_of_water"

on<HitDamageModifier>({ !isWatery(target) && type == "range" && damage > 0 && it.ammo == "pearl_bolts_e" && random.nextDouble() < 0.06 }, Priority.LOW) { player: Player ->
    damage += floor(player.levels.get(Skill.Ranged) * if (isFirey(target)) 1.0 / 15.0 else 0.05)
    target.setGraphic("sea_curse")
    player.playSound("sea_curse", delay = 40)
}

on<HitDamageModifier>({ type == "range" && damage > 0 && it.ammo == "topaz_bolts_e" && random.nextDouble() < 0.04 }) { player: Player ->
    target.levels.drain(Skill.Magic, 1)
    target.setGraphic("down_to_earth")
    player.playSound("down_to_earth", delay = 40)
}

on<HitDamageModifier>({ type == "range" && damage > 0 && it.ammo == "sapphire_bolts_e" && random.nextDouble() < 0.05 }) { player: Player ->
    val amount = floor(player["range", 0] * 0.05).toInt()
    target.levels.drain(Skill.Prayer, amount)
    player.levels.restore(Skill.Prayer, amount / 2)
    target.setGraphic("clear_mind")
    player.playSound("clear_mind", delay = 40)
}

on<HitDamageModifier>({ type == "range" && damage > 0 && it.ammo == "emerald_bolts_e" && random.nextDouble() < if (target is Player) 0.54 else 0.55 }) { player: Player ->
    player.poison(target, 50)
    target.setGraphic("magical_poison")
    player.playSound("magical_poison", delay = 40)
}

on<HitDamageModifier>({ type == "range" && damage > 0 && it.ammo == "ruby_bolts_e" && random.nextDouble() < if (target is Player) 0.11 else 0.06 }, Priority.LOW) { player: Player ->
    damage = floor(player.levels.get(Skill.Constitution) * 0.2)
    val drain = floor(player.levels.get(Skill.Constitution) * 0.1).toInt()
    player.levels.drain(Skill.Constitution, drain)
    target.setGraphic("blood_forfeit")
    player.playSound("blood_forfeit", delay = 40)
}

on<HitDamageModifier>({ type == "range" && damage > 0 && it.ammo == "diamond_bolts_e" && random.nextDouble() < 0.1 }, Priority.HIGH) { player: Player ->
    damage = floor(damage * 1.15)
    target.setGraphic("armour_piercing")
    player.playSound("armour_piercing", delay = 40)
}

fun dragonFireImmune(target: Character) = target.protectMagic() ||
        (target is Player && (target.equipped(EquipSlot.Shield).id.startsWith("dragonfire_shield") ||
                target.equipped(EquipSlot.Shield).id.startsWith("anti_dragon_shield") ||
                target.antifire ||
                target.superAntifire))

on<HitDamageModifier>({ type == "range" && damage > 0 && it.ammo == "dragon_bolts_e" && !dragonFireImmune(target) && random.nextDouble() < 0.06 }) { player: Player ->
    target.directHit(player, player.levels.get(Skill.Ranged) * 2, "dragonfire", weapon)
    target.setGraphic("dragons_breath")
    player.playSound("dragons_breath", delay = 40)
}

on<HitDamageModifier>({ type == "range" && damage > 0 && it.ammo == "onyx_bolts_e" && !target.undead && random.nextDouble() < if (target is Player) 0.1 else 0.11 },
    Priority.HIGH) { player: Player ->
    damage = floor(damage * 1.20)
    target.setGraphic("life_leech")
    player.playSound("life_leech", delay = 40)
    player.start("life_leech", 1)
}

on<CombatAttack>({ type == "range" && it.hasClock("life_leech") && damage >= 4 }, Priority.MEDIUM) { player: Player ->
    player.levels.restore(Skill.Constitution, damage / 4)
}