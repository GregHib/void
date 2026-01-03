package content.area.misthalin.varrock

import content.entity.effect.clearTransform
import content.entity.effect.transform
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.*
import content.entity.world.music.playTrack
import content.quest.free.demon_slayer.DemonSlayerSpell.getWord
import content.quest.free.demon_slayer.DemonSlayerSpell.randomiseOrder
import content.quest.quest
import content.quest.startCutscene
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.clearCamera
import world.gregs.voidps.engine.client.moveCamera
import world.gregs.voidps.engine.client.shakeCamera
import world.gregs.voidps.engine.client.turnCamera
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.areaSound
import world.gregs.voidps.engine.entity.character.jingle
import world.gregs.voidps.engine.entity.character.mode.Face
import world.gregs.voidps.engine.entity.character.move.running
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.combatLevel
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Region

class GypsyAris : Script {

    init {
        npcOperate("Talk-to", "gypsy_aris") { (target) ->
            when (quest("demon_slayer")) {
                "unstarted" -> {
                    npc<Neutral>("Hello, young one.")
                    npc<Neutral>("Cross my palm with silver and the future will be revealed to you.")
                    if (!inventory.contains("coins")) {
                        player<Sad>("Oh dear. I don't have any money.")
                        return@npcOperate
                    }
                    if (combatLevel < 15) {
                        statement("Before starting this quest, be aware that your combat level is lower than the recommended level of 15.")
                    }
                    choice {
                        hereYouGo(target)
                        whoYouCallingYoung(target)
                        notBeliever()
                        withSilver(target)
                    }
                }
                "sir_prysin", "key_hunt" -> howGoesQuest()
                "completed" -> {
                    npc<Idle>("Greetings young one.")
                    npc<Happy>("You're a hero now. That was a good bit of demon-slaying.")
                    choice {
                        option<Confused>("How do you know I killed it?") {
                            npc<Neutral>("You forget. I'm good at knowing things.")
                        }
                        option<Idle>("Thanks.")
                        stopCallingMeThat()
                    }
                }
            }
        }
        npcTimerStart("demon_slayer_crystal_ball") { 2 }
        npcTimerTick("demon_slayer_crystal_ball") {
            if (mode !is Face) {
                return@npcTimerTick Timer.CANCEL
            }
            areaSound("demon_slayer_crystal_ball_anim", tile)
            return@npcTimerTick Timer.CONTINUE
        }
    }

    suspend fun Player.whatToDo() {
        choice {
            cityDestroyer {
                wallyQuestions()
            }
            whereIsHe()
            notVeryHeroicName()
        }
    }

    suspend fun Player.howToDo() {
        choice {
            cityDestroyer {
                wallyQuestions()
            }
            whereIsHe()
            howWallyWon()
        }
    }

    fun ChoiceOption.howWallyWon(): Unit = option<Quiz>("So, how did Wally kill Delrith?") {
        playTrack("wally_the_hero")
        cutscene()
    }

    suspend fun Player.finalQuestions() {
        choice {
            cityDestroyer {
                otherQuestions()
            }
            whereIsHe()
            notVeryHeroicName()
            option("What is the magical incantation?") {
                incantation()
                finalQuestions()
            }
            okThanks()
        }
    }

    suspend fun Player.otherQuestions() {
        choice {
            whereIsHe()
            notVeryHeroicName()
            option("What is the magical incantation?") {
                incantation()
                finalQuestions()
            }
            option("Where can I find Silverlight?") {
                whereSilverlight()
                finalQuestions()
            }
            okThanks()
        }
    }

    fun ChoiceOption.cityDestroyer(end: suspend Player.() -> Unit): Unit = option<Scared>("How am I meant to fight a demon who can destroy cities?") {
        npc<Neutral>("If you face Delrith while he is still weak from being summoned, and use the correct weapon, you will not find the task too arduous.")
        npc<Neutral>("Do not fear. If you follow the path of the great hero Wally, then you are sure to defeat the demon.")
        end.invoke(this)
    }

    fun ChoiceOption.whereIsHe(): Unit = option<Happy>("Okay, where is he? I'll kill him for you.") {
        npc<Laugh>("Ah, the overconfidence of the young!")
        npc<Neutral>("Delrith can't be harmed by ordinary weapons. You must face him using the same weapon that Wally used.")
        howToDo()
    }

    fun ChoiceOption.notVeryHeroicName(): Unit = option<Happy>("Wally doesn't sound like a very heroic name.") {
        npc<Neutral>("Yes, I know. Maybe that is why history doesn't remember him. However, he was a great hero.")
        npc<Neutral>("Who knows how much pain and suffering Delrith would have brought forth without Wally to stop him!")
        npc<Neutral>("It looks like you are needed to perform similar heroics.")
        howToDo()
    }

    suspend fun Player.incantation() {
        player<Neutral>("What is the magical incantation?")
        npc<Neutral>("Oh yes, let me think a second.")
        npc<Idle>("Aright, I think I've got it now, it goes... ${getWord(this, 1)}... ${getWord(this, 2)}... ${getWord(this, 3)}.,. ${getWord(this, 4)}.,. ${getWord(this, 5)}. Have you got that?")
        player<Idle>("I think so, yes.")
    }

    suspend fun ChoiceOption.notBeliever(): Unit = option<Neutral>("No, I don't believe in that stuff.") {
        npc<Sad>("Ok suit yourself.")
    }

    fun ChoiceOption.hereYouGo(target: NPC): Unit = option<Neutral>("Okay, here you go.") {
        inventory.remove("coins", 1)
        npc<Happy>("Come closer and listen carefully to what the future holds, as I peer into the swirling mists o the crystal ball.")
        sound("demon_slayer_crystal_ball_start")
        target.softTimers.start("demon_slayer_crystal_ball")
        npc<Neutral>("I can see images forming. I can see you.")
        npc<Confused>("You are holding a very impressive-looking sword. I'm sure I recognise it...")
        npc<Confused>("There is a big, dark shadow appearing now.")
        target.softTimers.stop("demon_slayer_crystal_ball")
        sound("demon_slayer_crystal_ball_end")
        npc<Scared>("Aaargh!")
        player<Quiz>("Are you all right?")
        npc<Scared>("It's Delrith! Delrith is coming!")
        player<Scared>("Who's Delrith?")
        npc<Sad>("Delrith...")
        npc<Neutral>("Delrith is a powerful demon.")
        npc<Scared>("Oh! I really hope he didn't see me looking at him through my crystal ball!")
        npc<Sad>("He tried to destroy this city 150 years ago. He was stopped just in time by the great hero Wally.")
        npc<Sad>("Using his magic sword Silverlight, Wally managed to trap the demon in the stone circle just south of this city.")
        npc<Shock>("Ye gods! Silverlight was the sword you were holding in my vision! You are the one destined to stop the demon this time.")
        whatToDo()
    }

    fun ChoiceOption.whoYouCallingYoung(target: NPC): Unit = option<Frustrated>("Who are you called 'young one'?") {
        npc<Neutral>("You have been on this world a relatively short time. At least compared to me.")
        npc<Neutral>("So, do you want your fortune told or not?")
        choice {
            hereYouGo(target)
            notBeliever()
            option("Ooh, how old are you then?") {
                npc<Idle>("Count the number of legs on the stools in the Blue Moon inn, and multiply that number by seven.")
                player<Quiz>("Er, yeah, whatever.")
            }
        }
    }

    suspend fun Player.cutscene() {
        val region = Region(12852)
        open("fade_out")
        statement("", clickToContinue = false)
        delay(2)
        val cutscene = startCutscene("demon_slayer_aris", region)
        cutscene.onEnd {
            open("fade_out")
            delay(3)
            tele(3203, 3424)
            face(Direction.WEST)
            clearCamera()
            clearTransform()
        }
        delay(1)
        tele(cutscene.tile(3225, 3371), clearInterfaces = false)
        delay(2)
        transform("wally")
        clearCamera()
        moveCamera(cutscene.tile(3227, 3369), 300)
        turnCamera(cutscene.tile(3229, 3367), 250)
        shakeCamera(type = 1, intensity = 0, movement = 10, speed = 10, cycle = 0)
        shakeCamera(type = 3, intensity = 0, movement = 90, speed = 1, cycle = 0)
        sound("rumbling")
        delay(1)
        open("fade_in")
        npc<Neutral>("gypsy_aris", "Wally managed to arrive at the stone circle just as Delrith was summoned by a cult of chaos druids...")

        face(Direction.NORTH)
        clearCamera()
        turnCamera(cutscene.tile(3227, 3367), height = 200, speed = 2, acceleration = 10)
        turnCamera(cutscene.tile(3227, 3367), height = 100, speed = 1, acceleration = 10)
        shakeCamera(type = 3, intensity = 0, movement = 0, speed = 0, cycle = 0)
        sound("rumbling")
        npc<Angry>("wally", "Die, foul demon!", clickToContinue = false)
        tele(cutscene.tile(3225, 3363), clearInterfaces = false)

        delay(2)
        running = true
        walkOverDelay(cutscene.tile(3227, 3367), forceWalk = false)
        face(Direction.NORTH)
        anim("wally_demon_slay")
        sound("demon_slayer_wally_sword", delay = 10)
        delay(4)

        clearCamera()
        moveCamera(cutscene.tile(3227, 3369), height = 100, speed = 2, acceleration = 10)
        shakeCamera(type = 1, intensity = 0, movement = 10, speed = 5, cycle = 0)
        shakeCamera(type = 3, intensity = 0, movement = 2, speed = 50, cycle = 0)
        sound("rumbling")
        npc<Quiz>("wally", "Now, what was that incantation again?")
        randomiseOrder(this)
        npc<Frustrated>("wally", "${getWord(this, 1)}... ${getWord(this, 2)}... ${getWord(this, 3)}... ${getWord(this, 4)}... ${getWord(this, 5)}!")
        open("fade_out")
        delay(4)
        close("fade_out")
        clearCamera()
        shakeCamera(type = 1, intensity = 0, movement = 0, speed = 0, cycle = 0)
        shakeCamera(type = 3, intensity = 0, movement = 0, speed = 0, cycle = 0)
        sound("rumbling")
        moveCamera(cutscene.tile(3225, 3363), height = 500)
        turnCamera(cutscene.tile(3227, 3367), height = 200)
        sound("equip_silverlight")
        jingle("quest_complete_1")
        face(Direction.SOUTH_WEST)
        anim("silverlight_showoff")
        gfx("silverlight_sparkle")
        npc<Pleased>("wally", "I am the greatest demon slayer EVER!")

        npc<Neutral>("By reciting the correct magical incantation, and thrusting Silverlight into Delrith while he was newly summoned, Wally was able to imprison Delrith in the stone table at the centre of the circle.")

        statement("", clickToContinue = false)
        queue.clear("demon_slayer_wally_cutscene_end")
        cutscene.end()
        set("demon_slayer", "sir_prysin")
        delrithWillCome()
    }

    fun ChoiceOption.withSilver(target: NPC): Unit = option<Quiz>("With silver?") {
        npc<Idle>("Oh, sorry, I forgot. With gold, I mean. They haven't used silver coins since before you were born! So, do you want your fortune told?")
        choice {
            hereYouGo(target)
            notBeliever()
        }
    }

    suspend fun Player.delrithWillCome() {
        npc<Sad>("Delrith will come forth from the stone circle again.")
        npc<Sad>("I would imagine an evil sorcerer is already beginning the rituals to summon Delrith as we speak.")
        choice {
            cityDestroyer {
                otherQuestions()
            }
            whereIsHe()
            option("What is the magical incantation?") {
                incantation()
                finalQuestions()
            }
            option("Where can I find Silverlight?") {
                whereSilverlight()
                finalQuestions()
            }
        }
    }

    suspend fun Player.whereSilverlight() {
        player<Frustrated>("Where can I find Silverlight?")
        npc<Neutral>("Silverlight has been passed down by Wally's descendants. I believe it is currently in the care of one of the king's knights called Sir Prysin.")
        npc<Pleased>("He shouldn't be too hard to find. He lives in the royal palace in this city. Tell him Gypsy Aris sent you.")
    }

    suspend fun Player.howGoesQuest() {
        npc<Happy>("Greetings. How goes thy quest?")
        player<Neutral>("I'm still working on it.")
        npc<Neutral>("Well if you need any advice I'm always here, young one.")
        choice {
            incantationReminder()
            silverlightReminder()
            stopCallingMeThat()
            option<Neutral>("Well I'd better press on with it.") {
                npc<Neutral>("See you anon.")
            }
        }
    }

    fun ChoiceOption.okThanks(): Unit = option<Neutral>("Ok thanks. I'll do my best to stop the demon.") {
        npc<Happy>("Good luck, and may Guthix be with you!")
    }

    fun ChoiceOption.silverlightReminder(): Unit = option("Where can I find Silverlight?") {
        whereSilverlight()
        choice {
            okThanks()
            incantationReminder()
        }
    }

    fun ChoiceOption.incantationReminder(): Unit = option("What is the magical incantation?") {
        incantation()
        choice {
            okThanks()
            silverlightReminder()
        }
    }

    fun ChoiceOption.stopCallingMeThat(): Unit = option<Angry>("Stop calling me that!") {
        npc<Neutral>("In the scheme of things you are very young.")
        choice {
            option<Neutral>("Ok but how old are you?") {
                npc<Neutral>("Count the number of legs on the stools in the Blue Moon inn, and multiply that number by seven.")
                player<Neutral>("Er, yeah, whatever.")
            }
            option<Happy>("Oh if it's in the scheme of things that's ok.") {
                npc<Happy>("You show wisdom for one so young.")
            }
        }
    }

    suspend fun Player.wallyQuestions() {
        choice {
            whereIsHe()
            notVeryHeroicName()
            howWallyWon()
        }
    }
}
