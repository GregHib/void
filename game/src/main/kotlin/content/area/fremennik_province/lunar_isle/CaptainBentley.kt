package content.area.fremennik_province.lunar_isle

import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.character.jingle
import world.gregs.voidps.engine.entity.character.move.tele

class CaptainBentley : Script {
    init {
        playerSpawn {
            sendVariable("lunar_diplomacy")
        }

        npcOperate("Talk-to", "captain_bentley*") {
            if (tile in Areas["lunar_isle"]) {
                player<Neutral>("Hi.")
                npc<Neutral>("And you're wanting what now?")
                choice {
                    option<Neutral>("Can you take me back to Rellekka please?") {
                        npc<Neutral>("I'll take you as far as Pirates' Cove. You'll have to find the rest of the way back yourself.")
                        tele(2224, 3796, 2)
                    }
                    option<Neutral>("So we're here?") {
                        npc<Neutral>("Yep. You're free to explore the island. Be careful though, the Moon Clan are very powerful, it wouldn't be wise to wrong them.")
                        player<Neutral>("Thanks, I'll keep that seal of passage close.")
                    }
                }
            } else {
                player<Neutral>("Can we head to Lunar Isle?")
                npc<Neutral>("Sure matey!")
                // TODO interface 431
                jingle("sailing_theme_short")
                tele(2137, 3900, 2)
            }
        }

        npcOperate("Travel", "captain_bentley*") {
            if (tile in Areas["lunar_isle"]) {
                tele(2224, 3796, 2)
            } else {
                tele(2137, 3900, 2)
            }
        }
    }
}
