package content.area.kandarin.ardougne

import content.entity.player.bank.ownsItem
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.startQuest
import world.gregs.voidps.engine.entity.character.jingle
import world.gregs.voidps.engine.entity.character.sound
import content.quest.quest
import content.quest.questComplete
import content.quest.refreshQuestJournal
import content.quest.startCutscene
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.clearCamera
import world.gregs.voidps.engine.client.moveCamera
import world.gregs.voidps.engine.client.turnCamera
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.mode.PauseMode
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectShape
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.holdsItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Region

class Edmond : Script {

    val floorItems: FloorItems by inject()

    val region = Region(10136)
    val npcs: NPCs by inject()
    val objects: GameObjects by inject()

    init {
        npcOperate("Talk-to", "edmond") {
            when (quest("plague_city")) {
                "unstarted" -> {
                    player<Happy>("Hello old man.")
                    npc<Sad>("Sorry, I can't stop to talk...")
                    player<Quiz>("Why, what's wrong?")
                    npc<Sad>("I've got to find my daughter. I pray that she is still alive...")
                    choice {
                        option<Quiz>("What's happened to her?") {
                            npc<Sad>("Elena's a healer. Three weeks ago she managed to cross the wall into West Ardougne. No one's allowed to cross the wall in case they spread the plague.")
                            player<Quiz>("Plague?")
                            npc<Sad>("Not that long ago, West Ardougne was hit by a deadly plague. They had the wall built to try and keep it contained. No one is allowed to enter the city now apart from the mourners.")
                            npc<Sad>("They say the plague is a horrible way to go... That's why Elena felt she had to go help. She said she'd be gone for a few days but we've heard nothing since.")
                            player<Quiz>("Maybe I could help find her?")
                            npc<Uncertain>("Really, would you? I've been working on a plan to get into West Ardougne, but I'm too old and tired to carry it through. But you on the other hand, you should have no problem.")
                            if (startQuest("plague_city")) {
                                player<Quiz>("Where should I start?")
                                npc<Neutral>("If you're going into West Ardougne you'll need protection from the plague. My wife made a special gas mask for Elena with dwellberries rubbed into it.")
                                npc<Neutral>("They help to repel the plague apparently. We need some more though...")
                                player<Quiz>("Where can I find these dwellberries?")
                                set("plague_city", "started")
                                refreshQuestJournal()
                                npc<Neutral>("The only place I know of is McGrubor's Wood, just west of Seers' Village. The berries are bright blue so they're easy to spot.")
                                player<Neutral>("Okay, I'll go and get some.")
                                npc<Neutral>("The foresters keep a close eye on it, but there is a back way in.")
                            } else {
                                player<Neutral>("On second thoughts, I'd better not.")
                                npc<Neutral>("Well if you hear anything about Elena please tell me.")
                                player<Neutral>("I will. Goodbye.")
                            }
                        }
                        option<Neutral>("Well, good luck finding her.")
                    }
                }
                "started" -> started()
                "has_mask" -> hasMask()
                "about_digging" -> aboutDigging()
                "one_bucket_of_water" -> oneBucketOfWater()
                "two_bucket_of_water" -> twoBucketOfWater()
                "three_bucket_of_water" -> threeBucketOfWater()
                "four_bucket_of_water" -> fourBucketOfWater()
                "sewer" -> sewer()
                "grill_rope" -> grillRope()
                "spoken_to_jethick" -> spoken()
                "grill_open", "returned_book", "spoken_to_ted", "spoken_to_milli", "need_clearance", "talk_to_bravek", "has_cure_paper", "gave_cure" -> grillOpen()
                "freed_elena" -> freedElena()
                else -> completed()
            }
        }
    }

    suspend fun Player.started() {
        player<Happy>("Hello Edmond.")
        npc<Quiz>("Have you got the dwellberries yet?")
        if (holdsItem("dwellberries")) {
            player<Happy>("Yes I've got some here.")
            npc<Neutral>("Take them to my wife Alrena, she's inside.")
        } else {
            player<Upset>("Sorry, I'm afraid not.")
            npc<Talk>("You'll probably find them in McGrubor's Wood, just west of Seers' Village. The berries are bright blue so they're easy to spot.")
            player<Talk>("Okay, I'll go and get some.")
            npc<Talk>("The foresters keep a close eye on it, but there is a back way in.")
        }
    }

    suspend fun Player.hasMask() {
        player<Happy>("Hi Edmond, I've got the gas mask now.")
        npc<Neutral>("Good stuff, now for the digging. Beneath us are the Ardougne sewers. I've done some research, and I reckon you can use them to access to West Ardougne.")
        set("plague_city", "about_digging")
        npc<Neutral>("I've already tried digging down to them but the soil is rock hard. You'll need to pour on several buckets of water to soften it up. I reckon four buckets should do it.")
    }

    suspend fun Player.aboutDigging() {
        npc<Quiz>("How's it going?")
        player<Neutral>("I still need to pour four more buckets of water on the soil.")
    }

    suspend fun Player.oneBucketOfWater() {
        npc<Quiz>("How's it going?")
        player<Neutral>("I still need to pour three more buckets of water on the soil.")
    }

    suspend fun Player.twoBucketOfWater() {
        npc<Quiz>("How's it going?")
        player<Neutral>("I still need to pour two more buckets of water on the soil.")
    }

    suspend fun Player.threeBucketOfWater() {
        npc<Quiz>("How's it going?")
        player<Neutral>("I still need to pour one more bucket of water on the soil.")
    }

    suspend fun Player.fourBucketOfWater() {
        player<Happy>("I've soaked the soil with water.")
        npc<Happy>("That's great, it should be soft enough to dig through now. There should be a spade nearby that you can use.")
    }

    suspend fun Player.sewer() {
        if (get("plaguecity_checked_grill", false)) {
            player<Uncertain>("Edmond, I can't get through to West Ardougne! There's an iron grill blocking my way, I can't pull it off alone.")
            npc<Neutral>("If you get some rope you could tie to the grill, then we could both pull it at the same time.")
        } else {
            npc<Neutral>("I think it's the pipe to the south that comes up in West Ardougne.")
            player<Neutral>("Alright I'll check it out.")
        }
    }

    suspend fun Player.grillRope() {
        player<Neutral>("I've tied a rope to the grill over there, will you help me pull it off?")
        npc<Neutral>("Alright, let's get to it...")
        cutscene()
    }

    suspend fun Player.spoken() {
        player<Neutral>("Hello.")
        npc<Quiz>("Have you found Elena yet?")
        if (holdsItem("picture_plague_city")) {
            player<Sad>("Not yet, it's a big city over there.")
            npc<Sad>("I hope it's not too late.")
        } else {
            player<Sad>("Not yet, it's a big city over there. Do you have a picture of Elena?")
            npc<Sad>("There should be a picture of Elena in the house. Please find her quickly, I hope it's not too late.")
        }
    }

    suspend fun Player.grillOpen() {
        player<Neutral>("Hello.")
        npc<Quiz>("Have you found Elena yet?")
        player<Sad>("Not yet, it's a big city over there.")
        npc<Sad>("I hope it's not too late.")
    }

    suspend fun Player.freedElena() {
        npc<Happy>("Thank you, thank you! Elena beat you back by minutes. Now I said I'd give you a reward. What can I give you as a reward I wonder? Here take this magic scroll, I have little use for it but it may help you.")
        questComplete() // todo what if inv is full
    }

    suspend fun Player.completed() {
        player<Happy>("Hello there.")
        npc<Happy>("Ah hello. Thank you again for rescuing my daughter.")
        if (quest("plague_city") == "completed_with_spell") {
            player<Happy>("No problem.")
        } else {
            choice {
                if (!ownsItem("a_magic_scroll")) {
                    option<Quiz>("Do you have any more of those scrolls?") {
                        if (!inventory.add("a_magic_scroll")) {
                            floorItems.add(tile, "a_magic_scroll", disappearTicks = 300, owner = this)
                        }
                        npc<Happy>("Yes, here you go.")
                    }
                }
                option<Happy>("No problem.")
            }
        }
    }

    suspend fun Player.cutscene() {
        set("plaguecity_pipe", "grill_open")
        set("plague_city", "grill_open")
        open("fade_out")
        val cutscene = startCutscene("grill", region)
        cutscene.onEnd {
            open("fade_out")
            delay(3)
            tele(2514, 9740)
            clearCamera()
            clearAnim()
        }
        delay(4)
        tele(cutscene.tile(2514, 9740), clearInterfaces = false)
        face(Direction.SOUTH)
        val edmond = npcs.add("edmond", cutscene.tile(2514, 9741), Direction.SOUTH)
        edmond.mode = PauseMode
        delay(1)
        moveCamera(cutscene.tile(2517, 9744), 300)
        turnCamera(cutscene.tile(2513, 9740), 230)
        val hangingopeend = objects.add("hanging_rope_anim", cutscene.tile(2514, 9739), ObjectShape.CENTRE_PIECE_STRAIGHT, 2, 20, false)
        val straightrope = objects.add("straight_rope_anim", cutscene.tile(2514, 9740), ObjectShape.CENTRE_PIECE_STRAIGHT, 2, 20, false)
        val straightropeend = objects.add("straight_rope_end_anim", cutscene.tile(2514, 9741), ObjectShape.CENTRE_PIECE_STRAIGHT, 2, 20, false)
        open("fade_in")
        delay(2)
        say("1...")
        delay(2)
        say("2...")
        delay(2)
        say("3...")
        delay(2)
        say("pull")
        sound("plague_pull_grill")
        animDelay("lift_and_pull")
        edmond.animDelay("lift_and_pull")
        hangingopeend.anim("hanging_rope_lift")
        straightrope.anim("straight_rope_lift")
        straightropeend.anim("straight_rope_lift")
        delay(6)
        clearAnim()
        edmond.clearAnim()
        face(edmond)
        npc<Neutral>("Once you're in the city look for a man called Jethick. He's an old friend of the family. Hopefully he can help you.", clickToContinue = false)
        delay(4)
        player<Neutral>("Alright, thanks I will.", largeHead = true, clickToContinue = false)
        delay(2)
        cutscene.end()
    }

    suspend fun Player.questComplete() {
        set("plague_city", "completed")
        jingle("quest_complete_2")
        experience.add(Skill.Mining, 2425.0)
        inventory.add("a_magic_scroll")
        refreshQuestJournal()
        inc("quest_points")
        questComplete(
            "Plague City",
            "1 Quest Point",
            "2,425 Mining XP",
            "An Ardougne Teleport Scroll",
            item = "gas_mask",
        )
        npc<Happy>("Now I'd recommend you go and see Elena. She'll want to thank you herself. She lives in the house opposite ours.")
    }
}
