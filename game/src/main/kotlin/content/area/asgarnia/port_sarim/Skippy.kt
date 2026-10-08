package content.area.asgarnia.port_sarim

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.type.ChoiceOption
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.noInterest
import world.gregs.voidps.engine.inv.inventory

class Skippy : Script {

    init {
        npcOperate("Talk-To", "skippy") {
            introduction()
        }

        npcOperate("Sober-Up", "skippy") {
            if (!inventory.contains("bucket_of_water")) {
                needWaterReminder()
            } else {
                noInterest()
            }
        }
    }

    private suspend fun Player.introduction() {
        player<Quiz>("Are you all right? You seem a little...incoherent.")
        npc<Angry>("Inc'hearnt? Inc'herant! You...you...with yer fancy book-lernin' words. You'd be more than inc'herant if you'd seen...")
        npc<Angry>("(Dramatic pause)")
        npc<Angry>("THEM!")
        mainChoice()
    }

    private suspend fun Player.mainChoice() {
        choice("Select an Option") {
            reassureSkippy()
            askWhoTheyAre()
        }
    }

    private fun ChoiceOption.reassureSkippy() = option<Quiz>("I'm sure I would as well.") {
        player<Happy>("I'm going over here to talk to non-crazy people now.")
        npc<Angry>("Yeah? Yeah? Well when THEY come floppin' into your house and eat your furniture you'll be sorry!")
    }

    private fun ChoiceOption.askWhoTheyAre() = option("Who are (Dramatic pause) THEY?") {
        set("skippy_state", "need_bucket_of_water")
        player<Quiz>("Who are")
        player<Quiz>("(Dramatic pause)")
        player<Quiz>("THEY?")
        npc<Angry>("They! Those bloodthirsty, flesh-tearing devils! They are the reason I'm out here every day hurlin' bottles into the sea!")
        npc<Sad>("They are the reason I've lost everything, except the horrifying memory of what THEY look like...")
        player<Quiz>("And what do THEY look like?")
        npc<Angry>("Mudskippers!")
        player<Confused>("Mudskippers?")
        npc<Angry>("Aye, Mudskippers! Those ferocious, ravening, evil, beady-eyed terrors of the deep!")
        player<Quiz>("I...see...")
        npc<Angry>("I was ambushed by them way back, see. They got the drop on me...I can't remember where, somewhere around here though.")
        player<Quiz>("These would be the mudskippers, right?")
        npc<Angry>("Aye! The mudskippers! Huge they were! Ten feet of glistening, muddy flesh floppin' towards me with white foam flying from their gnashing fangs!")
        npc<Angry>("I fought them up and down the beach, with the tide rising and more of them leaping towards me with cutlasses drawn!")
        player<Neutral>("This is fascinating, but I have to be...")
        npc<Angry>("Shut yer' word-hole and listen! I can't remember all the details, as I'm sure they must have hit me quite hard, but the last thing I remember before it all went black...")
        npc<Angry>("...was one of those devils rearing over me, its eyes glowin' red with the fires of hell!")
        player<Shifty>("Fires of hell...right. I believe you.")
        npc<Angry>("No you don't! You think I'm crazy, like all the rest! Well, if I'm crazy, how did I get these?")
        player<Quiz>("Get what?")
        statement("Skippy shows you what appears to be massive bite scars on his legs. You're no expert, but they look like... giant mudskipper bites!")
        player<Neutral>("Giant mudskipper bites! Where did you get those?")
        npc<Sad>("I can't remember... I've been drinking to forget the horror, and all I seem to have forgotten is where it all happened...")
        player<Neutral>("Hmmm...I suppose if I sober you up you may well start to recall.")
        npc<Neutral>("You'll have a job. I've been drinking this for a week.")
        player<Quiz>("'Captain Braindeath's Extra Strength Rum/Drain Cleaner. Now 50% more debilitating'?")
        npc<Happy>("It's the extra sheep tranquilizers that gives it that added kick!")
        player<Neutral>("Stay here and I'll be right back. Try not to move. Or go near any open flames.")
    }

    private suspend fun Player.needWaterReminder() {
        player<Neutral>("You know, I could shock him out of it if I could find some cold water...")
    }
}
