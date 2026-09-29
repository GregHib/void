package content.minigame.fist_of_guthix

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player

class Getorix : Script {

    init {
        npcOperate("Talk-to", "getorix") {
            set("fist_of_guthix_spoken_to_getorix", true)
            if (hasCompletedFiaraWhatIsThisPlaceDialogue()) {
                postFiara()
            } else {
                npc<Neutral>("Welcome to this grand cave of Guthix, traveller.")
                player<Quiz>("So, what do I do here?")
                npc<Neutral>("Ah, you should probably talk to the organ grinder not the monkey.")
                player<Quiz>("What?")
                npc<Neutral>("Talk to Fiara, she's in charge here.")
                player<Quiz>("Which is she? None of you look female to me.")
                npc<Neutral>("No, no - Fiara is the Guardian of this place. Created by Guthix to keep a watchful eye over this holy site.")
                player<Quiz>("Guardian? You mean that weird looking giant earwig at the back of the cave?")
                npc<Angry>("Don't refer to a holy Guardian of Guthix like that. You can call her Lady Fiara.")
                player<Quiz>("But she's not a lady, she's a giant earwig.")
                npc<Angry>("Don't argue, just go and talk to her. And be polite.")
            }
        }

        objectOperate("Enter", "fist_of_guthix_lobby_passageway", handler = ::enterPassageway)
        objectOperate("Go-through", "fist_of_guthix_lobby_passageway", handler = ::enterPassageway)
        objectOperate("Pass-through", "fist_of_guthix_lobby_passageway", handler = ::enterPassageway)
    }

    private suspend fun Player.postFiara() {
        npc<Neutral>("Welcome to this grand cave of Guthix, traveller.")
        postFiaraMenu()
    }

    private suspend fun Player.postFiaraMenu() = choice("Select an Option") {
        option("So, what is this place?") {
            explainPlace()
        }
        option("Who's the weird looking giant earwig?") {
            askAboutFiara()
        }
        option<Neutral>("I'd better get going.") {
        }
    }

    private suspend fun Player.explainPlace() {
        player<Quiz>("So, what is this place?")
        npc<Neutral>("This is a sacred site of Guthix.")
        player<Quiz>("Why is it sacred?")
        npc<Neutral>("Guthix places guardians in places of holy power. There is one such guardian here.")
        player<Quiz>("But what is special about this place?")
        npc<Neutral>("It exudes a dangerous magical power that we have been entrusted to protect.")
        player<Quiz>("Where did this power come from?")
        npc<Neutral>("It comes from the centre of the big cave.")
        player<Quiz>("What's special about the centre?")
        npc<Neutral>("It's very magical.")
        player<Quiz>("Yes, I get that. Why is it magical?")
        npc<Neutral>("Something to do with the Fist of Guthix.")
        player<Quiz>("And what is that, exactly?")
        npc<Neutral>("The truth is, none of us really know.")
        npc<Neutral>("This place is special to Guthix, because Fiara is here.")
        npc<Neutral>("And we know it has something to do with the 'Fist of Guthix' because Fiara occasionally mentions it.")
        npc<Neutral>("But other than that, we only have theories. Do you want to hear about them?")
        choice("Select an Option") {
            option("I suppose so...") {
                theoryMenu()
            }
            option<Neutral>("I'd better get going.") {
            }
        }
    }

    private suspend fun Player.theoryMenu() {
        choice("Select an Option") {
            option("Getorix's theory.") {
                npc<Neutral>("Well, this is definitely the truth. Don't believe what Alran and Pontimer tell you, they are just making it up.")
                npc<Neutral>("At the end of the God Wars, Guthix returned to Gielinor and was angered by the destruction caused by the other gods in their vanity and quest for power. So, maybe he punched the ground in anger, leaving an imprint of power on the ground that has lasted ever since.")
                npc<Neutral>("Because he was angry at the time, the power would have been tainted with aggression, which is why he left Fiara as a guardian, to make sure the power did not escape unfettered.")
                npc<Neutral>("Do you want to hear another theory?")
                theoryMenu()
            }
            option("Pontimer's theory.") {
                npc<Neutral>("This is Pontimer's personal delusion on the subject.")
                npc<Neutral>("After the God Wars, Guthix found something dangerous here and used his power to protect it.")
                player<Quiz>("What did he find?")
                npc<Neutral>("I don't know for certain, but it must be under that strange impression in the big cave. I think that mark is some sort of ward that Guthix has left to protect the site.")
                player<Quiz>("So where does the Fist of Guthix come into this, then?")
                npc<Neutral>("Exactly! That's what I told him. It doesn't make sense. My theory makes much more sense.")
                npc<Neutral>("Do you want to hear another theory?")
                theoryMenu()
            }
            option("Alran's theory.") {
                npc<Neutral>("This is what Alran thinks, for what it's worth...which isn't very much, if you ask me.")
                npc<Neutral>("Guthix has apparently left us a test. He deliberately created this site of power as a test of individual strength and balance.")
                player<Quiz>("Does Guthix do that sort of thing?")
                npc<Neutral>("Well, no! I agree with you entirely. This theory has so many holes that it makes no sense at all!")
                npc<Neutral>("Do you want to hear another theory?")
                theoryMenu()
            }
            option<Neutral>("I'd better get going.") {
            }
        }
    }

    private suspend fun Player.askAboutFiara() {
        player<Quiz>("Who's the weird looking giant earwig?")
        npc<Angry>("Shhh! Give Fiara some respect! She can hear you, you know.")
        player<Quiz>("Fiara? It has a name?")
        npc<Angry>("Of course, Guthix chooses names for all his guardians.")
        player<Neutral>("If...err...Fiara is a Guardian, what is she guarding?")
        npc<Neutral>("She guards this site. It is a holy site of Guthix.")
        choice("Select an Option") {
            option<Quiz>("So, what is this place?") {
                explainPlace()
            }
            option<Quiz>("How long has Fiara been here?") {
                npc<Neutral>("Errr, ages. Since the end of the God Wars I think. She doesn't talk about that much.")
                choice("Select an Option") {
                    option("So what are you doing here?") {
                        explainPlace()
                    }
                    option<Neutral>("I'd better get going.") {
                    }
                }
            }
            option<Neutral>("I'd better get going.") {
            }
        }
    }

    private suspend fun enterPassageway(player: Player, interact: world.gregs.voidps.engine.entity.character.mode.interact.PlayerOnObjectInteract) {
        with(player) {
            if (!hasCompletedFiaraWhatIsThisPlaceDialogue()) {
                npc<Angry>("getorix", "You cannot go in there unless you've talked to Lady Fiara!")
            }
        }
    }

    private fun Player.hasCompletedFiaraWhatIsThisPlaceDialogue(): Boolean = get("fist_of_guthix_completed_fiara_what_is_this_place_dialogue", false)
}
