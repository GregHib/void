package content.area.asgarnia.falador

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.command.adminCommand
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.closeDialogue
import world.gregs.voidps.engine.client.ui.closeMenu
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.client.variable.stop
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.engine.timer.epochSeconds
import world.gregs.voidps.type.Tile
import java.util.concurrent.TimeUnit

class Iconis : Script {

    init {
        npcOperate("Talk-to", "iconis") {
            if (!World.members) {
                nonMember()
                return@npcOperate
            }
            npc<Happy>("Good day! Would you like to have your picture taken?")
            menu()
        }

        npcOperate("Take-picture", "iconis") {
            if (!World.members) {
                nonMember()
                return@npcOperate
            }
            enterBooth()
        }

        objectOperate("Enter", "photo_booth", arrive = false) {
            // Guard against client-side object injection: only the real photo booth
            // at its known tile may be interacted with.
            if (it.target.tile != BOOTH_OBJECT_TILE) {
                return@objectOperate
            }
            if (!World.members) {
                nonMember()
                return@objectOperate
            }
            enterBooth()
        }

        interfaceClosed("photo_booth") {
            if (tile == BOOTH_TILE) {
                queue("photo_booth_exit", 1) {
                    walkOverDelay(ENTRANCE_TILE)
                }
            }
        }

        // If the player logs out / disconnects while inside the tent, save their
        // position at the entrance so they don't return stuck inside the booth.
        playerDespawn {
            if (tile == BOOTH_TILE) {
                tele(ENTRANCE_TILE)
            }
        }

        interfaceOpened("photo_booth") { id ->
            // Entry is blocked while on cooldown (see enterBooth), so everything is
            // unlocked here.
            interfaceOptions.unlock(id, "close", 0..0, "Close")
            interfaceOptions.unlock(id, "take_picture", 0..0, "Take picture")
            interfaceOptions.unlock(id, "confirm_yes", 0..0, "Yes")
            interfaceOptions.unlock(id, "confirm_no", 0..0, "No")
        }

        interfaceOption("Yes", "photo_booth:confirm_yes") {
            takePicture()
        }

        interfaceOption("Close", "photo_booth:close") {
            closeMenu()
        }

        adminCommand("resetphoto", desc = "Reset the photo booth cooldown") {
            stop("photo_booth_cooldown")
            message("Photo booth cooldown reset.")
        }
    }

    suspend fun Player.nonMember() {
        npc<Neutral>("iconis", "Good day! I'm afraid you can't use the booth's services on a non-members world. Film costs a lot you know!")
    }

    /**
     * Iconis' conversation tree. Source: https://runescape.wiki/w/Transcript:Iconis
     */
    suspend fun Player.menu() {
        choice {
            option<Neutral>("Yes, please.") {
                enterBooth()
            }
            option<Neutral>("What are pictures for?") {
                npc<Happy>("They will be displayed on the forums every time you post there, showing what you look like to anyone who sees your post.")
                menu()
            }
            option<Neutral>("How does it work?") {
                npc<Happy>("An imp uses its magic to capture your likeness. Due to the demands we place upon it, it can only take a picture once every two hours.")
                menu()
            }
            option<Neutral>("I have some other questions.") {
                questions()
            }
            option<Neutral>("No, thank you.") {
                npc<Neutral>("As you wish.")
            }
        }
    }

    suspend fun Player.questions() {
        choice {
            option<Neutral>("Why an imp?") {
                npc<Happy>("I experimented with scrying orbs and cubes, but they distorted the picture terribly. One day an imp blundered into my workshop and destroyed my device,")
                npc<Happy>("but in doing so it revealed a remarkable talent for taking pictures. We've worked together ever since.")
                questions()
            }
            option<Neutral>("Is it safe?") {
                npc<Happy>("Perfectly! I've tested it on myself many times, and the booth is enchanted to protect your belongings while you're inside.")
                questions()
            }
            option<Neutral>("Never mind.") {
                menu()
            }
        }
    }

    /**
     * Walks the player up to the tent entrance, then inside the tent (object 46396
     * at 2928,3323), and opens the interface on arrival. If the player is on the
     * 2-hour cooldown they're told so up front and never walk in or open the booth.
     */
    suspend fun Player.enterBooth() {
        if (onCooldown()) {
            message("You can only have your picture taken once every two hours.")
            return
        }
        closeDialogue()
        clearWatch() // stop facing Iconis while walking to the booth
        walkToDelay(ENTRANCE_TILE)
        walkOverDelay(BOOTH_TILE)
        open("photo_booth")
    }

    private fun Player.onCooldown(): Boolean = hasClock("photo_booth_cooldown", epochSeconds())

    private suspend fun Player.takePicture() {
        if (onCooldown()) {
            message("You can only have your picture taken once every two hours.")
            return
        }
        val lent = (0 until equipment.size).any { equipment[it].isNotEmpty() && equipment[it].def.lent }
        if (lent) {
            message("You can't bring borrowed items into the photo booth.")
            return
        }
        saveSnapshot()
        this["photo_booth_dirty"] = true // flag for the avatar render tool / forum sync
        start("photo_booth_cooldown", TimeUnit.HOURS.toSeconds(2).toInt(), epochSeconds())
        GameObjects.findOrNull(BOOTH_OBJECT_TILE, "photo_booth")?.anim("photo_booth_picture")
        close("photo_booth")
        npc<Happy>("booth_imp", "There ya go, guv!")
    }

    private fun Player.saveSnapshot() {
        this["photo_booth_time"] = epochSeconds()
        this["photo_booth_male"] = body.male
        this["photo_booth_looks"] = body.looks.joinToString(",")
        this["photo_booth_colours"] = body.colours.joinToString(",")
        // One equipIndex per worn slot in slot order; position implies the slot.
        this["photo_booth_equipment"] = (0 until equipment.size).joinToString(",") { slot ->
            val item = equipment[slot]
            if (item.isEmpty()) "-1" else item.def.equipIndex.toString()
        }
    }

    companion object {
        private val BOOTH_OBJECT_TILE = Tile(2928, 3323)
        private val BOOTH_TILE = Tile(2930, 3324)
        private val ENTRANCE_TILE = Tile(2931, 3324)
    }
}
