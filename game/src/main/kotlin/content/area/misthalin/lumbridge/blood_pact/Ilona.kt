package content.area.misthalin.lumbridge.blood_pact

import content.area.misthalin.lumbridge.catacomb.completeBloodPact
import content.entity.player.dialogue.LookDown
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Scared
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.statement
import content.quest.exitInstance
import content.quest.refreshQuestJournal
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.RegionLevel
import world.gregs.voidps.type.Tile

class Ilona : Script  {

    init {
        npcOperate("Untie", "ilona_tied") { (target) ->
            //TODO: fix dialog
            NPCs.remove(target)
            set("blood_pact", "untied_ilona")
            refreshQuestJournal()
            open("fade_out")
            delay(2)
            exitInstance()
            open("fade_in")
            delay(1)
            val xenia = NPCs.findOrNull(RegionLevel(12849), "xenia")
            val ilona = NPCs.add("ilona_following", Tile(3245, 3197, 0), Direction.EAST)
            if (xenia != null) {
                talkWith(xenia) {
                    npc<LookDown>("You've freed the prisoner and defeated all three cultists. Well done.")
                    npc<LookDown>("I have to confess something to you, adventurer.")
                    npc<LookDown>("I wasn't wounded as badly as I looked. I faked it to see how you would handle the situation on your own.")
                    npc<LookDown>("I wanted to make sure the world has its next generation of heroes. You've proven yourself today.")
                }
            }
            talkWith(ilona) {
                npc<Scared>("Thank the gods we're safe. I thought I was going to die down there.")
                npc<Scared>("Thank you for saving me, adventurer. I won't forget this.")
            }
            NPCs.remove(ilona)
            completeBloodPact()
        }

        npcOperate("Talk-to", "ilona_following") { _ ->
            npc<Scared>("Can we please get out of here?")
        }
    }
}