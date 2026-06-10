package content.area.misthalin.lumbridge.blood_pact

import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.statement
import content.quest.exitInstance
import content.quest.refreshQuestJournal
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.handle.interactNpc
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

class Ilona : Script {

    init {
        npcOperate("Untie", "ilona_tied") { (target) ->
            untieIlonaOptions(target)
        }

        npcOperate("Talk-to", "ilona_following") { _ ->
            // TODO unknown dialogue
        }
    }

    suspend fun Player.untieIlonaOptions(target: NPC) {
        choice {
            option("Yes, rescue Ilona.") {
                untieIlona(target)
            }
            option("No.")
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
        val ilona = NPCs.findBySpawn(Tile(3244, 3197), "ilona")
        val xenia = NPCs.findBySpawn(Tile(3244, 3198), "xenia")

        open("fade_in")
        face(ilona)
        delay(1)

        talkWith(ilona)
        npc<Neutral>("Thank the gods. We're out.")
        npc<Neutral>("I thought I was going to die down there.")
        npc<Neutral>("You saved my life, whoever you are. Thank you.")

        talkWith(xenia)
        npc<Neutral>("Well, adventurer, it looks like you have prevailed. You should keep the cultists' weapons as a reward.")

        if (Xenia.checkForLostWeapons(this)) {
            npc<Neutral>("I took the liberty of retrieving the weapons you missed.")
            Xenia.giveWeapons(this)
            if (inventory.isFull() && Xenia.checkForLostWeapons(this)) {
                statement("You don't have room in your inventory to receive your last reward. Speak to Xenia again when you have dropped, destroyed or banked some of your items.")
            }
        }

        statement("Ilona departs.")
        set("blood_pact", "completed")
        set("blood_pact_ilona_departed", true)
        delay(1)
        interactNpc(xenia, "Talk-to")
    }
}
