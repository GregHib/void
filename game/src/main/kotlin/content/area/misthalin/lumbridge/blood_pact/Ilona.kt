package content.area.misthalin.lumbridge.blood_pact

import content.entity.player.bank.bank
import content.entity.player.bank.ownsItem
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.statement
import content.quest.exitInstance
import content.quest.refreshQuestJournal
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

class Ilona : Script {

    init {
        npcOperate("Untie", "ilona_tied") { (target) ->
            untieIlonaOptions(target)
        }

        npcOperate("Talk-to", "ilona_following") { _ ->
        }
    }

    suspend fun Player.untieIlonaOptions(target: NPC) {
        choice {
            option("Yes, rescue Ilona.") {
                untieIlona(target)
            }
            option("No.") { }
        }
    }

    suspend fun Player.untieIlona(target: NPC) {
        statement("You untie Ilona and return to the surface.")
        open("fade_out")
        delay(2)
        NPCs.remove(target)
        set("blood_pact", "untied_ilona")
        refreshQuestJournal()
        exitInstance()
        face(Direction.SOUTH)
        NPCs.add("ilona_following", Tile(3246, 3197, 0), Direction.NORTH)
        NPCs.add("xenia_2", Tile(3245, 3198, 0), Direction.EAST)

        open("fade_in")
        delay(1)

        npc<Neutral>("ilona_following", "Thank the gods. We're out.")
        npc<Neutral>("ilona_following", "I thought I was going to die down there.")
        npc<Neutral>("ilona_following", "You saved my life, whoever you are. Thank you.")

        npc<Neutral>("xenia_2", "Well, adventurer, it looks like you have prevailed. You should keep the cultists' weapons as a reward.")

        face(Direction.WEST)

        if (checkForUnlootedWeapons()) {
            npc<Neutral>("xenia_2", "I took the liberty of retrieving the weapons you missed.")

            if (!inventory.isFull() && !ownsItem("kayles_sling")) {
                statement("Xenia gives you Kayle's sling.")
                inventory.add("kayles_sling", 1)
            }
            if (!inventory.isFull() && !ownsItem("caitlins_staff")) {
                statement("Xenia gives you Caitlin's staff.")
                inventory.add("caitlins_staff", 1)
            }
            if (!inventory.isFull() && !ownsItem("reeses_sword")) {
                statement("Xenia gives you Reese's sword.")
                inventory.add("reeses_sword", 1)
            }

            if (inventory.isFull() && checkForUnlootedWeapons()) {
                statement("You don't have room in your inventory to receive your last reward. Speak to Xenia again when you have dropped, destroyed or banked some of your items.")
            }
        }

        val ilona = NPCs.find(Tile(3246, 3197, 0), "ilona_following")

        statement("Ilona departs.")
        NPCs.remove(ilona)

        val xenia = NPCs.find(Tile(3245, 3198, 0), "xenia_2")
        delay(1)
        talkWith(xenia)
    }

    fun Player.checkForUnlootedWeapons(): Boolean {
        val weapons = arrayOf("reeses_sword", "kayles_sling", "caitlins_staff") // quest weapons

        for (weapon in weapons) {
            if (!(inventory.contains(weapon) || bank.contains(weapon))) {
                return true
            }
        }
        return false
    }
}
