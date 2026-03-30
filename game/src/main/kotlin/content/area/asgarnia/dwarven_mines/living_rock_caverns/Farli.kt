package content.area.asgarnia.dwarven_mines.living_rock_caverns

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Idle
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Shock
import content.entity.player.dialogue.Unamused
import content.entity.player.dialogue.type.ChoiceOption
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.type.Tile

class Farli : Script {
    private val name = "Farli"

    init {
        objectOperate("Talk-to", "farli") {
            talkWith(NPCs.find(Tile(3655, 5115), "farli"))
            npc<Happy>("Hello there!", title = name)
            player<Happy>("Hi.")
            npc<Quiz>("What brings you down here then?", title = name)
            choice {
                thisPlace()
                whoAreYou()
                pulley()
                dontTalk()
            }
        }
    }

    private fun ChoiceOption.thisPlace() {
        option<Quiz>("What is this place?") {
            npc<Neutral>("It's a cavern.", title = name)
            player<Shock>("Really? Great, thanks for that.")
            npc<Happy>("No problem!", title = name)
            player<Quiz>("So, is there actually anything in this cavern?")
            npc<Neutral>("Well, there's rocks. A bit of water here and there too.", title = name)
            player<Unamused>("Right. Good to know. I'm glad you were here, actually, because, you know, I'd have been lost without that.")
            npc<Happy>("Happy to help!", title = name)
        }
    }

    private fun ChoiceOption.whoAreYou() {
        option<Quiz>("Who are you?") {
            npc<Happy>("I'm Farli. I came down here to explore the moment the cavern entrance opened up.", title = name)
            player<Shock>("Have you discovered anything down there?")
            npc<Confused>("Well, I did stumble across some nice-looking deposits of ore and some strange fish in the waters, but I haven't really been able to investigate further.", title = name)
            player<Quiz>("Why not?")
            npc<Shock>("It's as if the rocks are moving... Whenever I've tried to map this place, I find rocks are in different places to when I checked before!", title = name)
            player<Quiz>("Moving rocks? Are you sure you haven't been drinking?")
            npc<Neutral>("Well, yes. I mean no. Look, that's not important. What is important is that the rocks down here are moving!", title = name)
            player<Unamused>("I'll be sure to check that out...")
            npc<Neutral>("It's true! One time I was mining a rock, or what I thought was a rock, and it upped and ran off with my best pickaxe!", title = name)
            player<Idle>("...")
            choice {
                thisPlace()
                pulley()
                dontTalk()
            }
        }
    }

    private fun ChoiceOption.pulley() {
        option<Quiz>("What does this pulley do?") {
            npc<Neutral>("Oh, that's just to get my supplies in and out of the cavern.", title = name)
            player<Quiz>("How does that work then?")
            npc<Neutral>("The miners above use the mine cart system to move about what we need from Keldagrim.", title = name)
            player<Quiz>("Hmm, that could be handy for depositing things into my bank. You think you could arrange that?")
            npc<Quiz>("Hmm, maybe. I suppose if you're down here to help then I can sort something out.", title = name)
            player<Happy>("Thanks!")
            choice {
                thisPlace()
                whoAreYou()
                dontTalk()
            }
        }
    }

    private fun ChoiceOption.dontTalk() {
        option<Unamused>("Actually, I don't want to talk to you.") {
            npc<Angry>("Hmph! How rude!", title = name)
        }
    }
}