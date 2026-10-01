package content.area.kandarin.ardougne.west_ardougne

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Idle
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Shock
import content.entity.player.dialogue.type.ChoiceOption
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.inv.item.addOrDrop
import content.quest.questCompleted
import content.quest.questStage
import content.quest.refreshQuestJournal
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.carriesItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

class Elena : Script {

    init {
        npcOperate("Talk-to", "elenap_vis") {
            player<Happy>("Hi, you're free to go! Your kidnappers don't seem to be about right now.")
            npc<Idle>("Thank you, being kidnapped was so inconvenient. I was on my way back to East Ardougne with some samples, I want to see if I can diagnose a cure for this plague.")
            player<Idle>("Well you can leave via the manhole in the middle of the city.")
            npc<Idle>("Go and see my father, I'll make sure he adequately rewards you. Now I'd better leave while I still can.")
            open("fade_out")
            delay(4)
            set("plaguecity_hide_edmond_up_top", false)
            set("plaguecity_elena_at_home", true)
            set("plague_city", "freed_elena")
            delay(3)
            open("fade_in")
        }

        npcOperate("Talk-to", "elena2_vis") {
            when (questStage("biohazard")) {
                0 -> distillatorRequest()
                1 -> anyLuck()
                2, 3 -> spokenToJerico()
                4 -> guardsDistracted()
                5, 6 -> notYetFound()
                7 -> returnDistillator()
                10, 12 -> reminders()
                14 -> guidorsFindings()
                15 -> seeTheKing()
                else -> completed()
            }
        }
    }

    private suspend fun Player.distillatorRequest() {
        player<Happy>("Good day to you, Elena.")
        npc<Happy>("You too, thanks for freeing me.")
        npc<Sad>("It's just a shame the mourners confiscated my equipment.")
        player<Quiz>("What did they take?")
        npc<Neutral>("My distillator, I can't test any plague samples without it. They're holding it in the mourner quarters in West Ardougne.")
        npc<Neutral>("I must somehow retrieve that distillator if I am to find a cure for this awful affliction.")
        if (!questCompleted("plague_city")) {
            player<Neutral>("Well, good luck.")
            npc<Confused>("Thanks traveller.")
            return
        }
        choice {
            acceptQuest()
            goodLuck()
        }
    }

    private fun ChoiceOption.acceptQuest(): Unit = option<Neutral>("I'll try to retrieve it for you.") {
        npc<Neutral>("I was hoping you would say that. Unfortunately they discovered the tunnel and filled it in. We need another way over the wall.")
        player<Quiz>("Any ideas?")
        set("biohazard", "started")
        set("plaguecity_dug_mud_pile", false)
        refreshQuestJournal()
        npc<Neutral>("My father's friend Jerico is in communication with West Ardougne. He might be able to help us, he lives next to the chapel.")
    }

    private fun ChoiceOption.goodLuck(): Unit = option<Neutral>("Well, good luck.") {
        npc<Confused>("Thanks traveller.")
    }

    private suspend fun Player.anyLuck() {
        player<Happy>("Hello Elena.")
        npc<Quiz>("Hello brave adventurer. Any luck finding my distillator?")
        player<Sad>("No, I'm afraid not.")
        npc<Neutral>("Speak to Jerico, he will help you to cross the wall. He lives next to the chapel.")
    }

    private suspend fun Player.spokenToJerico() {
        player<Happy>("Hello Elena, I've spoken to Jerico.")
        npc<Quiz>("Was he able to help?")
        player<Neutral>("He has two friends who will help me cross the wall, but first I need to distract the watch tower.")
        npc<Confused>("Hmm, could be tricky.")
    }

    private suspend fun Player.guardsDistracted() {
        player<Happy>("Elena, I've distracted the guards at the watch tower.")
        npc<Happy>("Yes, I saw. Quickly meet with Jerico's friends and cross the wall before the pigeons fly off.")
    }

    private suspend fun Player.notYetFound() {
        player<Happy>("Hello again.")
        npc<Quiz>("You're back, did you find the distillator?")
        player<Sad>("I'm afraid not.")
        npc<Sad>("I can't test the samples without the distillator. Please don't give up until you find it.")
    }

    private suspend fun Player.returnDistillator() {
        npc<Confused>("So, have you managed to retrieve my distillator?")
        if (!carriesItem("distillator")) {
            player<Sad>("I'm afraid not.")
            npc<Sad>("Oh, you haven't... People may be dying even as we speak.")
            return
        }
        player<Happy>("Yes, here it is!")
        npc<Happy>("You have? That's great! Now can you pass me those reaction agents please?")
        inventory.remove("distillator")
        message("You hand Elena the distillator and an assortment of vials.")
        player<Neutral>("Those look pretty fancy.")
        npc<Neutral>("Well, yes and no. The liquid honey isn't worth much, but the others are. Especially this colourless ethenea. Be careful with the sulphuric broline, it's highly poisonous.")
        player<Neutral>("You're not kidding, I can smell it from here!")
        message("Elena puts the agents through the distillator.")
        delay(2)
        npc<Confused>("I don't understand... the touch paper hasn't changed colour at all...")
        npc<Neutral>("You'll need to go and see my old mentor Guidor. He lives in Varrock. Take these vials and this sample to him.")
        set("biohazard", "collect_chemicals")
        for (item in CHEMICALS) {
            addOrDrop(item)
        }
        message("Elena gives you three vials and a sample in a tin container.")
        npc<Neutral>("But first you'll need some more touch paper. Go and see the chemist in Rimmington.")
        npc<Neutral>("Just don't get into any fights, and be careful who you speak to.")
        npc<Neutral>("Those vials are fragile, and plague carriers don't tend to be too popular.")
    }

    private suspend fun Player.reminders() {
        npc<Quiz>("What are you doing back here?")
        choice {
            saidGoodbye()
            lostTheChemicals()
            forgotTheTask()
        }
    }

    private fun ChoiceOption.saidGoodbye(): Unit = option<Sad>("I just find it hard to say goodbye sometimes.") {
        npc<Neutral>("Yes... I have feelings for you too...")
        npc<Angry>("Now get back to work!")
    }

    private fun ChoiceOption.lostTheChemicals(): Unit = option<Sad>("I'm afraid I've lost some of the stuff that you gave me...") {
        npc<Neutral>("That's alright, I've got plenty.")
        for (item in CHEMICALS) {
            if (!carriesItem(item)) {
                addOrDrop(item)
            }
        }
        npc<Neutral>("Ok, so that's your colourless ethenea... Some highly toxic sulphuric broline... And some bog-standard liquid honey.")
        player<Happy>("Great, I'll be on my way.")
    }

    private fun ChoiceOption.forgotTheTask(): Unit = option<Sad>("I've forgotten what I need to do.") {
        npc<Neutral>("Go to Rimmington and get some touch paper from the chemist. Use his errand boys to smuggle the vials into Varrock.")
        npc<Neutral>("Then collect the samples and take them to Guidor, my old mentor.")
        player<Neutral>("Ok, I'll get to it.")
    }

    private suspend fun Player.guidorsFindings() {
        npc<Shock>("You're back! So what did Guidor say?")
        player<Neutral>("Nothing.")
        npc<Shock>("What?")
        player<Neutral>("He said that there is no plague.")
        npc<Confused>("So what, this thing has all been a big hoax?")
        player<Neutral>("Or maybe we're about to uncover something huge.")
        npc<Neutral>("Then I think this thing may be bigger than both of us.")
        player<Quiz>("What do you mean?")
        set("biohazard", "told_elena")
        npc<Neutral>("I mean you need to go right to the top... You need to see the King of East Ardougne!")
    }

    private suspend fun Player.seeTheKing() {
        player<Neutral>("Hello Elena.")
        npc<Neutral>("You must go see King Lathas immediately!")
    }

    private suspend fun Player.completed() {
        player<Happy>("Hello Elena.")
        npc<Happy>("Hey, how are you?")
        player<Happy>("Good thanks, yourself?")
        npc<Happy>("Not bad, let me know when you hear from King Lathas again.")
        player<Happy>("Will do.")
    }

    private companion object {
        val CHEMICALS = listOf("ethenea", "liquid_honey", "sulphuric_broline", "plague_sample")
    }
}
