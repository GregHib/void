package world.gregs.voidps.world.activity.quest

import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.ui.sendText
import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on

on<InterfaceOption>({ id == "quest_journals" && component == "journals" && itemSlot == 3}) { player: Player ->
    player.open("quest_scroll")
    if (player.getVar("dorics_quest", "unstarted") == "completed" ) {
        player.interfaces.sendText("quest_scroll", "quest_name", "<col=7f0000>Doric's Quest</col>")
        player.interfaces.sendText("quest_scroll", "textline1", "<str>I have spoken to <col=800000>Doric.")
        player.interfaces.sendText("quest_scroll", "textline3", "<str>I have collected some Clay, Copper Ore, and Iron Ore.")
        player.interfaces.sendText("quest_scroll", "textline5", "<str>Doric rewarded me for all my hard work.")
        player.interfaces.sendText("quest_scroll", "textline6", "<str>I can now use Doric's Anvils whenever I want.")
        player.interfaces.sendText("quest_scroll", "textline7", "<col=ff0000>QUEST COMPLETE!")
    }else if (player.getVar("dorics_quest", "unstarted") == "started" ) {
        player.interfaces.sendText("quest_scroll", "quest_name", "<col=7f0000>Doric's Quest</col>")
        player.interfaces.sendText("quest_scroll", "textline1", "<str>I have spoken to <col=800000>Doric.")
        player.interfaces.sendText("quest_scroll", "textline3", "<col=000080>I need to collect some items and bring them to <col=800000>Doric.")
        if (player.inventory.contains("clay",6)){
            player.interfaces.sendText("quest_scroll", "textline4", "<str>6 Clay.")
        }else{
            val count = player.inventory.getCount("clay")
            player.interfaces.sendText("quest_scroll", "textline4", "<col=800000>6 Clay<col=000080> - I need ${6 - count} more.")
        }
        if (player.inventory.contains("copper_ore",4)){
            player.interfaces.sendText("quest_scroll", "textline5", "<str>4 Copper Ore.")
        }else{
            val count = player.inventory.getCount("copper_ore")
            player.interfaces.sendText("quest_scroll", "textline5", "<col=800000>4 Copper Ore<col=000080> - I need ${4 - count} more.")
        }
        if (player.inventory.contains("iron_ore",2)){
            player.interfaces.sendText("quest_scroll", "textline6", "<str>2 Iron Ore.")
        }else{
            val count = player.inventory.getCount("iron_ore")
            player.interfaces.sendText("quest_scroll", "textline6", "<col=800000>2 Iron Ore<col=000080> - I need ${2 - count} more.")
        }
    }else{
        player.interfaces.sendText("quest_scroll", "quest_name", "<col=7f0000>Doric's Quest</col>")
        player.interfaces.sendText("quest_scroll", "textline1", "<col=000080>I can start this quest by speaking to <col=800000>Doric<col=000080> who is <col=800000>North of<col=000080>")
        player.interfaces.sendText("quest_scroll", "textline2", "<col=800000>Falador<col=000080>")
        player.interfaces.sendText("quest_scroll", "textline4", "<col=000080>There aren't any requirements but <col=800000>Level 15 Mining<col=000080> will help")
    }
}