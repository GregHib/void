package content.area.misthalin.varrock

import content.entity.effect.clearTransform
import content.entity.effect.transform
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.*
import content.entity.sound.areaSound
import content.entity.sound.jingle
import content.entity.sound.sound
import content.entity.world.music.playTrack
import content.quest.free.demon_slayer.DemonSlayerSpell.getWord
import content.quest.free.demon_slayer.DemonSlayerSpell.randomiseOrder
import content.quest.quest
import content.quest.startCutscene
import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.client.clearCamera
import world.gregs.voidps.engine.client.moveCamera
import world.gregs.voidps.engine.client.shakeCamera
import world.gregs.voidps.engine.client.turnCamera
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.mode.Face
import world.gregs.voidps.engine.entity.character.move.running
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.combatLevel
import world.gregs.voidps.engine.event.Context
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.suspend.SuspendableContext
import world.gregs.voidps.engine.timer.Key
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Region

@Script
class GypsyAris : Api {

    init {
        npcOperate("Talk-to", "gypsy_aris") {
            when (player.quest("demon_slayer")) {
                "unstarted" -> {
                    npc<Talk>("Hello, young one.")
                    npc<Talk>("Cross my palm with silver and the future will be revealed to you.")
                    if (!player.inventory.contains("coins")) {
                        player<Upset>("Oh dear. I don't have any money.")
                        return@npcOperate
                    }
                    if (player.combatLevel < 15) {
                        statement("Before starting this quest, be aware that your combat level is lower than the recommended level of 15.")
                    }
                    choice {
                        hereYouGo()
                        whoYouCallingYoung()
                        notBeliever()
                        withSilver()
                    }
                }
                "sir_prysin", "key_hunt" -> howGoesQuest()
                "completed" -> {
                    npc<Neutral>("Greetings young one.")
                    npc<Happy>("You're a hero now. That was a good bit of demon-slaying.")
                    choice {
                        option<Uncertain>("How do you know I killed it?") {
                            npc<Talk>("You forget. I'm good at knowing things.")
                        }
                        option<Neutral>("Thanks.")
                        stopCallingMeThat()
                    }
                }
            }
        }
    }

    @Key("demon_slayer_crystal_ball")
    override fun start(npc: NPC, timer: String, restart: Boolean) = 2

    @Key("demon_slayer_crystal_ball")
    override fun tick(npc: NPC, timer: String): Int {
        if (npc.mode !is Face) {
            return Timer.CANCEL
        }
        areaSound("demon_slayer_crystal_ball_anim", npc.tile)
        return Timer.CONTINUE
    }

    suspend fun SuspendableContext<Player>.whatToDo() {
        choice {
            cityDestroyer {
                wallyQuestions()
            }
            whereIsHe()
            notVeryHeroicName()
        }
    }

    suspend fun SuspendableContext<Player>.howToDo() {
        choice {
            cityDestroyer {
                wallyQuestions()
            }
            whereIsHe()
            howWallyWon()
        }
    }

    suspend fun PlayerChoice.howWallyWon(): Unit = option<Quiz>("So, how did Wally kill Delrith?") {
        player.playTrack("wally_the_hero")
        cutscene()
    }

    suspend fun SuspendableContext<Player>.finalQuestions() {
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

    suspend fun SuspendableContext<Player>.otherQuestions() {
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

    suspend fun PlayerChoice.cityDestroyer(end: suspend Context<Player>.() -> Unit): Unit = option<Afraid>("How am I meant to fight a demon who can destroy cities?") {
        npc<Talk>("If you face Delrith while he is still weak from being summoned, and use the correct weapon, you will not find the task too arduous.")
        npc<Talk>("Do not fear. If you follow the path of the great hero Wally, then you are sure to defeat the demon.")
        end.invoke(this)
    }

    suspend fun PlayerChoice.whereIsHe(): Unit = option<Happy>("Okay, where is he? I'll kill him for you.") {
        npc<Chuckle>("Ah, the overconfidence of the young!")
        npc<Talk>("Delrith can't be harmed by ordinary weapons. You must face him using the same weapon that Wally used.")
        howToDo()
    }

    suspend fun PlayerChoice.notVeryHeroicName(): Unit = option<Happy>("Wally doesn't sound like a very heroic name.") {
        npc<Talk>("Yes, I know. Maybe that is why history doesn't remember him. However, he was a great hero.")
        npc<Talk>("Who knows how much pain and suffering Delrith would have brought forth without Wally to stop him!")
        npc<Talk>("It looks like you are needed to perform similar heroics.")
        howToDo()
    }

    suspend fun SuspendableContext<Player>.incantation() {
        player<Talk>("What is the magical incantation?")
        npc<Talk>("Oh yes, let me think a second.")
        npc<Neutral>("Aright, I think I've got it now, it goes... ${getWord(player, 1)}... ${getWord(player, 2)}... ${getWord(player, 3)}.,. ${getWord(player, 4)}.,. ${getWord(player, 5)}. Have you got that?")
        player<Neutral>("I think so, yes.")
    }

    suspend fun PlayerChoice.notBeliever(): Unit = option<Talk>("No, I don't believe in that stuff.") {
        npc<Upset>("Ok suit yourself.")
    }

    suspend fun ChoiceBuilder<NPCOption<Player>>.hereYouGo(): Unit = option<Talk>("Okay, here you go.") {
        player.inventory.remove("coins", 1)
        npc<Happy>("Come closer and listen carefully to what the future holds, as I peer into the swirling mists o the crystal ball.")
        player.sound("demon_slayer_crystal_ball_start")
        target.softTimers.start("demon_slayer_crystal_ball")
        npc<Talk>("I can see images forming. I can see you.")
        npc<Uncertain>("You are holding a very impressive-looking sword. I'm sure I recognise it...")
        npc<Uncertain>("There is a big, dark shadow appearing now.")
        target.softTimers.stop("demon_slayer_crystal_ball")
        player.sound("demon_slayer_crystal_ball_end")
        npc<Afraid>("Aaargh!")
        player<Quiz>("Are you all right?")
        npc<Afraid>("It's Delrith! Delrith is coming!")
        player<Afraid>("Who's Delrith?")
        npc<Upset>("Delrith...")
        npc<Talk>("Delrith is a powerful demon.")
        npc<Afraid>("Oh! I really hope he didn't see me looking at him through my crystal ball!")
        npc<Upset>("He tried to destroy this city 150 years ago. He was stopped just in time by the great hero Wally.")
        npc<Upset>("Using his magic sword Silverlight, Wally managed to trap the demon in the stone circle just south of this city.")
        npc<Surprised>("Ye gods! Silverlight was the sword you were holding in my vision! You are the one destined to stop the demon this time.")
        whatToDo()
    }

    suspend fun ChoiceBuilder<NPCOption<Player>>.whoYouCallingYoung(): Unit = option<Frustrated>("Who are you called 'young one'?") {
        npc<Talk>("You have been on this world a relatively short time. At least compared to me.")
        npc<Talk>("So, do you want your fortune told or not?")
        choice {
            hereYouGo()
            notBeliever()
            option("Ooh, how old are you then?") {
                npc<Neutral>("Count the number of legs on the stools in the Blue Moon inn, and multiply that number by seven.")
                player<Quiz>("Er, yeah, whatever.")
            }
        }
    }

    suspend fun SuspendableContext<Player>.cutscene() {
        val region = Region(12852)
        player.open("fade_out")
        statement("", clickToContinue = false)
        delay(2)
        val cutscene = startCutscene("demon_slayer_aris", region)
        cutscene.onEnd {
            player.open("fade_out")
            delay(3)
            player.tele(3203, 3424)
            player.face(Direction.WEST)
            player.clearCamera()
            player.clearTransform()
        }
        delay(1)
        player.tele(cutscene.tile(3225, 3371), clearInterfaces = false)
        delay(2)
        player.transform("wally")
        player.clearCamera()
        player.moveCamera(cutscene.tile(3227, 3369), 300)
        player.turnCamera(cutscene.tile(3229, 3367), 250)
        player.shakeCamera(type = 1, intensity = 0, movement = 10, speed = 10, cycle = 0)
        player.shakeCamera(type = 3, intensity = 0, movement = 90, speed = 1, cycle = 0)
        player.sound("rumbling")
        delay(1)
        player.open("fade_in")
        npc<Talk>("gypsy_aris", "Wally managed to arrive at the stone circle just as Delrith was summoned by a cult of chaos druids...")

        player.face(Direction.NORTH)
        player.clearCamera()
        player.turnCamera(cutscene.tile(3227, 3367), height = 200, constantSpeed = 2, variableSpeed = 10)
        player.turnCamera(cutscene.tile(3227, 3367), height = 100, constantSpeed = 1, variableSpeed = 10)
        player.shakeCamera(type = 3, intensity = 0, movement = 0, speed = 0, cycle = 0)
        player.sound("rumbling")
        npc<Angry>("wally", "Die, foul demon!", clickToContinue = false)
        player.tele(cutscene.tile(3225, 3363), clearInterfaces = false)

        delay(2)
        player.running = true
        player.walkOverDelay(cutscene.tile(3227, 3367), forceWalk = false)
        player.face(Direction.NORTH)
        player.anim("wally_demon_slay")
        player.sound("demon_slayer_wally_sword", delay = 10)
        delay(4)

        player.clearCamera()
        player.moveCamera(cutscene.tile(3227, 3369), height = 100, constantSpeed = 2, variableSpeed = 10)
        player.shakeCamera(type = 1, intensity = 0, movement = 10, speed = 5, cycle = 0)
        player.shakeCamera(type = 3, intensity = 0, movement = 2, speed = 50, cycle = 0)
        player.sound("rumbling")
        npc<Quiz>("wally", "Now, what was that incantation again?")
        randomiseOrder(player)
        npc<Frustrated>("wally", "${getWord(player, 1)}... ${getWord(player, 2)}... ${getWord(player, 3)}... ${getWord(player, 4)}... ${getWord(player, 5)}!")
        player.open("fade_out")
        delay(4)
        player.close("fade_out")
        player.clearCamera()
        player.shakeCamera(type = 1, intensity = 0, movement = 0, speed = 0, cycle = 0)
        player.shakeCamera(type = 3, intensity = 0, movement = 0, speed = 0, cycle = 0)
        player.sound("rumbling")
        player.moveCamera(cutscene.tile(3225, 3363), height = 500)
        player.turnCamera(cutscene.tile(3227, 3367), height = 200)
        player.sound("equip_silverlight")
        player.jingle("quest_complete_1")
        player.face(Direction.SOUTH_WEST)
        player.anim("silverlight_showoff")
        player.gfx("silverlight_sparkle")
        npc<Pleased>("wally", "I am the greatest demon slayer EVER!")

        npc<Talk>("By reciting the correct magical incantation, and thrusting Silverlight into Delrith while he was newly summoned, Wally was able to imprison Delrith in the stone table at the centre of the circle.")

        statement("", clickToContinue = false)
        player.queue.clear("demon_slayer_wally_cutscene_end")
        cutscene.end()
        player["demon_slayer"] = "sir_prysin"
        delrithWillCome()
    }

    suspend fun ChoiceBuilder<NPCOption<Player>>.withSilver(): Unit = option<Quiz>("With silver?") {
        npc<Neutral>("Oh, sorry, I forgot. With gold, I mean. They haven't used silver coins since before you were born! So, do you want your fortune told?")
        choice {
            hereYouGo()
            notBeliever()
        }
    }

    suspend fun SuspendableContext<Player>.delrithWillCome() {
        npc<Upset>("Delrith will come forth from the stone circle again.")
        npc<Upset>("I would imagine an evil sorcerer is already beginning the rituals to summon Delrith as we speak.")
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

    suspend fun SuspendableContext<Player>.whereSilverlight() {
        player<Frustrated>("Where can I find Silverlight?")
        npc<Talk>("Silverlight has been passed down by Wally's descendants. I believe it is currently in the care of one of the king's knights called Sir Prysin.")
        npc<Pleased>("He shouldn't be too hard to find. He lives in the royal palace in this city. Tell him Gypsy Aris sent you.")
    }

    suspend fun NPCOption<Player>.howGoesQuest() {
        npc<Happy>("Greetings. How goes thy quest?")
        player<Talk>("I'm still working on it.")
        npc<Talk>("Well if you need any advice I'm always here, young one.")
        choice {
            incantationReminder()
            silverlightReminder()
            stopCallingMeThat()
            option<Talk>("Well I'd better press on with it.") {
                npc<Talk>("See you anon.")
            }
        }
    }

    suspend fun PlayerChoice.okThanks(): Unit = option<Talk>("Ok thanks. I'll do my best to stop the demon.") {
        npc<Happy>("Good luck, and may Guthix be with you!")
    }

    suspend fun PlayerChoice.silverlightReminder(): Unit = option("Where can I find Silverlight?") {
        whereSilverlight()
        choice {
            okThanks()
            incantationReminder()
        }
    }

    suspend fun PlayerChoice.incantationReminder(): Unit = option("What is the magical incantation?") {
        incantation()
        choice {
            okThanks()
            silverlightReminder()
        }
    }

    suspend fun PlayerChoice.stopCallingMeThat(): Unit = option<Angry>("Stop calling me that!") {
        npc<Talk>("In the scheme of things you are very young.")
        choice {
            option<Talk>("Ok but how old are you?") {
                npc<Talk>("Count the number of legs on the stools in the Blue Moon inn, and multiply that number by seven.")
                player<Talk>("Er, yeah, whatever.")
            }
            option<Happy>("Oh if it's in the scheme of things that's ok.") {
                npc<Happy>("You show wisdom for one so young.")
            }
        }
    }

    suspend fun SuspendableContext<Player>.wallyQuestions() {
        choice {
            whereIsHe()
            notVeryHeroicName()
            howWallyWon()
        }
    }
}
