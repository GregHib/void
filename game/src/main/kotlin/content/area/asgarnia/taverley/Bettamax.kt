package content.area.asgarnia.taverley

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Disheartened
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Pleased
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.*
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.mode.Follow
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player

class Bettamax : Script {

    init {
        npcSpawn("wilbur") {
            val bettamax = NPCs.find(tile.zone, "bettamax")
            mode = Follow(this, bettamax)
            watch(bettamax)
        }
        npcOperate("Talk-to", "bettamax") {
            // https://www.youtube.com/watch?v=UNh33Y0NJe8
            npc<Happy>("Hello there.")
            choice {
                whoBettamaxIs()
                jadinko()
                seeShop()
            }
        }
    }

    fun ChoiceOption.whoBettamaxIs() = option<Neutral>("Hello. Who are you?") {
        npc<Happy>("My name is Bettamax, I'm botanist in training.")
        choice {
            botanist()
            studies()
            seeShop()
        }
    }

    fun ChoiceOption.jadinko() = option<Quiz>("What's that walking next to you?") {
        npc<Neutral>("Oh, that's Wilbur. He's a jadinko.")
        player<Quiz>("What's a jadinko?")
        npc<Disheartened>("I have no idea.")
        choice {
            whoBettamaxIs()
            seeShop()
        }
    }
    fun ChoiceOption.seeShop() = option("Can I see your shop?") {
        openShop("bettamaxs_shop")
    }

    fun ChoiceOption.studies() = option("What are you studying?") {
        waitingMentor()
    }

    fun ChoiceOption.botanist() = option<Quiz>("What's a botanist?") {
        npc<Pleased>("Someone that studies and identifies different plants and their properties.")
        waitingMentor()
    }

    suspend fun Player.waitingMentor() {
        player<Quiz>("What are you studying?")
        npc<Disheartened>("Currently nothing. I'm waiting for Astlayrix, my mentor, to come back from the herblore habitat.")
        player<Quiz>("Habitat?")
        npc<Happy>("Oh, it's fascinating. There are all these new creatures and plants and, well, I'm probably not the best person to explain it...")
        npc<Neutral>("If you want to go see for yourself, I have a small stock of these weird teleport-bag things. They'll take you straight there.")
        choice("Would you like to buy one?") {
            option("Yes.") {
                openShop("bettamaxs_shop")
            }
            option("No.") {
                player<Disheartened>("Maybe another time.")
            }
        }
    }
}
