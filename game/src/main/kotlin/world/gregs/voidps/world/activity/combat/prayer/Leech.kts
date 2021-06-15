import kotlinx.coroutines.Job
import world.gregs.voidps.engine.action.ActionFinished
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.delay
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.character.update.visual.setGraphic
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.encode.message
import world.gregs.voidps.world.activity.combat.prayer.*
import world.gregs.voidps.world.interact.entity.combat.CombatDamage
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
    player.getOrNull<Job>("prayer_bonus_tick_job")?.cancel()
}

set("prayer_sap_warrior", Skill.Attack)
set("prayer_sap_ranger", Skill.Range)
set("prayer_sap_mage", Skill.Magic)
set("prayer_leech_attack", Skill.Attack)
set("prayer_leech_ranged", Skill.Range)
set("prayer_leech_defence", Skill.Defence)
set("prayer_leech_magic", Skill.Magic)

fun getLevel(target: Character, skill: Skill): Int {
    return target.levels.getMax(skill)
}

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
        if (drain * 100.0 / getLevel(target, skill) >= if (sap) 10 else 15) {// TODO should use npc def base stats + existing bonus
            val key = "${name}_drain_msg"
            if (!player[key, false]) {
                player[key] = true
                player.message("Your opponent has been weakened so much that your ${if (sap) "sap" else "leech"} curse has no effect.")
            }
            return@on
        }

        delay(target, 2) {
            val type = if (sap) "sap" else "leech"
            player.setAnimation(type)
            player.setGraphic("cast_${type}_${name}")
            player.shoot("proj_${type}_${name}", target, delay = if (sap) 30 else 40, flightTime = if (sap) 80 else 120)
            delay(target, 3) {
                target.setGraphic("land_${type}_${name}")
            }
        }

        if (sap) {
            player.message("Your curse drains ${skill.name} from the enemy, boosting your ${skill.name}.")
        }
        if (sap && skill == Skill.Attack) {
            target.setDrain(Skill.Attack, drain)
            target.setDrain(Skill.Strength, drain)
            target.setDrain(Skill.Defence, drain)
        } else {
            target.setDrain(skill, drain)
        }
        target.updateBonus(skill)

        if (!sap) {
            val leech = player.getLeech(skill) + 1
            if (leech * 100.0 / player.levels.getMax(skill) >= 5) {
                val key = "${name}_leech_msg"
                if (!player[key, false]) {
                    player[key] = true
                    player.message("Your curse drains ${skill.name} from the enemy, but has already made you so strong that", ChatType.GameFilter)
                    player.message("it can improve you no further.", ChatType.GameFilter)
                }
                return@on
            }
            player.message("Your curse drains ${skill.name} from the enemy, boosting your ${skill.name}.")
            player.setLeech(skill, leech)
            player.updateBonus(skill)
            if (!player.hasEffect("prayer_bonus_drain")) {
                player.start("prayer_bonus_drain")
            }
        }
    }
}