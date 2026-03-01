package content.area.misthalin.wizards_tower

import content.entity.gfx.areaGfx
import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Laugh
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.quest.quest
import content.quest.questComplete
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.clearCamera
import world.gregs.voidps.engine.client.command.adminCommand
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.moveCamera
import world.gregs.voidps.engine.client.turnCamera
import world.gregs.voidps.engine.entity.character.jingle
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.any
import world.gregs.voidps.engine.inv.contains
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

class WizardMizgog : Script {
    val beads = listOf(
        Item("red_bead"),
        Item("yellow_bead"),
        Item("black_bead"),
        Item("white_bead"),
    )

    init {
        npcOperate("Talk-to", "wizard_mizgog") { (target) ->
            when (quest("imp_catcher")) {
                "unstarted" -> {
                    player<Neutral>("Give me a quest!")
                    npc<Happy>("Give me a quest what?")
                    choice {
                        option<Neutral>("Give me a quest please.") {
                            npc<Happy>("Well seeing as you asked nicely... I could do with some help.")
                            npc<Sad>("The wizard Grayzag next door decided he didn't like me so he enlisted an army of hundreds of imps.")
                            npc<Sad>("These imps stole all sorts of my things. Most of these things I don't really care about, just eggs and balls of string and things.")
                            npc<Sad>("But they stole my four magical beads. There was a red one, a yellow one, a black one, and a white one.")
                            npc<Sad>("These imps have now spread out all over the kingdom. Could you get my beads back for me?")
                            choice("Start the Imp Catcher quest?") {
                                option("Yes.") {
                                    set("imp_catcher", "started")
                                    if (inventory.contains(beads)) {
                                        player<Happy>("Well this is a surprising turn of events!")
                                        npc<Quiz>("What?")
                                        player<Happy>("Well I just so happen to have all of those beads on me!")
                                        npc<Angry>("Are you saying that you stole my beads all this time and I’ve been blaming these imps!?")
                                        player<Neutral>("No, not at all! I just found them throughout my travels and presumed somebody would need them at some point.")
                                        npc<Angry>("Bah! Fine.")
                                        cutscene(target)
                                    } else {
                                        player<Happy>("I'll try.")
                                        npc<Happy>("That's great, thank you.")
                                    }
                                }
                                option("No.") {
                                    player<Neutral>("I've better things to do than chase imps.")
                                    npc<Angry>("Well if you're not interested in the quests I have to give you, don't waste my time by asking me for them.")
                                }
                            }
                        }
                        option("Give me a quest or else!")
                        option("Just stop messing around and give me a quest!")
                    }
                }
                "started" -> {
                    npc<Happy>("So how are you doing finding my beads?")
                    if (inventory.contains(beads)) {
                        player<Happy>("I've got all four beads. It was hard work I can tell you.")
                        cutscene(target)
                    } else if (inventory.any(beads)) {
                        player<Happy>("I have found some of your beads.")
                        npc<Neutral>("Come back when you have them all. The colour of the four beads that I need are red, yellow, black, and white. Go chase some imps!")
                    } else {
                        player<Sad>("I haven't found any yet.") // TODO proper message
                        npc<Neutral>("Come back when you have them all. The colour of the four beads that I need are red, yellow, black, and white. Go chase some imps!")
                    }
                }
                "given_beads" -> questCompleted()
                "completed" -> {
                    npc<Happy>("What can I do for you, adventurer?")
                    choice {
                        option<Neutral>("Got any more quests?") {
                            npc<Neutral>("No, everything is good with the world today.")
                        }
                        option("Do you know any interesting spells you could teach me?") {
                            player<Neutral>("Do you know any interesting spells you could teach me?")
                            npc<Laugh>("I don't think so, the type of magic I study involves years of meditation and research.")
                        }
                        option<Quiz>("Do you have any more amulets of accuracy?") {
                            openShop("mizgogs_amulets_of_accuracy")
//                            npc<Neutral>("I have a few spare. I'd like one of each coloured bead again in return, though! Black, white, yellow and red.")
//                            player<Quiz>("I don't have them all on me at the moment. I'll come back when I have them!")
//                            npc<Neutral>("Very well. See you soon!")
                        }
                    }
                }
            }
        }

        adminCommand("test") {
            val table = GameObjects.findOrNull(Tile(3102, 3163, 2), "imp_catcher_table_before")
            if (table != null) {
                GameObjects.replace(table, "imp_catcher_table", Tile(3102, 3163, 2), rotation = 1, ticks = 6)
            }
            set("imp_catcher", "given_beads")
        }
    }

    private suspend fun Player.cutscene(target: NPC) {
        npc<Happy>("Give them here and I'll check that they really are MY beads, before I give you your reward. You'll like it, it's an amulet of accuracy.")
        statement("You give four coloured beads to Wizard Mizgog.")
        jingle("mizgog_cutscene")
        turnCamera(tile = Tile(3103, 3162), height = 350, speed = 232, acceleration = 100)
        moveCamera(tile = Tile(3103, 3161), height = 775, speed = 232, acceleration = 100)
        delay(1)
        target.clearWatch()
        target.face(Direction.WEST)
        delay(3)
        target.anim("mizgog_place_beads")
        sound("mizgog_place_beads")
        delay(2)
        if (!inventory.remove(beads)) {
            return
        }
        set("imp_catcher", "given_beads")
        delay(1)
        val table = GameObjects.findOrNull(Tile(3102, 3163, 2), "imp_catcher_table_before")
        if (table != null) {
            GameObjects.replace(table, "imp_catcher_table", Tile(3102, 3163, 2), rotation = 1, ticks = 8)
        }
        areaGfx("imp_catcher_cast", Tile(3102, 3163, 2), height = 100)
        sound("mizgog_beads")
        delay(7)
        questCompleted()
    }

    private suspend fun Player.questCompleted() {
        clearCamera()
        message("The wizard hands you an amulet.")
        inventory.add("amulet_of_accuracy")
        set("imp_catcher", "completed")
        exp(Skill.Magic, 875.0)
        inc("quest_points")
        delay(2)
        jingle("quest_complete_1")
        message("Congratulations, you've completed a quest: <col=081190>Imp Catcher</col>")
        questComplete(
            "Imp Catcher",
            listOf(
                "1 Quest Point",
                "875 Magic XP.",
                "An Amulet of Accuracy.",
            ),
            Item("amulet_of_accuracy"),
        )
    }
}
