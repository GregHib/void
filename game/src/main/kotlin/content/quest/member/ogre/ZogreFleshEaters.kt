package content.quest.member.ogre

import content.entity.combat.killer
import content.entity.gfx.areaGfx
import content.entity.npc.findNearbyNPC
import content.entity.npc.owner
import content.entity.player.bank.ownsItem
import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Mad
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.items
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.entity.player.inv.item.addOrDrop
import content.quest.questCompleted
import content.quest.questJournal
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.clearCamera
import world.gregs.voidps.engine.client.instruction.handle.interactPlayer
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.moveCamera
import world.gregs.voidps.engine.client.turnCamera
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.Settings.Companion.getOrNull
import world.gregs.voidps.engine.entity.character.areaSound
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Teleport
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.hasMax
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectShape
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

class ZogreFleshEaters : Script {

    init {

        // ===== Quest journal =====

        questJournalOpen("zogre_flesh_eaters") {
            val lines = when (val stage = zogre_flesh_eaters) {
                0 -> notStartedJournal()
                14 -> completedJournal()
                else -> startedJournal(stage)
            }
            questJournal("Zogre Flesh Eaters", lines)
        }

        // ===== Movement-triggered cutscene =====
        // The blackened/charred area cutscene plays when the player first walks into the right tile.
        entered("zogre_blackened_area") {
            if (get("thzfe_cut_scene", false)) return@entered
            queue("zogre_blackened_cutscene") { playBlackenedCutscene() }
        }

        // ===== Climb over the smashed barricade =====
        objectOperate("Climb-over", "ogre_barricade_collapsed*") { (target) ->
            val enter = tile.x < target.tile.x
            val direction = if (enter) Direction.EAST else Direction.WEST
            anim("regicide_stepover")
            exactMoveDelay(
                target = tile.copy(x = tile.x + if (enter) 2 else -2),
                delay = 30,
                direction = direction,
            )
            sound("bonewalk")
            delay(2)
        }

        // ===== Sithik's tea cannot be picked up — he scolds the player =====
        takeable("cup_of_tea_zogre_flesh_eaters") { _, telegrab ->
            val sithik = if (get("thzfe_sithik_transformed", false)) "zogre_sithik_ogre" else "zogre_sithik_man"
            if (telegrab) {
                npc<Angry>(
                    sithik,
                    "Hey! What do you think you're doing? Don't go casting that kind of spell " +
                        "anywhere near my tea!  Leave my tea alone you telegrabbing fiend!",
                )
            } else {
                npc<Angry>(sithik, "Hey! What do you think you're doing? Leave my tea alone!")
            }
            null
        }

        // ===== Entrance stairs (down into Jiggig caves) =====
        objectOperate("Climb-down", "ogre_stairs_down") { (target) ->
            message("You climb down the steps.")
            sound("down_stone_stairs")
            open("fade_out")
            delay(3)
            if (target.rotation == 1) {
                tele(2442, 9418, 0)
            } else {
                tele(2477, 9437, 2)
            }
            delay(1)
            open("fade_in")
            delay(3)
        }

        // ===== Exit stairs (back up out of Jiggig caves) =====
        objectOperate("Climb-up", "ogre_stairs") { (target) ->
            message("You climb up the steps.")
            sound("up_stone_stairs")
            open("fade_out")
            delay(3)
            if (target.tile.x == 2443 && target.tile.y == 9417) {
                tele(2446, 9417, 2)
            } else {
                tele(2485, 3045, 0)
            }
            delay(1)
            open("fade_in")
            delay(3)
        }

        // ===== Lectern in the tomb (search for torn page) =====
        objectOperate("Search", "zogre_lecturn") {
            if (zogre_flesh_eaters >= 4) {
                return@objectOperate message("You search the lectern, but find nothing.")
            }
            message("You search the broken down lectern.")
            anim("human_pickupfloor")
            delay(2)
            if (ownsItem("torn_page")) {
                message("You find nothing here this time.")
                return@objectOperate
            }
            addOrDrop("torn_page")
            sound("pick")
            item(item = "torn_page", text = "You find a half torn page...it has spidery writing all over it.")
        }

        // ===== Skeleton corpse (spawns a zombie, then yields the backpack) =====
        objectOperate("Search", "zogre_brentle_skeleton") {
            val skeletonState = get("thzfe_brentle_skele", 0)
            if (skeletonState == 2) {
                if (zogre_flesh_eaters >= 4 ||
                    inventory.contains("ruined_backpack") ||
                    inventory.contains("dragon_inn_tankard")
                ) {
                    return@objectOperate message("You find nothing on the corpse.")
                }
                addOrDrop("ruined_backpack")
                item(item = "ruined_backpack", text = "You find a backpack on the corpse.")
                return@objectOperate
            }

            if (findNearbyNPC("zogre_human_brentle_vahn") != null) {
                return@objectOperate message("You're in mortal danger, you don't have time to search!")
            }


            areaGfx("smokepuff_large", Tile(2442, 9458, 2))
            delay(1)
            set("thzfe_brentle_skele", 1)
            message("Something screams into life right in front of you.")
            sound("disease_hitsplat") // 2388

            val zombie = NPCs.add(
                id = "zogre_human_brentle_vahn",
                tile = Tile(2442, 9458, 2),
                ticks = 1000,
                owner = this
            )
            zombie.interactPlayer(this, "Attack")
        }

        npcDespawn("zogre_human_brentle_vahn") { // TODO why is not suspended....
            areaGfx("smokepuff_large", tile)
            val owner = owner ?: return@npcDespawn
            owner.queue("brentle_zombie_wanders") {
                statement("This mindless zombie loses interest in fighting you and wanders off.")
            }
        }



        // ===== Knife on coffin (force the lock) =====
        itemOnObjectOperate("knife", "zogre_coffin_special") {
            if (get("thzfe_prismsearch", 0) != 1) {
                return@itemOnObjectOperate message("Nothing interesting happens.")
            }
            set("thzfe_prismsearch", 2)
            sound("unlock")
            item(
                item = "knife",
                text = "With some skill you manage to slide the blade along the lock edge and " +
                    "click into place the teeth of the primitive mechanism.",
            )
        }

        // ===== Locked ogre coffin (multi-stage search) =====
        objectOperate("Search", "zogre_coffin_special*") {
            when (val value = get("thzfe_prismsearch", 0)) {
                0, 1 -> {
                    statement(
                        "You search the coffin and find a small geometrically shaped hole in " +
                            "the side. It looks as if this hole was made with a considerable " +
                            "amount of force, maybe the thing which made the hole is still inside?",
                    )
                    if (value == 0) {
                        set("thzfe_prismsearch", 1)
                        statement(
                            "The lock looks quite crude, with some skill and a slender blade, you " +
                                "may be able to force it.",
                        )
                    }
                }
                2 -> liftCoffinLid()
                3 -> {
                    if (inventory.contains("black_prism")) {
                        return@objectOperate message("You find nothing inside this time.")
                    }
                    if (inventory.isFull()) {
                        return@objectOperate statement(
                            "You see something inside, but you have no space in your inventory " +
                                "to store the item.",
                        )
                    }
                    addOrDrop("black_prism")
                    item(item = "black_prism", text = "You find a creepy looking black prism inside.")
                }
            }
        }

        // ===== Zombie NPC (just a scream) =====
        npcOperate("Talk-to", "zogre_zombie") { (target) ->
            target.say("Raaarrrggghhh")
        }

        npcOperate("Talk-to", "pilg") { (target) ->
            npc<Sad>("Dey got me in da belly, mees gutsies feel like had a dead dead dog dinner.")
        }

        npcOperate("Talk-to", "grug") { (target) ->
            npc<Sad>("Ukk...I's dun fer...me's don't feel legsies anymore!")
        }

        // ===== Item examine: ruined backpack (open it) =====
        itemOption("Open", "ruined_backpack") {
            if (inventory.spaces < 3) {
                return@itemOption message(
                    "You don't have enough room in your inventory for the contents of this bag.",
                )
            }
            item(
                item = "ruined_backpack",
                text = "Just before you open the backpack, you notice a small leather patch " +
                    "with the moniker: 'B.Vahn', on it.",
            )
            inventory.remove("ruined_backpack")
            addOrDrop("dragon_inn_tankard")
            addOrDrop("rotten_food")
            addOrDrop("knife")
            message("You find a knife and some rotten food.")
            message("You find an interesting looking tankard.")
            item(item = "dragon_inn_tankard", text = "You find an interesting looking tankard.")
            items("knife", "rotten_food", "You find a knife and some rotten food, the backpack is ripped to shreds.")
        }

        // ===== Item examine handlers (read books, examine items) =====

        itemOption("Read", "torn_page") {
            statement(
                "You don't manage to understand all of it as there is only a half page here. " +
                    "But it seems the spell was used to place a curse on an area and for all " +
                    "time raise the dead.",
            )
            statement("If you look very carefully, you see what looks like a guild emblem.")
        }

        itemOption("Look-at", "black_prism") {
            item(
                item = "black_prism",
                text = "It looks like a smokey black gem of some sort...very creepy. Some " +
                    "magical force must have prevented it from being shattered when it hit " +
                    "the coffin.",
            )
        }

        itemOption("Look-at", "dragon_inn_tankard") {
            item(
                item = "dragon_inn_tankard",
                text = "A stout ceramic tankard with a Dragon Emblem on the side, the words, " +
                    "'Ye Olde Dragon Inn' are inscribed in the bottom.",
            )
        }

        itemOption("Look-at", "zogre_sithik_portrait_signed") {
            item(
                item = "signed_portrait",
                text = "You see an image of Sithik with a message underneath '<blue>I, the " +
                    "bartender of the Dragon Inn, do swear that this <blue>is a true likeness " +
                    "of the wizzy who was talking to <blue>Brentle Vahn, my customer the " +
                    "other day.'",
            )
        }

        itemOption("Read", "necromancy_book") {
            item(
                item = "necromancy_book",
                text = "This book uses very strange language and some incomprehensible symbols. " +
                    "It has a very dark feeling to it. As you're looking through the book, " +
                    "you notice that one of the pages has been torn and half of it is missing.",
            )
        }

        itemOption("Read", "book_of_portraiture") {
            item(
                item = "book_of_portraiture",
                text = "All interested artisans should really consider taking up the hobby of " +
                    "portraiture. To do so, one uses a piece of papyrus on the intended " +
                    "subject to initiate a likeness drawing activity.",
            )
        }

        itemOption("Read", "book_of_ham") {
            statement(
                "You read this book for a while, it seems to be some sort of political " +
                    "manifesto about how the king doesn't do enough to safeguard the citizens " +
                    "of the realm from the monsters that still thrive within the borders. It " +
                    "sends out a rallying cry to all people who would want to stop monsters, " +
                    "to join the HAM movement.",
            )
            player<Quiz>(
                "Hmmm, Sithik must really hate monsters then, I wonder if he hates ogres in " +
                    "particular?",
            )
        }

        // ===== Strange potion on Sithik's tea (ground item interaction) =====
        itemOnFloorItemOperate("zogre_ogre_trans_potion", "cup_of_tea_zogre_flesh_eaters") {
            arriveDelay()
            if (zogre_flesh_eaters != 4) {
                return@itemOnFloorItemOperate message("Nothing interesting happens.")
            }
            zogre_flesh_eaters = 6
            anim("human_pickuptable")
            sound("drip_poison")
            inventory.remove("zogre_ogre_trans_potion")
            addOrDrop("sample_bottle")
            delay(2)
            item(
                item = "zogre_ogre_trans_potion",
                text = "You pour some of the potion into the cup. Zavistic said it may take " +
                    "some time to have an effect.",
            )
        }

        objTeleportTakeOff("Climb-up", "basic_ladder_bottom") { obj, _ ->
            if (obj.tile == Tile(2597, 3107, 0)) {
                if (zogre_flesh_eaters == 6 && get("thzfe_sithik_transformed", 0) == 0) {
                    set("thzfe_sithik_transformed", 1)
                }
            }

            Teleport.CONTINUE
        }

        // ===== Ogre tomb doors (locked, need ogre gate key) =====
        objectOperate("Open", "ogre_cavedoor*") { (target) ->
            enterOgreCaveDoor(target)
        }

        // ===== Plinth in the tomb (Slash Bash spawn / artefact retrieval) =====
        objectOperate("Search", "zogre_stand") { (target) ->
            if (findNearbyNPC("slash_bash")  != null) {
                return@objectOperate message("You're in mortal danger, you don't have time to search!")
            }

            statement("You search the plinth...")
            val progress = zogre_flesh_eaters
            when {
                progress == 14 || inventory.contains("ogre_artefact") -> {
                    message("You find nothing in particular.")
                }
                progress == 12 -> {
                    areaGfx("smokepuff_large", target.tile) // 188
                    areaSound("smokepuff", target.tile) // 1930
                    addOrDrop("ogre_artefact")
                    item(
                        item = "ogre_artefact",
                        text = "An ogre artefact appears in front of you. You quickly put it " +
                            "into your backpack.",
                    )
                }
                else -> {
                    message("Something stirs behind you!")
                    areaGfx("smokepuff_large", Tile(2477, 9444, 0)) // 188
                    areaSound("smokepuff", Tile(2477, 9444, 0)) // 1930
                    NPCs.add(
                        id = "slash_bash",
                        tile = Tile(2477, 9444, 0),
                        ticks = 1000,
                        owner = this
                    )
                }
            }
        }

        npcDeath("slash_bash") {
            val killer = killer as? Player ?: return@npcDeath
            if (killer.zogre_flesh_eaters == 10) {
                killer.zogre_flesh_eaters = 12
            }
        }

        npcDeath("zogre_human_brentle_vahn") {
            val killer = killer as? Player ?: return@npcDeath
            areaGfx("smokepuff_large", tile)
            killer["thzfe_brentle_skele"] = 2
        }
    }

    // ===== Ogre tomb double doors =====
    // The closed pair lives at (x_r, y) + (x_r + 1, y) on level 2.  Each half morphs to its
    // `_inactive` open variant for 2 ticks: the right half rotates from 3 → 0, the left half
    // from 3 → 2 (Java spawns those rotations explicitly on the open temp objects).

    private suspend fun Player.enterOgreCaveDoor(target: GameObject) {
        val enter = tile.y >= target.tile.y
        if (enter && !inventory.contains("ogre_gate_key")) {
            message("These gates are locked, you don't seem to be able to open them.")
            return
        }
        message(if (enter) "You use the Ogre Tomb Key to unlock the door." else "You push the gates open.")
        sound("strangedoor_open")

        walkTo(
            target = Tile(tile.x, target.tile.y - if (enter) 1 else 0, tile.level),
            forceWalk = true,
            noCollision = true,
        )

        val targetIsRight = target.id == "ogre_cavedoorr"
        val partnerId = if (targetIsRight) "ogre_cavedoorl" else "ogre_cavedoorr"
        val partner = GameObjects.findOrNull(target.tile.add(1, 0), partnerId)
            ?: GameObjects.findOrNull(target.tile.add(-1, 0), partnerId)

        GameObjects.add(
            id = "inviswall",
            tile = target.tile,
            shape = ObjectShape.WALL_STRAIGHT,
            rotation = target.rotation,
            ticks = 2,
        )
        if (partner != null) {
            GameObjects.add(
                id = "inviswall",
                tile = partner.tile,
                shape = ObjectShape.WALL_STRAIGHT,
                rotation = partner.rotation,
                ticks = 2,
            )
        }

        target.replace(
            id = "${target.id}_inactive",
            rotation = if (targetIsRight) 0 else 2,
            ticks = 3,
        )
        partner?.replace(
            id = "${partner.id}_inactive",
            rotation = if (targetIsRight) 2 else 0,
            ticks = 3,
        )

        delay(2)
    }

    // ===== Cutscene: blackened, charred area =====

    private suspend fun Player.playBlackenedCutscene() {
        steps.clear()
        set("thzfe_cut_scene", true)
        message("You enter this blackened, charred area - it looks like there's been an explosion!")
        moveCamera(tile = Tile(2445, 9460), height = 400, speed = 4, acceleration = 4)
        turnCamera(tile = Tile(2441, 9459), height = 25, speed = 15, acceleration = 15)
        statement(
            "You enter this blackened, charred area - it looks like some sort of explosion has " +
                "taken place.",
            clickToContinue = false,
        )
        delay(3)

        moveCamera(tile = Tile(2444, 9458), height = 400, speed = 4, acceleration = 4)
        turnCamera(tile = Tile(2441, 9459), height = 25, speed = 15, acceleration = 15)
        delay(3)

        moveCamera(tile = Tile(2442, 9457), height = 400, speed = 4, acceleration = 4)
        turnCamera(tile = Tile(2442, 9459), height = 25, speed = 5, acceleration = 5)
        delay(2)

        moveCamera(tile = Tile(2440, 9458), height = 400, speed = 4, acceleration = 4)
        turnCamera(tile = Tile(2442, 9459), height = 25, speed = 10, acceleration = 10)
        delay(2)

        moveCamera(tile = Tile(2440, 9460), height = 400, speed = 4, acceleration = 4)
        turnCamera(tile = Tile(2442, 9459), height = 25, speed = 15, acceleration = 15)
        delay(2)

        moveCamera(tile = Tile(2442, 9461), height = 400, speed = 4, acceleration = 4)
        turnCamera(tile = Tile(2442, 9459), height = 25, speed = 10, acceleration = 10)
        delay(2)

        moveCamera(tile = Tile(2444, 9460), height = 400, speed = 4, acceleration = 4)
        turnCamera(tile = Tile(2437, 9459), height = 25, speed = 10, acceleration = 10)
        delay(2)

        moveCamera(tile = Tile(2444, 9458), height = 400, speed = 4, acceleration = 4)
        turnCamera(tile = Tile(2437, 9459), height = 25, speed = 15, acceleration = 15)
        delay(1)

        statement(
            "You enter this blackened, charred area - it looks like some sort of explosion has " +
                "taken place.",
        )
        clearCamera()
    }

    // ===== Coffin lid lifting (the "Urrrgggg" sequence) =====

    private suspend fun Player.liftCoffinLid() {
        if (inventory.isFull()) {
            statement(
                "You start to lift the lid and see something inside, but you have no space in " +
                    "your inventory to store the item.",
            )
            return
        }
        statement(
            "The lid looks heavy, but now that you've unlocked it, you may be able to lift it. " +
                "You prepare yourself.",
        )
        say("Urrrgggg.")
        player<Mad>("Urrrgggg.", clickToContinue = false)
        delay(3)
        say("Aarrrgghhh!")
        player<Mad>("Aarrrgghhh!", clickToContinue = false)
        delay(3)
        if ((0..1).random() == 0) {
            levels.drain(Skill.Strength, 2)
            statement(
                "You struggle, but just get weakened from your experience. Perhaps you should " +
                    "try again after you've recovered from the effort?",
            )
        } else {
            set("thzfe_prismsearch", 3)
            sound("coffin_open")
            say("Raarrrggggg! Yes!")
            player<Mad>("Raarrrggggg! Yes!", clickToContinue = false)
            delay(2)
            statement("You eventually manage to lift the lid.")
        }
    }

    // ===== Journal =====

    private fun Player.notStartedJournal(): List<String> {
        fun req(met: Boolean, text: String) = if (met) "<str>$text" else "<maroon>$text"
        return listOf(
            "<navy>I can start this quest by talking to <maroon>Grish<navy> at the Ugrish",
            "<navy>ceremonial dance place called <maroon>Jiggig<navy>.",
            "<navy>To start this <maroon>quest<navy> I should complete these quests:-",
            req(questCompleted("jungle_potion"), "Jungle Potion."),
            req(questCompleted("big_chompy_bird_hunting"), "Big Chompy Bird Hunting."),
            "<navy>It would help if I had the following skill levels:-",
            req(has(Skill.Ranged, 30), "Ranged level: 30"),
            req(has(Skill.Fletching, 30), "Fletching level: 30"),
            req(has(Skill.Smithing, 4), "Smithing level: 4"),
            req(has(Skill.Herblore, 4), "Herblore level: 4"),
            "<navy>Must be able to defeat a <maroon>level 111<navy> foe.",
        )
    }

    private fun completedJournal(): List<String> = listOf(
        "<str>I talked to an ogre called Grish who asked me to look into",
        "<str>the problem. After some searching around in a tomb, I",
        "<str>found some clues which pointed me to the human",
        "<str>habitation of Yannile.",
        "<str>With the help of Zavistic Rarve, the grand secretary of",
        "<str>the Wizards guild I was able to piece the clues together",
        "<str>and discover that a Wizard named 'Sithik Ints' was",
        "<str>responsible.",
        "<str>Unfortunately I couldn't remove the curse from the area,",
        "<str>however, I was able to return some important artefacts to",
        "<str>Grish, who can now set up a new ceremonial dance area for",
        "<str>the ogres of Gu' Tanoth.",
        "<str>Sithik Ints also told me how to make Brutal arrows which are",
        "<str>more effective against Zogres, and he also told me how to",
        "<str>make a disease balm.",
        "",
        "<red>QUEST COMPLETE!",
    )

    private fun Player.startedJournal(stage: Int): List<String> {
        val list = mutableListOf<String>()
        val varbit = get("thzfe_prismsearch", 0)

        // ===== Stage 2+ — initial Grish conversation =====
        if (stage < 3) {
            list += "<navy>I started this quest by talking to <maroon>Grish<navy>, he asked me to"
            list += "<navy>check out the underground area where some <maroon>Zombie ogres"
            list += "<navy>(Zogres)<navy> were coming from."
            list += ""
            list += "<navy>I have to <maroon>find a way into<navy> the <maroon>ceremonial dance area<navy> and"
            list += "<navy>then <maroon>underground<navy>"
        } else {
            list += "<str>I started this quest by talking to Grish, he asked me to"
            list += "<str>check out the underground area where some Zombie ogres"
            list += "<str>(Zogres) were coming from."
            list += "<str>I have to find a way into the ceremonial dance area and"
            list += "<str>then underground."
        }

        // ===== Stage 3+ — past the barricade =====
        if (stage >= 3) {
            list += "<str>I persuaded a guard to let me past, I only had to mention"
            list += "<str>Grish's name and the guard smashed the barricade down. I"
            list += "<str>can enter now."

            // Sub-stages tracked by the sithik_intro varbit
            if (varbit >= 4 || stage >= 4) {
                list += "<str>The guard has smashed the barricade down. I can enter"
                list += "<str>now. I need to find out what happened here"
            } else if (varbit >= 1) {
                list += "<navy>I need to find out what happened here."
            }

            if (varbit >= 2) {
                list += "<str>I have searched a coffin, it has a funny looking hole at the"
                list += "<str>side."
            }
            if (varbit >= 3) {
                list += "<str>I have forced the lock on a coffin, maybe I can open it"
                list += "<str>now?"
            }
            if (varbit >= 4) {
                list += "<str>I've opened the coffin and retrieved a black prism, this"
                list += "<str>may be useful."
                list += "<str>I found a half torn page from a necromantic spell book,"
                list += "<str>maybe this is a clue?"
            }
            if (varbit >= 5) {
                list += "<str>I have shown the prism to the grand secretary of the"
                list += "<str>wizards guild."
            }

            // Tankard sub-thread (only at quest progress 3)
            if (inventory.contains("dragon_inn_tankard") && stage == 3) {
                if (get("thzfe_showntankard", false)) {
                    list += "<str>I killed a human zombie which dropped a backpack. The"
                    list += "<str>backpack had the name 'B. Vahn' on it, inside the backpack"
                    list += "<str>I found a tankard."
                    if (inventory.contains("signed_portrait")) {
                        list += "<str>The Dragon Inn Innkeeper says the tankard belongs to one"
                        list += "<str>of his locals called Brentle Vahn. He was seen talking to a"
                        list += "<str>wizard the other day."
                    } else {
                        list += "<navy>The 'Dragon Inn' <maroon>Innkeeper<navy> says the tankard belongs to"
                        list += "<navy>one of his locals called <maroon>Brentle Vahn<navy>. He was seen talking"
                        list += "<navy>to a <maroon>wizard<navy> the other day."
                    }
                } else {
                    list += "<navy>I killed a <maroon>human zombie<navy> which dropped a <maroon>backpack<navy>. The"
                    list += "<navy>backpack had the name '<maroon>B. Vahn<navy>' on it, inside the backpack"
                    list += "<navy>I found a <maroon>tankard<navy>."
                }
            }
        }

        // Current-state hint based on varbit (overrides earlier <str> versions for the live state)
        if (stage == 3 && varbit < 4) {
            when (varbit) {
                0 -> list += "<navy>I need to find out what happened here."
                1 -> {
                    list += "<navy>I have searched a <maroon>coffin<navy>, it has a funny looking <maroon>hole<navy> at the"
                    list += "<navy>side."
                }
                2 -> {
                    list += "<navy>I have forced the <maroon>lock<navy> on a <maroon>coffin<navy>, maybe I can <maroon>open<navy> it"
                    list += "<navy>now?"
                }
                3 -> {
                    if (inventory.contains("black_prism")) {
                        list += "<navy>I've <maroon>opened<navy> the <maroon>coffin<navy> and retrieved a <maroon>black prism<navy>, this"
                        list += "<navy>may be useful."
                    } else {
                        list += "<navy>I've managed to <maroon>lift the lid<navy> on the <maroon>coffin<navy>, it was quite"
                        list += "<navy><maroon>heavy<navy>! Maybe there's <maroon>something<navy> inside the <maroon>coffin<navy>?"
                    }
                }
            }
        }

        if (stage == 3 && varbit == 4) {
            list += "<navy>I have shown the <maroon>prism<navy> and the <maroon>necromantic page<navy> to"
            list += "<navy><maroon>Zavistic Rarve<navy>. He's told me about a <maroon>wizard<navy> named <maroon>Sithik"
            list += "<navy>Ints<navy> who might have some information."
        }

        if (stage == 3 && varbit == 5) {
            list += "<navy>I've spoken to <maroon>Sithik<navy>, I need to see if he was <maroon>involved<navy> in"
            list += "<navy>some way."
            when {
                inventory.contains("signed_portrait") -> {
                    list += "<navy>I've got a <maroon>signed portrait<navy> of <maroon>Sithik<navy>, this may help to"
                    list += "<navy>convince <maroon>Zavistic Rarve<navy>."
                }
                inventory.contains("good_portrait") || inventory.contains("bad_portrait") -> {
                    list += "<navy>I've made a <maroon>portrait<navy> of <maroon>Sithik<navy>...not sure what this will do?"
                }
                inventory.contains("book_of_portraiture") -> {
                    list += "<navy>I've found a <maroon>book<navy> on <maroon>portraiture<navy>...what does this prove?"
                }
            }
            if (inventory.contains("book_of_ham")) {
                list += "<navy>I've found a <maroon>book<navy> on <maroon>HAM philosophy<navy>...what does this"
                list += "<navy>prove?"
            }
            if (inventory.contains("necromancy_book")) {
                list += "<navy>I've found a <maroon>necromantic book<navy>...what does this prove?"
            }
        }

        // ===== Stage 4+ — Zavistic gives the potion =====
        if (stage >= 4) {
            list += "<str>I've spoken to Sithik, I need to see if he was involved with"
            list += "<str>the Undead Ogres at 'Jiggig' in some way."
            list += "<str>I talked to Zavistic Rarve regarding the prism and the torn"
            list += "<str>page, he gave some information on a student called Sithik"
            list += "<str>Ints, he may know more about what's happening here."

            if (stage >= 6) {
                list += "<str>Zavistic has given me some sort of potion, apparently I"
                list += "<str>need to give it to Sithik."
            } else if (inventory.contains("zogre_ogre_trans_potion")) {
                list += "<navy><maroon>Zavistic<navy> has given me some sort of <maroon>potion<navy>, apparently I"
                list += "<navy>need to give it to <maroon>Sithik<navy>."
            } else {
                list += "<navy><maroon>Zavistic<navy> gave me some sort of <maroon>potion<navy>, but I don't have it"
                list += "<navy>on me anymore. Apparently I need to give some to <maroon>Sithik<navy>."
            }
        }

        // ===== Stage 6+ — potion put in tea =====
        if (stage >= 6) {
            if (get("thzfe_sithik_transformed", 0) >= 1) {
                list += "<str>I have put some of the potion into Sithik's tea, the potion"
                list += "<str>will take some time to act. Perhaps I should get out of here"
                list += "<str>in case there are any side effects?"
                if (stage == 6) {
                    list += "<navy>Perhaps I should go and check on <maroon>Sithik<navy> now?"
                }
            } else {
                list += "<navy>I have put some of the <maroon>potion<navy> into <maroon>Sithik's tea<navy>, the <maroon>potion"
                list += "<navy>will take some time to act. Perhaps I should <maroon>get out of here<navy>"
                list += "<navy>in case there are any <maroon>side effects<navy>?"
            }
        }

        // ===== Stage 8 — Sithik turned into ogre =====
        if (stage == 8) {
            list += "<str>I came back into Sithik's room to find that he had been"
            list += "<str>turned into an Ogre!"
            list += "<navy><maroon>Sithik<navy> has told me that there is no way I can <maroon>remove<navy> the"
            list += "<navy>effects of the <maroon>necromantic curse spell<navy> from the <maroon>Jiggig<navy>"
            list += "<navy>area. I'll have to go back and let <maroon>Grish<navy> know."

            if (get("thzfe_makebrutalarrow", false)) {
                list += "<navy><maroon>Sithik<navy> has told me how to make '<maroon>brutal arrows<navy>', which"
                list += "<navy>should be more <maroon>effective<navy> against <maroon>Zogres<navy>."
            }

            if (get("thzfe_makecuredisease", false)) {
                list += "<navy><maroon>Sithik<navy> has given me some pointers on how I can make a"
                list += "<navy><maroon>cure disease potion<navy>, though I'm still not sure exactly which"
                list += "<navy><maroon>herbs<navy> I should use."
            }
        }

        // ===== Stage 10+ — ogres want artefacts =====
        if (stage >= 10) {
            list += "<str>I came back into Sithik's room to find that he had been"
            list += "<str>turned into an Ogre!"
            list += "<str>Sithik has told me how to make 'brutal arrows', which"
            list += "<str>should be more effective against Zogres."
            list += "<str>Sithik has given me some pointers on how I can make a"
            list += "<str>cure disease potion, though I'm still not sure exactly which"
            list += "<str>herbs I should use."
            list += "<str>I've told Grish to relocate the dance area, but he needs"
            list += "<str>me to get something from the tomb to so that he can do"
            list += "<str>this."
            list += "<navy>I need to go back into the <maroon>tomb<navy> and look for some <maroon>'old'"
            list += "<navy>items<navy> that <maroon>Grish<navy> has asked for."
        }

        // ===== Stage 12+ — killed Slash Bash =====
        if (stage >= 12) {
            list += "<str>I've killed a monster called Slash Bash...it was a huge"
            list += "<str>Zogre!"
            if (inventory.contains("ogre_artefact")) {
                list += "<str>Slash Bash was wearing some odd artefacts, I can only"
                list += "<str>assume that these were what Grish wanted."
                list += "<navy>I have some <maroon>artefacts<navy> which I recovered from a <maroon>huge"
                list += "<navy>Zogre<navy> called <maroon>Slash Bash<navy>. I should return them to <maroon>Grish<navy>."
            } else {
                list += "<navy><maroon>Slash Bash<navy> was wearing some odd <maroon>artefacts<navy>, I can only"
                list += "<navy>assume that these were what <maroon>Grish<navy> wanted."
            }
        }

        return list
    }
}

var Player.zogre_flesh_eaters: Int
    get() = get("zogre_flesh_eaters", 0)
    set(value) {
        val current = get("zogre_flesh_eaters", 0)
        message("zogre_flesh_eaters $current -> $value")
        set("zogre_flesh_eaters", value)
    }



// disease doesn't work
