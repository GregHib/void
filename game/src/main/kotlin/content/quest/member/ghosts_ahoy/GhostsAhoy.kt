package content.quest.member.ghosts_ahoy

import content.entity.combat.hit.directHit
import content.entity.combat.killer
import content.entity.effect.clearTransform
import content.entity.effect.transform
import content.entity.obj.door.Door
import content.entity.obj.door.walkThroughDoor
import content.entity.player.bank.ownsItem
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Scared
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.entity.player.inv.item.addOrDrop
import content.quest.questComplete
import content.quest.questCompleted
import content.quest.questJournal
import content.quest.refreshQuestJournal
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.hint
import world.gregs.voidps.engine.client.instruction.handle.interactPlayer
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.jingle
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Teleport
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.hasMax
import world.gregs.voidps.engine.entity.character.player.wearingGhostspeak
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.event.AuditLog
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.inv.replace
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Tile

class GhostsAhoy : Script {

    init {

        questJournalOpen("ghosts_ahoy") {
            val progress = ghosts_ahoy
            val lines = mutableListOf<String>()

            if (progress == 0) {
                val ghost = questCompleted("the_restless_ghost")
                val priest = questCompleted("priest_in_peril")
                val agility = hasMax(Skill.Agility, 25, false)
                val cooking = hasMax(Skill.Cooking, 20, false)
                lines += "<navy>To start this quest I need to speak to <maroon>Velorina<navy>,"
                lines += "<navy>a ghost of <maroon>Port Phasmatys<navy>."
                lines += "<navy>I must have at least:"
                lines += if (agility) "<str>level 25 agility" else "<maroon>level 25 agility"
                lines += if (cooking) "<str>level 20 cooking." else "<maroon>level 20 cooking."
                lines += "<navy>I must also be able to defeat a <maroon>level 32 monster<navy>."
                lines += "<navy>I must also have completed the following quests:"
                lines += if (priest) "<str>Priest in Peril" else "<maroon>Priest in Peril"
                lines += if (ghost) "<str>Restless Ghost" else "<maroon>Restless Ghost"
                questJournal("Ghosts Ahoy", lines)
                return@questJournalOpen
            }

            lines += "<str>I have spoken with Velorina, who has told me the sad"
            lines += "<str>history of the ghosts of Port Phasmatys. She has asked"
            lines += "<str>me to plead with Necrovarus in the Phasmatyan Temple to"
            lines += "<str>let any ghost who so wishes pass over into the next world."
            if (progress == 1) {
                lines += ""
                lines += "<navy>I should speak to <maroon>Necrovarus<navy> in the <maroon>Temple<navy>."
            }
            if (progress >= 2) {
                lines += "<str>I pleaded with Necrovarus, to no avail."
            }
            if (progress == 2) {
                lines += ""
                lines += "<navy>I should tell <maroon>Velorina<navy> that <maroon>Necrovarus<navy> will not listen to"
                lines += "<navy>reason."
            }
            if (progress >= 3) {
                lines += "<str>Velorina was crestfallen at Necrovarus' refusal to lift his"
                lines += "<str>ban, and she told me of a woman who fled Port Phasmatys"
                lines += "<str>before the townsfolk died, and to seek her out, as she"
                lines += "<str>may know of a way around Necrovarus' stubbornness."
            }
            if (progress == 3) {
                lines += ""
                lines += "<navy>I should find the <maroon>old woman<navy> that <maroon>Velorina<navy> speaks of."
            }
            if (progress >= 4) {
                lines += "<str>I found the old woman, who told me of an enchantment she"
                lines += "<str>can perform on the Amulet of Ghostspeak, which will then"
                lines += "<str>let me command Necrovarus to do my bidding"
            }
            if (progress == 4) {
                lines += ""
                lines += "<navy>I need to bring the <maroon>old woman<navy> the following items:"
                val book = get("ahoy_given_book", false) || ownsItem("book_of_haricanto")
                val robes = get("ahoy_given_robes", false) || ownsItem("mystical_robes")
                val manual = get("ahoy_given_manual", false) || ownsItem("translation_manual")
                lines += if (book) "<str>The Book of Haricanto" else "<maroon>The Book of Haricanto"
                lines += if (robes) "<str>The Robes of Necrovarus" else "<maroon>The Robes of Necrovarus"
                lines += if (manual) "<str>Something to translate the Book of Haricanto" else "<maroon>Something to translate the Book of Haricanto"

                val toyStage = get("ahoy_subquest_toyboat", 0)
                if (toyStage == 3) {
                    lines += ""
                    lines += "<str>The old woman gave me a toy boat to give to her son, if I"
                    lines += "<str>came across him. I found him and gave him the boat."
                } else if (toyStage >= 1) {
                    lines += ""
                    lines += "<navy>The <maroon>old woman<navy> has given me a <maroon>toy boat<navy> which was made by"
                    lines += "<navy>her son when he was a young boy. It is a model of the same"
                    lines += "<navy>pirate-ship that he sailed away in when he later ran away."
                }

                val bow = get("ahoy_subquest_bow", 0)
                if (bow == 8) {
                    lines += ""
                    lines += "<str>I brought Ak-Haranu an oak longbow signed by Robin,"
                    lines += "<str>Master Bowman, and in exchange he gave me a translation"
                    lines += "<str>manual for the Eastern language."
                } else if (bow >= 1) {
                    lines += ""
                    lines += "<navy>I have agreed to bring <maroon>Ak-Haranu<navy> (a trader from the East)"
                    lines += "<navy>an oak longbow, signed by <maroon>Robin, Master Bowman<navy>. <maroon>Ak-"
                    lines += "<maroon>Haranu<navy> will give me a <maroon>translation manual<navy> for the Eastern"
                    lines += "<navy>language in exchange."
                    lines += "<maroon>Robin<navy> is staying at the inn in <maroon>Port Phasmatys<navy>."
                }

                val petition = get("ahoy_signaturecounter", 0)
                if (petition == 31) {
                    lines += ""
                    lines += "<str>Gravingas, a ghost protestor, asked me to help him with a"
                    lines += "<str>petition to free the ghosts of Phasmatys. I collected 10"
                    lines += "<str>signatures and presented the petition to Necrovarus, but"
                    lines += "<str>he burned it to ashes in my hands."
                } else if (petition >= 1) {
                    lines += ""
                    lines += "<maroon>Gravingas<navy>, a ghost protestor, has asked me to help with a"
                    lines += "<navy>petition to free the ghosts of <maroon>Phasmatys<navy>, and wants me"
                    lines += "<navy>to collect 10 signatures."
                    when (petition) {
                        1 -> lines += "<navy>I haven't got any signatures yet."
                        11 -> {
                            lines += "<navy>I have obtained all the signatures."
                            lines += "<navy>I need to present the petition to <maroon>Necrovarus<navy>."
                        }

                        else -> lines += "<navy>I have obtained <maroon>${petition - 1}<navy> signatures."
                    }
                }
            }
            if (progress >= 5) {
                lines += "<str>I brought the old woman the Book of Haricanto, the Robes"
                lines += "<str>of Necrovarus, and a translation manual."
            }
            if (progress == 5) {
                lines += ""
                lines += "<navy>I need to bring the <maroon>Amulet of Ghostspeak<navy> to the old woman"
                lines += "<navy>so that she can perform the enchantment."
            }
            if (progress >= 6) {
                lines += "<str>The old woman used the items I brought her to perform the"
                lines += "<str>enchantment on the Amulet of Ghostspeak."
            }
            if (progress == 6) {
                lines += ""
                lines += "<navy>I need to return to the <maroon>Temple of Phasmatys<navy> and use the"
                lines += "<navy>enchanted <maroon>Amulet of Ghostspeak<navy> to command <maroon>Necrovarus<navy>."
            }
            if (progress >= 7) {
                lines += "<str>I have commanded Necrovarus to remove his ban."
            }
            if (progress == 7) {
                lines += ""
                lines += "<navy>I need to tell <maroon>Velorina<navy> that I have successfully commanded"
                lines += "<navy><maroon>Necrovarus<navy> to lift his ban."
            }
            if (progress >= 8) {
                lines += "<str>I have told Velorina that Necrovarus has been commanded"
                lines += "<str>to remove his ban, and to allow any ghost who so desires"
                lines += "<str>to pass over into the next plane of existence. Velorina"
                lines += "<str>gave me the Ectophial in return, which I can use to teleport"
                lines += "<str>to the Temple of Phasmatys."
                lines += ""
                lines += "<red>QUEST COMPLETE!"
            }
            questJournal("Ghosts Ahoy", lines)
        }

        playerSpawn {
            if (equipped(EquipSlot.Hat).id == "bedsheet") {
                transform("ahoy_ghost_disguise")
            }

            if (equipped(EquipSlot.Hat).id == "bedsheet_ectoplasm") {
                transform("ahoy_ghost_disguise_green")
            }
        }

        teleportTakeOff("*") {
            if (transform.contains("ahoy_ghost_disguise")) {
                message("Please remove your ghost disguise before teleporting.")
                false
            } else {
                true
            }
        }

        // === Object hooks ===

        objectOperate("Pick", "nettles*") { (target) ->
            if (inventory.isFull()) {
                message("You can't carry any more nettles.")
                return@objectOperate
            }
            anim("human_pickupfloor")
            sound("pick")
            delay(2)
            val gloves = equipment[EquipSlot.Hands.index].def.name.contains("gloves", ignoreCase = true)
            if (gloves) {
                addOrDrop("nettles")
                message("You pick a handful of nettles.")
                target.replace(target.id, ticks = 13)
            } else {
                message("You have been stung by the nettles!")
                sound("stung")
                directHit(20)
            }
        }

        objectOperate("Open", "ahoy_pirate_chest_locked") { (target) ->
            if (target.tile.x == 3619 && target.tile.y == 3545 && target.tile.level == 1 && get("ahoy_subquest_toyboat", 0) == 3) {
                sound("chest_open")
                target.replace("ahoy_pirate_chest_unlocked", ticks = 3)
                if (!ownsItem("map_scrap_1") &&
                    !ownsItem("book_of_haricanto") &&
                    !ownsItem("treasure_map") &&
                    !get("ahoy_given_book", false)
                ) {
                    addOrDrop("map_scrap_1")
                    item(item = "map_scrap_1", text = "You find a piece of a map inside the chest.")
                } else {
                    message("You search the chest but find nothing.")
                }
            } else {
                sound("locked")
                message("The chest is locked.")
            }
        }

        objectOperate("Open", "ahoy_pirate_chest_closed") { (target) ->
            anim("open_chest")
            sound("chest_open")
            target.replace("ahoy_pirate_chest_open", ticks = 500)
            delay(2)
        }

        objectOperate("Close", "ahoy_pirate_chest_open") { (target) ->
            anim("close_chest")
            sound("chest_close")
            target.replace("ahoy_pirate_chest_closed")
            delay(2)
        }

        objectOperate("Search", "ahoy_pirate_chest_open") { (target) ->
            val progress = ghosts_ahoy
            if (target.tile.x == 3606 && target.tile.y == 3564 && target.tile.level == 0) {
                if (progress != 4 ||
                    get("ahoy_given_book", false) ||
                    ownsItem("book_of_haricanto") ||
                    ownsItem("treasure_map") ||
                    ownsItem("map_scrap_3")
                ) {
                    message("You search the chest but find nothing.")
                } else {
                    addOrDrop("map_scrap_3")
                    item(item = "map_scrap_3", text = "You find a piece of a map inside the chest.")
                }
                return@objectOperate
            }
            if (target.tile.x == 3618 && target.tile.y == 3542 && target.tile.level == 0) {
                val killed = get("ahoy_killed_lobster", false)
                if (killed || progress != 4) { // TODO or spawned lobster
                    if (progress != 4 ||
                        get("ahoy_given_book", false) ||
                        ownsItem("book_of_haricanto") ||
                        ownsItem("treasure_map") ||
                        ownsItem("map_scrap_2")
                    ) {
                        message("You search the chest but find nothing.")
                    } else {
                        addOrDrop("map_scrap_2")
                        item(item = "map_scrap_2", text = "You find a piece of a map inside the chest.")
                    }
                } else {
                    message("You are attacked by a giant lobster!!")
                    val lobster = NPCs.add("giant_lobster", Tile(3616, 3543, 0))
                    hint(lobster)
                    lobster.interactPlayer(this, "Attack")
                }
                return@objectOperate
            }
            message("You search the chest but find nothing.")
        }

        objectOperate("Open", "ahoy_necrovarus_coffin_closed") { (target) ->
            message("The coffin creaks open...")
            target.replace("ahoy_necrovarus_coffin_open", ticks = 100)
        }

        objectOperate("Search", "ahoy_necrovarus_coffin_open") {
            if (get("ahoy_given_robes", false) || ownsItem("mystical_robes")) {
                message("Nothing interesting happens.")
                return@objectOperate
            }
            item(
                item = "mystical_robes",
                text = "You take the Robes of Necrovarus from the remains of his mortal body.",
            )
            addOrDrop("mystical_robes")
        }

        objectOperate("Close", "ahoy_necrovarus_coffin_open") { (target) ->
            message("You close the coffin.")
            target.replace("ahoy_necrovarus_coffin_closed", ticks = 3)
        }

        objectOperate("Open", "ahoy_harbour_door_closed") { (target) ->
            if (target.tile.x == 3656 && target.tile.y == 3514 && target.tile.level == 1) {
                if (get("ahoy_templedoor_unlocked", false)) {
                    walkThroughDoor(
                        target = target,
                        enter = tile.x < target.tile.x,
                        enterOffset = -1,
                        exitOffset = 0,
                        openId = "ahoy_harbour_door_opened",
                        openRotation = 1,
                        openOffset = -1,
                        openSound = "creakydoor_open",
                    )
                    return@objectOperate
                }
                val disciple = NPCs.find(tile.regionLevel) { it.id.startsWith("ghost_disciple_port_phasmatys") }
                talkWith(disciple)
                npc<Neutral>("What are you doing going in there?")
                player<Scared>("Err, I was just curious...")
                npc<Neutral>(
                    "Inside that room is a coffin, inside which lie the mortal " +
                        "remains of our most glorious master, Necrovarus. None may enter.",
                )

                return@objectOperate
            }
            Door.openDoor(this, target)
        }

        objectApproach("Search", "ahoy_ship_mast") {
            if (get("ahoy_windspeed", false)) {
                when ((0..2).random()) {
                    0 -> statement(
                        "You can see a tattered flag blowing in the wind.<br>" +
                            "The top half of the flag is coloured ${flagColor(get("ahoy_mast_top", 0))}.",
                    )

                    1 -> statement(
                        "You can see a tattered flag blowing in the wind.<br>" +
                            "The bottom half of the flag is coloured ${flagColor(get("ahoy_mast_bottom", 0))}.",
                    )

                    else -> statement(
                        "You can see a tattered flag blowing in the wind.<br>" +
                            "The skull emblem is coloured ${flagColor(get("ahoy_mast_skull", 0))}.",
                    )
                }
            } else {
                statement(
                    "You can see a tattered flag blowing in the wind.<br>" +
                        "The wind is blowing too hard to make out any details.",
                )
            }
        }

        objectOperate("Talk-to", "ahoy_pirate_captain") {
            statement(
                "The pirate captain ignores you and continues to stare lifelessly at " +
                    "nothing, as he has clearly been dead for some time.",
            )
        }

        objTeleportTakeOff("Climb-down", "ahoy_ghostship_laddertop") { obj, _ ->
            if (obj.tile == Tile(3615, 3545, 2)) {
                message("That ladder doesn't go anywhere very safe.")
                Teleport.CANCEL
            }

            Teleport.CONTINUE
        }

        // === Item-on-object hooks ===

        itemOnObjectOperate("chest_key_ghosts_ahoy", "ahoy_pirate_chest_locked") { interaction ->
            if (interaction.target.tile.x != 3619 ||
                interaction.target.tile.y != 3545 ||
                interaction.target.tile.level != 1
            ) {
                return@itemOnObjectOperate message("Nothing interesting happens.")
            }
            if (get("ahoy_subquest_toyboat", 0) == 3) {
                return@itemOnObjectOperate message("The chest is already unlocked.")
            }
            inventory.remove("chest_key_ghosts_ahoy")
            sound("unlock")
            set("ahoy_subquest_toyboat", 3)
            item(item = "chest_key_ghosts_ahoy", text = "You unlock the chest.")
        }

        itemOnObjectOperate("bone_key_ghosts_ahoy", "ahoy_harbour_door_closed") { interaction ->
            if (interaction.target.tile.x != 3656 ||
                interaction.target.tile.y != 3514 ||
                interaction.target.tile.level != 1
            ) {
                return@itemOnObjectOperate message("Nothing interesting happens.")
            }
            if (get("ahoy_templedoor_unlocked", false)) {
                return@itemOnObjectOperate message("The door is already unlocked.")
            }
            sound("unlock")
            set("ahoy_templedoor_unlocked", true)
            inventory.remove("bone_key_ghosts_ahoy")
            message("You unlock the door.")
        }

        // === Item-on-item hooks ===

        itemOnItem("map_scrap_1", "map_scrap_2") { _, _ -> tryMergeMap() }
        itemOnItem("map_scrap_1", "map_scrap_3") { _, _ -> tryMergeMap() }
        itemOnItem("map_scrap_2", "map_scrap_3") { _, _ -> tryMergeMap() }

        itemOnItem("bedsheet", "bucket_of_slime") { _, _ ->
            inventory.remove("bedsheet")
            inventory.remove("bucket_of_slime")
            inventory.add("bucket")
            inventory.add("bedsheet_ectoplasm")
            message("You dip the bedsheet in the bucket of ectoplasm.")
        }

        // Dye the toy boat's flag. Java: `dyeOnToyBoat` (GhostsAhoy.java:346). Each dye id maps to
        // colour index ((dye_id - 1761) / 2): red=1, yellow=2, blue=3, orange=4, green=5, purple=6.
        // Player picks which of the three flag segments (top / skull / bottom) gets the colour.
        itemOnItem("model_ship_silk", "red_dye") { _, _ -> dyeToyBoat("red", 1) }
        itemOnItem("model_ship_silk", "yellow_dye") { _, _ -> dyeToyBoat("yellow", 2) }
        itemOnItem("model_ship_silk", "blue_dye") { _, _ -> dyeToyBoat("blue", 3) }
        itemOnItem("model_ship_silk", "orange_dye") { _, _ -> dyeToyBoat("orange", 4) }
        itemOnItem("model_ship_silk", "green_dye") { _, _ -> dyeToyBoat("green", 5) }
        itemOnItem("model_ship_silk", "purple_dye") { _, _ -> dyeToyBoat("purple", 6) }

        // Pour a brewed nettle tea into the crone's porcelain cup. Java: `fillPorcelainCup`
        // (GhostsAhoy.java:375). Source container empties (bowl → bowl; cup → empty_cup) and the
        // porcelain cup is filled with the appropriate (plain / milky) tea variant.
        itemOnItem("porcelain_cup", "nettle_tea") { _, _ ->
            sound("liquid")
            inventory.remove("porcelain_cup")
            inventory.remove("nettle_tea")
            inventory.add("bowl")
            inventory.add("cup_of_tea_ghosts_ahoy")
        }
        itemOnItem("porcelain_cup", "milky_nettle_tea") { _, _ ->
            sound("liquid")
            inventory.remove("porcelain_cup")
            inventory.remove("milky_nettle_tea")
            inventory.add("bowl")
            inventory.add("cup_of_milky_tea_ghosts_ahoy")
        }
        itemOnItem("porcelain_cup", "cup_of_tea_nettle_tea") { _, _ ->
            sound("liquid")
            inventory.remove("porcelain_cup")
            inventory.remove("cup_of_tea_nettle_tea")
            inventory.add("empty_cup")
            inventory.add("cup_of_tea_ghosts_ahoy")
        }
        itemOnItem("porcelain_cup", "cup_of_milky_tea") { _, _ ->
            sound("liquid")
            inventory.remove("porcelain_cup")
            inventory.remove("cup_of_milky_tea")
            inventory.add("empty_cup")
            inventory.add("cup_of_milky_tea_ghosts_ahoy")
        }

        // Add milk to the porcelain-cup tea. Java: `addMilkPorcelain` (GhostsAhoy.java:399).
        itemOnItem("cup_of_tea_ghosts_ahoy", "bucket_of_milk") { _, _ ->
            sound("liquid")
            inventory.remove("bucket_of_milk")
            inventory.add("bucket")
            inventory.replace("cup_of_tea_ghosts_ahoy", "cup_of_milky_tea_ghosts_ahoy")
        }

        // Add milk to the standard cup-of-tea (non-porcelain). Java: `addMilkTeaCup`
        // (GhostsAhoy.java:410).
        itemOnItem("cup_of_tea_nettle_tea", "bucket_of_milk") { _, _ ->
            sound("liquid")
            inventory.remove("bucket_of_milk")
            inventory.add("bucket")
            inventory.replace("cup_of_tea_nettle_tea", "cup_of_milky_tea")
        }

        // === Item options ===

        itemOption("Count", "petition_form") {
            val signed = (get("ahoy_signaturecounter", 0) - 1).coerceAtLeast(0)
            when (signed) {
                0 -> message("You haven't got any signatures yet.")
                1 -> message("You have obtained 1 signature.")
                else -> message("You have obtained $signed signatures.")
            }
        }

        itemOption("Drop", "petition_form") {
            inventory.remove("petition_form")
            message("You drop the petition form; it blows away in the wind.")
        }

        itemOption("Repair", "model_ship") {
            if (inventory.contains("silk") &&
                inventory.contains("needle") &&
                inventory.contains("thread") &&
                inventory.contains("knife")
            ) {
                inventory.remove("silk")
                inventory.remove("thread", 1)
                inventory.replace("model_ship", "model_ship_silk")
                item(item = "model_ship_silk", text = "You replace the toy boat's missing flag.")
            } else {
                statement(
                    "You need some silk to replace the flag, something to sew it to the " +
                        "boat, and something to cut the flag to the right size.",
                )
            }
        }

        itemOption("Inspect", "model_ship_silk") {
            item(
                item = "model_ship_silk",
                text = "The top of the flag is ${flagColor(get("ahoy_toy_top", 0))}.<br>" +
                    "The skull emblem is ${flagColor(get("ahoy_toy_skull", 0))}.<br>" +
                    "The bottom of the flag is ${flagColor(get("ahoy_toy_bottom", 0))}.",
            )
        }

        // === NPC death ===

        npcDeath("giant_lobster") {
            val killer = killer
            if (killer is Player) {
                killer["ahoy_killed_lobster"] = true
            }
        }

        itemOption("Dig", "spade") {
            if (tile.x == 3803 && tile.y == 3530 && inventory.contains("treasure_map") && !inventory.contains("book_of_haricanto") && inventory.spaces > 0) {
                anim("human_dig")
                delay(3)
                addOrDrop("book_of_haricanto")
                item(item = "book_of_haricanto", text = "You unearth the Book of Haricanto.")
            }
        }

        itemAdded("ghostspeak_amulet_enchanted", "worn_equipment", EquipSlot.Amulet) {
            set("wearing_ghost_speak_amulet", true)
        }

        itemRemoved("ghostspeak_amulet_enchanted", "worn_equipment", EquipSlot.Amulet) {
            set("wearing_ghost_speak_amulet", false)
        }

        itemAdded("bedsheet", "worn_equipment", EquipSlot.Hat) {
            message("I'll only wear the bedsheet inside Port Phasmatys!")
            transform("ahoy_ghost_disguise")
        }

        itemAdded("bedsheet_ectoplasm", "worn_equipment", EquipSlot.Hat) {
            message("I'll only wear the bedsheet inside Port Phasmatys!")
            transform("ahoy_ghost_disguise_green")
        }

        itemRemoved("bedsheet", "worn_equipment", EquipSlot.Hat) {
            if (this.transform == "ahoy_ghost_disguise" || this.transform == "ahoy_ghost_disguise_green") {
                clearTransform()
            }
        }

        itemRemoved("bedsheet_ectoplasm", "worn_equipment", EquipSlot.Hat) {
            if (this.transform == "ahoy_ghost_disguise" || this.transform == "ahoy_ghost_disguise_green") {
                clearTransform()
            }
        }

        entered("ahoy_shipwreck_top") {
            open("ahoy_windspeed")
            set("ahoy_windspeed", false)
            timers.start("windspeed")
        }

        exited("ahoy_shipwreck_top") {
            close("ahoy_windspeed")
            timers.stop("windspeed")
        }

        timerStart("windspeed") {
            (0..16).random() + 2
        }

        timerTick("windspeed") {
            val lowWind = get("ahoy_windspeed", false)
            set("ahoy_windspeed", !lowWind)
            interfaces.sendText("ahoy_windspeed", "content", if (lowWind) "High" else "Low")
            (0..16).random() + 2
        }
    }

    private suspend fun Player.dyeToyBoat(colourName: String, colourIndex: Int) {
        choice("Which part of the flag do you want to dye?") {
            option("Top half") {
                inventory.remove("${colourName}_dye")
                set("ahoy_toy_top", colourIndex)
                item(item = "model_ship_silk", text = "You dye the top of the flag $colourName.")
            }
            option("Bottom half") {
                inventory.remove("${colourName}_dye")
                set("ahoy_toy_bottom", colourIndex)
                item(item = "model_ship_silk", text = "You dye the bottom of the flag $colourName.")
            }
            option("Skull emblem") {
                inventory.remove("${colourName}_dye")
                set("ahoy_toy_skull", colourIndex)
                item(item = "model_ship_silk", text = "You dye the skull emblem $colourName.")
            }
        }
    }

    private suspend fun Player.tryMergeMap() {
        if (inventory.contains("map_scrap_1") &&
            inventory.contains("map_scrap_2") &&
            inventory.contains("map_scrap_3")
        ) {
            inventory.remove("map_scrap_1")
            inventory.remove("map_scrap_2")
            inventory.remove("map_scrap_3")
            inventory.add("treasure_map")
            item(
                item = "treasure_map",
                text = "You piece the three map scraps together to form a complete map.",
            )
        } else {
            statement("You don't have all the pieces of the map yet.")
        }
    }

    private fun flagColor(value: Int): String = when (value) {
        0 -> "white"
        1 -> "red"
        2 -> "yellow"
        3 -> "blue"
        4 -> "orange"
        5 -> "green"
        6 -> "purple"
        else -> ""
    }
}

var Player.ghosts_ahoy: Int
    get() = get("ahoy_questvar", 0)
    set(value) = set("ahoy_questvar", value)

suspend fun Player.sendGhostsAhoyReward() {
    jingle("quest_complete_1")
    exp(Skill.Prayer, 2400.0)
    addOrDrop("ectophial")
    inc("quest_points", 2)
    AuditLog.event(this, "quest_completed", "ghosts_ahoy")
    ghosts_ahoy = 8
    // Java completeQuest() side-effects: lock subquest varbits to their finished values and
    // randomise the in-world mast flag colours for inspection.
    set("ahoy_given_manual", true)
    set("ahoy_given_robes", true)
    set("ahoy_given_book", true)
    set("ahoy_signaturecounter", 31)
    set("ahoy_subquest_nettletea", 3)
    set("ahoy_subquest_bow", 8)
    set("ahoy_templedoor_unlocked", true)
    set("ahoy_subquest_toyboat", 3)
    set("ahoy_killed_lobster", true)
    clear("ahoy_toy_top")
    clear("ahoy_toy_skull")
    clear("ahoy_toy_bottom")
    set("ahoy_mast_top", (1..6).random())
    set("ahoy_mast_skull", (1..6).random())
    set("ahoy_mast_bottom", (1..6).random())
    refreshQuestJournal()
    questComplete(
        "Ghosts Ahoy",
        "2 Quest Points",
        "2400 Prayer XP",
        "An Ectophial",
        "Free Port Phasmatys entry",
        item = "ectophial",
    )
}

suspend fun Player.checkGhostspeak(): Boolean {
    if (!wearingGhostspeak) {
        npc<Neutral>("Woooo wooo wooooo woooo")
        statement("You cannot understand the ghost.")
        return false
    }
    return true
}

// TODO lobster spawn time
// TODO bedsheet runs when interacting
// TODO you shouldn't have to run up to the mast with object approach
// TODO <br> adds an extra line ?
// TODO bill teach with bedsheet
