package content.area.misthalin.paterdomus

import content.entity.player.bank.ownsItem
import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Laugh
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Shock
import content.entity.player.dialogue.type.ChoiceOption
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.items
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.inv.item.addOrDrop
import content.entity.proj.shoot
import content.quest.quest
import content.quest.questComplete
import content.quest.questCompleted
import content.quest.questStage
import content.quest.refreshQuestJournal
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.entity.World.queue
import world.gregs.voidps.engine.entity.character.jingle
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.removeToLimit
import world.gregs.voidps.engine.inv.replace
import world.gregs.voidps.engine.queue.longQueue

class Drezel : Script {
    init {
        npcOperate("Talk-to", "priestperiltrappedmonk*") {
            if (!ownsItem("wolfbane") && questCompleted("priest_in_peril")) {
                npc<Quiz>("Greetings again adventurer. How go your travels in Morytania? Is it as evil as I have heard?")
                player<Sad>("I've lost my wolfbane dagger.")
                npc<Neutral>("Yes, I know! Luckily for you it washed up on the banks of the Salve earlier! Here, take it again, but please try and be more careful this time.")
                addOrDrop("wolfbane")
                npc<Neutral>("It's a family heirloom after all!")
                player<Happy>("Thanks for that!")
                postQuestChoice()
                return@npcOperate
            }
            when (val natureProgress = questStage("nature_spirit")) {
                10 -> {
                    player<Happy>("Hello again!")
                    natureProgress10()
                    return@npcOperate
                }
                15 -> {
                    foundFilliman()
                    return@npcOperate
                }
                20 -> {
                    spiritIsFilliman()
                    return@npcOperate
                }
                25 -> {
                    wantsJournal()
                    return@npcOperate
                }
                30 -> {
                    needsPlans()
                    return@npcOperate
                }
                35 -> {
                    askForBlessing()
                    return@npcOperate
                }
                40 -> {
                    howsLifeSinceBlessed()
                    return@npcOperate
                }
                45 -> {
                    castSpellInSwamp()
                    return@npcOperate
                }
                50 -> {
                    pickedFungus()
                    return@npcOperate
                }
                55 -> {
                    showedFungus()
                    return@npcOperate
                }
                60, 65 -> {
                    placedItems()
                    return@npcOperate
                }
                70 -> {
                    needSilverSickle()
                    return@npcOperate
                }
                75, 80 -> {
                    sickleBlessed()
                    return@npcOperate
                }
                85 -> {
                    collectedSomething()
                    return@npcOperate
                }
                90 -> {
                    addedBlossom()
                    return@npcOperate
                }
                95, 100, 105 -> {
                    killingGhasts(natureProgress)
                    return@npcOperate
                }
                110 -> {
                    questComplete()
                    return@npcOperate
                }
            }
            pipOptions(false)
        }

        objectOperate("Talk-through", "pip_prisondoor_closed") {
            val drezel = NPCs.find(tile.regionLevel) { it.id.startsWith("priestperiltrappedmonk_vis") }
            talkWith(drezel)
            if (questStage("priest_in_peril") >= 10) {
                player<Quiz>("Why are you back up here again?")
                npc<Quiz>("I was making sure that horrible vampire was still safely trapped in that coffin. Why are YOU up here again?")
                player<Confused>("I don't know really. I was just wandering around I guess.")
                npc<Quiz>("Well could you get back to the job at hand then please?")
                return@objectOperate
            }
            pipOptions(true)
        }
    }

    private suspend fun Player.pipOptions(outsideGate: Boolean) {
        when (val stage = quest("priest_in_peril")) {
            "go_back" -> initialMeeting()
            "help_drezel" -> haveYouFoundKey()
            "drezel_free" -> keyFitTheLock(outsideGate)
            "coffin_destroyed" -> waterPouredOnCoffin()
            "meet_monument" -> meetByMonument()
            "completed" -> canIPassThrough()
            "completed_wolfbane" -> {
                npc<Quiz>("Greetings again adventurer. How go your travels in Morytania? Is it as evil as I have heard?")
                postQuestChoice()
            }
            else if stage.startsWith("essence_") -> handleEssenceDelivery()
        }
    }

    private suspend fun Player.initialMeeting() {
        player<Neutral>("Hello.")
        npc<Shock>("Oh! You do not appear to be one of those Zamorakians who imprisoned me here! Who are you and why are you here?")
        player<Neutral>("My name's $name. King Roald sent me to find out what was going on at the temple. I take it you are Drezel?")
        npc<Happy>("That is right! Oh, praise be to Saradomin! All is not yet lost! I feared that when those Zamorakians attacked this place and imprisoned")
        npc<Neutral>("me up here. Misthalin would be doomed! If they should manage to desecrate the holy river Salve we would be defenceless against Morytania!")
        player<Quiz>("How is a river a good defence then?")
        npc<Neutral>("Well, it is a long tale, and I am not sure we have time!")
        choice {
            tellMeAnyway()
            youreRightWeDont()
        }
    }

    fun ChoiceOption.tellMeAnyway(): Unit = option("Tell me anyway") {
        player<Neutral>("Tell me anyway. I'd like to know the full facts before acting any further.")
        riverStory()
    }

    fun ChoiceOption.youreRightWeDont(): Unit = option<Neutral>("You're right, we don't.") {
        npc<Sad>("Well, let's just say if we cannot undo whatever damage has been done here, the entire land is in grave peril!")
        helpRequest()
    }

    private suspend fun Player.riverStory() {
        npc<Neutral>("Ah. Saradomin has granted you wisdom I see. Well, the story of the river Salve and of how it protects Misthalin is the story of this temple,")
        npc<Neutral>("and of seven warrior priests who died here long ago, from whom I am descended. Once long ago Misthalin did not have the borders that")
        npc<Neutral>("it currently does. This entire area, as far West as Varrock itself was under the control of an evil god. There was frequent skirmishing")
        npc<Neutral>("along the borders, as the brave heroes of Varrock fought to keep the evil creatures that are now trapped on the eastern side of the River Salve from over running")
        npc<Neutral>("the human encampments, who worshiped Saradomin. Then one day, Saradomin himself appeared to one of our mighty heroes, whose name has been forgotten by history,")
        npc<Neutral>("and told him that should we be able to take the pass that this temple now stands in, Saradomin would use his power to bless this river, and make it")
        npc<Neutral>("impassible to all creatures with evil in their hearts. This unknown hero grouped together all of the mightiest fighters whose hearts were pure")
        npc<Neutral>("that he could find, and the seven of them rode here to make a final stand. The enemies swarmed across the Salve but they did not yield.")
        npc<Neutral>("For ten days and nights they fought, never sleeping, never eating, fuelled by their desire to make the world a better place for humans to live.")
        npc<Neutral>("On the eleventh day they were to be joined by reinforcements from a neighbouring encampment, but when those reinforcements arrived all they found")
        npc<Neutral>("were the bodies of these seven brave but unknown heroes, surrounded by the piles of the dead creatures of evil that had tried to defeat them.")
        npc<Neutral>("The men were saddened by the loss of such pure and mighty warriors, yet their sacrifice had not been in vain; for the water of the Salve")
        npc<Neutral>("had indeed been filled with the power of Saradomin, and the evil creatures of Morytania were trapped beyond the river banks forever, by their own evil.")
        npc<Neutral>("In memory of this brave sacrifice my ancestors built this temple so that the land would always be free of evil creatures")
        npc<Neutral>("who wish to destroy it, and laid the bodies of those brave warriors in tombs of honour below this temple with golden gifts on the tombs as marks of respect.")
        npc<Neutral>("They also built a statue on the river source so that all who might try and cross into Misthalin from Morytania would know that these lands are protected")
        npc<Neutral>("by the glory of Saradomin and that good will always defeat evil, no matter how the odds are stacked against them.")
        player<Neutral>("Ok, I can see how the river protects the border, but I can't see how anything could affect that from this temple.")
        npc<Sad>("Well, as much as it saddens me to say so adventurer, Lord Saradomin's presence has not been felt on the land for many years now, and even")
        npc<Neutral>("though all true Saradominists know that he watches over us his power upon the land is not as strong as it once was.")
        npc<Neutral>("I fear that should those Zamorakians somehow pollute the Salve and desecrate his blessing, his power might not be able to stop")
        npc<Neutral>("the army of evil that lurks to the east, longing for the opportunity to invade and destroy us all!")
        helpRequest()
    }

    private suspend fun Player.helpRequest() {
        npc<Quiz>("So what do you say adventurer? Will you aid me and all of Misthalin in foiling this Zamorakian plot?")
        choice {
            agreeToHelp()
            refuseToHelp()
        }
    }

    fun ChoiceOption.agreeToHelp(): Unit = option("Yes.") {
        set("priest_in_peril", "help_drezel")
        player<Neutral>("Yes, of course. Any threat to Misthalin must be neutralised immediately. So what can I do to help?")
        explainTask()
    }

    fun ChoiceOption.refuseToHelp(): Unit = option("No.") {
        player<Laugh>("Ha! NO! You can rot in there for all I care you stupid priest! All hail mighty Zamorak! Death to puny Misthalin!")
        npc<Sad>("Oooooh...I knew it was too good to be true... then leave me to my fate villain, there's no need to taunt me as well as keeping me imprisoned.")
    }

    private suspend fun Player.explainTask() {
        npc<Neutral>("Well, the immediate problem is that I am trapped in this cell. I know that the key to free me is nearby, for none of the Zamorakians")
        npc<Neutral>("who imprisoned me here were ever gone for long periods of time. Should you find the key however, as you may have noticed,")
        npc<Neutral>("there is a vampire in that coffin over there. I do not know how they managed to find it, but it is one of the ones that somehow")
        npc<Neutral>("survived the battle here all those years ago, and is by now quite, quite, mad. It has been trapped on this side of the river for centuries,")
        npc<Neutral>("and as those fiendish Zamorakians pointed out to me with delight, as a descendant of one of those who trapped it here, it will recognise")
        npc<Neutral>("the smell of my blood should I come anywhere near it. It will of course then wake up and kill me, very probably slowly and painfully.")
        player<Quiz>("Maybe I could kill it somehow then while it is asleep?")
        npc<Neutral>("No adventurer, I do not think it would be wise for you to wake it at all. As I say, it is little more than a wild animal, and must")
        npc<Neutral>("be extremely powerful to have survived until today. I suspect your best chance would be to incapacitate it somehow.")
        player<Neutral>("Okay, got it, find the key to your cell, and do something about the vampire.")
        npc<Neutral>("When you have done both of those I will be able to inspect the damage which those Zamorakians have done to the purity of the Salve.")
        npc<Neutral>("Depending on the severity of this damage, I may require further assistance from you in restoring its purity.")
        player<Neutral>("Okay, well first thing's first, let's get you out of here.")
    }

    private suspend fun Player.haveYouFoundKey() {
        npc<Quiz>("How goes it adventurer? Any luck in finding the key to the cell or a way of stopping the vampire yet?")
        when {
            inventory.contains("cell_key") -> {
                player<Happy>("I have this key from one of the monuments underground!")
                npc<Happy>("Excellent work adventurer! Quickly, try it on the door, and see if it will free me!")
            }
            inventory.contains("zamorakian_monk_key") -> {
                player<Happy>("I have this key from one of those Zamorakian monks!")
                npc<Happy>("Excellent work adventurer! Quickly, try it on the door, and see if it will free me!")
            }
            inventory.contains("bucket_murkywater") -> {
                player<Quiz>("I have some water from the Salve. It seems to have been desecrated though. Do you think you could bless it for me?")
                npc<Neutral>("Almost certainly adventurer, but not from inside here. You must first open my cell door!")
            }
            else -> {
                player<Sad>("No, not yet...")
                npc<Neutral>("Well don't give up adventurer! That key MUST be around here somewhere! I know none of those Zamorakians ever got very far from this building!")
                player<Quiz>("How do you know that?")
                npc<Neutral>("I could hear them laughing about some gullible fool that they tricked into killing the guard dog at the monument.")
                player<Sad>("Oh.")
                npc<Confused>("Honestly, what kind of idiot would go around killing things just because a stranger told them to? What kind of oafish, numb-skulled, dim-witted,")
                player<Angry>("Okay, OKAY, I get the picture!")
            }
        }
    }

    private suspend fun Player.keyFitTheLock(outsideGate: Boolean) {
        player<Happy>("The key fitted the lock! You're free to leave now!")
        npc<Happy>("Well excellent work adventurer! Unfortunately, as you know, I cannot risk waking that vampire in the coffin.")
        if (inventory.contains("bucket_blessedwater")) {
            player<Quiz>("I have some blessed water from the Salve in this bucket. Do you think it would help against that vampire?")
            npc<Happy>("Yes! Great idea! If his coffin is doused in the blessed water he will be unable to leave it! Use it on his coffin, quickly!")
        } else if (inventory.contains("bucket_murkywater")) {
            blessWater(outsideGate)
        } else {
            vampireSuggestion()
        }
    }

    private suspend fun Player.blessWater(outsideGate: Boolean) {
        player<Quiz>("I have some water from the Salve. It seems to have been desecrated though. Do you think you could bless it for me?")
        if (outsideGate) {
            return npc<Neutral>("Almost certainly adventurer, but not from inside here. You must first open my cell door!")
        }
        npc<Happy>("Yes, good thinking adventurer! Give it to me, I will bless it!")
        message("The priest blesses the water for you.")
        inventory.replace("bucket_murkywater", "bucket_blessedwater")
    }

    private suspend fun Player.vampireSuggestion() {
        player<Quiz>("Do you have any ideas about dealing with the vampire?")
        npc<Neutral>("Well, the water of the Salve should still have enough power to work against the vampire despite what those Zamorakians might have done to it...")
        npc<Neutral>("Maybe you should try and get hold of some from somewhere?")
    }

    private suspend fun Player.waterPouredOnCoffin() {
        player<Neutral>("I poured the blessed water over the vampire's coffin. I think that should trap him in there long enough for you to escape.")
        set("priest_in_peril", "meet_monument")
        npc<Neutral>("Excellent work adventurer! I am free at last! Let me ensure that evil vampire is trapped for good. I will meet you down by the monument.")
        npc<Neutral>("Look for me down there, I need to assess what damage has been done to our holy barrier by those evil Zamorakians!")
    }

    private suspend fun Player.meetByMonument() {
        if (tile.level == 2) {
            npc<Neutral>("I will meet you downstairs by the monuments. I must see what damage has been done. Meet me there, I fear I may require more of your assistance.")
            player<Neutral>("Okay.")
            return
        }
        npc<Neutral>("Ah, $name I see you finally made it down here. Things are worse than I feared. I'm not sure if I will be able to repair the damage.")
        player<Quiz>("Why, what's happened?")
        npc<Neutral>("From what I can tell, after you killed the guard dog who protected the entrance to the monuments, those Zamorakians forced the door into the main chamber")
        npc<Neutral>("and have used some kind of evil potion upon the well which leads to the source of the river Salve. As they have done this at the very source of the river")
        npc<Neutral>("it will spread along the entire river, disrupting the blessing placed upon it and allowing the evil creatures of Morytania to invade at their leisure.")
        player<Shock>("What can we do to prevent that?")
        npc<Neutral>("Well, as you can see, I have placed a holy barrier on the entrance to this room from the South, but it is not very powerful and requires me to remain")
        npc<Neutral>("here, focusing upon it to keep it intact. Should an attack come, they would be able to breach this defence very quickly indeed. What we need to do is")
        npc<Quiz>("find some kind of way of removing or counteracting the evil magic that has been put into the river source at the well, so that the river will flow pure once again.")
        player<Quiz>("Couldn't you bless the river to purify it? Like you did with the water I took from the well?")
        npc<Sad>("No, that would not work, the power I have from Saradomin is not great enough to cleanse an entire river of this foul Zamorakian pollutant.")
        if (questCompleted("rune_mysteries")) {
            runeEssenceIdea()
        } else {
            mageOreHint()
        }
    }

    private suspend fun Player.runeEssenceIdea() {
        player<Quiz>("Hmm. I think I may have an idea. Would a type of rock that absorbs magic easily be of any use in getting this evil magic out of the river somehow?")
        npc<Happy>("Yes! That would be perfect! It could work as a kind of natural filter, removing the potion from the water at the very source and keeping the river pure!")
        npc<Quiz>("I have never heard of such a rock however. You must tell me of what you speak!")
        player<Neutral>("Well it is somewhat of a secret, but recently I helped the Mages at the Wizards' Tower discover a new kind of rock that absorbed magic and could be used to make Runes from.")
        player<Neutral>("It's known as 'Rune Essence' and is very absorbent of magical energy.")
        npc<Happy>("Saradomin truly smiled upon me when first we met adventurer! This sounds like the very answer we are looking for!")
        player<Quiz>("I will be able to get you this essence very quickly. How many do you think we need?")
        set("priest_in_peril", "essence_0")
        npc<Neutral>("Well, should these essences be all that you say they are it would take around fifty of them I would estimate to cleanse the river and keep it pure.")
    }

    private suspend fun Player.mageOreHint() {
        npc<Neutral>("I have only one idea how we could possibly cleanse the river.")
        player<Quiz>("What's that?")
        npc<Neutral>("I have heard rumors recently that Mages have found some secret ore that absorbs magic into it and allows them to create runes.")
        npc<Neutral>("Should you be able to collect enough of this ore, it is possible it will soak up the evil potion that has been poured into the river, and purify it.")
        player<Neutral>("Kind of like a filter? Okay, I guess it's worth a try. How many should I get?")
        set("priestperiltrappedmonk_hide", true)
        set("priest_in_peril", "essence_0")
        npc<Neutral>("Well I have no knowledge of these ores other than speculation and gossip, but if the things I hear are true around fifty should be sufficient for the task.")
    }

    private suspend fun Player.handleEssenceDelivery() {
        val essence = inventory.count("pure_essence") + inventory.count("rune_essence")
        if (essence == 0) {
            if (quest("priest_in_peril") == "essence_0") {
                player<Quiz>("What am I supposed to do again?")
                npc<Neutral>("Find me fifty Rune Essences and bring them here to me.")
            } else {
                player<Quiz>("How many more essences do I need to bring you?")
                val needed = 60 - questStage("priest_in_peril")
                npc<Neutral>("I need $needed more.")
            }
            return
        }
        player<Happy>("I brought you some ${if (quest("priest_in_peril") == "essence_0") "" else "more "}Rune Essence.")
        npc<Neutral>("Quickly, give them to me!")
        val collected = deleteEssence()
        if (collected == 50) {
            longQueue("quest_reward") {
                sendQuestReward()
            }
            npc<Neutral>("Excellent! That should do it! I will bless these stones and place them within the well, and Misthalin should be protected once more!")
            npc<Neutral>("Please take this dagger, it has been handed down within my family for generations and is filled with the power of Saradomin. You will find that")
            npc<Neutral>("it has the power to prevent werewolves from adopting their wolf form in combat as long as you have it equipped.")
        }
    }

    private suspend fun Player.canIPassThrough() {
        player<Quiz>("So can I pass through that barrier now?")
        npc<Neutral>("Ah, $name. For all the assistance you have given both myself and Misthalin in your actions, I cannot let you pass without warning you.")
        npc<Neutral>("Morytania is an evil land, filled with creatures and monsters more terrifying than you have yet encountered. Although I will pray for you")
        npc<Neutral>("you should take some basic precautions before heading over the Salve into it. The first place you will come across is the Werewolf trading post.")
        npc<Neutral>("In many ways Werewolves are like you and me, except never forget that they are evil vicious beasts at heart. The dagger I have given you is named 'Wolfbane'")
        npc<Neutral>("and it is a holy relic that prevents the werewolf people from changing form. I suggest if you battle with them that you keep it always equipped, for their")
        npc<Neutral>("wolf form is incredibly powerful, and would savage you very quickly. Please adventurer, promise me this: I should hate for you to die foolishly.")
        set("priest_in_peril", "completed_wolfbane")
        player<Neutral>("Okay, I will keep it equipped whenever I fight werewolves.")
    }

    suspend fun Player.postQuestChoice() {
        choice {
            lookAroundMore()
            anythingElseInteresting()
        }
    }

    fun ChoiceOption.lookAroundMore(): Unit = option<Neutral>("Well, I'm going to look around a bit more.") {
        npc<Neutral>("Well, that sounds like a good idea. Don't get into any trouble though!")
    }

    fun ChoiceOption.anythingElseInteresting(): Unit = option<Neutral>("Is there anything else interesting to do around here?") {
        npc<Neutral>("Well, not a great deal... but there is something you could do for me if you're interested. Though it is quite dangerous.")
        natureSpiritOfferChoice()
    }

    suspend fun Player.natureSpiritOfferChoice() {
        choice {
            notInterested()
            whatIsIt()
        }
    }

    fun ChoiceOption.notInterested(): Unit = option<Neutral>("Sorry, not interested...") {
        npc<Neutral>("Well, I understand, you must be busy.")
    }

    fun ChoiceOption.whatIsIt(): Unit = option<Quiz>("Well, what is it, I may be able to help?") {
        npc<Neutral>("There's a man called Filliman who lives in Mort Myre, I wonder if you could look for him? The swamps of Mort Myre are dangerous though, they're infested with Ghasts!")
        fillimanQuestionsChoice()
    }

    suspend fun Player.fillimanQuestionsChoice() {
        choice {
            whoIsFilliman()
            whereIsMortMyre()
            whatIsAGhast()
            yesIllLook()
            cantHelp()
        }
    }

    fun ChoiceOption.whoIsFilliman(): Unit = option<Neutral>("Who is this Filliman?") {
        npc<Neutral>("Filliman Tarlock is his full name and he's a Druid. He lives in Mort Myre much like a hermit, but there's many a traveller who he's helped.")
        npc<Neutral>("Most people that come this way tell stories of when they were lost and paths that just seemed to 'open up' before them! I think it was Filliman Tarlock helping out.")
        fillimanQuestionsChoice()
    }

    fun ChoiceOption.whereIsMortMyre(): Unit = option<Quiz>("Where's Mort Myre?") {
        npc<Neutral>("Mort Myre is a decayed and dangerous swamp to the south. It was once a beautiful forest but has since become filled with vile emanations from within Morytania.")
        npc<Neutral>("The swamp decays everything. We put a fence around it to stop unwary travellers going in. Anyone who dies in the swamp is forever cursed to haunt it as a Ghast. Ghasts attack travellers, turning food to rotten filth.")
        fillimanQuestionsChoice()
    }

    fun ChoiceOption.whatIsAGhast(): Unit = option<Quiz>("What's a Ghast?") {
        npc<Neutral>("A Ghast is a poor soul who died in Mort Myre. They're undead of a special class, they're untouchable as far as I'm aware!")
        npc<Neutral>("Filliman knew how to tackle them, but I've not heard from him in a long time. Ghasts, when they attack, will devour any food you have. If you have no food, they'll draw their nourishment from you!")
        fillimanQuestionsChoice()
    }

    fun ChoiceOption.yesIllLook(): Unit = option<Happy>("Yes, I'll go and look for him.") {
        npc<Neutral>("That's great, but it is very dangerous. Are you sure you want to do this?")
        confirmFillimanChoice()
    }

    fun ChoiceOption.cantHelp(): Unit = option<Sad>("Sorry, I don't think I can help.") {
        npc<Neutral>("That's fine, I'm sure someone else will be along shortly who can help. Now, if you will excuse me I do have some things to be getting along with.")
    }

    suspend fun Player.confirmFillimanChoice() {
        choice {
            imSure()
            whoIsFilliman()
            whereIsMortMyre()
            whatIsAGhast()
            cantHelp()
        }
    }

    fun ChoiceOption.imSure(): Unit = option<Happy>("Yes, I'm sure.") {
        npc<Happy>("That's great! Many thanks! Now then, please be aware of the Ghasts, you cannot attack them, only Filliman knew how to take them on.")
        npc<Neutral>("Just run from them if you can. If you start to get lost, try to make your way back to the temple.")
        items(item1 = "apple_pie", item2 = "meat_pie", text = "The cleric hands you some food.")
        addOrDrop("meat_pie", 3)
        addOrDrop("apple_pie", 3)
        set("nature_spirit", "find_filliman")
        npc<Quiz>("Please take this food to Filliman, he'll probably appreciate a bit of cooked food. Now, he's never revealed where he lives in the swamps but I guess he'd be to the south, search for him won't you?")
        player<Happy>("I'll do my very best, don't worry, if he's in there and he's still alive I'll definitely find him.")
    }

    private suspend fun Player.natureProgress10() {
        npc<Quiz>("Have you managed to find Filliman yet?")
        player<Sad>("No not yet.")
        npc<Neutral>("Please go and look for him, I would appreciate it!")
        fillimanHelpChoice()
    }

    suspend fun Player.fillimanHelpChoice() {
        choice {
            whereForFilliman()
            explainGhast()
            mortMyreStory()
            fillimanStory()
            okThanks()
        }
    }

    fun ChoiceOption.whereForFilliman(): Unit = option<Quiz>("Where should I look for Filliman Tarlock again?") {
        npc<Neutral>("Search to the south of Mort Myre. Remember that he's a druid so he can conceal himself within nature quite well. My guess is that he lives in the southern area of the swamp, though he could be anywhere.")
        fillimanHelpChoice()
    }

    fun ChoiceOption.explainGhast(): Unit = option<Neutral>("Explain to me what a Ghast is again.") {
        npc<Neutral>("A Ghast is a poor soul who died in Mort Myre. They're undead of a special class, they're untouchable apart from with special druidic item.")
        npc<Neutral>("Filliman knew how to tackle them, but I've not heard from him in a long time. Ghasts, when they attack, will devour any food you have. If you have no food, they'll draw their nourishment from you!")
        fillimanHelpChoice()
    }

    fun ChoiceOption.mortMyreStory(): Unit = option<Quiz>("What's the story with Mort Myre?") {
        npc<Neutral>("Mort Myre was once a beautiful forest by the name of Humblethorn until the evil denizens of Morytania descended. Now their evil emanations have putrified and diseased the forest into a decaying swamp of death.")
        fillimanHelpChoice()
    }

    fun ChoiceOption.fillimanStory(): Unit = option<Quiz>("What's the story with Filliman Tarlock?") {
        player<Quiz>("What's the story with Filliman Tarlock?")
        npc<Neutral>("Filliman is a druid of some considerable power. He helped many people in Morytania escape when the evil descended upon the land. His knowledge of plants and nature was exceptional.")
        npc<Neutral>("But one day, he was betrayed by some of the people who he had tried to help. This naturally made him more careful when dealing with strangers again and so instead of showing himself, he would follow them.")
        fillimanHelpChoice()
    }

    fun ChoiceOption.okThanks(): Unit = option<Neutral>("Ok, thanks.") {
        npc<Happy>("Many thanks friend!")
    }

    private suspend fun Player.foundFilliman() {
        player<Sad>("I've found Filliman and you should prepare for some sad news.")
        npc<Sad>("You mean... he's dead?")
        player<Confused>("Well, er sort of. I got to his camp and I encountered a spirit of some kind. I don't think it was a Ghast, it tried to communicate with me, but made no sense, it was all 'ooooh' this and 'oooh' that.")
        npc<Confused>("Hmmm, that's very interesting, I seem to remember Father Aereck in Lumbridge and his predecessor Father Urhney having a similar issue. Though this is probably not related to your problem.")
        npc<Sad>("I will pray that it wasn't the spirit of my friend Filliman, but some lost soul who needs some help. Please do let me know how you get on with it.")
    }

    private suspend fun Player.spiritIsFilliman() {
        player<Sad>("I've spoken with the spirit and it is Filliman Tarlock, he's been slain.")
        npc<Sad>("Oh dear, such grave news. Filliman and I used to have such lovely chats...")
        player<Sad>("Well, he's still quite conversational actually but he won't accept that he's dead. No matter what I say, he won't accept it.")
        npc<Confused>("Hmm, it sounds like he's trapped here until he can accept that he's dead. He still sees the world as if he were in it. If he could somehow see that he is no longer here, that might just do the trick.")
        player<Quiz>("How would I do that?")
        npc<Neutral>("Hmm, I've no clue really my friend, the life of a priest does not mirror your adventurous life style. I'll certainly reflect on the matter, but I'm sure you'll come up with the answer before too long.")
    }

    private suspend fun Player.wantsJournal() {
        player<Neutral>("Filliman wants his journal.")
        npc<Neutral>("He was always losing things in his camp, take a good look around.")
    }

    private suspend fun Player.needsPlans() {
        player<Neutral>("Filliman has to make some plans, but I'm not sure what to do.")
        npc<Quiz>("Well, it sounds like he needs some help, have you tried offering to help him?")
    }

    private suspend fun Player.askForBlessing() {
        player<Neutral>("Hello again! I'm helping Filliman, he plans to become a nature spirit. I have a spell to cast but first I need to be blessed. Can you bless me?")
        npc<Laugh>("But you haven't sneezed!!")
        player<Laugh>("You're so funny!")
        player<Angry>("But can you bless me?")
        npc<Neutral>("Very well my friend, prepare yourself for the blessings of Saradomin. Here we go!")
        blessPlayer()
        set("nature_spirit", "blessed_spell")
        npc<Neutral>("There you go my friend, you're now blessed. It's funny, now that I look at you, there seems to be something of the faith about you. Anyway, good luck with your quest!")
        player<Happy>("Many thanks!")
    }

    private suspend fun Player.howsLifeSinceBlessed() {
        npc<Happy>("How's life been treating you since you got blessed?")
        player<Neutral>("Not so bad!")
        npc<Happy>("It's funny, because when I look at you, there does seem to be something of the faith about you. Have you considered a life of service to Saradomin?")
        player<Happy>("I serve Saradomin in other ways.")
        npc<Happy>("Fair enough!")
    }

    private suspend fun Player.castSpellInSwamp() {
        player<Confused>("Hello there, I've cast the spell in the swamp, but I'm not sure what to do now.")
        npc<Confused>("Perhaps the nature of the spell should give some guidance here. It is a bloom spell isn't it?")
        npc<Confused>("Perhaps you should harvest natures bounty? In any case, you'll need another bloom scroll, go and see Filliman, he may have another.")
    }

    private suspend fun Player.pickedFungus() {
        player<Confused>("I've picked the Fungus, but I'm not sure what to do now.")
        npc<Quiz>("Well, perhaps Filliman needs it? Why not show it to him?")
    }

    private suspend fun Player.showedFungus() {
        player<Neutral>("I've shown Filliman the fungus, but I'm not sure what to do now?")
        npc<Quiz>("Well, what does he need now? Has he asked for any other things? What does it take to become a Nature Spirit?")
    }

    private suspend fun Player.placedItems() {
        player<Confused>("I've placed all the items and Filliman said that spell was cast. What do I do now?")
        npc<Quiz>("Where did he go to? Why not ask him for some help. Perhaps he went into his grotto.")
    }

    private suspend fun Player.needSilverSickle() {
        if (inventory.contains("silver_sickle")) {
            player<Neutral>("Hi again. Filliman has turned into the Nature Spirit. I've just gotten the Silver sickle that he asked me to get. He says he's going to bless it!")
            npc<Happy>("Well, that's great! You'd best take it to him then with all haste!")
        } else {
            player<Neutral>("Hi again. Filliman has turned into the Nature Spirit. I now need to get a Silver sickle so that I can try and defeat the Ghasts. Do you know where I could get something like that?")
            npc<Neutral>("Well, let me think now. Aha, yes, if you're making something from silver, you'll more than likely need a mould, most crafting shops sell them, you could get one from Al Kharid.")
        }
    }

    private suspend fun Player.sickleBlessed() {
        player<Neutral>("Hey there, Filliman blessed my silver sickle, but now I've got to kill three Ghasts, but I'm not sure how to make them appear.")
        npc<Neutral>("Well, did Filliman give you any special instructions on how to combat them?")
        player<Neutral>("Only something about harvesting natures bounty... that's all!")
        npc<Neutral>("Well, perhaps it has something to do with the silver sickle, do you think that in blessing it, it did something special to it?")
    }

    private suspend fun Player.collectedSomething() {
        player<Neutral>("Hello again. I've collected something from the swamp, but I'm not sure what to do with it?")
        npc<Neutral>("Hmm, did Filliman ask you to do something special with it? Perhaps it has a special purpose? I'd keep it safe until you need it though, do you have somewhere safe to keep it?")
    }

    private suspend fun Player.addedBlossom() {
        player<Neutral>("Hi there, I've added the blossomed thing to the pouch, but I'm not sure what to do now.")
        npc<Neutral>("Perhaps it has some effect in the swamp? Have you tried using it?")
    }

    private suspend fun Player.killingGhasts(natureProgress: Int) {
        val ghastKills = when (natureProgress) {
            95 -> 1
            100 -> 2
            else -> 3
        }
        player<Neutral>("Hiya, I'm a mighty Ghast killer! I've killed $ghastKills so far!")
        npc<Neutral>("That's great! How many did Filliman ask you to kill?")
        player<Neutral>("He asked me to kill 3!")
        when (ghastKills) {
            1 -> npc<Neutral>("So you've got two more to kill then!")
            2 -> npc<Neutral>("So you've got one more to kill then!")
            3 -> npc<Neutral>(
                "So you've killed them all then! Go and tell him, I bet he'll be pleased.",
            )
        }
    }

    private suspend fun Player.questComplete() {
        player<Happy>("Hi there! Filliman says I've completed the quest!")
        npc<Happy>("That's fantastic, well done!")
    }

    private fun Player.sendQuestReward() {
        queue("quest_complete") {
            jingle("quest_complete_1")
            exp(Skill.Prayer, 1406.0)
            addOrDrop("wolfbane")
            inc("quest_points")
            refreshQuestJournal()
            questComplete(
                "the Priest in Peril Quest!",
                "1 Quest Point",
                "1406 Prayer XP",
                "Wolfbane dagger",
                "Route to Canifis",
                item = "wolfbane",
            )
        }
    }

    private fun Player.deleteEssence(): Int {
        message("You give the priest your blank runes.")
        val stage = questStage("priest_in_peril")
        var collected = stage - 10
        if (collected >= 50) {
            return 50
        }
        collected += inventory.removeToLimit("rune_essence", 50 - collected)
        collected += inventory.removeToLimit("pure_essence", 50 - collected)
        set("priest_in_peril", if (collected >= 50) "completed" else "essence_$collected")
        return collected
    }

    private suspend fun Player.blessPlayer() {
        val drezel = NPCs.findOrNull(tile.regionLevel) { it.id.startsWith("priestperiltrappedmonk") } ?: return
        face(drezel)
        drezel.face(this)
        drezel.say("Ashustru, blessidum, adverturasi, fidum!")
        drezel.anim("human_casting")
        drezel.shoot(
            id = "druid_shooting_star",
            target = this,
            delay = 30,
            flightTime = 60,
            height = 32,
            endHeight = 32,
            curve = 90,
            offset = 0,
        )
        delay(1)
        anim("altar_pray")
        gfx("druidicspirit_druidsshield")
        sound("prayer_recharge")
        delay(2)
    }
}
