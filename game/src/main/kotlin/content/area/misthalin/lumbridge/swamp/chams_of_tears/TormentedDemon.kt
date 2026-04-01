package content.area.misthalin.lumbridge.swamp.chams_of_tears

import content.entity.combat.hit.Hit
import content.entity.combat.hit.hit
import content.entity.effect.transform
import content.entity.gfx.areaGfx
import content.entity.proj.shoot
import content.skill.summoning.isFamiliar
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

class TormentedDemon : Script {
    init {
        npcSpawn("tormented_demon_melee") {
            set("tormented_style", "magic")
        }

        npcTimerStart("tds_change_attack") { 26 }

        npcTimerTick("tds_change_attack") {
            anim("tormented_demon_change")
            set(
                "tormented_style",
                when (get("tormented_style", "magic")) {
                    "range" -> "melee"
                    "melee" -> "magic"
                    else -> "range"
                },
            )
            start("action_delay", 6)
            Timer.CONTINUE
        }

        npcCondition("tormented_melee") { get("tormented_style", "magic") == "melee" }
        npcCondition("tormented_range") { get("tormented_style", "magic") == "range" }
        npcCondition("tormented_magic") { get("tormented_style", "magic") == "magic" }

        npcCombatDamage("tormented_demon_*") {
            softTimers.startIfAbsent("tds_change_attack")
            var damage = it.damage
            if (!hasClock("shield_cooldown")) {
                gfx("tormented_demon_shield")
                if (damage > 0 && (it.weapon.id == "darklight" || it.weapon.id == "holy_water")) {
                    start("shield_cooldown", TimeUnit.SECONDS.toTicks(60))
                    (it.source as? Player)?.message("The demon is temporarily weakened by your weapon.")
                }
            }
            if (it.source.isFamiliar) {
                damage *= 10
            }
            if (damage < 20) {
                damage = 20
            }
            val type = when {
                Hit.meleeType(it.type) -> "melee"
                it.type == "range" || it.type == "cannon" -> "range"
                else -> "magic"
            }
            inc("${type}_damage", damage)
            if (get("${type}_damage", 0) >= 310) {
                transform("tormented_demon_$type")
                (it.source as? Player)?.message("The Tormented demon regains its strength against your weapon.")
                clear("melee_damage")
                clear("range_damage")
                clear("magic_damage")
            }
        }

        npcAttack("tormented_demon", "special") { target ->
            val tile = target.tile.toCuboid(4).random()
            val time = shoot("tormented_demon_magic_travel", tile, flightTime = 80)
            areaGfx("tormented_demon_magic_impact", tile, time)
            queue("magic_special", time) {
                if (target.tile.distanceTo(tile) <= 1) {
                    message("The demon's magical attack splashes on you.")
                    this@npcAttack.hit(target, damage = 281, offensiveType = "magic")
                }
            }
        }
    }
}
