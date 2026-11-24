package content.area.kandarin.tree_gnome_village

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.closeDialogue
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player

class Elkoy : Script {

    init {
        // https://runescape.wiki/w/Transcript:Elkoy (After Tree Gnome Village)
        npcOperate("Talk-to", "elkoy_tree_gnome_village_4") { (target) ->
            val outside = target.outsideMaze()
            player<Happy>("Hello Elkoy.")
            if (outside) {
                npc<Happy>("Hi there, I hope life is treating you well. Would you like me to show you the way to the village?")
            } else {
                npc<Happy>("Hi there, I hope life is treating you well. Would you like me to show you the way out of the village?")
            }
            choice {
                option<Neutral>("Yes please.") {
                    npc<Neutral>("Ok then, follow me.")
                    escort(outside)
                    if (outside) {
                        npc<Neutral>("Here we are. Feel free to have a look around.")
                    } else {
                        npc<Neutral>("Here we are. Have a safe journey.")
                    }
                }
                option<Neutral>("No thanks Elkoy.") {
                    npc<Neutral>("Ok then, take care.")
                }
            }
        }

        // https://runescape.wiki/w/Transcript:Elkoy (Follow option)
        npcOperate("Follow", "elkoy_tree_gnome_village_4") { (target) ->
            escort(target.outsideMaze())
        }
    }

    private fun NPC.outsideMaze() = tile.y > 3177

    // Tiles and timing from 2009scape (TreeGnomeVillage.kt, ElkoyDialogue.travelCutscene)
    private suspend fun Player.escort(toVillage: Boolean) {
        statement("Elkoy escorts you through the maze...", clickToContinue = false)
        open("fade_out")
        delay(2)
        if (toVillage) {
            tele(2515, 3159)
        } else {
            tele(2504, 3192)
        }
        open("fade_in")
        closeDialogue()
    }
}
