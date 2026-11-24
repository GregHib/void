package content.area.kandarin.piscatoris

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
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

class KathyCorkat : Script {

    init {
        // https://runescape.wiki/w/Transcript:Kathy_Corkat (After Swan Song)
        npcOperate("Talk-to", "kathy_corkat_piscatoris") { (target) ->
            if (target.atColony()) {
                colony()
            } else {
                eaglesPeak()
            }
        }

        npcOperate("Travel", "kathy_corkat_piscatoris") { (target) ->
            row(target.atColony())
        }
    }

    private fun NPC.atColony() = tile.y > 3560

    private suspend fun Player.eaglesPeak() {
        npc<Happy>("Hello dearie. Heading up north to the Fishing Colony, are we?")
        choice {
            option<Happy>("Yes please.") {
                row(false)
            }
            option<Neutral>("No thanks.")
        }
    }

    private suspend fun Player.colony() {
        npc<Happy>("Are ye wantin' a lift up the river, dearie?")
        choice {
            option("Why don't you row right into the Colony?") {
                player<Quiz>("Why don't you row right into the Colony? It'd make it much easier to get in there.")
                npc<Happy>("Oh, Franklin doesn't like it if I take my boat too close. He says I'll damage the nets and scare the fish. But were you wanting a lift up to the river?")
                colonyChoice()
            }
            option<Happy>("Yes please.") {
                row(true)
            }
            option<Neutral>("No thanks.")
        }
    }

    private suspend fun Player.colonyChoice() {
        choice {
            option<Happy>("Yes please.") {
                row(true)
            }
            option<Neutral>("No thanks.")
        }
    }

    // Tiles and timing from 2009scape (KathyCorkatDialogue.sail)
    private suspend fun Player.row(fromColony: Boolean) {
        statement(if (fromColony) "Kathy Corkat rows you up the river..." else "Kathy Corkat rows you down the river to the sea...", clickToContinue = false)
        open("fade_out")
        delay(4)
        if (fromColony) {
            tele(2369, 3484)
        } else {
            tele(2357, 3641)
        }
        open("fade_in")
        delay(4)
        closeDialogue()
    }
}
