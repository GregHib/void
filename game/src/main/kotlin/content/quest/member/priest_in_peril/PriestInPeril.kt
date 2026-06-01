package content.quest.member.priest_in_peril

import content.entity.combat.hit.directHit
import content.entity.combat.killer
import content.entity.gfx.areaGfx
import content.entity.obj.door.enterDoor
import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.entity.player.inv.item.addOrDrop
import content.quest.quest
import content.quest.questCompleted
import content.quest.questJournal
import content.quest.questStage
import content.skill.magic.spell.spell
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.equals
import world.gregs.voidps.type.random

class PriestInPeril : Script {

    init {
        questJournalOpen("priest_in_peril") {
            val progress = questStage("priest_in_peril")
            val lines = mutableListOf<String>()
            if (progress == 0) {
                lines += "<navy>I can start this quest by speaking to <maroon>King Roald<navy> in <maroon>Varrock"
                lines += "<maroon>Palace"
                lines += ""
                lines += "<navy>I must be able to defeat a <maroon>level 30 enemy"
            } else {
                lines += "<str>I spoke to King Roald who asked me to investigate why his"
                lines += "<str>friend Priest Drezel has stopped communicating with him."
                if (progress >= 2) {
                    lines += "<str>I headed to the temple where Drezel lives, but it was all"
                    lines += "<str>locked shut. I spoke through the locked door to Drezel."
                }
                if (progress >= 3) {
                    lines += "<str>He told me that there was an annoying dog below the"
                    lines += "<str>temple, and asked me to kill it, which I did easily."
                }
                if (progress >= 4) {
                    lines += "<str>When I told Roald what I had done, he was furious. The"
                    lines += "<str>person who told me to kill the dog wasn't Drezel at all!"
                }
                if (progress >= 5) {
                    lines += "<str>I returned to the temple and found the real Drezel locked"
                    lines += "<str>in a makeshift cell upstairs, guarded by a vampire."
                }
                if (progress >= 7) {
                    lines += "<str>I used a key from the monument to open the cell door and"
                    lines += "<str>used Holy Water to trap the vampire in his coffin."
                } else if (progress == 6) {
                    lines += "<str>I used a key from the monument to open the cell door"
                }
                if (progress >= 10) {
                    lines += "<str>I followed Drezel downstairs only to find that the Salve"
                    lines += "<str>had been contaminated and now needed purifying."
                }
                if (progress >= 60) {
                    lines += "<str>I brought Drezel fifty rune essences and the"
                    lines += "<str>contaminants were dissolved from the Salve, and Drezel"
                    lines += "<str>rewarded me for all of my help with an ancient holy weapon"
                    lines += "<str>to fight with."
                }
                lines += ""
                when {
                    progress == 1 -> {
                        lines += "<maroon>Drezel<navy> lives in a <maroon>temple<navy> to the <maroon>east<navy> of Varrock Palace. I"
                        lines += "<navy>should head there and <maroon>investigate<navy> what's happened to him"
                    }
                    progress == 2 -> {
                        lines += "<navy>He told me that there was an annoying <maroon>dog<navy> below the"
                        lines += "<navy>temple, and has asked me to <maroon>kill it<navy> for him"
                    }
                    progress == 3 -> {
                        lines += "<navy>I should tell <maroon>King Roald<navy> everything's fine with <maroon>Drezel<navy> now I"
                        lines += "<navy>have killed that <maroon>dog<navy> for him, and claim my <maroon>reward."
                    }
                    progress == 4 -> {
                        lines += "<navy>I must return to the <maroon>temple<navy> and find out what happened to"
                        lines += "<navy>the real <maroon>Drezel<navy>, or the King will have me executed!"
                    }
                    progress == 5 -> {
                        lines += "<navy>I need to find the <maroon>key<navy> to his cell and free him!"
                    }
                    progress == 6 -> {
                        lines += "<navy>But I still have to do something about that <maroon>vampire."
                    }
                    progress == 7 -> {
                        lines += "<navy>I should speak to <maroon>Drezel<navy> again."
                    }
                    progress == 8 -> {
                        lines += "<navy>I should head downstairs to the <maroon>monument<navy> like <maroon>Drezel"
                        lines += "<navy>asked me to, and assess what <maroon>damage<navy> has been done"
                    }
                    progress in 10..59 -> {
                        val remaining = 60 - progress
                        lines += "<navy>I need to bring <maroon>$remaining<navy> rune essences<navy> to undo the damage"
                        lines += "<navy>done by the Zamorakians and <maroon>purify the salve"
                    }
                    progress >= 60 -> {
                        lines += "<red>QUEST COMPLETE!"
                    }
                }
            }
            questJournal("Priest in Peril", lines)
        }

        objectOperate("Study", "priestperil_grave_base*") { (target) ->
            if (questCompleted("priest_in_peril")) {
                message("A monument dedicated to the fallen.")
                return@objectOperate
            }
            val value = get(target.id, 0)
            sendMonumentInterface(value)
        }

        objectOperate("Take-from", "priestperil_grave_base*") {
            if (questCompleted("priest_in_peril")) {
                message("It would be wrong to dishonour this monument.")
                return@objectOperate
            }
            anim("human_opencupboard")
            directHit(10)
            message("A holy power prevents you from stealing from the monument.")
        }

        objectOperate("Search", "priestperil_well") {
            val clean = questCompleted("priest_in_peril")
            val desc = if (clean) "fresh water of the River Salve moving swiftly" else "filthy polluted water of the River Salve moving slowly"
            statement("You look down the well and see the $desc along.")
        }

        itemOnObjectOperate("bucket", "priestperil_well") {
            inventory.remove("bucket")
            inventory.add("bucket_murkywater")
            anim("take")
            sound("well_fill")
            message("You fill the bucket from the well.")
        }

        itemOnObjectOperate(
            "pipkey_gold,pipkey_iron,piptinderbox_gold,pipcandle_gold,pippot_gold,piphammer_gold,pipfeather_gold,pipneedle_gold,tinderbox,unlit_candle,empty_pot,hammer,feather,needle",
            "priestperil_grave_base*",
        ) { interaction ->
            val value = get(interaction.target.id, 0)
            when (value) {
                1 -> handleGoldItemSwamp(
                    index = 1,
                    itemUsed = interaction.item,
                    firstItem = "pipneedle_gold",
                    secondItem = "needle",
                )
                2 -> handleGoldItemSwamp(
                    index = 2,
                    itemUsed = interaction.item,
                    firstItem = "pipfeather_gold",
                    secondItem = "feather",
                )
                3 -> handleGoldItemSwamp(
                    index = 3,
                    itemUsed = interaction.item,
                    firstItem = "hammer",
                    secondItem = "piphammer_gold",
                )
                4 -> handleGoldItemSwamp(
                    index = 4,
                    itemUsed = interaction.item,
                    firstItem = "pipcandle_gold",
                    secondItem = "unlit_candle",
                )
                5 -> handleGoldItemSwamp(
                    index = 5,
                    itemUsed = interaction.item,
                    firstItem = "pipkey_gold",
                    secondItem = "pipkey_iron",
                )
                6 -> handleGoldItemSwamp(
                    index = 6,
                    itemUsed = interaction.item,
                    firstItem = "piptinderbox_gold",
                    secondItem = "tinderbox",
                )
                7 -> handleGoldItemSwamp(
                    index = 7,
                    itemUsed = interaction.item,
                    firstItem = "pippot_gold",
                    secondItem = "pot",
                )
            }
        }

        itemOnObjectOperate("bucket_of_water,bucket_murkywater,bucket_blessedwater", "priestperil_coffin_noanim") { interaction ->
            handleWaterOnCoffin(interaction.item)
        }

        objectOperate("Open", "priestperil_coffin_noanim") { (target) ->
            player<Confused>("It sounds like there's something alive inside it. I don't think it would be a very good idea to open it.")
        }

        objectOperate("Open", "priestperiltempledoor*") { (target) ->
            if (questStage("priest_in_peril") < 4) {
                sound("locked")
                message("This door is securely locked from the inside.")
                return@objectOperate
            }
            sound("barrows_door_open")
            enterDoor(target, delay = 2)
        }

        itemOnObjectOperate("pipkey_iron,pipkey_gold", "pip_prisondoor_closed") { interaction ->
            if (interaction.item.id == "pipkey_gold") {
                message("The key is a similar size to the lock, but does not fit.")
                return@itemOnObjectOperate
            }
            if (questStage("priest_in_peril") >= 6) {
                message("Nothing interesting happens.")
                return@itemOnObjectOperate
            }
            foundKey()
        }

        objectOperate("Open", "pip_prisondoor_closed") { (target) ->
            if (questStage("priest_in_peril") < 6) {
                if (inventory.contains("pipkey_iron")) {
                    return@objectOperate foundKey()
                }
                message("The door is securely locked shut.")
                return@objectOperate
            }
            enterDoor(target)
        }

        objectOperate("Open", "pip_underground_door1_closed") { (target) ->
            if (questStage("priest_in_peril") < 4) {
                sound("locked")
                message("The door is securely locked shut.")
                player<Quiz>("Hmmm... from the looks of things, it seems as though somebody has been trying to force this door open. It's still securely locked however.")
                return@objectOperate
            }
            enterDoor(target)
        }

        objectOperate("Open", "pip_underground_door2_closed") { (target) ->
            if (questStage("priest_in_peril") < 8) {
                sound("locked")
                message("The door is securely locked shut.")
                return@objectOperate
            }
            enterDoor(target)
        }

        objectOperate("Climb-up", "priestperil_temple_stair_sw_lower") {
            delay(1)
            tele(3415, 3485, 1)
        }

        objectOperate("Climb-down", "priestperil_temple_stair_sw_upper") {
            delay(1)
            tele(3415, 3485, 0)
        }

        objectOperate("Climb-up", "priestperil_temple_stair_se_lower") {
            delay(1)
            tele(3415, 3492, 1)
        }

        objectOperate("Climb-down", "priestperil_temple_stair_se_upper") {
            delay(1)
            tele(3415, 3492, 0)
        }

        objectOperate("Climb-up", "priestperil_cell_ladder_bottom") {
            delay(1)
            tele(3408, 3485, 2)
        }

        objectOperate("Climb-down", "priestperil_cell_ladder_top") {
            delay(1)
            tele(3410, 3484, 1)
        }

        objectOperate("Pass-through", "pip_underground_wall_side_withportal") {
            val stage = quest("priest_in_peril")
            if (stage == "completed_wolfbane") {
                tele(Tile(3423, 3484, 0))
                message("You pass through the holy barrier.")
                delay(1)
                return@objectOperate
            }
            val drezel = NPCs.findBySpawn(Tile(3440, 9895), "priestperiltrappedmonk2")
            talkWith(drezel)
            npc<Angry>("STOP!")
            player<Quiz>("Can't I go through there?")
            if (stage == "completed") {
                npc<Neutral>("Yes, now the Salve is restored you may, but speak to me first, as I have advice for you before you pass through.")
            } else {
                npc<Angry>("No, you cannot! It is taking all of my willpower to hold that barrier in place. You must restore the sanctity of the Salve as soon as possible!")
            }
        }

        canAttack("priestperilguarddog") {
            val stage = questStage("priest_in_peril")
            when {
                stage < 2 -> {
                    message("You have no reason to attack a helpless dog!")
                    false
                }
                stage >= 3 -> {
                    message("I'd better not make the King mad at me again!")
                    false
                }
                spell.isNotBlank() -> {
                    message("Your spells do not seem to affect it.")
                    false
                }
                else -> true
            }
        }

        npcDeath("priestperilguarddog") {
            val killer = killer as? Player ?: return@npcDeath
            if (killer.quest("priest_in_peril") == "kill_dog") {
                killer["priest_in_peril"] = "dog_dead"
            }
        }

        variableSet("priest_in_peril") { _, _, to ->
            if (to == "find_drezel") {
                listOf(1, 2, 3, 4, 5, 6, 7)
                    .shuffled(random)
                    .forEachIndexed { index, value ->
                        set("priestperil_grave_base${index + 1}", value)
                    }
            }
        }
    }

    fun Player.sendMonumentInterface(index: Int) {
        when (index) {
            1 -> sendMonumentInterface(
                index = index,
                originalId = "pipneedle_gold",
                swappedId = "needle",
                message = "Saradomin is the <br> <br> needle that binds <br> <br>  our lives <br> <br>  together.",
            )
            2 -> sendMonumentInterface(
                index = index,
                originalId = "pipfeather_gold",
                swappedId = "feather",
                message = "Saradomin is the <br> <br> delicate touch <br> <br>  that brushes us <br> <br>  with love.",
            )
            3 -> sendMonumentInterface(
                index = index,
                originalId = "piphammer_gold",
                swappedId = "hammer",
                message = "Saradomin is the <br> <br> hammer that <br> <br>  crushes evil <br> <br>  everywhere.",
            )
            4 -> sendMonumentInterface(
                index = index,
                originalId = "pipcandle_gold",
                swappedId = "unlit_candle",
                message = "Saradomin is the <br> <br> light that shines <br> <br>  throughout <br> <br> our lives.",
            )
            5 -> sendMonumentInterface(
                index = index,
                originalId = "pipkey_iron",
                swappedId = "pipkey_gold",
                message = "Saradomin is the <br> <br> key that unlocks <br> <br> the mysteries <br> <br> of life.",
            )
            6 -> sendMonumentInterface(
                index = index,
                originalId = "piptinderbox_gold",
                swappedId = "tinderbox",
                message = "Saradomin <br> <br> lights my way <br> <br>  through <br> <br> the darkness of life.",
            )
            7 -> sendMonumentInterface(
                index = index,
                originalId = "pippot_gold",
                swappedId = "pot",
                message = "Saradomin is the <br> <br> vessel that <br> <br> keeps us safe <br> <br> from harm.",
            )
        }
    }

    fun Player.sendMonumentInterface(index: Int, originalId: String, swappedId: String, message: String) {
        val swapped = get("priestperil_${index}_swapped", false)
        open("priestperil_gravemonument")
        interfaces.sendText(
            id = "priestperil_gravemonument",
            component = "text_component",
            text = message,
        )
        interfaces.sendItem(
            id = "priestperil_gravemonument",
            component = "item_component",
            item = Item(if (swapped) swappedId else originalId),
        )
    }

    suspend fun Player.handleGoldItemSwamp(index: Int, itemUsed: Item, firstItem: String, secondItem: String) {
        val firstName = ItemDefinitions.get(firstItem).name
        val secondName = ItemDefinitions.get(secondItem).name

        if (itemUsed.id != firstItem && itemUsed.id != secondItem) {
            message("Nothing interesting happens.")
            return
        }

        if (itemUsed.id == secondItem) {
            if (firstItem == "pipkey_gold") {
                player<Quiz>("I think this key is more useful to me right now than the Golden key is.")
            } else {
                player<Confused>("You know... I think I'd rather keep the valuable solid gold $secondName.")
            }
            return
        }

        if (inventory.contains(secondItem) || (get("priestperil_${index}_swapped", false) && index != 5)) {
            message("You have already swapped the $firstName for the $secondName.")
            return
        }

        anim("human_opencupboard")
        inventory.remove(firstItem)
        addOrDrop(secondItem)
        message("You swap the $firstName for the $secondName.")
        set("priestperil_${index}_swapped", true)
    }

    suspend fun Player.handleWaterOnCoffin(itemUsed: Item) {
        when (itemUsed.id) {
            "bucket_murkywater" -> player<Confused>("This water doesn't look particularly holy to me... I think I'd better check with the priest first.")
            "bucket_blessedwater" -> {
                if (quest("priest_in_peril") == "drezel_free") {
                    set("priest_in_peril", "coffin_destroyed")
                }
                message("You pour the water over the coffin...")
                anim("throw_bucketofwater")
                inventory.remove("bucket_blessedwater")
                inventory.add("bucket")
                sound("holy_water_pour", delay = 20)
                areaGfx(id = "priestperil_coffin_spell", tile = Tile(3410, 3489, 2), delay = 4)
                areaGfx(id = "priestperil_coffin_spell", tile = Tile(3410, 3488, 2), delay = 4)
                delay(3)
            }
            "bucket_of_water" -> player<Confused>("I don't think pouring normal water on the coffin is going to help...")
        }
    }

    suspend fun Player.foundKey() {
        val drezel = NPCs.find(tile.regionLevel) { it.id.startsWith("priestperiltrappedmonk_vis") }
        talkWith(drezel)
        inventory.remove("pipkey_iron")
        message("You unlock the cell door.")
        set("priest_in_peril", "drezel_free")
        npc<Happy>("Oh! Thank you! You have found the key!")
    }
}
