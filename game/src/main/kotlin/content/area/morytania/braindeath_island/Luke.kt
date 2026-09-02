package content.area.morytania.braindeath_island

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.Shock
import content.entity.player.dialogue.type.ChoiceOption
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.male

class Luke : Script {
    init {
        npcOperate("Talk-to", "50_luke") { (target) ->
            player<Quiz>("Are you all right?")
            npc<Quiz>("Who goes there?")
            npc<Angry>("Arr! A landlubber!")
            npc<Neutral>("Begone afore I take my cutlass to ye! I've been charged with guardin' this gate and no noodle-armed landlubbers will make it past me alive!")
            player<Neutral>("That's not a cutlass.")
            player<Neutral>("I think it's a twig.")
            npc<Angry>("Ye cheeky begger! I was wavin' my finger at ye!")
            player<Neutral>("All right...")
            npc<Sad>("Arrr... just 'cos me body happens to be 50% wood does not mean I'm heartless.")
            npc<Happy>("I got a bag of 'em here. Wanna see?")
            player<Quiz>("I think I'll pass.")
            mainQuestionsChoice()
        }
    }

    suspend fun Player.mainQuestionsChoice() {
        choice {
            whatHappened()
            howFlammable()
            whatsGoingOn()
        }
    }

    fun ChoiceOption.whatHappened(): Unit = option<Quiz>("What happened to you?") {
        npc<Neutral>("Well, it all starts with this albatross...")
        npc<Shifty>("Wait, never mind, I'll skip forward a bit.")
        player<Quiz>("Are you sure?")
        npc<Shifty>("I have to lad, Cap'n Donnie will flay what little is left of me if I told ye.")
        npc<Neutral>("But anyway, I got recruited to the zombie pirates along with the rest of the crew in an unspecified incident involvin this albatross.")
        npc<Neutral>("We was sailin' along happily, and I was partakin' of a little 'rum' in the crows nest.")
        npc<Sad>("Well, we hit either a really rough wave or some rocks.")
        npc<Happy>("Twas kind of hard for me to tell which, as I was well out of it by then!")
        npc<Sad>("Regardless I toppled from the crows nest into the water.")
        player<Quiz>("Is that how you got so badly injured?")
        npc<Neutral>("No ${ladOrLass()}!")
        npc<Neutral>("What happened next was that I discovered a new, previously uncharted reef of hard, spiky coral.")
        npc<Neutral>("I made a mental note of its location, and to this day it is still marked on our fleet's charts as Lukes Reef.")
        npc<Neutral>("I managed to grab a hold of our ship, the Inebriated, as it passed overhead.")
        npc<Sad>("And then I discovered another, taller, spikier reef of even sharper and more painful coral.")
        npc<Neutral>("To this day it is still marked on our fleet's charts as The Other 50% Reef.")
        player<Shock>("Owwwwwwwww...")
        npc<Sad>("It gets worse...")
        npc<Neutral>("When they hauled what was left of me on deck, they dropped me onto the floor while they decided what to do with me.")
        npc<Neutral>("Bear in mind this would be on a ghost ship, the planks of which sweat a thick mixture of stagnant water...and pure salt crystals.")
        player<Shock>("Oh...my...god...")
        npc<Happy>("But on the good side, all my thrashin' and pained squealin' settled the matter in the Captain's mind, and he had the shipwright carve me half a body out of his Witchwood Planks.")
        player<Quiz>("Witchwood? What's that?")
        npc<Neutral>("Tis a special, magical wood from a now extinct tree.")
        npc<Neutral>("Once they nailed it all in place the stuff moves like it is part of me body.")
        npc<Neutral>("The stuff will also grow back if it breaks, which is dead handy!")
        player<Happy>("Wow, that stuff must be very valuable!")
        npc<Happy>("Arr! That it be!")
        npc<Sad>("So, that be the tale of how I managed to lose precisely 50% of my body.")
        player<Sad>("There there.")
    }

    fun ChoiceOption.howFlammable(): Unit = option("How flammable are you?") {
        player<Neutral>("So how flammable are you?")
        npc<Angry>("What kind of a question is that?")
        player<Neutral>("A rather rude, personal one?")
        npc<Angry>("Ye've got that right!")
    }

    fun ChoiceOption.whatsGoingOn(): Unit = option<Quiz>("So what is going on here anyway?") {
        npc<Angry>("Ye expect me to talk?")
        player<Neutral>("No Mr. Luke, I expect you to die!")
        npc<Happy>("Hah! I'm one step ahead of ye!")
        player<Sad>("Egad, outsmarted by the man with the wooden brain.")
        player<Quiz>("But seriously, what is going on here?")
        npc<Neutral>("I can't tell ye lad.")
        npc<Neutral>("The Cap'n would have me whittled down to toothpicks if I did.")
        player<Quiz>("Well if you can't tell me, perhaps you could show me through the medium of Interpretive Dance?")
        npc<Neutral>("No. Just...no.")
        player<Quiz>("Mime?")
        npc<Angry>("Look, lad I'm not tellin' ye a thing! So clear out while ye still can!")
    }

    private fun Player.ladOrLass(): String = if (male) "lad" else "lass"
}
