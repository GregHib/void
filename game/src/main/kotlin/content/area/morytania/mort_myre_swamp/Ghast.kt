package content.area.morytania.mort_myre_swamp

import content.entity.combat.killer
import content.entity.effect.transform
import content.entity.proj.shoot
import content.quest.quest
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.Retreat
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.inv.replace
import world.gregs.voidps.engine.queue.queue

class Ghast : Script {
    init {
        npcCondition("has_food") {
            !hasPouch(it) && hasFood(it)
        }

        npcCondition("has_no_food") {
            !hasPouch(it) && !hasFood(it)
        }

        npcCondition("has_druid_pouch") {
            transform.isEmpty() && hasPouch(it)
        }

        npcCondition("has_no_druid_pouch") {
            !hasPouch(it)
        }

        npcAttack("ghast", "miss") {
            mode = Retreat(this, it)
        }

        npcAttack("ghast", "energy") {
            mode = Retreat(this, it)
        }

        npcAttack("ghast", "rot") {
            rotFood(it)
            mode = Retreat(this, it)
        }

        npcAttack("ghast", "reveal") { attacker ->
            if (attacker !is Player) {
                return@npcAttack
            }
            mode = Retreat(this, attacker)
            attacker.inventory.remove("druid_pouch_2")
            if (attacker.inventory.count("druid_pouch_2") == 0) {
                attacker.inventory.add("druid_pouch")
            }
            attacker.shoot(
                id = "druid_shooting_star",
                target = this,
                delay = 51,
                flightTime = 39,
                height = 43,
                endHeight = 31,
                curve = 16,
                offset = 64,
            )
            queue("transform", 2) {
                transform("ghast_level_30")
            }
        }

        npcDeath("ghast") {
            gfx("ghast_spotdeath")
            val killer = killer
            if (killer !is Player) {
                return@npcDeath
            }
            killer.exp(Skill.Prayer, 30.0)
            when (killer.quest("nature_spirit")) {
                "ghast_3" -> {
                    killer.message("That's one Ghast, 2 more to kill.")
                    killer["nature_spirit"] = "ghast_2"
                }
                "ghast_2" -> {
                    killer.message("That's two Ghasts, 1 more to kill.")
                    killer["nature_spirit"] = "ghast_1"
                }
                "ghast_1" -> {
                    killer.message("That's all three ghasts!")
                    killer["nature_spirit"] = "ghasts_killed"
                }
            }
        }
    }

    fun hasPouch(character: Character): Boolean = character is Player && character.inventory.contains("druid_pouch_2")

    fun hasFood(character: Character): Boolean {
        if (character !is Player) {
            return false
        }
        for (item in character.inventory.items) {
            if (item.isEmpty()) {
                continue
            }
            if (item.def.options.contains("Eat")) {
                return true
            }
        }
        return false
    }

    fun rotFood(character: Character) {
        if (character !is Player) {
            return
        }
        for (item in character.inventory.items) {
            if (item.isEmpty()) {
                continue
            }
            if (item.def.options.contains("Eat")) {
                character.inventory.replace(item.id, "rotten_food")
                return
            }
        }
    }
}
