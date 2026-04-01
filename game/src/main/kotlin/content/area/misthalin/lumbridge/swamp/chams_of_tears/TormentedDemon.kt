package content.area.misthalin.lumbridge.swamp.chams_of_tears

import content.entity.combat.hit.Hit
import content.entity.effect.transform
import content.skill.summoning.isFamiliar
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

class TormentedDemon : Script {
    init {
        npcSpawn("tormented_demon_melee") {
            softTimers.start("tds_change_attack")
            set("tormented_style", "magic")
        }

        npcTimerStart("tds_change_attack") { 26 }

        npcTimerTick("tds_change_attack") {
            anim("tormented_demon_change")
            set("tormented_style", "range")
            Timer.CONTINUE
        }

        npcCondition("tormented_melee") { it["tormented_style", "magic"] == "melee" }
        npcCondition("tormented_range") { it["tormented_style", "magic"] == "range" }
        npcCondition("tormented_magic") { it["tormented_style", "magic"] == "magic" }

        npcCombatDamage("tormented_demon_*") {
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
                clear("magic_damage") // TODO reset damage or nah?
            }
        }
    }
}
