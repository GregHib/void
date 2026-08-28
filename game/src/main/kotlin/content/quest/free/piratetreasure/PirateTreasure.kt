package content.quest.free.piratetreasure

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.inv.item.addOrDrop
import content.quest.messageScroll
import content.quest.questComplete
import content.quest.questJournal
import content.quest.questStage
import content.quest.refreshQuestJournal
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.command.adminCommand
import world.gregs.voidps.engine.client.instruction.handle.interactPlayer
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.jingle
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.event.AuditLog
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.Tile
import java.util.concurrent.TimeUnit

class PirateTreasure : Script {

    init {

        adminCommand("reset_pirates_treasure", desc = "Reset Pirate's Treasure back to unstarted") {
            resetQuest()
            message("Pirate's Treasure reset.")
        }

        questJournalOpen("pirates_treasure") {
            val lines = when (val stage = questStage("pirates_treasure")) {
                0 -> notStartedJournal()
                4 -> completedJournal()
                else -> startedJournal(stage)
            }
            questJournal("Pirate's Treasure", lines)
        }

        itemOnNPCOperate("casket_pirates_treasure", "redbeard_frank_port_sarim") {
            player<Happy>("I have the treasure, would you like a share?")
            npc<Happy>("No lad, you got it fair and square.")
            npc<Happy>("You enjoy it. It's what Hector would have wanted.")
        }

        objectOperate("Open", "bluemoon_chest") {
            message("The chest is locked.")
            sound("locked")
        }

        itemOnObjectOperate("chest_key_pirates_treasure", "bluemoon_chest") { (target) ->
            anim("open_chest")
            sound("chest_open")
            target.replace(id = "bluemoon_chest_open", ticks = 4)
            message("You unlock the chest.")
            message("All that's in the chest is a message...")

            delay(4)

            anim("close_chest")
            sound("chest_close")
            message("You take the message from the chest.")
            inventory.remove("chest_key_pirates_treasure")
            inventory.add("pirate_message")
        }

        itemOption("Read", "pirate_message") {
            messageScroll(
                listOf(
                    "",
                    "",
                    "",
                    "",
                    "Visit the city of the White Knights. In the park,",
                    "Saradomin points to the X which marks the spot.",
                ),
                handwriting = true,
            )
            if (questStage("pirates_treasure") == 2) {
                set("pirates_treasure", "read_message")
            }
        }

        itemOption("Open", "casket_pirates_treasure") {
            val needsSpace = if (inventory.contains("coins")) 1 else 2
            if (inventory.spaces < needsSpace) {
                return@itemOption message(
                    "From the weight of this you can guess you need more inventory space to get at the contents.",
                )
            }
            sound("chest_open")
            message("You open the casket, and find One-Eyed Hector's treasure.")
            inventory.remove("casket_pirates_treasure")
            inventory.add("gold_ring")
            inventory.add("emerald")
            inventory.add("coins", 450)
        }

        itemOption("Dig", "spade") {
            if (tile != DIG_SITE) {
                return@itemOption
            }
            handleSpadeDig()
        }

        objectOperate("Search", "crate_13") { (target) ->
            if (target.tile != WYDIN_BANANA_CRATE) {
                return@objectOperate
            }
            message("There are a lot of bananas in the crate.")
            if (get("pirates_treasure_delivered_rum", false)) {
                delay(4)
                message("You find your bottle of rum in amongst the bananas.")
                addOrDrop("karamjan_rum")
                anim("human_pickuptable")
                sound("pick_2")
                set("pirates_treasure_collected_rum", true)
                set("pirates_treasure_stashed_rum", false)
                set("pirates_treasure_delivered_rum", false)
            }

            delay(4)

            choice("Do you want to take a banana?") {
                option("Yes.") {
                    if (inventory.spaces > 0) {
                        message("You take a banana.")
                        inventory.add("banana")
                        sound("pick_2")
                        anim("human_pickuptable")
                    } else {
                        message("You do not have enough free space to take a banana.")
                    }
                }
                option("No.") {}
            }
        }

        objectOperate("Search", "crate_116") { (target) ->
            if (target.tile != POTATO_CRATE) {
                return@objectOperate
            }
            searchFoodCrate("potatoes", "potato", "raw_potato")
        }

        objectOperate("Search", "crate_117") { (target) ->
            if (target.tile != CABBAGE_CRATE) {
                return@objectOperate
            }
            searchFoodCrate("cabbages", "cabbage", "cabbage")
        }

        objectOperate("Search", "raw_chicken_crate") { (target) ->
            if (target.tile != CHICKEN_CRATE) {
                return@objectOperate
            }
            searchFoodCrate("raw chickens", "raw chicken", "raw_chicken")
        }

        for (type in MAGIC_TELEPORTS) {
            teleportTakeOff(type) {
                spillRum()
                true
            }
        }

        itemOnItem("banana", "karamjan_rum") { _, _ ->
            inventory.remove("karamjan_rum")
            inventory.remove("banana")
            inventory.add("karamjan_rum_banana")
            message("You stuff the banana into the neck of the bottle. You begin to wonder why.")
        }

        itemOnItem("sliced_banana", "karamjan_rum") { _, _ ->
            inventory.remove("karamjan_rum")
            inventory.remove("sliced_banana")
            inventory.add("karamjan_rum_sliced_banana")
            message("You add the banana slices to the Karamjan rum.")
        }
    }

    private suspend fun Player.searchFoodCrate(plural: String, name: String, itemId: String) {
        message("There are a lot of $plural in the crate.")
        delay(4)
        choice("Do you want to take a $name?") {
            option("Yes.") {
                if (inventory.spaces > 0) {
                    message("You take a $name.")
                    inventory.add(itemId)
                    sound("pick_2")
                    anim("human_pickuptable")
                } else {
                    message("You do not have enough free space to take a $name.")
                }
            }
            option("No.") {}
        }
    }

    private suspend fun Player.handleSpadeDig() {
        if (questStage("pirates_treasure") != 3) {
            return message("It seems a shame to dig up these nice flowers for no reason.")
        }
        if (gardener() != null) {
            return message("I can't dig up anything with him attacking me!")
        }
        if (!get("pirates_treasure_spawned_gardener", false)) {
            set("pirates_treasure_spawned_gardener", true)
            val gardener = NPCs.add(
                id = "gardener_level_4",
                tile = tile.add(x = 1),
                ticks = TimeUnit.MINUTES.toTicks(8),
                owner = this,
            )
            gardener.say("First moles, now this! Take this, vandal!")
            gardener.interactPlayer(this, "Attack")
            return
        }
        message("You dig a hole in the ground...")
        delay(4)
        message("and find a little chest of treasure.")
        addOrDrop("casket_pirates_treasure")
        completeQuest()
    }

    private fun Player.resetQuest() {
        val completed = questStage("pirates_treasure") >= 4
        clear("pirates_treasure")
        for (flag in FLAGS) {
            clear(flag)
        }
        clear("banana_plantation_job")
        clear("banana_crate_bananas")
        for (item in QUEST_ITEMS) {
            inventory.remove(item, inventory.count(item))
        }
        NPCs.remove(gardener())
        if (completed) {
            inc("quest_points", -2)
        }
        refreshQuestJournal()
    }

    private fun Player.spillRum() {
        val amount = inventory.count("karamjan_rum")
        if (amount <= 0) {
            return
        }
        inventory.remove("karamjan_rum", amount)
        message("Your Karamjan rum gets broken and spilled.")
    }

    private fun Player.gardener(): NPC? = NPCs.at(tile.regionLevel).firstOrNull {
        it.id == "gardener_level_4" && it["owner", ""] == accountName && !it["dead", false]
    }

    private suspend fun Player.completeQuest() {
        set("pirates_treasure", "completed")
        jingle("quest_complete_1")
        inc("quest_points", 2)
        AuditLog.event(this, "quest_completed", "pirates_treasure")
        refreshQuestJournal()
        questComplete(
            "Pirate's Treasure",
            "2 Quest Points",
            "One-Eyed Hector's Treasure",
            item = "casket_pirates_treasure",
        )
    }

    private fun Player.notStartedJournal(): List<String> = listOf(
        "<navy>I can start this quest by speaking to <maroon>Redbeard Frank<navy> who",
        "<navy>is at <maroon>Port Sarim<navy>, south of the <maroon>Rusty Anchor<navy>.",
        "",
        "<navy>There aren't any requirements for this quest.",
    )

    private fun completedJournal(): List<String> = listOf(
        "<str>I have spoken to Redbeard Frank. He has agreed to tell me",
        "<str>the location of some treasure for some Karamja Rum.",
        "",
        "<str>I have smuggled some rum off Karamja, and retrieved it",
        "<str>from the back room of Wydin's shop.",
        "<str>I have given the rum to Redbeard Frank. He has told me",
        "<str>that the treasure is hidden in the chest in the upstairs",
        "<str>room of the Blue Moon Inn in Varrock.",
        "<str>I have opened the chest in the Blue Moon, and found a",
        "<str>note inside. I think it will tell me where to dig.",
        "<str>The note reads 'Visit the city of the White Knights. In the",
        "<str>park, Saradomin points to the X which marks the spot.'",
        "",
        "<red>QUEST COMPLETE!",
        "",
        "<navy>I've found the treasure, gained 2 Quest Points and gained",
        "<navy>access to the Pay-fare option to travel to and from",
        "<navy>Karamja!",
    )

    private fun Player.startedJournal(stage: Int): List<String> {
        val list = mutableListOf<String>()

        list += "<navy>I have spoken to <maroon>Redbeard Frank<navy>. He has agreed to tell me"
        list += "<navy>the location of some <maroon>treasure<navy> for some <maroon>Karamja Rum<navy>."
        list += ""

        if (stage == 1) {
            val collected = get("pirates_treasure_collected_rum", false)
            val luthas = get("banana_plantation_job", false)
            val delivered = get("pirates_treasure_delivered_rum", false)
            val wydin = get("pirates_treasure_wydin", false)
            val stashed = get("pirates_treasure_stashed_rum", false)
            val bananas = get("banana_crate_bananas", 0)
            val hasRum = inventory.contains("karamjan_rum")

            when {
                collected -> {
                    if (hasRum) {
                        list += "<navy>I have the <maroon>Karamja Rum<navy>. I should take it to <maroon>Redbeard Frank<navy>."
                    } else {
                        list += "<navy>I had some <maroon>rum<navy>, but I seem to have lost it. I will need to"
                        list += "<navy>smuggle some more off <maroon>Karamja<navy>."
                    }
                }
                luthas -> {
                    if (delivered) {
                        list += "<str>I have taken employment on the banana plantation, as the"
                        list += "<str>Customs Officers might not notice the rum if it is covered"
                        list += "<str>in bananas."
                        list += ""
                        list += "<str>I have hidden my rum in the crate. I should fill it with"
                        list += "<str>bananas and speak to Luthas to have it shipped over."
                        list += ""
                        list += "<navy>I have spoken to <maroon>Luthas<navy>, and the crate has been shipped"
                        list += "<navy>to <maroon>Wydin's store<navy> in <maroon>Port Sarim<navy> north of the jail. Now all I"
                        list += "<navy>have to do is get to it..."

                        if (wydin) {
                            list += "<str>I have spoken to Luthas, and the crate has been shipped"
                            list += "<str>to Wydin's store in Port Sarim. Now all I have to do is get to"
                            list += "<str>it..."
                            list += ""
                            list += "<navy>I have taken a job at <maroon>Wydin's store<navy>. I now have access to"
                            list += "<navy>the back room of his shop where the <maroon>rum<navy> is hidden..."
                        }
                    } else {
                        list += "<navy>I have taken employment on the <maroon>banana plantation<navy>, as the"
                        list += "<maroon>Customs Officers<navy> might not notice the <maroon>rum<navy> if it is covered"
                        list += "<navy>in <maroon>bananas<navy>."
                        list += ""

                        if (stashed) {
                            list += if (bananas == 10) {
                                "<navy>I have hidden my <maroon>rum<navy> in the crate and filled it with"
                            } else {
                                "<navy>I have hidden my <maroon>rum<navy> in the crate. I should fill it with"
                            }
                            list += "<maroon>bananas<navy> and speak to <maroon>Luthas<navy> to have it shipped over."
                        } else {
                            list += if (hasRum) {
                                "<navy>I'm sure I will be able to hide my <maroon>rum<navy> in the next crate"
                            } else {
                                "<navy>Now all I need is some <maroon>rum<navy> to hide in the next crate"
                            }
                            list += "<navy>destined for <maroon>Wydin's store<navy>..."
                        }
                    }
                }
                hasRum -> {
                    list += "<navy>I have the <maroon>rum<navy>, and now I need to find a way to get the rum"
                    list += "<navy>off <maroon>Karamja<navy>. This might be tricky, as the <maroon>Customs Officers"
                    list += "<navy>are searching people for it. I should look around and see if"
                    list += "<navy>there is a way to smuggle the rum off the island."
                }
                else -> {
                    list += "<navy>I need to go to <maroon>Karamja<navy> and buy some <maroon>rum<navy>. I hope it is not"
                    list += "<navy>too expensive."
                }
            }
        }

        if (stage >= 2) {
            list += "<str>I have smuggled some rum off Karamja, and retrieved it"
            list += "<str>from the back room of Wydin's shop."
            list += ""
            list += "<navy>I have given the rum to <maroon>Redbeard Frank<navy>. He has told me"
            list += "<navy>that the <maroon>treasure<navy> is hidden in the chest in the upstairs"
            list += "<navy>room of the <maroon>Blue Moon Inn<navy> in <maroon>Varrock<navy>."
            list += ""

            val hasMessage = inventory.contains("pirate_message")
            val hasKey = inventory.contains("chest_key_pirates_treasure")

            when {
                hasMessage || stage >= 3 -> {
                    list += "<navy>I have opened the chest in the <maroon>Blue Moon<navy>, and found a"
                    list += "<maroon>note<navy> inside. I think it will tell me where to dig."
                }
                hasKey -> {
                    list += "<navy>I have a <maroon>key<navy> that can be used to unlock the chest that"
                    list += "<navy>holds the treasure. The chest is located in <maroon>Hector's old"
                    list += "<maroon>room<navy>, at the <maroon>Blue Moon Inn<navy> in <maroon>Varrock<navy>."
                }
                else -> {
                    list += "<navy>I have lost the <maroon>key<navy> that <maroon>Redbeard Frank<navy> gave me. I should"
                    list += "<navy>see if he has another."
                }
            }
        }

        if (stage >= 3) {
            list += "<str>I have opened the chest in the Blue Moon, and found a"
            list += "<str>note inside. I think it will tell me where to dig."
            list += ""
            list += "<navy>The note reads: <maroon>'Visit the city of the White Knights. In the"
            list += "<maroon>park, Saradomin points to the X which marks the spot.'<navy>"
        }

        return list
    }

    private companion object {
        val MAGIC_TELEPORTS = listOf("modern", "ancient", "lunar")

        val FLAGS = listOf(
            "pirates_treasure_wydin",
            "pirates_treasure_stashed_rum",
            "pirates_treasure_delivered_rum",
            "pirates_treasure_collected_rum",
            "pirates_treasure_spawned_gardener",
        )
        val QUEST_ITEMS = listOf(
            "karamjan_rum",
            "karamjan_rum_banana",
            "karamjan_rum_sliced_banana",
            "chest_key_pirates_treasure",
            "pirate_message",
            "casket_pirates_treasure",
        )
        val DIG_SITE = Tile(3000, 3383)
        val WYDIN_BANANA_CRATE = Tile(3009, 3207)
        val POTATO_CRATE = Tile(3011, 3203)
        val CABBAGE_CRATE = Tile(3010, 3203)
        val CHICKEN_CRATE = Tile(3009, 3209)
    }
}

/*
 * TODO Karamjan rum destruction routes from the original quest that have no equivalent here yet:
 *
 * - Fairy rings. The original only breaks the rum when departing one of three specific rings
 *   (2801,3003 / 2900,3111 / 2650,4730); the teleport take-off hook here doesn't expose which
 *   ring was used, and fairy ring travel isn't implemented on this server.
 * - Gnome glider. The original destroys the rum inside the glider pilot's dialogue ("Oh my! What
 *   is that thing over there?" ... "Hmm, my mistake, it must have been an optical illusion."),
 *   which distracts you while he takes it. This server's gliders travel from the glider map
 *   interface instead and there is no pilot conversation to hang it off.
 * - Captain Shanks' boat to Port Sarim, which loses the rum to a sailor in a game of dice
 *   ("During the trip you lose your rum to a sailor in a game of dice. Better luck next time!").
 *   Captain Shanks doesn't exist here.
 * - Shilo Village gate and Mosol Rei's dialogue, both of which drop the rum on entry
 *   ("Oh dear, you have dropped your rum!"). Neither is implemented here.
 * - The TzHaar city cave entrance, which destroys the rum and burns you for 10 damage per bottle
 *   ("Your Karamja Rum explodes in the heat!" with a fire wave graphic). The object
 *   'cave_entrance_tzhaar_city' has no handler here, so entering the city isn't implemented.
 */
