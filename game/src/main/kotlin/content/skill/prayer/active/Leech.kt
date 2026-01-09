package content.skill.prayer.active

import content.entity.player.combat.special.MAX_SPECIAL_ATTACK
import content.entity.player.combat.special.specialAttackEnergy
import content.entity.player.effect.energy.MAX_RUN_ENERGY
import content.entity.player.effect.energy.runEnergy
import content.entity.proj.shoot
import content.skill.prayer.*
import net.pearx.kasechange.toTitleCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.combat.CombatDamage
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.type.random

class Leech : Script {

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
        timerStart("prayer_bonus_drain") { 50 }

        timerTick("prayer_bonus_drain") {
            val attack = getLeech(Skill.Attack)
            val strength = getLeech(Skill.Strength)
            val defence = getLeech(Skill.Defence)
            val ranged = getLeech(Skill.Ranged)
            val magic = getLeech(Skill.Magic)
            if (attack == 0 && strength == 0 && defence == 0 && ranged == 0 && magic == 0) {
                return@timerTick Timer.CANCEL
            }
            clear("stat_reduction_msg")
            restore(this, Skill.Attack, attack)
            restore(this, Skill.Strength, strength)
            restore(this, Skill.Defence, defence)
            restore(this, Skill.Ranged, ranged)
            restore(this, Skill.Magic, magic)
            return@timerTick Timer.CONTINUE
        }

        combatDamage { (source) ->
            if (source !is Player || !source.praying("sap_spirit")) {
                return@combatDamage
            }
            if (random.nextDouble() >= 0.25) {
                return@combatDamage
            }
            val energy = specialAttackEnergy
            if (energy <= 0) {
                weakMessage(source, true, "spirit")
                return@combatDamage
            }
            specialAttackEnergy = (energy - (MAX_SPECIAL_ATTACK / 10)).coerceAtLeast(0)
            cast(source, this, true, "spirit")
        }

        combatDamage { (source) ->
            if (source !is Player || !source.praying("special_attack")) {
                return@combatDamage
            }
            if (random.nextDouble() >= 0.15) {
                return@combatDamage
            }
            var energy = specialAttackEnergy
            if (energy <= 0) {
                weakMessage(source, true, "spirit")
                return@combatDamage
            }
            val amount = MAX_SPECIAL_ATTACK / 10
            specialAttackEnergy = (energy - amount).coerceAtLeast(0)
            cast(source, this, false, "special_attack")

            energy = source.specialAttackEnergy
            if (energy == MAX_SPECIAL_ATTACK) {
                drainMessage(source, "special_attack")
                return@combatDamage
            }
            source.specialAttackEnergy = (energy + amount).coerceAtMost(MAX_SPECIAL_ATTACK)
            boostMessage(source, "Special Attack")
        }

        combatDamage { (source) ->
            if (source !is Player || !source.praying("leech_energy")) {
                return@combatDamage
            }
            if (random.nextDouble() >= 0.15) {
                return@combatDamage
            }
            var energy = runEnergy
            if (energy <= 0) {
                weakMessage(source, false, "run_energy")
                return@combatDamage
            }
            val amount = MAX_RUN_ENERGY / 10
            runEnergy = energy - amount
            cast(source, this, false, "energy")

            energy = source.runEnergy
            if (energy == MAX_RUN_ENERGY) {
                drainMessage(source, "run_energy")
                return@combatDamage
            }
            runEnergy = energy + amount
            boostMessage(source, "Run Energy")
        }

        combatDamage(handler = ::prayers)
        npcCombatDamage(handler = ::prayers)

        variableSet("under_attack") { _, _, to ->
            if (to == 0) {
                for ((_, skill) in map) {
                    clear("${skill.name.lowercase()}_drain_msg")
                    clear("${skill.name.lowercase()}_leech_msg")
                }
            }
        }
    }

    fun prayers(target: Character, it: CombatDamage) {
        val source = it.source
        if (source.praying("sap_warrior")) {
            sap(source, target, Skill.Attack, 10, 20)
            sap(source, target, Skill.Strength, 10, 20)
            sap(source, target, Skill.Defence, 10, 20)
        } else if (source.praying("sap_mage")) {
            sap(source, target, Skill.Magic, 10, 20)
            sap(source, target, Skill.Defence, 10, 20)
        } else if (source.praying("sap_ranger")) {
            sap(source, target, Skill.Ranged, 10, 20)
            sap(source, target, Skill.Defence, 10, 20)
        } else {
            if (source.praying("leech_magic")) {
                leech(source, target, Skill.Magic, 5, 10)
            }
            if (source.praying("leech_ranged")) {
                leech(source, target, Skill.Ranged, 5, 10)
            }
            if (source.praying("leech_attack")) {
                leech(source, target, Skill.Attack, 5, 10)
            }
            if (source.praying("leech_strength")) {
                leech(source, target, Skill.Strength, 5, 10)
            }
            if (source.praying("leech_defence")) {
                leech(source, target, Skill.Defence, 5, 10)
            }
        }
    }

    fun sap(source: Character, target: Character, skill: Skill, base: Int, max: Int) {
        if (random.nextDouble() >= 0.25) {
            return
        }

        val name = skill.name.lowercase()
        val drain = target.getDrain(skill) + 1
        if (drain * 100.0 / getLevel(target, skill) > max - base) {
            weakMessage(source, sap = true, name)
            return
        }

        cast(source, target, sap = true, name)

        source.message("Your curse drains ${skill.name} from the enemy, boosting your ${skill.name}.")
        target.setDrain(skill, drain, base)
        target.updateBonus(skill)
    }

    fun leech(source: Character, target: Character, skill: Skill, base: Int, max: Int) {
        if (random.nextDouble() >= 0.15) {
            return
        }

        val name = skill.name.lowercase()
        val drain = target.getDrain(skill) + 1
        if (drain * 100.0 / getLevel(target, skill) > max - base) {
            weakMessage(source, sap = false, name)
            return
        }

        cast(source, target, sap = false, name)
        target.setDrain(skill, drain, base)
        target.updateBonus(skill)
        val leech = source.getLeech(skill) + 1
        if (leech * 100.0 / source.levels.getMax(skill) > 5) {
            drainMessage(source, name)
            return
        }
        boostMessage(source, skill.name)
        source.setLeech(skill, leech)
        source.updateBonus(skill)
        source.softTimers.startIfAbsent("prayer_bonus_drain")
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
