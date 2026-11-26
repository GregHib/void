package content.minigame.castle_wars

import content.entity.effect.transform
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectLayer
import world.gregs.voidps.engine.entity.obj.stepAway
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

class CastleWarsBarricade : Script {
    init {
        itemOption("Set-up", "barricade_castle_wars") {
            if (GameObjects.getLayer(tile, ObjectLayer.GROUND) != null) {
                // https://youtu.be/UUiSohxhRuw?t=178
                message("You can't set up a barricade here.")
                return@itemOption
            }
            if (NPCs.at(tile).any { it.id.startsWith("castle_wars_barricade_") }) {
                // https://youtu.be/UUiSohxhRuw?t=178
                message("You can't set up a barricade here.")
                return@itemOption
            }
            if (!inventory.remove("barricade_castle_wars")) {
                return@itemOption
            }
            val team = equipped(EquipSlot.Cape).id.removeSuffix("_team_cape")
            val npc = NPCs.add("castle_wars_barricade_$team", tile) // TODO what if both queued on same tick?
            stepAway(npc, tile)
        }

        itemOnNPCOperate("tinderbox", "castle_wars_barricade_*") { (target) ->
            if (target.id.endsWith("burning")) {
                message("That barricade is already on fire")// TODO message
            } else {
                target.transform("${target.id}_burning")
                // TODO message + anim + timer
            }
        }

        itemOnNPCOperate("explosive_potion", "castle_wars_barricade_*") { (target) ->
            // TODO what if own teams?
            // TODO gfx + message
            if (NPCs.remove(target)) {
                inventory.remove("explosive_potion")
            }
        }
    }
}