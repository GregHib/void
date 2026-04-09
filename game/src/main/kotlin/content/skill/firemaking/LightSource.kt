package content.skill.firemaking

import content.entity.combat.hit.hit
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.replace
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

class LightSource : Script {
    init {
        val unlitSources = Tables.get("light_source").rows().joinToString(",") { it.rowId }

        itemOnItem("tinderbox*", unlitSources) { _, toItem ->
            val source = Rows.getOrNull("light_source.${toItem.id}") ?: return@itemOnItem
            if (!has(Skill.Firemaking, source.int("level"), message = true)) {
                return@itemOnItem
            }
            if (!inventory.replace(toItem.id, source.item("lit"))) {
                return@itemOnItem
            }
            message("You light the ${source.string("type")}.", ChatType.Game)
        }

        itemOption("Extinguish") { (item) ->
            val extinguished = Tables.itemOrNull("extinguish.${item.id}.unlit") ?: return@itemOption
            val type = Tables.string("extinguish.${item.id}.type")
            if (Areas.get(tile.zone).any { it.tags.contains("darkness") }) {
                var lightSources = 0
                val set = Tables.get("extinguish").rows().map { it.rowId }.toSet()
                for (item in inventory.items) {
                    if (!set.contains(item.id)) {
                        continue
                    }
                    lightSources++
                }
                if (equipped(EquipSlot.Shield).id == "lit_bug_lantern") {
                    lightSources++
                }
                if (lightSources == 1) {
                    message("Extinguishing the $type would leave you without a light source.")
                    return@itemOption
                }
            }
            if (!inventory.replace(item.id, extinguished)) {
                return@itemOption
            }
            message("You extinguish the flame.", ChatType.Game)
        }

        entered("*") {
            if (!it.tags.contains("darkness")) {
                return@entered
            }
            if (Light.hasLightSource(this)) {
                open("level_one_darkness")
            } else {
                open("level_three_darkness")
                timers.start("insect_swarm")
            }
        }

        exited("*") {
            if (it.tags.contains("darkness")) {
                close("level_one_darkness")
                close("level_three_darkness")
            }
        }

        timerStart("insect_swarm") {
            message("Tiny biting insects swarm all over you!")
            sound("insect_swarm")
            10
        }

        timerTick("insect_swarm") {
            hit(this, damage = 10)
            sound("insect_bites")
            1
        }

        interfaceClosed("level_three_darkness") {
            timers.stop("insect_swarm")
        }
    }
}
