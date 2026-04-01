package content.area.misthalin.lumbridge.swamp.chams_of_tears

import content.entity.effect.clearTransform
import content.entity.effect.transform
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.mode.PauseMode
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.clearRenderEmote
import world.gregs.voidps.engine.entity.character.player.renderEmote
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

class LightCreature : Script {
    init {
        itemOnNPCApproach("sapphire_lantern_lit", "light_creature") { (target) ->
            if (!target.tile.within(tile, 8) || tile.x > 3238) {
                message("That light creature is too far away to see you clearly.")
                return@itemOnNPCApproach
            }

            val temple = questCompleted("tears_of_guthix")
            val destination = if (temple) {
                Tile(3224, 9527, 2)
            } else if (tile.y > 9515) {
                Tile(3224, 9504, 2)
            } else {
                Tile(3224, 9530, 2)
            }
            message("The light creature is attracted to your beam and comes towards you...")
            target.steps.clear()
            target.mode = PauseMode
            target.walkOverDelay(tile)
            target.anim("light_creature_grow")
            anim("float_up")
            sound("light_creature_up")
            renderEmote("light_creature_float")
            delay(4)
            target.transform("light_creature_large")
            target.walkOverDelay(tile.add(Direction.SOUTH_WEST))
            target.walkTo(destination.add(Direction.SOUTH_WEST), noCollision = true, forceWalk = true)
            walkOverDelay(destination)
            if (temple) {
                // TOOD interface
                tele(2538, 5884)
                return@itemOnNPCApproach
            }
            target.anim("light_creature_shrink")
            anim("float_down")
            sound("light_creature_down")
            clearRenderEmote()
            target.clearTransform()
            target.walkOverDelay(target["spawn_tile", Tile(3225, 9515, 2)])
        }

        itemOnNPCApproach("bullseye_lantern_lit", "light_creature") { (target) ->
            message("The creature is momentarily attracted to the light, but quickly loses interest.")
        }
    }
}
