package content.skill.summoning.familiar

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Scared
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.skill.summoning.familiarTeleport
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random

class SpiritLarupia : Script {
    init {
        npcOperate("Interact", "spirit_larupia_familiar") {
            choice {
                option("Chat") {
                    chat()
                }
                // The larupia carries its owner home to the Feldip Hills hunter area.
                option("Teleport") {
                    familiarTeleport(Tile(2556, 2913), "the larupia")
                }
            }
        }
    }

    private suspend fun Player.chat() {
        when (random.nextInt(4)) {
            0 -> {
                player<Happy>("Kitty cat!")
                npc<Neutral>("What is your wish master?")
                player<Happy>("Have you ever thought about doing something other than hunting and serving me?")
                npc<Neutral>("You mean, like stand-up comedy, master?")
                player<Happy>("Umm...yes, like that.")
                npc<Neutral>("No, master.")
            }
            1 -> {
                player<Happy>("Hello friend!")
                npc<Neutral>("'Friend', master? I do not understand this word.")
                player<Happy>("Friends are people, or animals, who like one another. I think we are friends.")
                npc<Neutral>("Ah, I think I understand friends, master.")
                player<Happy>("Great!")
                npc<Neutral>("A friend is someone who looks tasty, but you don't eat.")
                player<Scared>("!")
            }
            2 -> {
                npc<Neutral>("What are we doing today, master?")
                player<Happy>("I don't know, what do you want to do?")
                npc<Neutral>("I desire only to hunt and to serve my master.")
                player<Happy>("Err...great! I guess I'll decide then.")
            }
            3 -> {
                npc<Neutral>("Master, do you ever worry that I might eat you?")
                player<Happy>("No, of course not! We're pals.")
                npc<Neutral>("That is good, master.")
                player<Happy>("Should I?")
                npc<Neutral>("Of course not, master.")
                player<Happy>("Oh. Good.")
            }
        }
    }
}
