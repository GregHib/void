package content.area.misthalin.lumbridge

import content.entity.effect.clearTransform
import content.entity.effect.transform
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.*
import content.entity.world.music.playTrack
import content.quest.free.demon_slayer.DemonSlayerSpell.getWord
import content.quest.free.demon_slayer.DemonSlayerSpell.randomiseOrder
import content.quest.instanceOffset
import content.quest.quest
import content.quest.refreshQuestJournal
import content.quest.setInstanceLogout
import content.quest.smallInstance
import content.quest.startCutscene
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.clearCamera
import world.gregs.voidps.engine.client.moveCamera
import world.gregs.voidps.engine.client.shakeCamera
import world.gregs.voidps.engine.client.turnCamera
import world.gregs.voidps.engine.client.ui.InterfaceApi.Companion.option
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.dialogue
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.areaSound
import world.gregs.voidps.engine.entity.character.jingle
import world.gregs.voidps.engine.entity.character.mode.Face
import world.gregs.voidps.engine.entity.character.move.running
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Teleport
import world.gregs.voidps.engine.entity.character.player.combatLevel
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Region
import world.gregs.voidps.type.RegionLevel
import world.gregs.voidps.type.Tile
import java.awt.Choice

class Xenia : Script {
    init {
        objTeleportTakeOff("Climb-down", "lumbridge_catacomb_stairs") { _, _ ->
            when (quest("blood_pact")) {
                "unstarted" -> {
                    val xenia = NPCs.find(RegionLevel(12849), "xenia")
                    queue("xenia_greet") {
                        talkWith(xenia) {
                            npc<Neutral>("Hey! I want to talk to you!")
                        }
                    }
                    Teleport.CANCEL
                }
                "started" -> {
                    val instance = smallInstance(Region(15446))
                    setInstanceLogout(Tile(3247, 3197))
                    val offset = instanceOffset()
                    tele(offset.tile(3877, 5526, 1))
                    Teleport.CANCEL
                }
                else -> Teleport.CONTINUE
            }
        }

        npcOperate("Talk-to", "xenia_2") { (target) ->
            when (quest("blood_pact")) {
                "unstarted" -> {
                    npc<Neutral>("I'm glad you've come by. I need some help.")
                    choiceBase(target)
                }
                "completed" -> {
                    npc<Happy>("Hello again, adventurer.")
                    choiceAfterQuest(target)
                }
            }
        }
    }

    suspend fun Player.choiceBase(target: NPC) {
        choice {
            whatDoYouNeed(target)
            whoAreYou(target)
            howDoYouKnow(target)
            option<Neutral>("Sorry, I've got to go.")
        }
    }

    fun ChoiceOption.leaving(): Unit = option<Neutral>("I think I'll go now.") {
        npc<Neutral>("Farewell, adventurer.")
    }

    fun ChoiceOption.whatDoYouNeed(target: NPC): Unit = option<Neutral>("What do you need help with?") {
        npc<Neutral>("Some cultists of Zamorak have gone into the catacombs with a prisoner. I don't know what they're planning, but I'm pretty sure it's not a tea party.")
        npc<Neutral>("There are three of them, and I'm not as young as I was the last time I was here. I don't want to go down there without backup.")
        questAccept(target)
    }

    suspend fun Player.questAccept(target: NPC) {
        choice {
            acceptQuest(target)
            moreInfos(target)
            whoAreYou(target)
            howDoYouKnow(target)
        }
    }

    fun ChoiceOption.acceptQuest(target : NPC) : Unit = option<Neutral>("I'll help you.") {
        if (startQuest("blood_pact")) {
            set("blood_pact", "started")
            refreshQuestJournal()
            npc<Happy>("I knew you would!")
            npc<Neutral>("We've got no time to lose. You head down the stairs, and I'll follow.")
        } else {
            player<No>("Not Right Now.")
        }
    }

    fun ChoiceOption.moreInfos(target: NPC) : Unit = option<Neutral>("I need to know more before I help you.") {
        npc<Neutral>("Very wise. I got into a lot of trouble in my youth by rushing in without knowing a situation.")
        moreInfoChoices(target)
    }

    suspend fun Player.moreInfoChoices(target: NPC) {
        choice {
            option<Neutral>("Tell me more about these cultists.") {
                npc<Neutral>("Lumbridge is a Saradominist town, but there will always be some people drawn to worship Zamorak. They must have found some ritual that they think will give them power over other people.")
                moreInfoChoices(target)
            }

            option<Neutral>("Who did they kidnap?") {
                npc<Neutral>("A young woman named Ilona. She had just left Lumbridge to apprentice at the Wizards' Tower.")
                npc<Neutral>("They grabbed her on the road. Without training she didn't have a chance.")
                moreInfoChoices(target)
            }

            option<Neutral>("What's down there?") {
                npc<Neutral>("The catacombs of Lumbridge Church. The dead of Lumbridge have been buried there since...well, for about forty years now.")
                moreInfoChoices(target)
            }

            option<Neutral>("Is there a reward if I help you?") {
                npc<Neutral>("The cultists all have weapons, and you'll be able to keep them if we succeed. This adventure will also help to train your combat skills.")
                moreInfoChoices(target)
            }

            option<Neutral>("Enough questions.") {
                npc<Neutral>("So, will you help me, adventurer?")
                questAccept(target)
            }
        }
    }

    fun ChoiceOption.whoAreYou(target: NPC) : Unit = option<Confused>("Who are you?"){
        npc<Neutral>("My name's Xenia. I'm an adventurer.")
        npc<Neutral>("I'm one of the old guard, I suppose. I helped found the Champions' Guild, and I've done a fair few quests in my time.")
        npc<Neutral>("Now I'm starting to get a bit old for action, which is why I need your help.")
        choiceBase(target)
    }

    fun ChoiceOption.howDoYouKnow(target: NPC) : Unit = option<Confused>("How did you know who I am?"){
        npc<Neutral>("Oh, I have my ways. I get the feeling that you're one to watch; you could be quite the hero some day.")
        choiceBase(target)
    }

    suspend fun Player.choiceAfterQuest(target: NPC) {
        choice {
            choiceQuestDetail(target)
            if(checkForLostWeapons()) { lostWeapon(target) }
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

    fun ChoiceOption.lostWeapon(target: NPC) : Unit = option<Neutral>("I've lost some of the cultists' weapons.") {
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

    fun ChoiceOption.choiceQuestDetail(target: NPC) : Unit = option<Neutral>("I've got a question about my adventure in the catacombs...") {
        afterQuestDetail(target)
    }

    fun ChoiceOption.notWounded(target: NPC) : Unit = option<Neutral>("You weren't really wounded, were you?") {
        npc<Neutral>("Very perceptive, adventurer. I was wounded, but not as badly as I looked. I took the opportunity to see how you would fare.")
        woundedDetails(target)
    }

    fun ChoiceOption.whatNow(target: NPC) : Unit = option<Neutral>("What will happen in the catacombs now?\n") {
        npc<Neutral>(" Reese managed to complete the ritual with his own death. He's opened the staircase to the nest of undead creatures in the lower level of the catacombs. Without a necromancer to control them, the creatures won't leave the tomb. I'll warn Father Aereck not to let people go down there. You're an adventurer, though. If you want to, you can venture into the tomb and fight the creatures.\n")
        afterQuestDetail(target)
    }

    fun ChoiceOption.whatBloodPact(target: NPC) : Unit = option<Neutral>("What is a blood pact?\n") {
        npc<Neutral>(" It's something Zamorakian cults do sometimes; a way of swearing loyalty to their leader. A blood pact doesn't have real magical power, but that kind of thing can have great power over a person if they believe strongly enough.")
        afterQuestDetail(target)
    }

    fun ChoiceOption.whoDragith(target: NPC) : Unit = option<Neutral>("Who was Dragith Nurn?\n") {
        npc<Neutral>("Dragith Nurn was a wizard. He studied at the Wizards' Tower, but he also studied the dar ark, necromancy, on his own. He had a secret magical workshop beneath Lumbridge. He would steal bodies from the graveyard and perform experiments on them. Necromancy was like an addiction for him. When I met him he was very troubled; very conflicted. I convinced him to put an end to it all. He couldn't destroy all the undead he had created - not permanently - so he trapped them all in the lower level of his workshop and sealed it off. He converted the upper level into these catacombs. Everyone thinks Dragith Nurn is buried here in the tomb, but he isn't. He built the tomb to hide the entrance to the lower level. Dragith Nurn is still down there. He knew that when he died he would rise again as a monster, so he sealed himself in with his creatures.")
        afterQuestDetail(target)
    }

    fun ChoiceOption.womansLife(target: NPC) : Unit = option<Neutral>("You risked that woman's life for the sake of a test?") {
        npc<Neutral>(" I was prepared to step in and rescue her if you failed, but I won't always be that ready. That's why I had to do this. The world needs heroes. I was a hero, once, but I'm not getting any younger. I need to make sure the news generation has its own heroes.")
        woundedDetails(target)
    }

    fun ChoiceOption.playerLife(target: NPC) : Unit = option<Neutral>("You risked my life for the sake of a test?") {
        npc<Neutral>("You're a born adventurer. I can practically smell it on you. People like you have a habit of coming back from things that would kill an ordinary person.")
        woundedDetails(target)
    }

    fun ChoiceOption.howDidIDo(target: NPC) : Unit = option<Neutral>("So how did I do?") {
        npc<Neutral>("Very well indeed. You're a hero. You're exactly the sort of person the world needs. I'm glad I met you.")
        woundedDetails(target)
    }

    suspend fun Player.backToQuestions(target: NPC) {
        choice {
            whatNow(target)
            whatBloodPact(target)
            whoDragith(target)
            leaving()
        }
    }

    suspend fun Player.woundedDetails(target: NPC) {
        choice {
            womansLife(target)
            playerLife(target)
            howDidIDo(target)
            option<Neutral>("Back to my other questions...") { backToQuestions(target) }
        }
    }

    suspend fun Player.afterQuestDetail(target : NPC) {
        choice {
            notWounded(target)
            whatNow(target)
            whatBloodPact(target)
            whoDragith(target)
            leaving()
        }
    }
}