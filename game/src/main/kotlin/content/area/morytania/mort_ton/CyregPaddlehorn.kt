package content.area.morytania.mort_ton

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Scared
import content.entity.player.dialogue.type.ChoiceOption
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.member.myreque.hasAllWeapons
import content.quest.member.myreque.myrequeStage
import content.quest.questCompleted
import content.quest.questStage
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

class CyregPaddlehorn : Script {
    init {
        npcOperate("Talk-to", "cyreg_paddlehorn") {
            when {
                questCompleted("in_search_of_the_myreque") -> {
                    player<Neutral>("Thanks for your help in finding the Myreque.")
                    npc<Sad>(
                        "Well, I'd like to say that you're welcome. But I heard that you led Vanstrom " +
                            "straight to their hideout and he killed Sani and Harold. I feel so guilty.",
                    )
                    player<Sad>("Hmm, yeah, I know how you feel.")
                    generalQuestionsChoice()
                }
                questStage("in_search_of_the_myreque") <= myrequeStage("persuaded_boatman") -> earlyCyregOptions()
                else -> {
                    player<Quiz>("Can I ask some more questions?")
                    npc<Neutral>("Sure you can.")
                    generalQuestionsChoice()
                }
            }
        }
    }
}

suspend fun Player.earlyCyregOptions() {
    when (questStage("in_search_of_the_myreque")) {
        myrequeStage("unstarted") -> npc<Neutral>(
            "Sorry my friend but the boat rides are out of service due to the high ghast " +
                "activity in Mort Myre.",
        )
        myrequeStage("agreed_to_help") -> {
            npc<Neutral>("Hello there friend.")
            startWeaponDelivery()
        }
        myrequeStage("refused_delivery") -> askToFindMyreque()
        myrequeStage("persuaded_boatman") -> askForBoatRide()
    }
}

private suspend fun Player.startWeaponDelivery() {
    player<Happy>("Hello there, I have some weapons for you to give to the 'Myreque'.")
    if (!hasAllWeapons()) {
        npc<Confused>("Hmm, well, it doesn't look as if you've got the right sort of weapons to me!")
        return
    }
    npc<Neutral>("Hmm, I'm sure I don't know what you're talking about.")
    player<Neutral>("Come on, I know you're in cahoots with them, just take these weapons to them.")
    npc<Sad>(
        "Ok, seriously, I did some work for them before, but now it's just too dangerous. " +
            "I won't take the weapons to them, I'm sorry, it's just too dangerous.",
    )

    if (questStage("in_search_of_the_myreque") == myrequeStage("agreed_to_help")) {
        set("in_search_of_the_myreque", "refused_delivery")
    }

    askToFindMyreque()
}

private suspend fun Player.askToFindMyreque() {
    player<Quiz>("Can you tell me how to find the Myreque?")
    npc<Neutral>(
        "Their base is well hidden and I'm sorry but I can't reveal the directions. " +
            "Sorry but I guess you're all out of luck.",
    )
    persuasionChoice()
}

private suspend fun Player.persuasionChoice() {
    choice {
        comeOnTellMe()
        guessTheyllDie()
        offerCash()
        justWantToHelp()
        option<Neutral>("Ok, thanks.")
    }
}

private fun ChoiceOption.comeOnTellMe(): Unit = option<Neutral>("Oh come on, you can tell me!") {
    npc<Scared>("I'm sorry, I can't I just can't... people are watching... ...eyes everywhere!")
    persuasionChoice()
}

private fun ChoiceOption.offerCash(): Unit = option<Neutral>("I'll give you some cash if you tell me.") {
    npc<Angry>("You think you can buy me!")
    player<Confused>("Er, no, I just want to compensate you for your troubles.")
    npc<Neutral>("You keep your money and I'll keep my secrets.")
    persuasionChoice()
}

private fun ChoiceOption.justWantToHelp(): Unit = option<Neutral>("I just want to help them, I think they need help.") {
    npc<Sad>(
        "Aye, well, they may do...but it's just not safe and it's not likely to get safer any " +
            "time soon. Though I do feel sorry for Ivan, the baby of the group. He's seen too " +
            "few winters to be involved in such toil.",
    )
    persuasionChoice()
}

private fun ChoiceOption.guessTheyllDie(): Unit = option<Sad>("Well. I guess they'll just die without weapons.") {
    npc<Neutral>(
        "Hmm, you don't seem too concerned about their welfare... I'm glad I didn't tell you " +
            "where they were...in any case they're resourceful, they can look after themselves.",
    )
    player<Confused>("What's that supposed to mean?")
    npc<Neutral>(
        "They're resourceful folks, that's all I'm saying. Their leader, Veliaf, looks after " +
            "them well.",
    )
    resourcefulChoice()
}

private suspend fun Player.resourcefulChoice() {
    choice {
        comeOnTellMe()
        resourcefulEnough()
        offerCash()
        justWantToHelp()
        option<Neutral>("Ok, thanks.")
    }
}

private fun ChoiceOption.resourcefulEnough(): Unit = option<Quiz>("Resourceful enough to get their own steel weapons?") {
    npc<Quiz>(
        "Maybe they are...what do you care anyway? They've been up against it ever since they " +
            "got started. All of 'em have suffered more loss and heartache than you'll ever know. " +
            "Now, leave me be!",
    )
    upAgainstChoice()
}

private suspend fun Player.upAgainstChoice() {
    choice {
        whatHaveTheyBeenUpAgainst()
        whatLossAndHeartache()
        deathsOnYourHead()
        justWantToHelp()
        option<Neutral>("Ok, thanks.")
    }
}

private fun ChoiceOption.whatHaveTheyBeenUpAgainst(): Unit = option<Quiz>("What have they been up against?") {
    npc<Neutral>(
        "You're not from around here or you wouldn't be asking such foolish questions. " +
            "Morytania is ruled by a cruel dark overlord by the name of Drakan. His reign over " +
            "Morytania means we all live in fear.",
    )
    upAgainstChoice()
}

private fun ChoiceOption.whatLossAndHeartache(): Unit = option<Quiz>("What kind of loss and heartache?") {
    npc<Sad>(
        "The worst kind, most have lost members of their family. Little Sani Piliu, she was " +
            "orphaned overnight when a vampire went on the rampage... imagine that, losing your " +
            "entire family in one night? Terrible!",
    )
    player<Quiz>("It sounds awful... Who is Sani Pillu?")
    npc<Neutral>(
        "She's the only female member of the Myreque. She's already proven herself with her " +
            "agility and light fingers, if you know what I mean!",
    )
    upAgainstChoice()
}

private fun ChoiceOption.deathsOnYourHead(): Unit = option<Angry>("If you don't tell me, their deaths are on your head!") {
    npc<Angry>(
        "There's death a plenty in this forsaken place...what do I care that some fool hardy " +
            "vigilantes decided to go it alone against the drakans? Stupidity of youth is to " +
            "blame, I shan't carry it on my shoulders.",
    )
    forsakenChoice()
}

private suspend fun Player.forsakenChoice() {
    choice {
        vigilanteFreedomFighter()
        whoAreDrakans()
        whatKindOfMan()
        whyForsaken()
        option<Neutral>("Ok, thanks.")
    }
}

private fun ChoiceOption.vigilanteFreedomFighter(): Unit = option<Quiz>(
    "One man's vigilante is another man's freedom fighter!",
) {
    npc<Neutral>(
        "Aye, you can see it from both sides I suppose. But many of us consider it fool hardy " +
            "to fight for something we'll never get, even Polmafi, a scholar, such as he was, " +
            "agrees that the chances are slim.",
    )
    player<Quiz>("Polmafi? Who's he?")
    npc<Neutral>(
        "Polmafi Ferdygris is one of the Myreque, he's a technical sort and advises on all " +
            "sorts of things to Veliaf. He was a scholar before he became a renegade.",
    )
    forsakenChoice()
}

private fun ChoiceOption.whoAreDrakans(): Unit = option<Quiz>("Who are the Drakans?") {
    npc<Neutral>(
        "The Drakans are the family of overlords that rule Morytania. They're the ones to " +
            "whom the blood tithes are paid. Too much I have told you already!",
    )
    npc<Neutral>(
        "Ignorance is better than these truths I tell you! I can pretend once more that I am " +
            "a free man and some relief from this gloom can I feel again. Be gone with you now " +
            "and leave me with my dreams.",
    )
    forsakenChoice()
}

private fun ChoiceOption.whyForsaken(): Unit = option<Quiz>("Why do you say that this place is 'forsaken'?") {
    npc<Neutral>(
        "All of these lands are forsaken of Saradomin's kindness, only cold death from the " +
            "evil gods do we now feels. Those lucky ones to the west of the Salve little realise " +
            "their fate if the river should one day become tained.",
    )
    forsakenChoice()
}

private fun ChoiceOption.whatKindOfMan(): Unit = option<Quiz>(
    "What kind of man are you to say that you don't care?",
) {
    npc<Angry>(
        "Don't dare to judge me young fool...what do you know of the heartache I carry? " +
            "Can you not see the anchor of woe that holds me fast?",
    )
    npc<Neutral>("Very well, if you would take your chance to help these strangers, who am I to stop you?")
    player<Quiz>("But will you help me? Will you take me to them?")
    offerBoat()
}

private suspend fun Player.askForBoatRide() {
    player<Quiz>("Will you still take me to the Myreque?")
    offerBoat()
}

private suspend fun Player.offerBoat() {
    npc<Neutral>(
        "No, I won't take you but you can use my boat. You'll be going through Mort Myre " +
            "though so I won't be letting you go unless you've some defence against the Ghasts.",
    )
    if (questStage("in_search_of_the_myreque") == myrequeStage("refused_delivery")) {
        set("in_search_of_the_myreque", "persuaded_boatman")
    }
    if (inventory.contains("druid_pouch_2", 5) && inventory.contains("silver_sickle_b")) {
        showDruidPouch()
    } else {
        player<Sad>("I don't have anything which I can use against them at this time.")
    }
}

private suspend fun Player.showDruidPouch() {
    item("druid_pouch_2", "You show the boatman your druid pouch.")
    player<Happy>(
        "I have this druid pouch! This turns the Ghasts visible and I can kill them once I " +
            "can see them.",
    )
    npc<Neutral>(
        "Very well, You can go! But you'll need to bring me some wood planks first, I need " +
            "three and you need three.",
    )
    if (!inventory.contains("plank", 3)) {
        npc<Neutral>(
            "The bridge you cross later is rotten and may need to be mended, so bring tools " +
                "and steel metal fixers as you may find them useful.",
        )
        return
    }
    npc<Neutral>(
        "The bridge you cross later is rotten and may need to be mended, so bring tools " +
            "and steel metal fixers as well, you may find them useful. I see that you have " +
            "some with you now, do you want to give them to me?",
    )
    plankChoice()
}

private suspend fun Player.plankChoice() {
    choice("Cyreg wants wooden planks!") {
        givePlanks()
        keepPlanks()
    }
}

private fun ChoiceOption.givePlanks(): Unit = option("Give wooden planks to Cyreg.") {
    if (!inventory.remove("plank", 3)) {
        return@option
    }
    set("in_search_of_the_myreque", "gave_planks")
    item("plank", "The boatman takes 3 wooden planks from you.")
    npc<Neutral>(
        "Very well, you can take the boat. Just jump in when you're ready to leave. When you " +
            "get to the hollows, just keep going North and look for an unusual tree.",
    )
}

private fun ChoiceOption.keepPlanks(): Unit = option<Quiz>("Don't give any wooden planks to Cyreg.") {
    player<Quiz>("Not just yet sorry.")
}

private suspend fun Player.generalQuestionsChoice() {
    choice {
        whereAfterHollows()
        tellMeAboutMyreque()
        tellMeAboutDrakans()
        tellMeAboutYourself()
        option<Neutral>("Ok, thanks.")
    }
}

private fun ChoiceOption.whereAfterHollows(): Unit = option<Quiz>("Where do I go after I get to the hollows?") {
    npc<Neutral>(
        "You should head north and look for an unusual tree. You look like you know a thing " +
            "or two about exploring so I guess I don't need to tell you to keep your eyes peeled!",
    )
    generalQuestionsChoice()
}

private fun ChoiceOption.tellMeAboutMyreque(): Unit = option<Quiz>("Tell me about the 'Myreque.'") {
    npc<Quiz>("What do you want to know?")
    myrequeQuestionsChoice()
}

private fun ChoiceOption.tellMeAboutDrakans(): Unit = option<Quiz>("Tell me about the Drakans.") {
    npc<Neutral>(
        "Well it's rumoured that they're controlling the whole of Morytania. They live in " +
            "the Sanguinesti reigon, bu I've never been, like most villagers in Morytania, " +
            "I'm afraid to leave the village and dread what lies beyond.",
    )
    generalQuestionsChoice()
}

private fun ChoiceOption.tellMeAboutYourself(): Unit = option<Quiz>("Tell me about yourself.") {
    npc<Neutral>(
        "I'm just a humble boatman, Cyreg Paddlehorn is my name, like most of the Paddlehorns " +
            "before me, I make my living by tackling the swamps of Mort Myre.",
    )
    generalQuestionsChoice()
}

private suspend fun Player.myrequeQuestionsChoice() {
    choice {
        whyCalledMyreque()
        tellMeAboutMembers()
        askDifferentQuestion()
    }
}

private fun ChoiceOption.whyCalledMyreque(): Unit = option<Quiz>("Why are they called, 'The Myreque'?") {
    npc<Neutral>(
        "The locals just called them after the place where they hide out, in Mort Myre. " +
            "They are Myreque, 'hidden in the myre'.",
    )
    myrequeQuestionsChoice()
}

private fun ChoiceOption.tellMeAboutMembers(): Unit = option<Quiz>("Tell me about the members of the 'Myreque'.") {
    npc<Neutral>(
        "Well, you have Radigad Ponfit...he's your average mercenary from Asgarnia...ready to " +
            "slice anyone's head off for a price. He's got a personal score to settle with the " +
            "Drakans, though no one knows what it is.",
    )
    npc<Neutral>(
        "Then there's Veliaf, he's the leader. And then there's Ivan, he's the baby of the " +
            "group. There's Sani Piliu she's a lovely girl, though a bit shady if you know what " +
            "I mean.",
    )
    npc<Neutral>(
        "You've also got Harold Evans, he's a bit hot headed, always straight into the fray. " +
            "And the brains of the operation reporting directly to Veliaf is Polmafi Ferdygris, " +
            "he used to be quite a clever scholar!",
    )
    myrequeQuestionsChoice()
}

private fun ChoiceOption.askDifferentQuestion() {
    option("~~~ Ask a different question. ~~~") {
        generalQuestionsChoice()
    }
}
