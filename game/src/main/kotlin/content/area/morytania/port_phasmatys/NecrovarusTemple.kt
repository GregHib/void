package content.area.morytania.port_phasmatys

import content.entity.obj.door.Door
import content.entity.obj.door.enterDoor
import content.entity.player.bank.ownsItem
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Scared
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.inv.item.addOrDrop
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.chat.noInterest
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.equals

class NecrovarusTemple : Script {
    init {
        itemOnObjectOperate("bone_key_ghosts_ahoy", "ahoy_harbour_door_closed") { interaction ->
            if (!interaction.target.tile.equals(3656, 3514, 1)) {
                noInterest()
                return@itemOnObjectOperate
            }
            if (get("ahoy_templedoor_unlocked", false)) {
                message("The door is already unlocked.")
                return@itemOnObjectOperate
            }
            sound("unlock")
            set("ahoy_templedoor_unlocked", true)
            inventory.remove("bone_key_ghosts_ahoy")
            message("You unlock the door.")
        }

        objectOperate("Open", "ahoy_harbour_door_closed") { (target) ->
            if (target.tile.equals(3656, 3514, 1)) {
                if (get("ahoy_templedoor_unlocked", false)) {
                    enterDoor(target)
                    return@objectOperate
                }
                val disciple = NPCs.findBySpawn(Tile(3653, 3518, 1), "ahoy_disciple")
                talkWith(disciple)
                npc<Neutral>("What are you doing going in there?")
                player<Scared>("Err, I was just curious...")
                npc<Neutral>("Inside that room is a coffin, inside which lie the mortal remains of our most glorious master, Necrovarus. None may enter.")
                return@objectOperate
            }
            Door.openDoor(this, target)
        }

        objectOperate("Open", "ahoy_necrovarus_coffin_closed") { (target) ->
            message("The coffin creaks open...")
            target.replace("ahoy_necrovarus_coffin_open", ticks = 100)
        }

        objectOperate("Search", "ahoy_necrovarus_coffin_open") {
            if (get("ahoy_given_robes", false) || ownsItem("mystical_robes")) {
                noInterest()
                return@objectOperate
            }
            item(item = "mystical_robes", text = "You take the Robes of Necrovarus from the remains of his mortal body.")
            addOrDrop("mystical_robes")
        }

        objectOperate("Close", "ahoy_necrovarus_coffin_open") { (target) ->
            message("You close the coffin.")
            target.replace("ahoy_necrovarus_coffin_closed", ticks = 3)
        }
    }
}
