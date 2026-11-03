package content.area.misthalin.dorgesh_kaan

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.type.random

class Zanik : Script {
    init {
        // https://runescape.wiki/w/Transcript:Zanik?oldid=5066494 (Zanik's Room in Dorgesh-Kaan)
        npcOperate("Talk-to", "zanik_dorgesh_kaan_5") {
            npc<Happy>("Hello, $name.")
            choice {
                option<Quiz>("What have you been up to?") {
                    travels()
                }
                option<Neutral>("About Bandos...") {
                    npc<Sad>("Oh, $name, I don't want to talk about that now.")
                }
                option<Neutral>("Goodbye.") {
                    npc<Happy>("Goodbye $name. See you soon.")
                }
            }
        }
    }

    private suspend fun Player.travels() {
        when (random.nextInt(5)) {
            0 -> npc<Neutral>("I crossed the river into Morytania; the whole land was so gloomy and depressing. There was a whole castle full of strange creatures, and a swamp... I found this nice little town... Or at least I thought it was nice... But when I was in the bar I accidentally spilt someone's drink, and they all turned into wolf-men!")
            1 -> npc<Neutral>("I spent some time in Keldagrim. Some of the people were nice, but I didn't like the city so much. Everything was so industrial and it was all controlled by corporations. I ended up hanging out with this dwarf who ate a lot of kebabs. I don't quite remember what happened after that. I think I got a bit drunk!")
            2 -> npc<Sad>("I went and talked to Juna again, but she wouldn't talk to me. She still says I should have helped Bandos take over Dorgesh-Kaan. She says I had no right to go against my 'destiny'. She thinks that gods should always get what they want, just because they're gods, and that mortals just don't matter. I didn't realise she thought such hateful things. We used to be such good friends... Back when the city was still cut off, I used to sit with her for hours, listening to her stories of the surface and just imagining what it was like. I can see the surface for myself now, but still, I'm going to miss that.")
            3 -> npc<Happy>("I visited Varrock the other day. It's such a huge city, isn't it? Even bigger than home! There were so many people! I met a gypsy who said she could foresee the future, but she couldn't see mine!")
            else -> npc<Happy>("I visited the Kharidian Desert. I've never seen such a big expanse of empty ground! After growing up in these confined caves, it was amazing! Just the yellow ground and the blue sky, and me in the middle. I found a ruined city where there was a man made out of clay. He said he was left over from the God Wars and he said you had repaired him.")
        }
    }
}
