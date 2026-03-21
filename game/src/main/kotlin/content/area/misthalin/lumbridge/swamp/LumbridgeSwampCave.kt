package content.area.misthalin.lumbridge.swamp

import content.entity.combat.hit.hit
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.statement
import content.entity.player.dialogue.type.warning
import content.skill.firemaking.Light
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

class LumbridgeSwampCave : Script {
    init {
        objectOperate("Climb-down", "goblin_cave_entrance") {
            if (get("cave_goblin_rope", false)) {
                val light = Light.hasLightSource(this)
                if (!light && !warning("lumbridge_cellar")) {
                    message("You should find a light source and a tinderbox before going down there.")
                    return@objectOperate
                }
                if (!inventory.contains("tinderbox") && !warning("lumbridge_swamp_cave_rope")) {
                    message("You should find a tinderbox before going down there.")
                    return@objectOperate
                }
                anim("climb_down")
                delay(2)
                tele(3167, 9573)
            } else if (inventory.contains("rope")) {
                choice("Attach a rope to the top of the hole?") {
                    option("Yes.") {
                        if (!inventory.remove("rope")) {
                            return@option
                        }
                        anim("climb_down")
                        set("cave_goblin_rope", true)
                        sound("attach_rope")
                        item("rope", 400, "You tie the rope to the top of the hole and throw it down.")
                    }
                    option("No.")
                }
            } else {
                statement("There is a sheer drop below the hole. You will need a rope.")
            }
        }

        entered("lumbridge_swamp_caves") {
            if (Light.hasLightSource(this)) {
                open("level_one_darkness")
            } else {
                open("level_three_darkness")
                timers.start("insect_swarm")
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

        exited("lumbridge_swamp_caves") {
            close("level_one_darkness")
            close("level_three_darkness")
        }
    }
}
