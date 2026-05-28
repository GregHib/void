package content.area.misthalin.lumbridge.swamp

import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.statement
import content.entity.player.dialogue.type.warning
import content.skill.firemaking.Light
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.type.Direction

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
                        item("rope", "You tie the rope to the top of the hole and throw it down.")
                    }
                    option("No.")
                }
            } else {
                statement("There is a sheer drop below the hole. You will need a rope.")
            }
        }

        objectApproach("Jump-across", "lumbridge_cave_stepping_stone") { (target) ->
            val direction = if (tile.y > target.tile.y) Direction.SOUTH else Direction.NORTH
            walkToDelay(target.tile.add(direction.inverse()).add(direction.inverse()))
            face(direction)
            message("You leap across with a mighty leap!", type = ChatType.Filter)
            anim("stepping_stone_step", delay = 30)
            sound("jump", delay = 35)
            exactMoveDelay(target.tile, startDelay = 58, delay = 70, direction = direction)
            delay(2)
            anim("stepping_stone_step", delay = 30)
            sound("jump", delay = 35)
            exactMoveDelay(target.tile.add(direction).add(direction), startDelay = 58, delay = 70, direction = direction)
        }
    }
}
