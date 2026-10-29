package content.area.kandarin.ardougne.witchaven

import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player

class Jeb : Script {

    init {
        // https://runescape.wiki/w/Transcript:Jeb (Between The Slug Menace and Salt in the Wound)
        npcOperate("Talk-to", "jeb_witchaven_3") {
            player<Neutral>("I understand that you can take me to the Fishing Platform.")
            npc<Neutral>("Yes, we can do that. Board the boat and we shall depart.")
            sailToPlatform()
        }
    }

    private suspend fun Player.sailToPlatform() {
        open("fade_out")
        delay(4)
        tele(2782, 3273)
        open("fade_in")
        statement("You arrive at the fishing platform.")
    }
}
