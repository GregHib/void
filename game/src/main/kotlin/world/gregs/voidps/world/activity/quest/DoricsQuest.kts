package world.gregs.voidps.world.activity.quest

import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.chat.ChatBlue
import world.gregs.voidps.engine.client.ui.chat.ChatRed
import world.gregs.voidps.engine.client.ui.chat.Red
import world.gregs.voidps.engine.client.ui.chat.Strike
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.ui.sendText
import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on

on<InterfaceOption>({ id == "quest_journals" && component == "journals" && itemSlot == 3 }) { player: Player ->
    player.open("quest_scroll")
    if (player.getVar("dorics_quest", "unstarted") == "completed") {
        player.interfaces.sendText("quest_scroll", "quest_name", ChatRed { "Doric's Quest" })
        player.interfaces.sendText("quest_scroll", "textline1", Strike { "I have spoken to ${ChatRed.open("Doric.")}" })
        player.interfaces.sendText("quest_scroll", "textline3", Strike { "I have collected some Clay, Copper Ore, and Iron Ore." })
        player.interfaces.sendText("quest_scroll", "textline5", Strike { "Doric rewarded me for all my hard work." })
        player.interfaces.sendText("quest_scroll", "textline6", Strike { "I can now use Doric's Anvils whenever I want." })
        player.interfaces.sendText("quest_scroll", "textline7", Red { "QUEST COMPLETE!" })
    } else if (player.getVar("dorics_quest", "unstarted") == "started") {
        player.interfaces.sendText("quest_scroll", "quest_name", Red { "Doric's Quest" })
        player.interfaces.sendText("quest_scroll", "textline1", Strike { "I have spoken to ${ChatRed.open("Doric.")}" })
        player.interfaces.sendText("quest_scroll", "textline3", ChatBlue { "I need to collect some items and bring them to ${ChatRed.open("Doric.")}" })
        if (player.inventory.contains("clay", 6)) {
            player.interfaces.sendText("quest_scroll", "textline4", Strike { "6 Clay." })
        } else {
            val count = player.inventory.count("clay")
            player.interfaces.sendText("quest_scroll", "textline4", "${ChatRed { "6 Clay" }} ${ChatBlue { "- I need ${6 - count} more." }}")
        }
        if (player.inventory.contains("copper_ore", 4)) {
            player.interfaces.sendText("quest_scroll", "textline5", Strike { "4 Copper Ore." })
        } else {
            val count = player.inventory.count("copper_ore")
            player.interfaces.sendText("quest_scroll", "textline5", "${ChatRed { "4 Copper Ore" }} ${ChatBlue { "- I need ${4 - count} more." }}")
        }
        if (player.inventory.contains("iron_ore", 2)) {
            player.interfaces.sendText("quest_scroll", "textline6", Strike { "2 Iron Ore." })
        } else {
            val count = player.inventory.count("iron_ore")
            player.interfaces.sendText("quest_scroll", "textline6", "${ChatRed { "2 Iron Ore" }} ${ChatBlue { "- I need ${2 - count} more." }}")
        }
    } else {
        player.interfaces.sendText("quest_scroll", "quest_name", ChatRed { "Doric's Quest" })
        player.interfaces.sendText("quest_scroll", "textline1", ChatBlue { "I can start this quest by speaking to ${ChatRed { "Doric" }} who is ${ChatRed { "North of" }}" })
        player.interfaces.sendText("quest_scroll", "textline2", ChatRed { "Falador" })
        player.interfaces.sendText("quest_scroll", "textline4", ChatBlue { "There aren't any requirements but ${ChatRed { "Level 15 Mining" }} will help" })
    }
}