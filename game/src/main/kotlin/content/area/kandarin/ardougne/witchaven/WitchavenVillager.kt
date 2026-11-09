package content.area.kandarin.ardougne.witchaven

import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.type.random
import kotlin.random.nextInt

class WitchavenVillager : Script {

    init {
        // https://runescape.wiki/w/Transcript:Witchaven_villager (Between The Slug Menace and Salt in the Wound)
        npcOperate("Talk-to", "witchaven_villager_witchaven_4_2,witchaven_villager_witchaven_5_2,witchaven_villager_witchaven_6_2") {
            player<Neutral>("Hello there.")
            statement("Their eyes are staring vacantly into space.")
            randomChat()
        }
    }

    private suspend fun Player.randomChat() {
        when (random.nextInt(0..4)) {
            0 -> {
                npc<Neutral>("Ahh, our saviour.")
                player<Neutral>("Please don't remind me.")
                npc<Neutral>("Do not worry, soon your regret will be gone.")
                player<Neutral>("If you think you will get to me...")
                npc<Neutral>("All in good time.")
            }
            1 -> {
                npc<Neutral>("You! You did it!")
                player<Neutral>("I didn't mean to!")
                npc<Neutral>("You killed him!")
                player<Neutral>("It was an accide... Killed who?")
                npc<Neutral>("Our Prince, you killed our Prince.")
                player<Neutral>("Oh that, yes I did.")
                npc<Neutral>("Leave us alone.")
            }
            2 -> {
                npc<Neutral>("Free. She is free...")
                player<Neutral>("What?")
                npc<Neutral>("The mother is free.")
            }
            3 -> {
                npc<Neutral>("Soon now... So soon...")
                npc<Neutral>("The stars are almost right.")
                player<Neutral>("For what?")
                npc<Neutral>("...")
            }
            else -> {
                npc<Neutral>("Ye mariners all, as ye pass by,")
                npc<Neutral>("Come in and drink if you are dry,")
                npc<Neutral>("Come spend, me lads, your money brisk,")
                npc<Neutral>("And pop your nose in a jug of this.")
                player<Neutral>("You're not fooling anyone you know.")
                npc<Neutral>("We fooled you easily enough.")
            }
        }
    }
}
