import kotlinx.coroutines.Job
import world.gregs.voidps.engine.action.ActionFinished
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.delay
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.character.update.visual.setGraphic
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.utility.toTitleCase
import world.gregs.voidps.world.activity.combat.prayer.*
import world.gregs.voidps.world.interact.entity.combat.CombatDamage
import world.gregs.voidps.world.interact.entity.player.combat.MAX_SPECIAL_ATTACK
import world.gregs.voidps.world.interact.entity.player.combat.specialAttackEnergy
import world.gregs.voidps.world.interact.entity.player.energy.MAX_ENERGY
import world.gregs.voidps.world.interact.entity.player.energy.runEnergy
import world.gregs.voidps.world.interact.entity.proj.shoot
import kotlin.random.Random

on<EffectStart>({ effect == "prayer_bonus_drain" }) { player: Player ->
    player["prayer_bonus_tick_job"] = delay(player, 50, loop = true) {
        val attack = player.getLeech(Skill.Attack)
        val strength = player.getLeech(Skill.Strength)
        val defence = player.getLeech(Skill.Defence)
        val range = player.getLeech(Skill.Range)
        val magic = player.getLeech(Skill.Magic)
        if (attack == 0 && strength == 0 && defence == 0 && range == 0 && magic == 0) {
            player.stop(effect)
        } else {
            player.clear("stat_reduction_msg")
            restore(player, Skill.Attack, attack)
            restore(player, Skill.Strength, strength)
            restore(player, Skill.Defence, defence)
            restore(player, Skill.Range, range)
            restore(player, Skill.Magic, magic)
        }
    }
}

fun restore(player: Player, skill: Skill, leech: Int) {
    if (leech > 0) {
        player.setLeech(skill, leech - 1)
        val key = "stat_reduction_msg"
        if (!player[key, false]) {
            player[key] = true
            player.message("The sap or leech curses currently affecting your stats reduce a little.")
        }
    } else if (leech < 0) {
        player.setLeech(skill, leech + 1)
    }
}

on<EffectStop>({ effect == "prayer_bonus_drain" }) { player: Player ->
    player.remove<Job>("prayer_bonus_tick_job")?.cancel()
}

fun getLevel(target: Character, skill: Skill): Int {
    return target.levels.getMax(skill)
}

on<CombatDamage>({ it.hasEffect("prayer_sap_spirit") && target is Player }) { player: Player ->
    if (Random.nextDouble() >= 0.25) {
        return@on
    }
    target as Player
    val energy = target.specialAttackEnergy
    if (energy <= 0) {
        weakMessage(player, true, "spirit")
        return@on
    }
    target.specialAttackEnergy = (energy - (MAX_SPECIAL_ATTACK / 10)).coerceAtLeast(0)
    cast(player, target, true, "spirit")
}

on<CombatDamage>({ it.hasEffect("prayer_leech_special_attack") }) { player: Player ->
    if (Random.nextDouble() >= 0.15) {
        return@on
    }
    target as Player
    var energy = target.specialAttackEnergy
    if (energy <= 0) {
        weakMessage(player, true, "spirit")
        return@on
    }
    val amount = MAX_SPECIAL_ATTACK / 10
    target.specialAttackEnergy = (energy - amount).coerceAtLeast(0)
    cast(player, target, false, "special_attack")

    energy = player.specialAttackEnergy
    if (energy == MAX_SPECIAL_ATTACK) {
        drainMessage(player, "special_attack")
        return@on
    }
    player.specialAttackEnergy = (energy + amount).coerceAtMost(MAX_SPECIAL_ATTACK)
    boostMessage(player, "Special Attack")
}

on<CombatDamage>({ it.hasEffect("prayer_leech_energy") && target is Player }) { player: Player ->
    if (Random.nextDouble() >= 0.15) {
        return@on
    }
    target as Player
    var energy = target.runEnergy
    if (energy <= 0) {
        weakMessage(player, false, "run_energy")
        return@on
    }
    val amount = MAX_ENERGY / 10
    target.runEnergy = energy - amount
    cast(player, target, false, "energy")

    energy = player.runEnergy
    if (energy == MAX_ENERGY) {
        drainMessage(player, "run_energy")
        return@on
    }
    target.runEnergy = energy + amount
    boostMessage(player, "Run Energy")
}

fun cast(player: Player, target: Character, sap: Boolean, name: String) {
    delay(target, 2) {
        val type = if (sap) "sap" else "leech"
        player.setAnimation(type)
        player.setGraphic("cast_${type}_${name}")
        player.shoot("proj_${type}_${name}", target)
        delay(target, 3) {
            target.setGraphic("land_${type}_${name}")
        }
    }
}

set("prayer_sap_warrior", Skill.Attack)
set("prayer_sap_ranger", Skill.Range)
set("prayer_sap_mage", Skill.Magic)
set("prayer_leech_attack", Skill.Attack)
set("prayer_leech_ranged", Skill.Range)
set("prayer_leech_defence", Skill.Defence)
set("prayer_leech_magic", Skill.Magic)

fun set(effect: String, skill: Skill) {
    val sap = effect.startsWith("prayer_sap")
    on<ActionFinished>({ type == ActionType.Combat }) { player: Player ->
        player.clear("${skill.name.toLowerCase()}_drain_msg")
        player.clear("${skill.name.toLowerCase()}_leech_msg")
    }

    on<CombatDamage>({ it.hasEffect(effect) }) { player: Player ->
        if (Random.nextDouble() >= if (sap) 0.25 else 0.15) {
            return@on
        }
        val name = skill.name.toLowerCase()
        val drain = target.getDrain(skill) + 1
        if (drain * 100.0 / getLevel(target, skill) > if (sap) 10 else 15) {
            weakMessage(player, sap, name)
            return@on
        }

        cast(player, target, sap, name)

        if (sap) {
            player.message("Your curse drains ${skill.name} from the enemy, boosting your ${skill.name}.")
        }
        if (sap && skill == Skill.Attack) {
            target.setDrain(Skill.Attack, drain, 10)
            target.setDrain(Skill.Strength, drain, 10)
            target.setDrain(Skill.Defence, drain, 10)
        } else {
            target.setDrain(skill, drain, 10)
        }
        target.updateBonus(skill)

        if (!sap) {
            val leech = player.getLeech(skill) + 1
            if (leech * 100.0 / player.levels.getMax(skill) > 5) {
                drainMessage(player, name)
                return@on
            }
            boostMessage(player, skill.name)
            player.setLeech(skill, leech)
            player.updateBonus(skill)
            player.hasOrStart("prayer_bonus_drain")
        }
    }
}

fun weakMessage(player: Player, sap: Boolean, name: String) {
    val key = "${name}_drain_msg"
    if (!player[key, false]) {
        player[key] = true
        player.message("Your opponent has been weakened so much that your ${if (sap) "sap" else "leech"} curse has no effect.")
    }
}

fun boostMessage(player: Player, name: String) {
    player.message("Your curse drains $name from the enemy, boosting your $name.")
}

fun drainMessage(player: Player, name: String) {
    val key = "${name}_leech_msg"
    if (!player[key, false]) {
        player[key] = true
        player.message("Your curse drains ${name.toTitleCase()} from the enemy, but has already made you so strong that", ChatType.GameFilter)
        player.message("it can improve you no further.", ChatType.GameFilter)
    }
}