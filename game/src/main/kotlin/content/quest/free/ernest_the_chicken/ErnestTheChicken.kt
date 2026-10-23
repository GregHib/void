package content.quest.free.ernest_the_chicken

import content.entity.combat.hit.damage
import content.entity.effect.transform
import content.entity.obj.door.enterDoor
import content.entity.player.bank.bank
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.quest.quest
import content.quest.questComplete
import content.quest.questJournal
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.jingle
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.type.Tile

internal const val ERNEST = "ernest_the_chicken"
private val PARTS = listOf("pressure_gauge", "rubber_tube", "oil_can")

class ErnestTheChicken : Script {
    init {
        npcOperate("Talk-to", "veronica") {
            when (quest(ERNEST)) {
                "unstarted" -> {
                    npc<Neutral>("Please help me find my fiance Ernest. He went into Draynor Manor to ask for directions and hasn't come back.")
                    choice {
                        option("I'll look for him.") {
                            npc<Happy>("Thank you! Please find out what has happened to him.")
                            set(ERNEST, "started")
                        }
                        option("I'm sorry, I can't help.")
                    }
                }
                "completed" -> npc<Happy>("Thank you for helping Ernest!")
                "parts" -> npc<Neutral>("A chicken? Please help the professor turn him back!")
                else -> npc<Neutral>("Please look inside the manor for Ernest.")
            }
        }
        itemOnItem("poison", "fish_food") { _, _ ->
            if (inventory.transaction {
                    remove("poison")
                    remove("fish_food")
                    add("poisoned_fish_food")
                }
            ) {
                message("You mix the poison with the fish food.")
            }
        }
        objectOperate("Search", "fountain") { (target) ->
            if (target.tile != Tile(3087, 3334)) return@objectOperate
            if (!get("ernest_fountain_poisoned", false)) {
                message("The piranhas bite your hand!")
                damage(10)
            } else if (ownsErnestPart("pressure_gauge")) {
                message("You find nothing else in the fountain.")
            } else if (inventory.add("pressure_gauge")) {
                message("You find a pressure gauge in the fountain.")
            } else {
                message("You don't have enough inventory space to take the pressure gauge.")
            }
        }
        itemOnObjectOperate("poisoned_fish_food", "fountain") { (target) ->
            if (target.tile != Tile(3087, 3334)) return@itemOnObjectOperate
            if (inventory.remove("poisoned_fish_food")) {
                set("ernest_fountain_poisoned", true)
                message("The piranhas eat the poisoned fish food and die.")
            }
        }
        itemOnObjectOperate("fish_food", "fountain") { (target) ->
            if (target.tile != Tile(3087, 3334)) return@itemOnObjectOperate
            message("The piranhas eat the food, but they still look hungry.")
            inventory.remove("fish_food")
        }
        objectOperate("Search", "ernest_compost") { searchCompost() }
        itemOnObjectOperate("spade", "ernest_compost") { searchCompost() }
        itemOption("Dig", "spade") {
            if (tile.level == 0 && tile.x in 3083..3086 && tile.y in 3359..3362) searchCompost()
        }
        objectOperate("Open", "door_11_closed") { (target) ->
            if (target.tile != Tile(3107, 3368)) return@objectOperate
            openTubeDoor(target)
        }
        itemOnObjectOperate("grimy_key", "door_11_closed") { (target) ->
            if (target.tile == Tile(3107, 3368)) openTubeDoor(target)
        }
        takeable("rubber_tube") { item, telegrab ->
            if (item.tile != Tile(3112, 3368)) {
                item.id
            } else if (telegrab || !get("ernest_tube_unlocked", false)) {
                message("You need to unlock the room and collect the tube from inside.")
                null
            } else {
                item.id
            }
        }
        questJournalOpen(ERNEST) {
            val lines = when (quest(ERNEST)) {
                "completed" -> listOf("<str>I found the missing machine parts and helped restore Ernest.", "", "<red>QUEST COMPLETE!")
                "parts" -> buildList {
                    add("<navy>Professor Oddenstein turned Ernest into a chicken.")
                    add("<navy>I must find three parts to repair his machine:")
                    for (part in PARTS) add("${if (ownsErnestPart(part)) "<str>" else "<maroon>"}${part.replace('_', ' ')}")
                    add("<navy>The parts are hidden around Draynor Manor.")
                }
                "started" -> listOf("<navy>Veronica asked me to find Ernest inside Draynor Manor.", "<navy>I should speak to the professor on the top floor.")
                else -> listOf("<navy>I can start this quest by speaking to Veronica", "<navy>outside the gates of Draynor Manor.")
            }
            questJournal("Ernest the Chicken", lines)
        }
    }

    private suspend fun Player.searchCompost() {
        if (!inventory.contains("spade")) {
            message("You need a spade to search the compost heap properly.")
            return
        }
        if (inventory.contains("grimy_key") || bank.contains("grimy_key")) {
            message("You have already found the key.")
            return
        }
        anim("ernest_dig")
        delay(2)
        if (inventory.add("grimy_key")) {
            message("You find a grimy key in the compost heap.")
        } else {
            message("You don't have enough inventory space to take the key.")
        }
    }

    private suspend fun Player.openTubeDoor(target: GameObject) {
        if (!get("ernest_tube_unlocked", false)) {
            if (!inventory.contains("grimy_key")) {
                message("The door is locked.")
                return
            }
            set("ernest_tube_unlocked", true)
            message("You unlock the door with the grimy key.")
        }
        enterDoor(target)
    }
}

private fun Player.ownsErnestPart(id: String) = inventory.contains(id) || bank.contains(id)

/** Return false to retain the professor's ordinary and post-quest dialogue. */
suspend fun Player.oddensteinErnest(): Boolean {
    when (quest(ERNEST)) {
        "started" -> {
            npc<Neutral>("Ernest helped test my pouletmorph machine. Unfortunately, he is now a chicken!")
            player<Neutral>("Can you turn him back?")
            npc<Neutral>("Yes, but the poltergeists have hidden my pressure gauge, rubber tube and oil can. Please find all three.")
            set(ERNEST, "parts")
            return true
        }
        "parts" -> {
            val missing = PARTS.filterNot { inventory.contains(it) }
            if (missing.isNotEmpty()) {
                npc<Neutral>("I still need my ${missing.joinToString(", ") { it.replace('_', ' ') }}. Bring the parts here so I can repair the machine.")
                return true
            }
            npc<Neutral>("You've found all the parts! Now I can repair the machine and change Ernest back.")
            // All rewards stack. Removing the three parts makes room even in a full inventory.
            val restored = NPCs.add("chicken_draynor_2", Tile(3111, 3366, 2), ticks = -1, owner = this)
            try {
                statement("The machine hums and shakes.")
                restored.transform("ernest_human", collision = false)
                npc<Happy>("ernest_human", "Thank you! Being a chicken was most inconvenient. Please accept this reward.")
                if (quest(ERNEST) != "parts") return true
                val paid = inventory.transaction {
                    for (part in PARTS) remove(part)
                    add("coins", 3000)
                    add("egg_noted", 10)
                    add("feather", 300)
                }
                if (!paid) {
                    message("Make room for your reward, then talk to the professor again.")
                    return true
                }
                set(ERNEST, "completed")
                inc("quest_points", 4)
                jingle("quest_complete_1")
                questComplete("Ernest the Chicken", "4 Quest Points", "3,000 coins", "10 noted eggs", "300 feathers", item = "feather")
            } finally {
                NPCs.remove(restored)
            }
            return true
        }
        else -> return false
    }
}
