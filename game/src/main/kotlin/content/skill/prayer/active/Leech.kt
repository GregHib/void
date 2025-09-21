package content.skill.prayer.active

import content.entity.combat.hit.characterCombatDamage
import content.entity.combat.hit.combatDamage
import content.entity.player.combat.special.MAX_SPECIAL_ATTACK
import content.entity.player.combat.special.specialAttackEnergy
import content.entity.player.effect.energy.MAX_RUN_ENERGY
import content.entity.player.effect.energy.runEnergy
import content.entity.proj.shoot
import content.skill.prayer.*
import net.pearx.kasechange.toTitleCase
import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.engine.timer.timerStart
import world.gregs.voidps.engine.timer.timerTick
import world.gregs.voidps.type.random

@Script
class Leech : Api {

    val map = mapOf(
        "sap_warrior" to Skill.Attack,
        "sap_ranger" to Skill.Ranged,
        "sap_mage" to Skill.Magic,
        "leech_attack" to Skill.Attack,
        "leech_ranged" to Skill.Ranged,
        "leech_defence" to Skill.Defence,
        "leech_magic" to Skill.Magic,
    )

    init {
        timerStart("prayer_bonus_drain") {
            interval = 50
        }

        timerTick("prayer_bonus_drain") { player ->
            val attack = player.getLeech(Skill.Attack)
            val strength = player.getLeech(Skill.Strength)
            val defence = player.getLeech(Skill.Defence)
            val ranged = player.getLeech(Skill.Ranged)
            val magic = player.getLeech(Skill.Magic)
            if (attack == 0 && strength == 0 && defence == 0 && ranged == 0 && magic == 0) {
                cancel()
            } else {
                player.clear("stat_reduction_msg")
                restore(player, Skill.Attack, attack)
                restore(player, Skill.Strength, strength)
                restore(player, Skill.Defence, defence)
                restore(player, Skill.Ranged, ranged)
                restore(player, Skill.Magic, magic)
            }
        }

        combatDamage { target ->
            if (source !is Player || !source.praying("sap_spirit")) {
                return@combatDamage
            }
            if (random.nextDouble() >= 0.25) {
                return@combatDamage
            }
            val player = source
            val energy = target.specialAttackEnergy
            if (energy <= 0) {
                weakMessage(player, true, "spirit")
                return@combatDamage
            }
            target.specialAttackEnergy = (energy - (MAX_SPECIAL_ATTACK / 10)).coerceAtLeast(0)
            cast(player, target, true, "spirit")
        }

        combatDamage { target ->
            if (source !is Player || !source.praying("special_attack")) {
                return@combatDamage
            }
            if (random.nextDouble() >= 0.15) {
                return@combatDamage
            }
            val player = source
            var energy = target.specialAttackEnergy
            if (energy <= 0) {
                weakMessage(player, true, "spirit")
                return@combatDamage
            }
            val amount = MAX_SPECIAL_ATTACK / 10
            target.specialAttackEnergy = (energy - amount).coerceAtLeast(0)
            cast(player, target, false, "special_attack")

            energy = player.specialAttackEnergy
            if (energy == MAX_SPECIAL_ATTACK) {
                drainMessage(player, "special_attack")
                return@combatDamage
            }
            player.specialAttackEnergy = (energy + amount).coerceAtMost(MAX_SPECIAL_ATTACK)
            boostMessage(player, "Special Attack")
        }

        combatDamage { target ->
            if (source !is Player || !source.praying("leech_energy")) {
                return@combatDamage
            }
            if (random.nextDouble() >= 0.15) {
                return@combatDamage
            }
            val player = source
            var energy = target.runEnergy
            if (energy <= 0) {
                weakMessage(player, false, "run_energy")
                return@combatDamage
            }
            val amount = MAX_RUN_ENERGY / 10
            target.runEnergy = energy - amount
            cast(player, target, false, "energy")

            energy = player.runEnergy
            if (energy == MAX_RUN_ENERGY) {
                drainMessage(player, "run_energy")
                return@combatDamage
            }
            target.runEnergy = energy + amount
            boostMessage(player, "Run Energy")
        }

        characterCombatDamage { target ->
            for ((prayer, skill) in map) {
                if (!source.praying(prayer)) {
                    continue
                }
                val sap = prayer.startsWith("sap")

                if (random.nextDouble() >= if (sap) 0.25 else 0.15) {
                    continue
                }
                val name = skill.name.lowercase()
                val drain = target.getDrain(skill) + 1
                if (drain * 100.0 / getLevel(target, skill) > if (sap) 10 else 15) {
                    weakMessage(source, sap, name)
                    continue
                }

                cast(source, target, sap, name)

                if (sap) {
                    source.message("Your curse drains ${skill.name} from the enemy, boosting your ${skill.name}.")
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
                    val leech = source.getLeech(skill) + 1
                    if (leech * 100.0 / source.levels.getMax(skill) > 5) {
                        drainMessage(source, name)
                        continue
                    }
                    boostMessage(source, skill.name)
                    source.setLeech(skill, leech)
                    source.updateBonus(skill)
                    source.softTimers.startIfAbsent("prayer_bonus_drain")
                }
            }
        }
    }

    override fun variableSet(player: Player, key: String, from: Any?, to: Any?) {
        if (key == "in_combat" && to == 0) {
            for ((_, skill) in map) {
                player.clear("${skill.name.lowercase()}_drain_msg")
                player.clear("${skill.name.lowercase()}_leech_msg")
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
        player.updateBonus(skill)
    }

    fun getLevel(target: Character, skill: Skill): Int = target.levels.getMax(skill)

    fun cast(source: Character, target: Character, sap: Boolean, name: String) {
        source.queue("leech", 1) {
            val type = if (sap) "sap" else "leech"
            source.anim(type)
            source.gfx("cast_${type}_$name")
            val time = source.shoot("proj_${type}_$name", target)
            target.gfx("land_${type}_$name", delay = time)
        }
    }

    fun weakMessage(source: Character, sap: Boolean, name: String) {
        val key = "${name}_drain_msg"
        if (!source[key, false]) {
            source[key] = true
            source.message("Your opponent has been weakened so much that your ${if (sap) "sap" else "leech"} curse has no effect.")
        }
    }

    fun boostMessage(source: Character, name: String) {
        source.message("Your curse drains $name from the enemy, boosting your $name.")
    }

    fun drainMessage(source: Character, name: String) {
        val key = "${name}_leech_msg"
        if (!source[key, false]) {
            source[key] = true
            source.message("Your curse drains ${name.toTitleCase()} from the enemy, but has already made you so strong that", ChatType.Filter)
            source.message("it can improve you no further.", ChatType.Filter)
        }
    }
}
