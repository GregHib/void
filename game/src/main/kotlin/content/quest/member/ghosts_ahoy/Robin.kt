package content.quest.member.ghosts_ahoy

import content.entity.player.bank.ownsItem
import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Bored
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.queue.longQueue

class Robin : Script {
    init {

        itemOnNPCOperate("*", "ahoy_robin") {
            npc<Angry>("Go away peasant, why would I want that?")
        }

        npcOperate("Talk-To", "ahoy_robin") {
            val stage = get("ahoy_subquest_bow", 0)
            when {
                inventory.contains("bedsheet") -> deliverBedsheet()
                stage == 0 -> intro()
                stage == 1 -> stageOneFlow()

                stage == 7 || stage == 8 -> {
                    if (stage == 7 && !ownsItem("signed_oak_bow") && inventory.contains("oak_longbow")) {
                        player<Neutral>("I need you to sign another longbow.")
                        npc<Neutral>("Oh, okay - just keep your promise not to tell the ghosts anything.")
                        inventory.remove("oak_longbow")
                        inventory.add("signed_oak_bow")
                        item(item = "signed_oak_bow", text = "Robin signs another oak longbow for you.")
                    } else {
                        player<Neutral>("So, still here then?")
                        npc<Neutral>("Do you want another game of Runedraw?")
                        player<Angry>("No!")
                    }
                }

                else -> runedrawResult()
            }
        }

        interfaceOption("Draw", "ahoy_runedraw:runedraw_btn_draw") {
            playerTurn()
        }

        interfaceOption("Hold", "ahoy_runedraw:runedraw_btn_hold") {
            set("ahoy_runedraw_held", 1)
            set("ahoy_player_runedraw_turn", get("ahoy_player_runedraw_turn", 0) + 1)
            interfaces.sendText("ahoy_runedraw", "runedraw_totalscore_r", "HELD")
            interfaces.sendText("ahoy_runedraw", "runedraw_totalscore_l", "Thinking...")
            interfaces.sendVisibility("ahoy_runedraw", "runedraw_btn_draw", false)
            interfaces.sendVisibility("ahoy_runedraw", "runedraw_btn_hold", false)
            robinTurn()
        }
    }

    private suspend fun Player.intro() {
        player<Neutral>("It's nice to see another human face around here.")
        npc<Neutral>("Leave me be, peasant - I am relaxing.")
        player<Neutral>("Well, that's nice!")
        npc<Neutral>("Do you know who I am?")
        player<Bored>("I'm sorry, I haven't had the privilege.")
        npc<Neutral>("I, peasant, am Robin, Master Bowman. I am very famous you know.")
        player<Neutral>("Oh, Robin, Master Bowman, I see.")
        npc<Neutral>("So have you heard of me?")
        player<Neutral>("No.")
        npc<Neutral>(
            "Would you do me a favour? I appear to have run out of bed linen. Can you run along " +
                "to the innkeeper and get me a clean bedsheet?",
        )
        choice {
            option<Neutral>("Anything for a famous person such as yourself.") {
                npc<Neutral>("Now that sounds more like it. Run along now, get me my sheet.")
            }
            option<Neutral>("Go and get it yourself.") {
                npc<Neutral>("Oh charming. You just can't get the staff these days.")
            }
        }
    }

    private suspend fun Player.deliverBedsheet() {
        player<Neutral>("I've brought you a clean bedsheet, Robin.")
        inventory.remove("bedsheet")
        npc<Neutral>("Well it's about time. Run along now, I need some Robin time.")
    }

    private suspend fun Player.stageOneFlow() {
        if (inventory.contains("oak_longbow")) {
            player<Quiz>("Would you sign this oak longbow for me?")
            npc<Neutral>("I'm sorry, I don't sign autographs.")
            npc<Neutral>(
                "While you're here, though, why don't you have a game of Runedraw with me? " +
                    "If you've got 25 gold pieces I've got a bag of runes we can use.",
            )
        } else {
            npc<Neutral>(
                "Hey you, why don't you have a game of Runedraw with me? If you've got 25 gold " +
                    "pieces I've got a bag of runes we can use.",
            )
        }
        runedrawOptions(withDebt = false)
    }

    private suspend fun Player.askAnotherGame() {
        val stage = get("ahoy_subquest_bow", 0)
        if (stage == 2) {
            npc<Neutral>("How about another game?")
        } else {
            npc<Neutral>("Err ... how about another game?", "We can settle up after we've finished playing.")
        }
        runedrawOptions(withDebt = false)
    }

    private suspend fun Player.runedrawOptions(withDebt: Boolean) {
        choice {
            option("Yes, I'll give you a game.") {
                if (!inventory.contains("coins", 25)) {
                    player<Neutral>("Yes, I'll give you a game.")
                    player<Sad>("I don't have enough money on me for a game of Runedraw at the moment.")
                    return@option
                }
                if (get("ahoy_subquest_bow", 0) == 1) {
                    set("ahoy_subquest_bow", 2)
                }
                inventory.remove("coins", 25)
                player<Neutral>("Yes, I'll give you a game.")
                start()
            }
            option<Quiz>("How do you play Runedraw?") {
                npc<Neutral>(
                    "Two players take turns to draw a rune from a bag, which contains ten " +
                        "runes in total. Each rune has a different value: an air rune is worth " +
                        "one point, up to a Nature rune which is worth nine points.",
                )
                npc<Neutral>("If a player draws the Death rune then the game is over, and they have lost.")
                npc<Neutral>(
                    "A player can choose to hold if they wish and not draw any more runes, but " +
                        "this runs the risk of the other player drawing more runes until they " +
                        "have a greater points total and win.",
                )
            }
            if (withDebt) {
                option<Angry>("No, you didn't pay up the last time you lost.")
            } else {
                option<Neutral>("No, that doesn't sound fun.")
            }
        }
    }

    private suspend fun Player.handleBowSign(extraSign: Boolean) {
        if (inventory.contains("oak_longbow")) {
            npc<Neutral>("Yes, anything!!!")
            inventory.remove("oak_longbow")
            inventory.add("signed_oak_bow")
            item(
                item = "signed_oak_bow",
                text = if (extraSign) "Robin signs another oak longbow for you." else "Robin signs the oak longbow for you.",
            )
            set("ahoy_subquest_bow", 7)
        } else {
            npc<Neutral>("I'll certainly sign your longbow, if you bring me one!!")
        }
    }

    private suspend fun Player.playerTurn() {
        val turn = get("ahoy_player_runedraw_turn", 0)
        val roll = if (turn == 9) 9 else (0..9).random()
        if (roll != 9) {
            set("ahoy_player_runedraw_sum", get("ahoy_player_runedraw_sum", 0) + roll + 1)
        }
        val slot = "player_slot_${turn + 1}"
        interfaces.sendItem("ahoy_runedraw", slot, Item(RUNEDRAW_RUNES[roll], 80))
        interfaces.sendVisibility("ahoy_runedraw", slot, true)
        set("ahoy_player_runedraw_turn", turn + 1)
        interfaces.sendVisibility("ahoy_runedraw", "runedraw_btn_draw", false)
        interfaces.sendVisibility("ahoy_runedraw", "runedraw_btn_hold", false)
        if (roll == 9) {
            sendLoss()
            return
        }
        interfaces.sendText("ahoy_runedraw", "runedraw_score_r", "${get("ahoy_player_runedraw_sum", 0)}")
        interfaces.sendText("ahoy_runedraw", "runedraw_totalscore_r", "")
        interfaces.sendText("ahoy_runedraw", "runedraw_totalscore_l", "Thinking...")
        robinTurn()
    }

    private suspend fun Player.robinTurn() {
        delay(4)
        val turn = get("ahoy_robin_runedraw_turn", 0)
        val roll = if (get("ahoy_player_runedraw_turn", 0) == 9) 9 else (0..9).random()
        if (roll != 9) {
            set("ahoy_robin_runedraw_sum", get("ahoy_robin_runedraw_sum", 0) + roll + 1)
        }
        val slot = "robin_slot_${turn + 1}"
        interfaces.sendItem("ahoy_runedraw", slot, Item(RUNEDRAW_RUNES[roll], 80))
        interfaces.sendVisibility("ahoy_runedraw", slot, true)
        set("ahoy_robin_runedraw_turn", turn + 1)
        interfaces.sendText("ahoy_runedraw", "runedraw_score_l", "${get("ahoy_robin_runedraw_sum", 0)}")

        val playerSum = get("ahoy_player_runedraw_sum", 0)
        val robinSum = get("ahoy_robin_runedraw_sum", 0)
        val held = get("ahoy_runedraw_held", 0) == 1

        when {
            turn + 1 == 10 -> if (playerSum > robinSum) sendWin() else sendLoss()
            roll == 9 -> sendWin()
            held && playerSum < robinSum -> sendLoss()
            held -> {
                set("ahoy_player_runedraw_turn", get("ahoy_player_runedraw_turn", 0) + 1)
                robinTurn()
            }

            else -> {
                interfaces.sendVisibility("ahoy_runedraw", "runedraw_btn_draw", true)
                interfaces.sendVisibility("ahoy_runedraw", "runedraw_btn_hold", true)
                interfaces.sendText("ahoy_runedraw", "runedraw_totalscore_l", "")
                interfaces.sendText("ahoy_runedraw", "runedraw_totalscore_r", "Your turn")
            }
        }
    }

    private fun Player.sendWin() {
        interfaces.sendText("ahoy_runedraw", "runedraw_totalscore_l", "")
        interfaces.sendText("ahoy_runedraw", "runedraw_totalscore_r", "You win!")
        interfaces.sendVisibility("ahoy_runedraw", "runedraw_btn_draw", false)
        interfaces.sendVisibility("ahoy_runedraw", "runedraw_btn_hold", false)
        set("ahoy_subquest_bow", get("ahoy_subquest_bow", 0) + 1)
        set("ahoy_runedraw_outcome", "win")
        message("You've won the game of Runedraw!")
    }

    private fun Player.sendLoss() {
        interfaces.sendText("ahoy_runedraw", "runedraw_totalscore_l", "")
        interfaces.sendText("ahoy_runedraw", "runedraw_totalscore_r", "You lose!")
        interfaces.sendVisibility("ahoy_runedraw", "runedraw_btn_draw", false)
        interfaces.sendVisibility("ahoy_runedraw", "runedraw_btn_hold", false)
        set("ahoy_runedraw_outcome", "loss")
        message("You've lost the game of Runedraw.")
    }

    private suspend fun Player.runedrawResult() {
        val outcome = get("ahoy_runedraw_outcome", "")
        val stage = get("ahoy_subquest_bow", 0)

        if (stage == 6) {
            player<Angry>(
                "I've had enough of you not paying up - you owe me 100 gold coins. " +
                    "I'm going to tell the ghosts what you're doing.",
            )
            npc<Neutral>("Please don't do that!!! They will suck the life from my bones!!!")
            player<Neutral>("How about you signing my longbow then?")
            handleBowSign(extraSign = false)
            return
        }

        when (outcome) {
            "" -> {
                set("ahoy_runedraw_outcome", "loss")
                message("Your resignation from the game of Runedraw has been treated as a loss.")
                player<Neutral>("Oh, I give up.")
                npc<Neutral>("How about another game?")
                runedrawOptions(stage > 2)
            }

            "loss" -> {
                val text = if (stage <= 2) {
                    "Well done, you beat me."
                } else {
                    "Well done, you beat me. But you still owe me ${(stage - 2) * 25} gold pieces."
                }
                player<Neutral>(text)
                askAnotherGame()
            }

            "win" -> {
                player<Quiz>(
                    "So are you going to pay up then? You owe me ${(stage - 2) * 25} gold coins!",
                )
                npc<Neutral>("How about another game? I'll pay you back with the winnings.")
                player<Quiz>("What if you lose again?")
                npc<Neutral>("Er ... we'll deal with that when we come to it.")
                runedrawOptions(stage > 2)
            }
        }
    }

    fun Player.start() {
        longQueue("Runedraw result") {
            runedrawResult()
        }
        set("ahoy_runedraw_held", 0)
        set("ahoy_player_runedraw_turn", 0)
        set("ahoy_player_runedraw_sum", 0)
        set("ahoy_robin_runedraw_turn", 0)
        set("ahoy_robin_runedraw_sum", 0)
        set("ahoy_runedraw_outcome", "")
        for (i in 1..10) {
            interfaces.sendVisibility("ahoy_runedraw", "player_slot_$i", false)
            interfaces.sendVisibility("ahoy_runedraw", "robin_slot_$i", false)
        }
        interfaces.sendText("ahoy_runedraw", "runedraw_score_r", "")
        interfaces.sendText("ahoy_runedraw", "runedraw_totalscore_r", "Your turn")
        interfaces.sendText("ahoy_runedraw", "runedraw_score_l", "")
        interfaces.sendText("ahoy_runedraw", "runedraw_totalscore_l", "")
        interfaces.sendVisibility("ahoy_runedraw", "runedraw_btn_draw", true)
        interfaces.sendVisibility("ahoy_runedraw", "runedraw_btn_hold", true)
        open("ahoy_runedraw")
    }

    companion object {
        private val RUNEDRAW_RUNES = listOf(
            "air_rune",
            "mind_rune",
            "water_rune",
            "earth_rune",
            "fire_rune",
            "body_rune",
            "cosmic_rune",
            "chaos_rune",
            "nature_rune",
            "death_rune",
        )
    }
}
