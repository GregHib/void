package content.area.kandarin.ardougne.witchaven

import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.type.Tile

class Jeb : Script {

    init {
        // https://runescape.wiki/w/Transcript:Jeb (Between The Slug Menace and Salt in the Wound)
        npcOperate("Talk-to", "jeb_witchaven_3") {
            // The same npc stands on the platform once Jeb takes over from Holgart
            if (tile.x > PLATFORM_X) {
                player<Neutral>("Hey, Jeb.")
                npc<Neutral>("Business here is complete. Do you wish to return to the dry land?")
                choice {
                    option<Neutral>("Okay, let's go back.") {
                        npc<Neutral>("Then board the rowing boat.")
                        sail(Tile(2719, 3301))
                    }
                    option<Neutral>("No, I'm going to stay a while.") {
                        npc<Neutral>("Then stay if that is your wish.")
                    }
                }
                return@npcOperate
            }
            player<Neutral>("I understand that you can take me to the Fishing Platform.")
            npc<Neutral>("Yes, we can do that. Board the boat and we shall depart.")
            sail(Tile(2782, 3273), "You arrive at the fishing platform.")
        }
    }

    private suspend fun Player.sail(destination: Tile, arrival: String? = null) {
        open("fade_out")
        delay(4)
        tele(destination)
        open("fade_in")
        if (arrival != null) {
            statement(arrival)
        }
    }

    companion object {
        private const val PLATFORM_X = 2760
    }
}
