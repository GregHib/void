package world.gregs.voidps.world.activity.achievement

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.variable.BitwiseValues
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.VariableDefinitions
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.world.activity.achievement.Tasks.isCompleted
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Talk
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player

npcOperate("Talk-to", "explorer_jack") {
    if (player["introducing_explorer_jack_task", "uncompleted"] == "uncompleted") {
        npc<Talk>("Ah! Welcome to ${Settings["server.name"]}, lad. My name's Explorer jack. I'm an explorer by trade, and I'm one of the Taskmasters around these parts")
        player<Quiz>("Taskmaster? What Tasks are you Master of?")
        whatIsTaskSystem()
    }
    if (!player["unlocked_emote_explore", false] && completedAllBeginner(player)) {
        player<Happy>("I think I've finished all of the Beginner Tasks in the Lumbridge set.")
        npc<Happy>("You have? Oh, well done! We'll make an explorer of you yet.")
        player<Happy>("Thank you. Is there a reward?")
        npc<Talk>("Ah, yes indeed.")
        if (!player.inventory.add("explorers_ring_1", "antique_lamp_beginner_lumbridge_tasks")) {
            npc<Talk>("You don't seem to have space, speak to me again when you have two free spaces in your inventory.") // TODO proper message
            return@npcOperate
        }
        player["unlocked_emote_explore"] = true
        npc<Talk>("Having completed the beginner tasks, you have been granted the ability to use the Explorer emote to show your friends.")
        npc<Happy>("I have also given you an explorer's ring. Now, this is more than just any old ring. Aside from looking good, it also has magical properties giving you a small but useful boost to your Magic and Prayer.")
        npc<Talk>("Your ring has the ability to restore some of your run energy to you.")
        npc<Talk>("For each of the first three sections of the diary you complete, your ring will gain an extra charge; so the ring you receive from the medium level tasks will have 3 charges for example.")
        npc<Talk>("If they should run out, the ring is recharged by the sun each day, so you will be able to use it again tomorrow and so on.")
        npc<Talk>("As an extra reward, you can have this old magical lamp to help with your skills. I was going to use it myself, but I don't really need it.")
        player<Happy>("Thanks very much.")
        npc<Talk>("If you should lose your ring, come back to see me and I'm sure I'll have another. Now, did you have anything further to ask?")
    }
    choice {
        option<Quiz>("Tell me about the Task System.") {
            whatIsTaskSystem()
        }
        option<Happy>("Can I claim any rewards from you?") {
            npc<Happy>("You certainly can!")
            choice("Where would you like the items sent?") {
                option("Inventory.") {
                    claim("inventory")
                }
                option("Bank.") {
                    claim("bank")
                }
            }
        }
        option<Neutral>("Sorry, I was just leaving.")
    }
    /*
    npc<Talk>("What ho! Where did you come from?")
    player<Shifty>("Um... Well, I was in the cellar of some old guy called Roddeck, and then there was a dragon, and we had to break through your wall to escape.")
    npc<Laugh>("Hahaha! I always told Roddeck he shouldn't keep a dragon in his cellar. They're wild creatures, you know. It takes real skill to rear them as pets.")
    npc<Neutral>("I don't think he'll be trying it again. You're not angry about your wall?")
    npc<Neutral>("No, no. I'm an explorer; my house is just a place where I sleep between expeditions. Anyway, can I do anything for you?")
    player<Quiz>("What do you mean?")
    npc<Talk>("I can tell you about the Achievement Diary.")
    player<Quiz>("What is the Achievement Diary?")
    npc<Neutral>("Ah, well it's a diary that helps you keep track of particular achievements in the world of ${World.name}. In Lumbridge and Draynor, it can help you discover some very useful things indeed.")
    npc<Talk>("Eventually, with enough exploration, you will be rewarded for your explorative efforts.")
    npc<Talk>("You can find your Achievement Diary by clicking on the green star icon.")
    npc<Talk>("You should see the icon flashing now. Go ahead and click on it to find your Achievement Diary. If you have any questions, feel free to speak to me again.") // TODO
*/
}

suspend fun NPCOption<Player>.whatIsTaskSystem() {
    npc<Neutral>("Well, the Task System is a potent method of guiding yourself to useful things to do around the world.")
    npc<Talk>("You'll see up to six Tasks in your side bar if you click on the glowing Task List icon. You can click on one for more information about it, hints, waypoint arrows, that sort of thing.")
    npc<Talk>("Every Task you do will earn you something of value which you can claim from me. It'll be money, mostly, but the Rewards tab for a Task will tell you more.<br>Good luck!")
    player["introducing_explorer_jack_task"] = "completed"
    player["unstable_foundations"] = "completed"
    player.sendScript("task_list_button_hide", 0)
    player.interfaces.sendVisibility("task_system", "ok", true)
}

val variables: VariableDefinitions by inject()

suspend fun NPCOption<Player>.claim(inventoryId: String) {
    npc<Neutral>("I'll just fill your $inventoryId with what you need, then.")
    val inventory = player.inventories.inventory(inventoryId)
    val progress = player["task_progress_overall", 0]
    val rewards = progress - player["task_progress_rewarded", 0]
    var coins = 0
    for (i in 0 until rewards) {
        coins += when {
            progress + i < 10 -> 10
            progress + i < 25 -> 40
            progress + i < 50 -> 160
            progress + i < 75 -> 640
            else -> 2560
        }
    }
    val values = (variables.get("task_reward_items")!!.values as BitwiseValues).values
    inventory.transaction {
        add("coins", coins)
        if (player.contains("task_reward_items")) {
            for (value in values) {
                if (player.containsVarbit("task_reward_items", value)) {
                    add(value as String)
                }
            }
        }
    }
    when (inventory.transaction.error) {
        is TransactionError.Full -> player.inventoryFull()
        TransactionError.None -> {
            player.message("You receive $coins coins.")
            npc<Happy>("There you go.")
            player["task_progress_rewarded"] = player["task_progress_overall", 0]
            player.clear("task_reward_items")
            if (coins > 100) {
                player["must_be_funny_in_a_rich_mans_world_task"] = true
            }
        }
        else -> {
        }
    }
}

fun completedAllBeginner(player: Player): Boolean {
    return Tasks.forEach(1) {
        if (definition["task_difficulty", 0] == 1 && !isCompleted(player, definition.stringId)) {
            return@forEach false
        }
        null
    } ?: false
}