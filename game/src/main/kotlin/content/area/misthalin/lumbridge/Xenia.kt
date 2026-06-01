package content.area.misthalin.lumbridge

import content.entity.combat.hit.directHit
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.*
import content.quest.exitInstance
import content.quest.instance
import content.quest.instanceOffset
import content.quest.quest
import content.quest.refreshQuestJournal
import content.quest.setInstanceLogout
import content.quest.smallInstance
import content.quest.startCutscene
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.clearCamera
import world.gregs.voidps.engine.client.instruction.handle.interactPlayer
import world.gregs.voidps.engine.client.moveCamera
import world.gregs.voidps.engine.client.turnCamera
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.Follow
import world.gregs.voidps.engine.entity.character.move.running
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Teleport
import world.gregs.voidps.engine.queue.longQueue
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.type.Delta
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Region
import world.gregs.voidps.type.RegionLevel
import world.gregs.voidps.type.Tile

class Xenia : Script {
    init {

        npcOperate("Talk-to", "xenia*") {
            when (quest("blood_pact")) {
                "unstarted" -> {
                    npc<Neutral>("I'm glad you've come by. I need some help.")
                    choiceBase()
                }
                "watched_cutscene" -> {
                    npc<Neutral>("There's a guard in the room ahead. Together we should be able to take him out.")
                    firstFightOptions()
                }
                "xenia_wounded" -> {
                    if (false) { //check if player has no melee weapon
                        //give bronze dagger
                        npc<LookDown>("You'll need a weapon. Equip this bronze dagger, then talk to me again.")
                    } else {
                        npc<LookDown>("Ah...")
                        npc<LookDown>("It looks like I'm too old for this after all. You'll have to do the rest without me.")
                        npc<LookDown>("I'll follow you, but I'll stay out of combat. Return to me if you're wounded. I have some food to share.")
                        npc<LookDown>("The first cultist is using a ranged weapon, so you should attack him with your melee weapon.")

                        optionsBeforeFirstFight()
                    }
                }
                "completed" -> {
                    npc<Happy>("Hello again, adventurer.")
                    choiceAfterQuest()
                }
            }
        }
        moved { _ ->
            if (quest("blood_pact") != "watched_cutscene") return@moved
            if (tile != instanceOffset().tile(3877, 5530, 1) && tile != instanceOffset().tile(3877, 5531, 1)) return@moved

            set("blood_pact", "xenia_wounded")

            val region = instance() ?: return@moved
            val xenia = NPCs.findOrNull(region.toLevel(1), "xenia_after_cutscene") ?: return@moved
            val kayle = NPCs.findOrNull(region.toLevel(1), "kayle_attackable") ?: return@moved

            xenia.mode = EmptyMode
            xenia.walkTo(instanceOffset().tile(3877, 5530, 1))

            queue("blood_pact_xenia_hit") {
                delay(1)   // time for xenia to walk there
                kayle.anim("sling_sling")
                delay(3)
                xenia.directHit(kayle, 19, "range")
                xenia.anim("human_defend")
                open("fade_out")
                delay(3)
                tele(instanceOffset().tile(3876, 5528, 1))
                NPCs.remove(xenia)
                NPCs.add("xenia_wounded", instanceOffset().tile(3875, 5529, 1), Direction.SOUTH)
                delay(3)
                open("fade_in")
                // anything that happens AFTER the hit goes here
            }
        }
    }

    suspend fun Player.choiceBase() {
        choice {
            whatDoYouNeed()
            whoAreYou()
            howDoYouKnow()
            option<Neutral>("Sorry, I've got to go.")
        }
    }

    fun ChoiceOption.leaving(): Unit = option<Neutral>("I think I'll go now.") {
        npc<Neutral>("Farewell, adventurer.")
    }

    fun ChoiceOption.whatDoYouNeed(): Unit = option<Neutral>("What do you need help with?") {
        npc<Neutral>("Some cultists of Zamorak have gone into the catacombs with a prisoner. I don't know what they're planning, but I'm pretty sure it's not a tea party.")
        npc<Neutral>("There are three of them, and I'm not as young as I was the last time I was here. I don't want to go down there without backup.")
        questAccept()
    }

    suspend fun Player.questAccept() {
        choice {
            acceptQuest()
            moreInfos()
            whoAreYou()
            howDoYouKnow()
        }
    }

    fun ChoiceOption.acceptQuest( ) : Unit = option<Neutral>("I'll help you.") {
        if (startQuest("blood_pact")) {
            set("blood_pact", "started")
            refreshQuestJournal()
            npc<Happy>("I knew you would!")
            npc<Neutral>("We've got no time to lose. You head down the stairs, and I'll follow.")
        } else {
            player<No>("Not Right Now.")
        }
    }

    fun ChoiceOption.moreInfos() : Unit = option<Neutral>("I need to know more before I help you.") {
        npc<Neutral>("Very wise. I got into a lot of trouble in my youth by rushing in without knowing a situation.")
        moreInfoChoices()
    }

    suspend fun Player.moreInfoChoices() {
        choice {
            option<Neutral>("Tell me more about these cultists.") {
                npc<Neutral>("Lumbridge is a Saradominist town, but there will always be some people drawn to worship Zamorak. They must have found some ritual that they think will give them power over other people.")
                moreInfoChoices()
            }

            option<Neutral>("Who did they kidnap?") {
                npc<Neutral>("A young woman named Ilona. She had just left Lumbridge to apprentice at the Wizards' Tower.")
                npc<Neutral>("They grabbed her on the road. Without training she didn't have a chance.")
                moreInfoChoices()
            }

            option<Neutral>("What's down there?") {
                npc<Neutral>("The catacombs of Lumbridge Church. The dead of Lumbridge have been buried there since...well, for about forty years now.")
                moreInfoChoices()
            }

            option<Neutral>("Is there a reward if I help you?") {
                npc<Neutral>("The cultists all have weapons, and you'll be able to keep them if we succeed. This adventure will also help to train your combat skills.")
                moreInfoChoices()
            }

            option<Neutral>("Enough questions.") {
                npc<Neutral>("So, will you help me, adventurer?")
                questAccept()
            }
        }
    }

    fun ChoiceOption.whoAreYou() : Unit = option<Confused>("Who are you?"){
        npc<Neutral>("My name's Xenia. I'm an adventurer.")
        npc<Neutral>("I'm one of the old guard, I suppose. I helped found the Champions' Guild, and I've done a fair few quests in my time.")
        npc<Neutral>("Now I'm starting to get a bit old for action, which is why I need your help.")
        choiceBase()
    }

    fun ChoiceOption.howDoYouKnow() : Unit = option<Confused>("How did you know who I am?"){
        npc<Neutral>("Oh, I have my ways. I get the feeling that you're one to watch; you could be quite the hero some day.")
        choiceBase()
    }

    suspend fun Player.choiceAfterQuest() {
        choice {
            choiceQuestDetail()
            if(checkForLostWeapons()) { lostWeapon() }
            leaving()
        }
    }

    fun checkForLostWeapons() : Boolean {
        val weapons = arrayOf("") //quest weapons

        for (weapon in weapons) {
            //check player bank and inventory for weapon
            //return true if missing
        }

        return false
    }

    fun ChoiceOption.lostWeapon() : Unit = option<Neutral>("I've lost some of the cultists' weapons.") {
        npc<Neutral>("Yes, one of my contacts in the Champion's Guild found them and returned them to me.")
        //if not in invent or bank "Kayle's chargebow"
        //  Xenia gives you Kayle's chargebow.

        //if not in invent or bank "Catilin's staff"
        //  Xenia gives you Catilin's staff.

        //if not in invent or bank "Reese's sword"
        //  Xenia gives you Reese's sword.

        //if not in invent or bank "Reese's off-hand sword"
        //  Xenia gives you Reese's off-hand sword.
    }

    fun ChoiceOption.choiceQuestDetail() : Unit = option<Neutral>("I've got a question about my adventure in the catacombs...") {
        afterQuestDetail()
    }

    fun ChoiceOption.notWounded() : Unit = option<Neutral>("You weren't really wounded, were you?") {
        npc<Neutral>("Very perceptive, adventurer. I was wounded, but not as badly as I looked. I took the opportunity to see how you would fare.")
        woundedDetails()
    }

    fun ChoiceOption.whatNow() : Unit = option<Neutral>("What will happen in the catacombs now?\n") {
        npc<Neutral>(" Reese managed to complete the ritual with his own death. He's opened the staircase to the nest of undead creatures in the lower level of the catacombs. Without a necromancer to control them, the creatures won't leave the tomb. I'll warn Father Aereck not to let people go down there. You're an adventurer, though. If you want to, you can venture into the tomb and fight the creatures.\n")
        afterQuestDetail()
    }

    fun ChoiceOption.whatBloodPact() : Unit = option<Neutral>("What is a blood pact?\n") {
        npc<Neutral>(" It's something Zamorakian cults do sometimes; a way of swearing loyalty to their leader. A blood pact doesn't have real magical power, but that kind of thing can have great power over a person if they believe strongly enough.")
        afterQuestDetail()
    }

    fun ChoiceOption.whoDragith() : Unit = option<Neutral>("Who was Dragith Nurn?\n") {
        npc<Neutral>("Dragith Nurn was a wizard. He studied at the Wizards' Tower, but he also studied the dar ark, necromancy, on his own. He had a secret magical workshop beneath Lumbridge. He would steal bodies from the graveyard and perform experiments on them. Necromancy was like an addiction for him. When I met him he was very troubled; very conflicted. I convinced him to put an end to it all. He couldn't destroy all the undead he had created - not permanently - so he trapped them all in the lower level of his workshop and sealed it off. He converted the upper level into these catacombs. Everyone thinks Dragith Nurn is buried here in the tomb, but he isn't. He built the tomb to hide the entrance to the lower level. Dragith Nurn is still down there. He knew that when he died he would rise again as a monster, so he sealed himself in with his creatures.")
        afterQuestDetail()
    }

    fun ChoiceOption.womansLife() : Unit = option<Neutral>("You risked that woman's life for the sake of a test?") {
        npc<Neutral>(" I was prepared to step in and rescue her if you failed, but I won't always be that ready. That's why I had to do this. The world needs heroes. I was a hero, once, but I'm not getting any younger. I need to make sure the news generation has its own heroes.")
        woundedDetails()
    }

    fun ChoiceOption.playerLife() : Unit = option<Neutral>("You risked my life for the sake of a test?") {
        npc<Neutral>("You're a born adventurer. I can practically smell it on you. People like you have a habit of coming back from things that would kill an ordinary person.")
        woundedDetails()
    }

    fun ChoiceOption.howDidIDo() : Unit = option<Neutral>("So how did I do?") {
        npc<Neutral>("Very well indeed. You're a hero. You're exactly the sort of person the world needs. I'm glad I met you.")
        woundedDetails()
    }

    suspend fun Player.firstFightOptions() {
        choice {
            option<Neutral>("What's the plan of attack?") {
                npc<Neutral>("It looks like the cultist has a bow. The best way to deal with someone with a ranged weapon is to get close to them and attack with melee.")
                firstFightOptions()
            }
            option<Neutral>("What's a blood pact?") {
                npc<Neutral>("It's something Zamorakian cults do sometimes; a way of swearing loyalty to their leader.")
                npc<Neutral>("A blood pact doesn't have real magical power, but that kind of thing can have great power over a person if they believe strongly enough.")
                firstFightOptions()
            }
            option<Neutral>("Let's get on with this.") { }
        }
    }

    suspend fun Player.optionsBeforeFirstFight() {
        choice {
            option<LookDown>("Tell me more about melee combat.") {
                npc<LookDown>("There's not much to tell. Just run up and attack. You don't even need a weapon - you can use your fists. Melee combat is strong against rangers. Avoid magic users, though.")
                optionsBeforeFirstFight()
            }
            option<LookDown>("What's a blood pact?") {
                npc<LookDown>("It's something Zamorakian cults do sometimes; a way of swearing loyalty to their leader.")
                npc<LookDown>("A blood pact doesn't have real magical power, but that kind of thing can have great power over a person if they believe strongly enough.")
                optionsBeforeFirstFight()
            }
            option<LookDown>("Are you going to be alright?") {
                npc<LookDown>("Don't worry about me. I've survived worse wounds than this. I'm going to hang back from combat, but I'll be here to give you advice if you need it. I'm sure you can beat these cultists on your own.")
                optionsBeforeFirstFight()
            }
            option<LookDown>("I can handle this.") { }
        }
    }

    suspend fun Player.backToQuestions() {
        choice {
            whatNow()
            whatBloodPact()
            whoDragith()
            leaving()
        }
    }

    suspend fun Player.woundedDetails() {
        choice {
            womansLife()
            playerLife()
            howDidIDo()
            option<Neutral>("Back to my other questions...") { backToQuestions() }
        }
    }

    suspend fun Player.afterQuestDetail( ) {
        choice {
            notWounded()
            whatNow()
            whatBloodPact()
            whoDragith()
            leaving()
        }
    }
}