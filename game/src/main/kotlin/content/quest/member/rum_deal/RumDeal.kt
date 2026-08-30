package content.quest.member.rum_deal

import content.entity.combat.killer
import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Mad
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.Shock
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.quest.questJournal
import content.quest.questStage
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.clearHint
import world.gregs.voidps.engine.client.hint
import world.gregs.voidps.engine.client.instruction.handle.interactPlayer
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectLayer
import world.gregs.voidps.engine.entity.obj.ObjectShape
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.area.Cuboid
import java.util.concurrent.TimeUnit

class RumDeal : Script {

    init {

        val zombieSwabs = listOf(
            "a" to 2843,
            "b" to 2844,
            "c" to 2845,
            "d" to 2846,
            "e" to 2847,
            "f" to 2848,
        )

        questJournalOpen("rum_deal") {
            val lines = when (val stage = questStage("rum_deal")) {
                0 -> notStartedJournal()
                19 -> completedJournal()
                else -> startedJournal(stage)
            }
            questJournal("Rum Deal", lines)
        }

        // ---------- Item-on-NPC: braindeath reactions to weird items ----------

        itemOnNPCOperate("sluglings", "captain_braindeath") {
            player<Confused>("The slugs are talking to me... They tell me to do bad things...")
            npc<Mad>("Gimme that!")
            statement("The Captain takes the Slugling and stamps on it until it stops squirming, then hands it back.")
            player<Shock>("Egad... what just happened?")
            npc<Mad>("What happened is ye were messin' about with Sluglings!")
            npc<Mad>("Don't ye have anything better to do?")
        }

        itemOnNPCOperate("karamthulhu", "captain_braindeath") {
            npc<Confused>("Gah! Get that slimy thing away from me!")
        }

        itemOnNPCOperate("fever_spider_body", "captain_braindeath") {
            npc<Confused>("What are ye doin', lass?  I want ye to shove that in the hopper, not in me face!")
            npc<Confused>("Ye just can't get good help these days...")
        }

        // ---------------------------------------------------------------- //
        // Luke's gate (10172) — stand-and-distract
        // ---------------------------------------------------------------- //

        // The zombies wrecked every patch but one. FarmingPatchInspect only handles `farming_*`
        // ids, so these would otherwise fall through to "nothing interesting happens".
        objectOperate("Inspect", "blindweed_patch_trashed") {
            message("The soil here is too poor to farm on.")
        }

        // Nothing advances the quest when the blindweed finishes growing - the original
        // leaves progress 4 falling through to Braindeath's generic "So..." reply forever,
        // so harvesting the patch is what moves it on.
        // Neither the original nor void tells you a crop has finished growing, which leaves the
        // player waiting at the patch with no signal. The quest needs the blindweed, so say so.
        variableSet("farming_blindweed_patch_braindeath_island") { _, _, to ->
            if (to == "blindweed_life1" && questStage("rum_deal") == 4) {
                message("I wonder how my Blindweed is coming along...")
            }
        }

        itemAdded("blindweed", "inventory") {
            if (questStage("rum_deal") == 4) {
                set("rum_deal", "grown_blindweed")
            }
        }

        objectOperate("Open", "gate_104_closed") { (target) ->
            // Luke wanders his post, so he is looked up by region rather than by tile.
            val luke = NPCs.findOrNull(tile.regionLevel, "50_luke") ?: return@objectOperate

            if (tile.y > 5098) {
                // Outside the compound - Luke marches the player straight back in
                talkWith(luke)
                npc<Angry>("Hey! What are you doing out there?")
                player<Shifty>("Nothing.")
                npc<Angry>("Well Cap'n Donnie said no livin' landlubbers were allowed out of the compound.")
                npc<Angry>("So get yerself back in here, or yer for it!")
                useGate(target, luke, north = false)
                return@objectOperate
            }

            // Trying to sneak out
            talkWith(luke)
            npc<Angry>("Arr! Tryin' ter get away eh? Well ye'll never sneak past me, I'm the best lookout this crew has ever seen!")

            if (questStage("rum_deal") < 8) {
                player<Neutral>("Err...look...an astonishing...thing. There. Behind you.")
                npc<Angry>("Ye'll have te do better than that, landlubber!")
                return@objectOperate
            }

            // Any of the six diversions works once Braindeath has sent you for water
            when ((0..5).random()) {
                0 -> player<Shock>("Oh my! Is that a genuine 3rd Age Diversion?")
                1 -> player<Quiz>("Who's that making faces behind you?")
                2 -> player<Quiz>("Is that your distraction?")
                3 -> player<Quiz>("Who is that behind you?")
                4 -> player<Neutral>("Hey you! Look over there!")
                5 -> player<Shock>("That is the most amazing thing I have ever seen!")
            }
            useGate(target, luke, north = true)
        }

        objectOperate("Open", "braindeath_island_tool_cupboard") { (target) ->
            anim("human_opencupboard")
            sound("cupboard_open")
            target.replace("braindeath_island_tool_cupboard_open", ticks = 500)
        }

        objectOperate("Shut", "braindeath_island_tool_cupboard_open") { (target) ->
            anim("human_opencupboard")
            sound("cupboard_close")
            target.replace("braindeath_island_tool_cupboard")
        }

        objectOperate("Search", "braindeath_island_tool_cupboard_open") {
            statement("There is a tangle of rusty old farming equipment in here.")
            statement("What would you like to take?")
            choice("What would you like to take?") {
                option("A rake") {
                    takeFromCupboard("rake", "rake", "You can't find another rake. Good job you already have one.")
                }
                option("A seed dibber") {
                    takeFromCupboard(
                        "seed_dibber",
                        "seed dibber",
                        "The only dibber you find has a bent dibbing bit. You should use the one you have.",
                    )
                }
                option("A watering can") {
                    takeFromCupboard(
                        "watering_can",
                        "watering can",
                        "You can't find any watering cans without holes in them, so you decide to use your own.",
                    )
                }
                option("All of the above!") {
                    if (inventory.contains("watering_can") &&
                        inventory.contains("rake") &&
                        inventory.contains("seed_dibber")
                    ) {
                        statement("The remaining stuff seems fused together in a lump of spiky, tetanus-inducing rust. Best leave it alone for now.")
                    } else if (inventory.spaces > 0) {
                        for (item in listOf("watering_can", "rake", "seed_dibber")) {
                            if (!inventory.contains(item) && inventory.spaces > 0) {
                                inventory.add(item)
                            }
                        }
                        statement("You help yourself to what you need.")
                    } else {
                        statement("You do not have any free space for all of these items.")
                    }
                }
            }
        }

        // ---------------------------------------------------------------- //
        // Intake hopper (10170) — accepts blindweed / stagnant water / spider
        // ---------------------------------------------------------------- //

        itemOnObjectOperate("blindweed,bucket_of_water_stagnant,fever_spider_body", "braindeath_island_intake_hopper") { interaction ->
            val progress = questStage("rum_deal")
            val id = interaction.item.id

            val matches = (id == "blindweed" && progress == 6) ||
                (id == "bucket_of_water_stagnant" && progress == 9) ||
                (id == "fever_spider_body" && progress == 15)

            if (!matches) {
                return@itemOnObjectOperate message("Nothing interesting happens.")
            }

            when (id) {
                "blindweed" -> {
                    set("rum_deal", "blindweed_added")
                    delay(1)
                    message("You stuff the Blindweed into the Hopper.")
                    anim("net_catch")
                    inventory.remove("blindweed")
                    sound("rumdeal_stuff_other")
                    delay(2)
                }
                "bucket_of_water_stagnant" -> {
                    set("rum_deal", "water_added")
                    delay(1)
                    message("You dump the water into the hopper.")
                    anim("farming_pour_water")
                    inventory.remove("bucket_of_water_stagnant")
                    inventory.add("bucket")
                    sound("pour_tea")
                    delay(5)
                }
                "fever_spider_body" -> {
                    set("rum_deal", "spider_added")
                    delay(1)
                    message("You cram the diseased Fever Spider body into the hopper.")
                    anim("net_catch")
                    inventory.remove("fever_spider_body")
                    sound("rumdeal_stuff_other")
                    delay(2)
                }
            }
        }

        // ---------------------------------------------------------------- //
        // Pressure barrel (10171) — accepts sluglings / karamthulhu
        // ---------------------------------------------------------------- //

        itemOnObjectOperate("sluglings,karamthulhu", "braindeath_island_pressure_barrel") { interaction ->
            if (questStage("rum_deal") != 11) {
                return@itemOnObjectOperate message("Nothing Interesting Happens")
            }

            val slug = interaction.item.id == "sluglings"
            val pressureCount = get("rum_deal_pressure_count", 0)

            if (pressureCount == 5) {
                val name = if (slug) "Sluglings" else "Karamthulhu"
                return@itemOnObjectOperate message("You don't think you can stuff any more $name in there.")
            }

            set("rum_deal_pressure_count", pressureCount + 1)
            if (slug) {
                set("rum_deal_slugling_count", get("rum_deal_slugling_count", 0) + 1)
            } else {
                set("rum_deal_karamthulhu_count", get("rum_deal_karamthulhu_count", 0) + 1)
            }

            val name = if (slug) "Sluglings" else "Karamthulhu"
            message("You stuff the squirming $name into the barrel.")
            anim("human_pickuptable")
            inventory.remove(interaction.item.id)
            sound("rumdeal_stuff_slugling")
            delay(2)
        }

        objectOperate("Count", "braindeath_island_pressure_barrel") {
            val slugs = get("rum_deal_slugling_count", 0)
            val karam = get("rum_deal_karamthulhu_count", 0)
            message(
                when {
                    slugs == 0 && karam == 0 -> "This barrel is empty and smells nasty."
                    slugs == 1 && karam == 0 -> "There are some Sluglings in this barrel."
                    slugs == 0 && karam == 1 -> "There is a Karamthulhu in this barrel."
                    slugs == 1 && karam == 1 -> "There are some Sluglings and a Karamthulhu in this barrel."
                    else -> "There are $slugs loads of Sluglings and $karam Karamthulhu in this barrel."
                },
            )
        }

        // ---------- Pressure lever (10165 / 10166) ----------

        objectOperate("Pull", "braindeath_island_pressure_lever_up,braindeath_island_pressure_lever_down") {
            if (get("rum_deal_pressure_count", 0) != 5) {
                return@objectOperate message("You do not yet have five sea creatures in the barrel!")
            }

            set("rum_deal_pressure_count", 0)
            set("rum_deal_slugling_count", 0)
            set("rum_deal_karamthulhu_count", 0)
            message("You pressurise the assorted sea creatures.")
            sound("rumdeal_pressurize")
            delay(2)
            set("rum_deal", "pressurised")
            set("rum_deal_brewing_control", 1)
            GameObjects.findLayerOrNull(Tile(2141, 5102, 2), ObjectLayer.GROUND, "braindeath_island_pressure_barrel")
                ?.anim("deal_press_press")
            delay(2)
        }

        // ---------------------------------------------------------------- //
        // Stagnant water collection (10105)
        // ---------------------------------------------------------------- //

        itemOnObjectOperate("bucket", "braindeath_island_stagnant_lake") {
            if (questStage("rum_deal") == 8) {
                set("rum_deal", "collected_water")
            }
            message("You scoop up a bucket of the stagnant water.")
            anim("fill_bucket_slime") // 4471
            inventory.remove("bucket")
            inventory.add("bucket_of_water_stagnant")
            delay(3)
        }

        // ---------------------------------------------------------------- //
        // Fishbowl-net combine / uncombine
        // ---------------------------------------------------------------- //

        itemOption("Unwrap", "fishbowl_and_net") {
            if (inventory.spaces < 1) {
                return@itemOption message("You do not have enough space to unwrap the net from the fishbowl.")
            }
            message("You unwrap the net from the fishbowl.")
            inventory.remove("fishbowl_and_net")
            inventory.add("fishbowl")
            inventory.add("big_fishing_net")
        }

        itemOnItem("fishbowl", "big_fishing_net") { _, _ ->
            message("You wrap the net around the empty bowl.")
            inventory.remove("fishbowl")
            inventory.remove("big_fishing_net")
            inventory.add("fishbowl_and_net")
        }

        // ---------------------------------------------------------------- //
        // Sea creature fishing spot (NPC 2859)
        // ---------------------------------------------------------------- //

        npcOperate("Fish", "fishing_spot_braindeath_island") {
            arriveDelay()
            if (questStage("rum_deal") != 11) {
                return@npcOperate message("You don't think you have time for fishing.")
            }

            if (!has(Skill.Fishing, 50)) {
                return@npcOperate message("You cannot fish here, you need a Fishing level of at least 50.")
            }

            if (!inventory.contains("fishbowl_and_net")) {
                return@npcOperate message("You do not have the correct equipment to fish here.")
            }

            if (inventory.spaces < 1) {
                return@npcOperate message("You do not have any free space for anything that you will catch!")
            }

            message("You dunk the bowl in the water...")
            anim("deal_bowl_fish")
            sound("rumdeal_bowl_fish", delay = 20)
            delay(4)

            if ((0..2).random() == 0) {
                message("...and you catch a Karamthulhu!")
                inventory.add("karamthulhu")
            } else {
                message("...and you catch some Sluglings!")
                inventory.add("sluglings")
            }
            delay(1)
        }

        // ---------------------------------------------------------------- //
        // Output tap (10148) — fill bucket with swill
        // ---------------------------------------------------------------- //

        objectOperate("Turn", "braindeath_island_output_tap") {
            handleSwillFill(emptyVatMessage = "Nothing interesting happens.")
        }

        itemOnObjectOperate("bucket", "braindeath_island_output_tap") {
            handleSwillFill(emptyVatMessage = "The vat is empty.")
        }

        // ---------------------------------------------------------------- //
        // Brewing controls (10143) — smite with holy wrench
        // ---------------------------------------------------------------- //

        itemOnObjectOperate("holy_wrench", "braindeath_island_brewing_controls_possessed") {
            if (questStage("rum_deal") != 13) {
                return@itemOnObjectOperate message("Nothing interesting happens.")
            }

            if (evilSpirit() != null) {
                return@itemOnObjectOperate message("The Evil Spirit has already manifested!")
            }

            message("You raise your wrench on high and smite the controls mightily!")
            sound("wrench_crush")
            anim("deal_smite_control")
            say("The power of Guthix compels you!")
            delay(2)
            message("The Evil Spirit is forced from the controls!")
            sound("rumdeal_hopper_1")
            val spirit = NPCs.addRandom(
                "evil_spirit",
                Cuboid(Tile(2143, 5098, 1), width = 4, height = 4, levels = 1),
                ticks = TimeUnit.MINUTES.toTicks(8),
                owner = this,
            )
            if (spirit != null) {
                spirit["hint_index"] = hint(spirit)
                spirit.interactPlayer(this, "Attack")
            }
        }

        npcDeath("evil_spirit") {
            val killer = killer as? Player ?: return@npcDeath
            killer["rum_deal_brewing_control"] = 0
            if (killer.questStage("rum_deal") == 13) {
                killer["rum_deal"] = "spirit_banished"
            }
            // Keep the arrow's slot on the spirit; clearHint() with no index wipes every
            // hint the player has, including arrows belonging to other content.
            val arrow: Int = get("hint_index", -1)
            if (arrow != -1) {
                killer.clearHint(arrow)
            }
            killer.message("You have banished the Evil Spirit!")
        }
    }

    // ----------------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------------

    /** Slips the player through Luke's gate, [north] out of the compound or south back into it. */
    private suspend fun Player.useGate(
        target: GameObject,
        luke: NPC,
        north: Boolean,
    ) {
        if (north) {
            luke.face(Direction.SOUTH)
            luke.say("Where?")
        }
        sound("barrows_door_open")
        target.replace(
            "inviswall",
            shape = ObjectShape.WALL_STRAIGHT,
            rotation = target.rotation,
            ticks = 2,
        )
        // The open gate is a wall piece; without the shape it defaults to a centrepiece,
        // lands on the wrong layer and renders nothing.
        GameObjects.add(
            id = "gate_104_opened",
            tile = Tile(2120, 5099, 0),
            shape = ObjectShape.WALL_STRAIGHT,
            rotation = 0,
            ticks = 2,
        )
        walkTo(
            target = Tile(this.tile.x, this.tile.y + if (north) 1 else -1, this.tile.level),
            forceWalk = true,
            noCollision = true,
        )
        delay(2)
    }

    /** [name] is the readable name; the item id would otherwise be shown to the player verbatim. */
    private suspend fun Player.takeFromCupboard(item: String, name: String, alreadyHave: String) {
        if (inventory.contains(item)) {
            statement(alreadyHave)
            return
        }
        if (inventory.spaces < 1) {
            statement("You do not have any free space for the $name.")
            return
        }
        inventory.add(item)
        statement("You take an old $name.")
    }

    private fun Player.evilSpirit(): NPC? = NPCs.at(tile.regionLevel).firstOrNull { it.id == "evil_spirit" && it["owner", ""] == accountName }

    private fun Player.handleSwillFill(emptyVatMessage: String) {
        val progress = questStage("rum_deal")

        if (progress >= 18) {
            return message("You thankfully have no need to do that any more.")
        }
        if (progress != 17) {
            return message(emptyVatMessage)
        }
        if (!inventory.contains("bucket")) {
            return message("Nothing interesting happens.")
        }

        inventory.remove("bucket")
        inventory.add("unsanitary_swill")
        message("You carefully fill the bucket with the foul-smelling swill.")
    }

    // ----------------------------------------------------------------------
    // Journal
    // ----------------------------------------------------------------------

    /** The patch variable only names a `blindweed_*` state once the seeds are in the ground. */
    private fun Player.blindweedPlanted(): Boolean = get("farming_blindweed_patch_braindeath_island", "weeds_0").startsWith("blindweed")

    private fun Player.notStartedJournal(): List<String> {
        // Java: checks Farming/Fishing/Prayer/Crafting/Slayer levels and Zogre Flesh Eaters completion.
        val farming = levels.getMax(Skill.Farming) >= 40
        val fishing = levels.getMax(Skill.Fishing) >= 50
        val prayer = levels.getMax(Skill.Prayer) >= 47
        val crafting = levels.getMax(Skill.Crafting) >= 42
        val slayer = levels.getMax(Skill.Slayer) >= 42
        val zogre = get("zogre_flesh_eaters", "") == "completed"

        fun line(done: Boolean, text: String) = if (done) "<str>$text" else "<maroon>$text"

        return listOf(
            "<navy>I need to speak to <maroon>Pirate Pete<navy> in Port Phasmatys.",
            "",
            "<navy>To complete this quest I need:",
            line(farming, "40 Farming"),
            line(fishing, "50 Fishing"),
            line(prayer, "47 Prayer"),
            line(crafting, "42 Crafting"),
            line(slayer, "42 Slayer"),
            line(zogre, "I must have completed Zogre Flesh Eaters"),
            "<maroon>To be able to defeat a level 150 Monster",
        )
    }

    /**
     * The original's journal is cumulative - completing the quest strikes through everything that
     * came before and appends the closing lines, rather than replacing it with a summary.
     */
    private fun Player.completedJournal(): List<String> = startedJournal(19) + listOf(
        "",
        "<str>I have spoken to Captain Braindeath, who has decided to",
        "<str>stay on the island and keep the Zombie Pirates drunk!",
        "<str>Despite claiming he has never heard of Rabid Jack he was",
        "<str>more than happy with my performance, and rewarded me",
        "<str>with Farming, Fishing and Prayer experience, as well as the",
        "<str>Blessed Wrench.",
        "",
        "<red>QUEST COMPLETE!",
    )

    private fun Player.startedJournal(stage: Int): List<String> {
        val list = mutableListOf<String>()

        // Stage 1+
        if (stage < 2) {
            list += "<navy>I have spoken to <maroon>Pirate Pete<navy>, and I have agreed to help him"
            list += "<navy>find his <maroon>Family Sword<navy>."
        } else {
            list += "<str>I have spoken to Pirate Pete, and have agreed to help him"
            list += "<str>find his Family Sword."
        }

        // Stage 2+
        if (stage in 2..2) {
            list += ""
            list += "<navy>I have agreed to help <maroon>Pirate Pete<navy> slay <maroon>Barrelor the"
            list += "<maroon>Destroyer<navy> so that he can reclaim his lands."
        } else if (stage > 2) {
            list += "<str>I have agreed to help Pirate Pete slay Barrelor the"
            list += "<str>Destroyer so that he can reclaim his lands."
        }

        // Stage 3+
        if (stage in 3..3) {
            list += ""
            list += "<navy>I have a splitting headache. <maroon>Captain Braindeath<navy> has"
            list += "<navy>explained the situation to me, and I have agreed to help"
            list += "<navy>brew up a batch of <maroon>'rum'<navy>."
        } else if (stage > 3) {
            list += "<str>I have a splitting headache. Captain Braindeath has"
            list += "<str>explained the situation to me, and I have agreed to help"
            list += "<str>brew up a batch of 'rum'."
        }

        // Stage 4+
        if (stage in 4..4) {
            list += ""
            list += "<navy>I have been given some <maroon>Blindweed Seeds<navy>, and told to grow"
            list += "<navy>them in the <maroon>Herb Patch<navy> outside. <maroon>Captain Braindeath"
            list += "<navy>recommended that I try and intimidate the <maroon>Swabs<navy> guarding"
            list += "<navy>the <maroon>Herb Patch<navy> if I want them to stop attacking me."

            if (blindweedPlanted()) {
                list += ""
                list += "<navy>I have planted my <maroon>Blindweed Seeds<navy>, and should wait for"
                list += "<navy>them to grow."
            }
        } else if (stage > 4) {
            list += "<str>I have been given some Blindweed Seeds, and told to grow"
            list += "<str>them in the Herb Patch outside. Captain Braindeath"
            list += "<str>recommended that I try and intimidate the Swabs guarding"
            list += "<str>the Herb Patch if I want them to stop attacking me."
            list += "<str>I have planted my Blindweed Seeds, and should wait for"
            list += "<str>them to grow."
        }

        // Stage 5
        if (stage == 5) {
            list += ""
            list += "<navy>I have grown some <maroon>Blindweed<navy>, I should show it to <maroon>Captain"
            list += "<maroon>Braindeath<navy> so that he can tell me what to do with it."

            if (!inventory.contains("blindweed")) {
                list += ""
                list += "<navy>Having lost my <maroon>Blindweed<navy> I should go and see <maroon>Captain"
                list += "<maroon>Braindeath<navy>, he may have some kept to one side for use in"
                list += "<navy>an emergency."
            }
        } else if (stage > 5) {
            list += "<str>I have grown some Blindweed, I should show it to Captain"
            list += "<str>Braindeath so that he can tell me what to do with it."
        }

        // Stage 6
        if (stage in 6..6) {
            list += ""
            list += "<navy>I have shown my <maroon>Blindweed<navy> to <maroon>Captain Braindeath<navy>. He told"
            list += "<navy>me to put it into the <maroon>Intake Hopper<navy>."
        } else if (stage > 6) {
            list += "<str>I have shown my Blindweed to Captain Braindeath. He told"
            list += "<str>me to put it into the Intake Hopper."
        }

        // Stage 7
        if (stage in 7..7) {
            list += ""
            list += "<navy>I have added the <maroon>Blindweed<navy> to the <maroon>Intake Hopper<navy>. I should"
            list += "<navy>ask <maroon>Captain Braindeath<navy> what he wants me to do next."
        } else if (stage > 7) {
            list += "<str>I have added the Blindweed to the hopper. I should ask"
            list += "<str>Captain Braindeath what he wants me to do next."
        }

        // Stage 8
        if (stage in 8..8) {
            list += ""
            list += "<maroon>Captain Braindeath<navy> has told me to fetch a bucket of"
            list += "<maroon>Stagnant Water<navy> from the <maroon>Stagnant Lake<navy> near the top of"
            list += "<navy>the <maroon>Volcano<navy> to the North."
        } else if (stage > 8) {
            list += "<str>Captain Braindeath has told me to fetch a bucket of"
            list += "<str>Stagnant Water from the Stagnant Lake near the top of"
            list += "<str>the Volcano to the North."
        }

        // Stage 9
        if (stage in 9..9) {
            list += ""
            list += "<navy>Using superior cunning and stealth I have managed to exit"
            list += "<navy>the Brewery compound and have collected a bucket of"
            list += "<maroon>Stagnant Water<navy>."

            if (!inventory.contains("bucket_of_water_stagnant")) {
                list += ""
                list += "<navy>Due to my inferior ability to hold objects I have lost my"
                list += "<maroon>Bucket of Stagnant Water<navy>. I should go and see <maroon>Captain"
                list += "<maroon>Braindeath<navy> to see what he wants me to do about this"
                list += "<navy>situation."
            }
        } else if (stage > 9) {
            list += "<str>Using superior cunning and stealth I have managed to exit"
            list += "<str>the Brewery compound and have collected a bucket of"
            list += "<str>Stagnant Water."
        }

        // Stage 10
        if (stage in 10..10) {
            list += ""
            list += "<navy>I have poured the <maroon>Stagnant Water<navy> into the <maroon>Intake Hopper<navy>. I"
            list += "<navy>should speak to <maroon>Captain Braindeath<navy> again to see what else"
            list += "<navy>he needs."
        } else if (stage > 10) {
            list += "<str>I have poured the Stagnant Water into the Intake Hopper. I"
            list += "<str>should speak to Captain Braindeath again to see what else"
            list += "<str>he needs."
        }

        // Stage 11
        if (stage in 11..11) {
            list += ""
            list += "<maroon>Captain Braindeath<navy> has told me to fish with a <maroon>Fishbowl and"
            list += "<maroon>Big Net<navy> in the <maroon>Squid Fishing Spot<navy>. Whatever <maroon>5 Sea"
            list += "<maroon>Creatures<navy> I dredge up are to be put into a barrel and"
            list += "<navy>pressurised."
        } else if (stage > 11) {
            list += "<str>Captain Braindeath has told me to fish with a Fishbowl and"
            list += "<str>Big Net in the Squid Fishing Spot. Whatever 5 Sea"
            list += "<str>Creatures I dredge up are to be put into a barrel and"
            list += "<str>pressurised."
        }

        // Stage 12
        if (stage in 12..12) {
            list += ""
            list += "<navy>I have pressurised the <maroon>Assorted Sea Creatures<navy> in the"
            list += "<maroon>Pressure Barrel<navy>. I should see <maroon>Captain Braindeath<navy> to check"
            list += "<navy>if this is all he needs me to do."
        } else if (stage > 12) {
            list += "<str>I have pressurised the Assorted Sea Creatures in the"
            list += "<str>Pressure Barrel. I should see Captain Braindeath to check"
            list += "<str>if this is all he needs me to do."
        }

        // Stage 13
        if (stage in 13..13) {
            list += ""
            list += "<str>Apparently the Brewing Equipment has become possessed."
            list += "<str>Captain Braindeath has given me a Wrench and told me to"
            list += "<str>sort it out. I should check amongst the Brewers to see"
            list += "<str>which of them can bless it for me."
            list += ""

            if (inventory.contains("holy_wrench")) {
                list += "<navy>I am now the proud owner of a <maroon>Holy Wrench<navy>. I will take up"
                list += "<navy>this <maroon>Holy Wrench<navy> and strike the <maroon>Brewing Controls<navy> until they"
                list += "<navy>are <maroon>very, very sorry<navy>."
            } else if (!inventory.contains("wrench")) {
                list += "<navy>I have just realised that I will have a hard time getting my"
                list += "<maroon>Wrench<navy> blessed now that I have thrown it away. I think I"
                list += "<navy>should see <maroon>Captain Braindeath<navy> to get a replacement"
                list += "<navy>before the situation deteriorates further."
            }
        } else if (stage > 13) {
            list += "<str>Apparently the Brewing Equipment has become possessed."
            list += "<str>Captain Braindeath has given me a Wrench and told me to"
            list += "<str>sort it out. I should check amongst the Brewers to see"
            list += "<str>which of them can bless it for me."
            list += "<str>I am now the proud owner of a Holy Wrench. I will take up"
            list += "<str>this Holy Wrench and strike the Brewing Controls until they"
            list += "<str>are very, very sorry."
        }

        // Stage 14
        if (stage in 14..14) {
            list += ""
            list += "<navy>I have banished the <maroon>Evil Spirit<navy>. <maroon>Captain Braindeath<navy> told me"
            list += "<navy>there is one more ingredient left. I should ask him what"
            list += "<navy>this is."
        } else if (stage > 14) {
            list += "<str>I have banished the Evil Spirit. Captain Braindeath told me"
            list += "<str>there is one more ingredient left. I should ask him what"
            list += "<str>this is."
        }

        // Stage 15
        if (stage in 15..15) {
            list += ""
            list += "<maroon>Captain Braindeath<navy> wants me to kill a <maroon>Fever Spider<navy>, and put"
            list += "<navy>its <maroon>Body<navy> into the <maroon>Intake Hopper<navy>. He told me to wear <maroon>Slayer"
            list += "<maroon>Gloves<navy> (Available from the <maroon>Slayer Masters<navy> apparently)"
            list += "<navy>while I did so, as they carry a nasty disease. <maroon>Mental Note;"
            list += "<navy>do not drink whatever comes out of that machine."
        } else if (stage > 15) {
            list += "<str>Captain Braindeath wants me to kill a Fever Spider, and put"
            list += "<str>its Body into the Intake Hopper. He told me to wear Slayer"
            list += "<str>Gloves (Available from the Slayer Masters apparently)"
            list += "<str>while I did so, as they carry a nasty disease. Mental Note;"
            list += "<str>do not drink whatever comes out of that machine."
        }

        // Stage 16
        if (stage in 16..16) {
            list += ""
            list += "<navy>I have placed the <maroon>Fever Spider Body<navy> in the <maroon>Hopper<navy>. <maroon>Captain"
            list += "<maroon>Braindeath<navy> will be able to produce some <maroon>'rum'<navy> when I tell"
            list += "<navy>him."
        } else if (stage > 16) {
            list += "<str>I have placed the Fever Spider Body in the Hopper. Captain"
            list += "<str>Braindeath will be able to produce some 'rum' when I tell"
            list += "<str>him."
        }

        // Stage 17
        if (stage in 17..17) {
            list += ""
            list += "<maroon>Captain Braindeath<navy> has filled the <maroon>Output Vat<navy> with <maroon>'rum'<navy>. I"
            list += "<navy>should take a bucket of the stuff to <maroon>Captain Donnie<navy> and"
            list += "<navy>give it to him."
        } else if (stage > 17) {
            list += "<str>Captain Braindeath has filled the Output Vat with 'rum'. I"
            list += "<str>should take a bucket of the stuff to Captain Donnie and"
            list += "<str>give it to him."
        }

        // Stage 18
        if (stage in 18..18) {
            list += ""
            list += "<navy>I have spoken to <maroon>Captain Donnie<navy>, and have found out the"
            list += "<navy>name of his boss, <maroon>Rabid Jack<navy>.  I should tell <maroon>Captain"
            list += "<maroon>Braindeath<navy> about this."
        } else if (stage > 18) {
            list += "<str>I have spoken to Captain Donnie, and have found out the"
            list += "<str>name of his boss, Rabid Jack.  I should tell Captain"
            list += "<str>Braindeath about this."
        }

        return list
    }
}
