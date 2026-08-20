package content.quest.member.creature_of_fenkenstrain

import content.entity.obj.door.Door
import content.entity.obj.door.closeDoor
import content.entity.obj.door.enterDoor
import content.entity.obj.door.openDoor
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.entity.player.inv.item.addOrDrop
import content.entity.player.modal.book.openBook
import content.quest.letterScroll
import content.quest.quest
import content.quest.questCompleted
import content.quest.questJournal
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.male
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.hasMax
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectShape
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.type.Tile

class CreatureOfFenkenstrain : Script {

    init {

        questJournalOpen("creature_of_fenkenstrain") {
            val quest = quest("creature_of_fenkenstrain")
            val progress = FENK_STAGES.indexOf(quest).coerceAtLeast(0)
            val lines = mutableListOf<String>()
            if (progress == 0) {
                if (get("fenk_read_signpost", false)) {
                    lines += "<str>I read the signpost in Canifis, which tells of a butler"
                    lines += "<str>position that is available at the castle to the northeast."
                    lines += "<navy>I should go up to the castle and speak to <maroon>Dr Fenkenstrain<navy>."
                } else {
                    lines += "<navy>I can start this quest by reading the signpost in the"
                    lines += "<navy>centre of <maroon>Canifis."
                    lines += "<navy>I must be able to defeat a <maroon>level 51 monster<navy>, and need the"
                    lines += "<navy>following skill levels:"
                    val craft = hasMax(Skill.Crafting, 20, false)
                    val thiev = hasMax(Skill.Thieving, 25, false)
                    val priest = questCompleted("priest_in_peril")
                    val ghost = questCompleted("the_restless_ghost")
                    lines += if (craft) "<str>Level 20 Crafting" else "<navy>Level 20 <maroon>Crafting"
                    lines += if (thiev) "<str>Level 25 Thieving" else "<navy>Level 25 <maroon>Thieving"
                    lines += "<navy>I also need to have completed the following quests:"
                    lines += if (priest) "<str>Priest in Peril" else "<maroon>Priest in Peril"
                    lines += if (ghost) "<str>Restless Ghost" else "<maroon>Restless Ghost"
                    if (craft && thiev && priest && ghost) {
                        lines += "<navy>I have all the requirements to start this quest."
                    }
                }
            } else {
                lines += "<str>I read the signpost in Canifis, which tells of a butler"
                lines += "<str>position that is available at the castle to the northeast."
                lines += "<str>I spoke to Fenkenstrain, who wanted me to find him some"
                lines += "<str>body parts so that he could build a creature."
                if (progress == 1) {
                    lines += "<navy>I need to find these body parts for <maroon>Fenkenstrain<navy>:"
                    lines += if (get("fenk_arms", false)) "<str>a pair of arms" else "<navy>a pair of <maroon>arms"
                    lines += if (get("fenk_legs", false)) "<str>a pair of legs" else "<navy>a pair of <maroon>legs"
                    lines += if (get("fenk_torso", false)) "<str>a torso" else "<navy>a <maroon>torso"
                    lines += if (get("fenk_head", false)) "<str>a head" else "<navy>a <maroon>head"
                    lines += ""
                    lines += "<navy>Apparently the soil of <maroon>Morytania<navy> has a unique quality"
                    lines += "<navy>which preserves the bodies of the dead better than"
                    lines += "<navy>elsewhere, so perhaps I should look at the graves in the"
                    lines += "<navy>local area."
                }
                if (progress >= 2) {
                    lines += "<str>I gave a torso, some arms and legs, and a head to"
                    lines += "<str>Fenkenstrain, who then wanted a needle and 5 lots of"
                    lines += "<str>thread, so that he could sew the bodyparts together and"
                    lines += "<str>create his creature."
                }
                if (progress == 2) {
                    lines += if (get("fenk_needle", false)) {
                        "<str>I have given Fenkenstrain a needle."
                    } else {
                        "<navy>I need to bring <maroon>Fenkenstrain<navy> a <maroon>needle."
                    }
                    val thread = get("fenk_threads_given", 0)
                    lines += if (thread == 5) {
                        "<str>I brought Fenkenstrain 5 quantities of thread."
                    } else {
                        "<navy>I need to bring Fenkenstrain <maroon>${5 - thread}<navy> quantities of <maroon>thread."
                    }
                }
                if (progress >= 3) {
                    lines += "<str>I brought Fenkenstrain a needle and 5 quantities of"
                    lines += "<str>thread."
                }
                if (progress == 3) {
                    lines += "<maroon>Fenkenstrain<navy> has ordered me to repair the lightning"
                    lines += "<navy>conductor."
                }
                if (progress >= 4) {
                    lines += "<str>I repaired the lightning conductor, and Fenkenstrain"
                    lines += "<str>brought the Creature to life."
                }
                if (progress == 4) {
                    lines += "<navy>I should go to <maroon>Fenkenstrain<navy> to see if he is pleased with my"
                    lines += "<navy>work on the lightning conductor."
                }
                if (progress >= 5) {
                    lines += "<str>The Creature went on a rampage, and Fenkenstrain sent"
                    lines += "<str>me up to the Tower to destroy it."
                }
                if (progress == 5) {
                    lines += "<maroon>Fenkenstrain<navy> has ordered me to go and destroy the"
                    lines += "<navy>Creature."
                }
                if (progress >= 6) {
                    lines += "<str>The Creature convinced me to stop Fenkenstrain's"
                    lines += "<str>experiments once and for all, and has told me the true"
                    lines += "<str>history of Fenkenstrain's treachery."
                }
                if (progress == 6) {
                    lines += "<navy>I must find a way to stop <maroon>Fenkenstrain<navy>'s experiments."
                }
                if (progress >= 7) {
                    lines += "<str>I stole Fenkenstrain's Ring of Charos, and he released me"
                    lines += "<str>from his service"
                    lines += ""
                    lines += "<red>QUEST COMPLETE!"
                }
            }
            questJournal("Creature of Fenkenstrain", lines)
        }

        objectOperate("Read", "fenk_signpost") {
            val started = fenkStage >= 1
            if (started) {
                statement("The signpost has a note pinned onto it. The note says:<br>'~~~Braindead Butler Position Filled~~~<br>****No Further Applicants Please****'")
            } else {
                if (!get("fenk_read_signpost", false)) {
                    set("fenk_read_signpost", true)
                }
                statement("The signpost has a note pinned onto it. The note says:<br>'----Braindead Butler Wanted----<br>Gravedigging skills essential - Hunchback advantageous<br>See Dr Fenkenstrain at the castle NE of Canifis'")
            }
        }

        objectOperate("Wind", "fenk_clock") {
            if (!inventory.contains("fenk_letter") && !get("fenk_wound_clock", false)) {
                item(
                    item = "fenk_letter",
                    text = "As you wind the old clock a letter falls out. Judging by the thick covering of dust it must have been here for some time.",
                )
                addOrDrop("fenk_letter")
                set("fenk_wound_clock", true)
            }
            message("You wind the old clock.")
        }

        objectOperate("Take-from", "fenk_canepile") {
            message("You take a garden cane from the pile.")
            addOrDrop("fenk_cane")
        }

        objectOperate("Search", "fenk_chest_open") {
            if (get("fenk_unlocked_cavern", false) || inventory.contains("fenk_mausoleum_key")) {
                message("The chest is empty.")
                return@objectOperate
            }
            item(item = "fenk_mausoleum_key", text = "You take a key out of the chest.")
            addOrDrop("fenk_mausoleum_key")
        }

        objectOperate("Close", "fenk_chest_open") {
            message("The hinges are filled with rust and won't budge.")
        }

        objectOperate("Open", "fenk_broomcupboard") { (target) ->
            anim("human_opencupboard")
            sound("cupboard_open")
            target.replace("fenk_broomcupboard_open", ticks = 500)
        }

        objectOperate("Shut", "fenk_broomcupboard_open") { (target) ->
            anim("human_opencupboard")
            sound("cupboard_close")
            target.replace("fenk_broomcupboard")
        }

        objectOperate("Search", "fenk_broomcupboard_open") {
            if (inventory.contains("fenk_brush0")) {
                message("You search the cupboard but find nothing.")
                return@objectOperate
            }
            item(item = "fenk_brush0", text = "You find a garden brush in the cupboard.")
            addOrDrop("fenk_brush0")
        }

        objectOperate("Search", "fenk_bookcase") { (target) ->
            if (fenkStage < 1) {
                message("It is a bookcase full of books.")
                return@objectOperate
            }
            if (target.tile.x == 3555 && target.tile.y == 3558) {
                eastBookcase()
            } else if (target.tile.x == 3542 && target.tile.y == 3558) {
                westBookcase()
            }
        }

        objectOperate("Push", "fenk_coffin") { (target) ->
            if (graveInscription(target.tile)) {
                return@objectOperate
            }
            if (target.tile.x == 3574 && target.tile.y == 3526) {
                message("This coffin is incredibly heavy, and does not budge.")
                return@objectOperate
            }
            if (target.tile.x == 3578 && target.tile.y == 3527) {
                if (get("fenk_coffin", false)) {
                    sound("scrape")
                    target.anim("fenk_capstone_moves")
                    delay(3)
                    tele(3577, 9927, 0)
                    delay(1)
                } else {
                    message("This coffin is incredibly heavy, and does not budge.")
                }
                return@objectOperate
            }
            if (target.tile.x == 3505 && target.tile.y == 3571) {
                sound("scrape")
                target.anim("fenk_capstone_moves")
                delay(3)
                tele(3504, 9969, 0)
                delay(1)
                return@objectOperate
            }
        }

        objectOperate("Search", "fenk_coffin") { (target) ->
            if ((target.tile.x == 3505 && target.tile.y == 3571) ||
                (target.tile.x == 3574 && target.tile.y == 3526)
            ) {
                message("You find nothing remarkable about the memorial stone.")
                return@objectOperate
            }
            if (target.tile.x == 3578 && target.tile.y == 3527) {
                if (get("fenk_coffin", false)) {
                    message("The memorial stone holds a star amulet in place on its lid.")
                } else {
                    message("You find a depression in the memorial stone in the shape of a six-pointed star.")
                }
            }
        }

        objectOperate("Read", "fenk_grave") { (target) ->
            graveInscription(target.tile)
        }

        objectOperate("Read", "fenk_grave_poor") { (target) ->
            graveInscription(target.tile)
        }

        objectOperate("Dig", "fenk_grave") {
            digGrave()
        }

        objectOperate("Dig", "fenk_grave_poor") {
            digGrave()
        }

        itemOption("Dig", "spade") {
            if (atGraveTIle()) {
                digGrave()
            }
        }

        objectOperate("Climb-up", "tithe_roof_g") { (target) ->
            // Object 1757 is verified correct: a Ladder placed at exactly these two cave
            //  tiles. Only the name is wrong - loctypes.txt has 1750-1762 as Tithe Farm
            //  roofs, so it carries no 634-era name for this id.
            if (target.tile.x == 3578 && target.tile.y == 9927) {
                tele(3577, 3527, 0)
            } else if (target.tile.x == 3504 && target.tile.y == 9970) {
                tele(3504, 3569, 0)
            }
        }

        itemOnObjectOperate("fenk_star_amulet", "fenk_coffin") { interaction ->
            if (interaction.target.tile.x != 3578 || interaction.target.tile.y != 3527) {
                return@itemOnObjectOperate message("Nothing interesting happens.")
            }
            inventory.remove("fenk_star_amulet")
            set("fenk_coffin", true)
            item(
                item = "fenk_star_amulet",
                text = "The star amulet fits exactly into the depression on the coffin lid.",
            )
        }

        objectOperate("Open", "fenk_mausoleum_door_closed") { (target) ->
            if (!get("fenk_unlocked_cavern", false) && !inventory.contains("fenk_mausoleum_key")) {
                message("The door is locked.")
                return@objectOperate
            }
            if (inventory.contains("fenk_mausoleum_key")) {
                inventory.remove("fenk_mausoleum_key")
                set("fenk_unlocked_cavern", true)
            }
            enterDoor(target)
        }

        itemOnObjectOperate("fenk_mausoleum_key", "fenk_mausoleum_door_closed") { interaction ->
            if (get("fenk_unlocked_cavern", false)) {
                return@itemOnObjectOperate message("The gate is already unlocked.")
            }
            inventory.remove("fenk_mausoleum_key")
            set("fenk_unlocked_cavern", true)
            enterDoor(interaction.target)
        }

        objectOperate("Open", "fenk_tower_door_closed") { (target) ->
            if (!get("fenk_unlocked_tower", false) && !inventory.contains("fenk_tower_key")) {
                message("The door is locked.")
                return@objectOperate
            }
            if (inventory.contains("fenk_tower_key")) {
                inventory.remove("fenk_tower_key")
                set("fenk_unlocked_tower", true)
            }
            enterDoor(target)
        }

        objectOperate("Open", "fenk_shed_door_closed") { (target) ->
            if (!get("fenk_unlocked_shed", false) && !inventory.contains("fenk_shed_key")) {
                message("The door is locked.")
                return@objectOperate
            }
            if (inventory.contains("fenk_shed_key")) {
                inventory.remove("fenk_shed_key")
                set("fenk_unlocked_shed", true)
            }
            enterDoor(target)
        }

        objectOperate("Open", "fenk_door_closed,fenk_door_mirror_closed") { (target) ->
            if (target.tile.level == 0) {
                Door.openDoor(this, target)
                return@objectOperate
            }

            val enter = tile.y >= target.y

            if (fenkStage < 3) {
                message("The door to the lightning conductor is locked.")
                return@objectOperate
            }

            sound("door_open")
            walkTo(
                target = Tile(tile.x, if (enter) 3542 else 3543, 1),
                forceWalk = true,
                noCollision = true,
            )
            val leftDoor = GameObjects.find(Tile(3548, 3543, 1), "fenk_door_mirror_closed")
            leftDoor.replace(
                id = "fenk_door_mirror_opened",
                tile = Tile(3548, 3542, 1),
                shape = leftDoor.shape,
                rotation = 0,
                ticks = 3,
            )
            val rightDoor = GameObjects.find(Tile(3549, 3543, 1), "fenk_door_closed")
            rightDoor.replace(
                id = "fenk_door_opened",
                tile = Tile(3549, 3542, 1),
                shape = leftDoor.shape,
                rotation = 2,
                ticks = 3,
            )

            GameObjects.add(
                id = "inviswall",
                tile = Tile(3549, 3543, 1),
                shape = ObjectShape.WALL_STRAIGHT,
                rotation = 3,
                ticks = 3,
            )
            GameObjects.add(
                id = "inviswall",
                tile = Tile(3548, 3543, 1),
                shape = ObjectShape.WALL_STRAIGHT,
                rotation = 3,
                ticks = 3,
            )
            delay(2)
        }

        objectOperate("Open", "fenk_door_mirror_opened,fenk_door_opened") { (target) ->
            if (target.tile.level == 1) {
                return@objectOperate message("The door seems to be stuck.")
            }

            Door.closeDoor(this, target)
        }

        objectOperate("Repair", "fenk_conductor_broken") { (target) ->
            repairConductor(target)
        }

        itemOnObjectOperate("fenk_conductor", "fenk_conductor_broken") { (target) ->
            repairConductor(target)
        }

        itemOnObjectOperate("fenk_brush1,fenk_brush2,fenk_brush3", "fenk_fireplace") { interaction ->
            anim("fenk_human_poke")
            sound("brushing")
            val brushId = interaction.item.id
            val westFire = interaction.target.tile.x == 3544 && interaction.target.tile.y == 3555
            if (brushId != "fenk_brush3") {
                message("You stick the garden brush up the chimney, but it is not long enough to clear the blockage.")
                return@itemOnObjectOperate
            }
            if (westFire && !inventory.contains("fenk_lightning_mould") && fenkStage < 4) {
                addOrDrop("fenk_lightning_mould")
                item(item = "fenk_lightning_mould", text = "A lightning conductor mould falls down out of the chimney.")
            } else {
                message("You give the chimney a jolly good clean out.")
            }
        }

        itemOnItem("fenk_marble_amulet", "fenk_obsidian_amulet") { _, _ ->
            inventory.remove("fenk_marble_amulet")
            inventory.remove("fenk_obsidian_amulet")
            inventory.add("fenk_star_amulet")
            item(item = "fenk_star_amulet", text = "The marble and obsidian amulets snap together tightly to form a six-pointed amulet.")
        }

        itemOnItem("fenk_head_empty", "fenk_brain") { _, _ ->
            inventory.remove("fenk_head_empty")
            inventory.remove("fenk_brain")
            inventory.add("fenk_head_full")
            item(item = "fenk_head_full", text = "You squeeze the pickled brain into the decapitated head.")
        }

        itemOnItem("fenk_cane", "fenk_brush0,fenk_brush1,fenk_brush2,fenk_brush3") { from, to ->
            val brushId = if (from.id == "fenk_cane") to.id else from.id
            extendBrush(brushId)
        }

        itemOption("Read", "fenk_letter") {
            readRologarthLetter()
        }

        canAttack("experiment_dog") {
            if (inventory.contains("fenk_mausoleum_key") || get("fenk_unlocked_cavern", false)) {
                message("You don't have the heart to kill this poor creature again.")
                false
            } else {
                true
            }
        }

        takeable("fenk_brain") { item, telegrab ->
            if (item.tile == Tile(3504, 3576) && !telegrab) {
                val roavar = NPCs.find(tile.regionLevel) { it.id.startsWith("roavar") }
                talkWith(roavar)
                face(roavar)
                roavar.face(this)
                if (inventory.contains(item.id)) {
                    npc<Neutral>("You can leave that alone, my friend. I've already sold you one of your own- eat that. I can't afford to give away freebies in this business!")
                } else {
                    npc<Neutral>("You're interested in our speciality, I see. Would you like to buy some?")
                    player<Neutral>("What exactly is in the jar?")
                    npc<Neutral>("Pickled brain, my friend. Only 50 gold to you.")
                    player<Neutral>("Err...pickled brain from what animal?")
                    npc<Neutral>("Animal? Don't be disgusting, man! No, this is a human brain - only the best for my customers.")
                    if (inventory.count("coins") < 50) {
                        player<Neutral>("That sounds very nice, but I'm afraid I don't have enough gold at the moment.")
                    } else {
                        choice {
                            option<Neutral>("I'll buy one, please.") {
                                inventory.remove("coins", 50)
                                addOrDrop("fenk_brain")
                                npc<Neutral>("A very wise choice, ${if (male) "sir" else "miss"}. Don't eat it all at once, savour every morsel - that's my advice to you.")
                            }
                            option<Neutral>("I'm afraid I'm not really hungry at the moment.")
                        }
                    }
                }
                null
            } else {
                item.id
            }
        }
    }

    private suspend fun Player.repairConductor(target: GameObject) {
        if (fenkStage > 3) {
            message("The lightning conductor is now beyond repair.")
            return
        }
        if (!inventory.remove("fenk_conductor")) {
            message("You don't have anything to repair the conductor with.")
            return
        }
        target.replace(id = "fenk_conductor_repaired", ticks = 5)
        sound("lightning")
        set("creature_of_fenkenstrain", "creature_alive")
        statement("You repair the lightning conductor not one moment too soon - a tremendous bolt of lightning melts the new lightning conductor, and power blazes throughout the castle, if only briefly.")
    }

    private suspend fun Player.eastBookcase() {
        choice("Which book would you like to read?") {
            option("Men are from Morytania, Women are from Lumbridge") {
                item(
                    item = "fenk_journal",
                    text = "You discover some fascinating insights into the mind of the male kind.",
                )
            }
            option("Chimney Sweeping on a Budget") {
                openBook("chimney_sweeping_on_a_budget")
                interfaces.sendText("book_long", "page_number_left", "26")
                interfaces.sendText("book_long", "page_number_right", "")
            }
            option("Handy Maggot Avoidance Techniques") {
                sound("bookcasedoor")
                item(
                    item = "fenk_journal",
                    text = "As you pull the book a hidden latch springs into place and the bookcase swings open, revealing a secret compartment.",
                )
                if (inventory.contains("fenk_obsidian_amulet") || inventory.contains("fenk_star_amulet") || get("fenk_coffin", false)) {
                    statement("The secret compartment is empty.")
                } else {
                    addOrDrop("fenk_obsidian_amulet")
                    item(item = "fenk_obsidian_amulet", text = "You find an obsidian amulet in the secret compartment.")
                }
            }
            option("My Family and Other Zombies") {
                val lines = listOf(
                    "The book is a mediocre read.",
                    "This book is appallingly dull.",
                    "This book is mildly amusing.",
                    "This book is a fantastic read.",
                )
                item(item = "fenk_journal", text = lines.random())
            }
        }
    }

    private suspend fun Player.westBookcase() {
        choice("Which book would you like to read?") {
            option("1001 Ways To Eat Fried Gizzards") {
                item(item = "fenk_journal", text = "This book leaves you contemplating vegetarianism.")
            }
            option("Practical Gardening For The Headless") {
                item(
                    item = "fenk_journal",
                    text = "This book has some very enlightening points to make, but you are at a loss to know how anyone without a head could possibly read it.",
                )
            }
            option("Human Taxidermy for Nincompoops") {
                item(
                    item = "fenk_journal",
                    text = "This book seems to have been read hundreds of times, and has scribbles and formulae on every page. One such scribble says 'None good enough - have had to lock them in the caverns...'",
                )
            }
            option("The Joy of Gravedigging") {
                sound("bookcasedoor")
                item(
                    item = "fenk_journal",
                    text = "As you pull the book a hidden latch springs into place and the bookcase swings open, revealing a secret compartment.",
                )
                if (inventory.contains("fenk_marble_amulet") || inventory.contains("fenk_star_amulet") || get("fenk_coffin", false)) {
                    statement("The secret compartment is empty.")
                } else {
                    addOrDrop("fenk_marble_amulet")
                    item(item = "fenk_marble_amulet", text = "You find a marble amulet in the secret compartment.")
                }
            }
        }
    }

    private fun Player.graveInscription(tile: Tile): Boolean {
        val name = graveNames[tile] ?: return false
        message("The grave says:")
        message(" 'Here lies $name - REST IN PEACE.'")
        return true
    }

    private fun Player.extendBrush(brushId: String) {
        if (brushId == "fenk_brush3") {
            message("The brush is too long to attach any more canes.")
            return
        }
        if (!inventory.contains("bronze_wire")) {
            message("You try to attach the canes, but you need something suitable to hold them together.")
            return
        }
        if (!hasMax(Skill.Crafting, 20)) {
            message("You need Level 20 Crafting to attach the cane to the brush.")
            return
        }
        val nextBrush = when (brushId) {
            "fenk_brush0" -> "fenk_brush1"
            "fenk_brush1" -> "fenk_brush2"
            "fenk_brush2" -> "fenk_brush3"
            else -> return
        }
        inventory.remove("bronze_wire")
        inventory.remove("fenk_cane")
        inventory.remove(brushId)
        addOrDrop(nextBrush)
        message("You attach the cane to the brush.")
    }

    private fun Player.readRologarthLetter() {
        letterScroll(
            "A letter",
            listOf(
                "Rologarric,",
                "",
                "I am writing to you, my brother, to ask for",
                "your aid, and to forgive me on behalf of all the",
                "poor souls that I have sacrificed to save my own",
                "neck. I have no doubt that my own soul lies",
                "forfeit, but I would wish to see you once again",
                "before I am cast into the eternal fire. You well",
                "know the history of our noble ancestors, who",
                "have stood against the wave of darkness covering",
                "all of Morytania, protecting the inhabitants of this",
                "castle by sheer force of personality. I, however,",
                "stand lacking in their all too dominating shadow.",
                "I have been weak, my brother, letting Him rule",
                "in my stead.",
                "",
                "You know of whom I speak but dare not name -",
                "my 'doctor' of many years, the very same",
                "creature you sought to escape ten years ago.",
                "He advised me that the power in the South was",
                "rising, and that soon our ancestral home would",
                "fall to the growing evil. He advised me to send a",
                "gift of tribute to the vampires, to satisfy their",
                "hunger for at least a while. I regret to say that",
                "I acceded to this request. I ordered a young",
                "girl out into the Forest to pick mushrooms,",
                "knowing that I was sending her to her death, or",
                "",
                "Of course, soon after my advisor told me that",
                "the vampires were again restive, and to send",
                "another 'gift'. And of course, this I did, to",
                "ensure the safety of everyone in the castle.",
                "",
                "But a vampires' hunger can never be sated.",
                "Every innocent soul I have sent into the Forest",
                "to their doom both strengthens their power and",
                "increases their hunger.",
                "",
                "He knows this. I look into his eyes and see",
                "amusement. In his eyes I see the truth - that he",
                "must have struck a deal with them some time",
                "ago. He sold my people to them, in exchange",
                "for ... this castle.",
                "",
                "I have sent them all to their doom, and now I",
                "fear my own.",
                "",
                "Your brother,",
                "",
                "Rologarth,",
                "",
                "15th Lord of the North Coast",
            ),
        )
    }

    private suspend fun Player.digGrave() {
        arriveDelay()
        if (!inventory.contains("spade")) {
            message("You don't have anything to dig with.")
            return
        }
        anim("human_dig_long")
        message("You start digging...")
        delay(6)
        clearAnim()
        if (fenkStage < 1) {
            message("...but the grave is empty.")
            return
        }
        val (x, y) = tile.x to tile.y
        when {
            x == 3503 && y == 3576 && !inventory.contains("fenk_torso") && !get("fenk_torso", false) -> {
                addOrDrop("fenk_torso")
                item(item = "fenk_torso", text = "... and you unearth a torso.")
            }
            x == 3504 && y == 3576 && !inventory.contains("fenk_arms") && !get("fenk_arms", false) -> {
                addOrDrop("fenk_arms")
                item(item = "fenk_arms", text = "... and you unearth a pair of arms.")
            }
            x == 3505 && y == 3576 && !inventory.contains("fenk_legs") && !get("fenk_legs", false) -> {
                addOrDrop("fenk_legs")
                item(item = "fenk_legs", text = "... and you unearth a pair of legs.")
            }
            x == 3608 && y == 3490 && !inventory.contains("fenk_head_empty") && !get("fenk_head", false) -> {
                addOrDrop("fenk_head_empty")
                item(item = "fenk_head_empty", text = "... and you unearth a decapitated head.")
            }
            else -> message("...but the grave is empty.")
        }
    }

    private fun Player.atGraveTIle(): Boolean = tile in listOf(
        Tile(3503, 3576),
        Tile(3504, 3576),
        Tile(3505, 3576),
        Tile(3608, 3490),
    )

    companion object {
        private val graveNames: Map<Tile, String> = mapOf(
            Tile(3608, 3491) to "Ed Lestwit",
            Tile(3594, 3491) to "Isla Skye",
            Tile(3596, 3479) to "Kandik Kludge",
            Tile(3588, 3472) to "Jayna Harrow",
            Tile(3604, 3466) to "Korvic Frey",
            Tile(3608, 3466) to "Marcus Harrow",
            Tile(3619, 3469) to "Anton Hayes",
            Tile(3616, 3478) to "Serra Alcanthric",
            Tile(3631, 3476) to "Petrik Corbo",
            Tile(3639, 3470) to "Jayna Corbo",
            Tile(3629, 3483) to "Eryn Treforest",
            Tile(3634, 3503) to "Domin O'Raleigh",
            Tile(3626, 3495) to "Callum Elding",
            Tile(3593, 3509) to "Elena Frey",
            Tile(3585, 3497) to "Marabella Kludge",
            Tile(3502, 3576) to "Rolomere, 14th Lord of the North Coast",
            Tile(3504, 3577) to "Rolovanne, 13th Lord of the North Coast",
            Tile(3506, 3576) to "Rologray, 12th Lord of the North Coast",
            Tile(3542, 3486) to "Unknown",
            Tile(3541, 3471) to "Unknown",
            Tile(3572, 3527) to "Unknown",
            Tile(3576, 3526) to "Unknown",
        )
    }
}

private val FENK_STAGES = listOf("unstarted", "body_parts", "sewing", "conductor", "creature_alive", "creature_loose", "creature_convinced", "completed")

private val Player.fenkStage: Int
    get() = FENK_STAGES.indexOf(quest("creature_of_fenkenstrain")).coerceAtLeast(0)
