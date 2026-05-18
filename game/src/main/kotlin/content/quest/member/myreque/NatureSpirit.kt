package content.quest.member.myreque

import content.entity.combat.killer
import content.entity.effect.transform
import content.entity.gfx.areaGfx
import content.entity.player.bank.ownsItem
import content.entity.proj.shoot
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.statement
import content.entity.player.inv.item.addOrDrop
import content.quest.questComplete
import content.quest.questCompleted
import content.quest.questJournal
import content.quest.refreshQuestJournal
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.handle.interactNpc
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.entity.character.jingle
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.inv.replace
import world.gregs.voidps.engine.queue.longQueue
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.type.Area
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.area.Cuboid
import kotlin.collections.set
import kotlin.text.get

class NatureSpirit : Script {

    init {

        questJournalOpen("nature_spirit") {
            val progress = nature_spirit
            val lines = mutableListOf<String>()

            if (nature_spirit >= 110) {
                lines += "<str>Drezel, a priest of Saradomin, asked me to look for the"
                lines += "<str>druid Filliman Tarlock in the swamps of Mort Myre. However"
                lines += "<str>Filliman had been slain and appeared as a ghost. After"
                lines += "<str>persuading Filliman that he was in fact dead I helped him to"
                lines += "<str>make a transformation into a Nature Spirit."
                lines += ""
                lines += "<str>In return for this help, Filliman blessed a silver sickle and"
                lines += "<str>showed me how to defeat the ghasts of Mort Myre."
                lines += "<str>He also gave me some skill experience in crafting,"
                lines += "<str>hitpoints and defence."
                lines += ""
                lines += "<red>QUEST COMPLETE!"
            } else if (progress == 0) {
                val ghost = questCompleted("the_restless_ghost")
                val priest = questCompleted("priest_in_peril")
                lines += "<navy>I can start this quest by speaking to <maroon>Drezel<navy> in the temple"
                lines += "<navy>to <maroon>Saradomin<navy> at the mouth of the river <maroon>Salve."
                lines += "<navy>I first need to complete:"
                lines += if (ghost) "<str>The Restless Ghost" else "<maroon>The Restless Ghost"
                lines += if (priest) "<str>Priest in Peril" else "<maroon>Priest in Peril"
                lines += "<navy>In order to complete this quest <maroon>level 18 Crafting<navy> would be"
                lines += "<navy>an advantage."
            } else {
                lines += "<str>After talking to Drezel in the temple of Saradomin I've"
                lines += "<str>agreed to look for a Druid called Filliman Tarlock."

                if (progress < 15) {
                    lines += "<navy>I need to look for <maroon>Filliman Tarlock<navy> in the <maroon>Swamps<navy> of Mort"
                    lines += "<navy>Myre. I should be wary of the <maroon>Ghasts<navy>."
                }
                if (progress >= 15) {
                    lines += "<str>I've found a spirit in the swamp which I think might be"
                    lines += "<str>Filliman Tarlock."
                }
                if (progress in 15..19) {
                    lines += "<str>I can't really communicate with this spirit."
                    lines += "<navy>I need to find out what happened to <maroon>Filliman<navy>."
                }
                if (progress >= 20) {
                    lines += "<str>I've communicated with Filliman using the amulet of"
                    lines += "<str>ghostspeak."
                }
                if (progress in 20..24) {
                    lines += "<navy>I think I need to convince this poor fellow <maroon>Tarlock<navy> that he's"
                    lines += "<navy>actually <maroon>dead<navy>!"
                }
                if (progress >= 25) {
                    lines += "<str>I managed to convince Filliman that he's a ghost."
                    lines += "<str>Filliman is looking for his journal to help him plan what his"
                    lines += "<str>next step is."
                }
                if (progress == 25) {
                    if (inventory.contains("journal_nature_spirit")) {
                        lines += "<str>Perhaps I should try to help this poor fellow find his journal?"
                        lines += "<navy>I've found <maroon>Filliman's journal<navy>, perhaps <maroon>Filliman<navy> is still"
                        lines += "<navy>looking for it?"
                    } else {
                        lines += "<navy>Perhaps I should try to help <maroon>Filliman<navy> to find his <maroon>journal<navy>?"
                    }
                }
                if (progress >= 30) {
                    lines += "<str>I've given Filliman his journal. I wonder what he plans to do"
                    lines += "<str>now?"
                }
                if (progress == 30) {
                    lines += "<maroon>Filliman<navy> might need <maroon>my help<navy> with his <maroon>plan<navy>."
                }
                if (progress >= 35) {
                    lines += "<str>I've agreed to help Filliman become a nature spirit."
                    lines += "<str>I need to find 'something from nature', 'something of"
                    lines += "<str>faith' and 'something of the spirit-to-become freely"
                    lines += "<str>given'."
                }
                if (progress == 35) {
                    lines += "<maroon>Filliman<navy> gave me a '<maroon>bloom<navy>' spell but I need to be <maroon>blessed<navy> at"
                    lines += "<navy>the <maroon>temple<navy> before I can cast it. I am supposed to collect"
                    lines += "<navy>'<maroon>something from nature<navy>'."
                }
                if (progress >= 40) {
                    lines += "<str>I've been blessed at the temple by Drezel."
                }
                if (progress == 40) {
                    lines += "<navy>I need to collect '<maroon>something from nature<navy>'."
                }
                if (progress == 45) {
                    lines += "<str>I've cast the bloom spell in the swamp."
                    lines += "<navy>I need to collect '<maroon>something of nature<navy>'."
                }
                if (progress >= 50) {
                    lines += "<str>I collected a Mort Myre Fungi."
                }
                if (progress == 50) {
                    lines += "<navy>I have a <maroon>Mort Myre Fungi<navy>, I hope this is what <maroon>Filliman"
                    lines += "<navy>wanted."
                    lines += "<navy>I need to find '<maroon>Something with faith<navy>'."
                    lines += "<navy>I need to find:"
                    lines += "<navy>'<maroon>Something of the spirit-to-become freely given<navy>.'"
                }
                if (progress == 55) {
                    val brown = get("ns_brown_correct", false)
                    val grey = get("ns_grey_correct", false)
                    if (brown) {
                        lines += "<str>The Mort Myre Fungi was absorbed into the nature stone."
                        lines += "<str>'I think I have collected something from nature.'"
                    } else {
                        lines += "<navy>I need to find '<maroon>something from nature<navy>'."
                    }
                    if (grey) {
                        lines += "<str>The spell scroll was absorbed into the spirit stone. I think I"
                        lines += "<str>have collected 'something of spirit-to-become freely"
                        lines += "<str>given.'"
                    } else {
                        lines += "<navy>I need to find '<maroon>Something with faith<navy>'."
                        lines += "<navy>I need to find:"
                        lines += "<navy>'<maroon>Something of the spirit-to-become freely given<navy>.'"
                    }
                }
                if (progress >= 60) {
                    lines += "<str>I managed to get all of the required items that Filliman asked"
                    lines += "<str>for. He says that he can cast the spell now which will"
                    lines += "<str>transform him into a Nature Spirit."
                }
                if (progress in 60..65) {
                    lines += "<maroon>Filliman<navy> asked me to meet him in his <maroon>grotto<navy>."
                }
                if (progress >= 70) {
                    lines += "<str>I entered Filliman's grotto as he asked me to."
                    lines += "<str>Filliman has turned into a nature spirit, it was an"
                    lines += "<str>impressive transformation!"
                    lines += "<str>Filliman says he can help me to defeat the ghasts."
                }
                if (progress == 70) {
                    if (inventory.contains("silver_sickle")) {
                        lines += "<str>I have the silver sickle that Filliman asked me to get."
                        lines += "<navy>I need to take this <maroon>silver sickle<navy> back to <maroon>Filliman<navy>."
                    } else {
                        lines += "<maroon>Filliman<navy> asked me to get a <maroon>silver sickle<navy>."
                    }
                }
                if (progress >= 75) {
                    lines += "<str>Filliman has blessed the silver sickle for me."
                }
                if (progress == 75) {
                    lines += "<navy>I need to use the <maroon>sickle<navy> to make the swamp bloom."
                }
                if (progress >= 80) {
                    lines += "<str>I cast the bloom spell in the swamp."
                }
                if (progress == 80) {
                    lines += "<maroon>Filliman<navy> said something about collecting '<maroon>natures bounty<navy>'."
                }
                if (progress >= 85) {
                    lines += "<str>I collected some bloomed items from the swamp."
                }
                if (progress == 85) {
                    lines += "<maroon>Filliman<navy> said something about a '<maroon>druid pouch<navy>' perhaps this"
                    lines += "<navy>will help with the <maroon>ghasts<navy>."
                }
                if (progress >= 90) {
                    lines += "<str>I collected some bloomed items from the swamp and put"
                    lines += "<str>them into a druid pouch."
                }
                if (progress == 90) {
                    lines += "<maroon>Filliman<navy> asked me to kill <maroon>three ghasts<navy>."
                }
                if (progress in 95..100) {
                    val killed = (progress - 90) / 5
                    lines += "<str>The druid pouch made a ghost appear which I attacked and"
                    lines += "<str>killed."
                    lines += "<maroon>Filliman<navy> asked me to kill <maroon>three ghasts<navy>."
                    when (killed) {
                        1 -> lines += "<navy>I've killed <maroon>one<navy> ghast, I have <maroon>two<navy> more to kill."
                        2 -> lines += "<navy>I've killed <maroon>two<navy> ghasts, I have <maroon>one<navy> more to kill."
                    }
                }
                if (progress == 105) {
                    lines += "<str>I've killed three ghasts now."
                    lines += "<navy>I should tell <maroon>Filliman<navy> that I've killed the <maroon>three ghasts<navy>."
                }
            }
            questJournal("Nature Spirit", lines)
        }

        objectOperate("Look-at", "grotto_druidicspirit") {
            message("It looks like a tree on a large rock with roots trailing down to the ground.")
        }

        objectOperate("Search", "grotto_druidicspirit") {
            if (nature_spirit != 25 || inventory.contains("journal_nature_spirit")) {
                message("You find nothing interesting.")
                return@objectOperate
            }
            item(
                item = "journal_nature_spirit",
                text = "You search the strange rock. You find a knot and inside of it you discover " +
                        "a small tome. The words on the front are a bit vague, but you make out the " +
                        "words 'Tarlock' and 'journal'."
            )
            addOrDrop("journal_nature_spirit")
        }

        objectOperate("Enter", "grotto_door_druidicspirit") {
            enterGrotto()
        }

        objectOperate("Search", "druidic_spirit_grotto") {
            searchGrottoInside()
        }

        objectOperate("Exit", "underground_rootwall_door,underground_rootwall_door_green") {
            exitGrotto()
        }

        objectOperate("Search", "stonedisc_ds_nature") {
            val brown = get("ns_brown_correct", false)
            statement(
                if (brown) "You search the stone and find that it has some sort of nature symbol scratched into it.<br>This stone seems complete in some way."
                else "You search the stone and find that it has some sort of nature symbol scratched into it."
            )
        }

        objectOperate("Search", "stonedisc_ds_faith") {
            val placedFaith = (tile.x == 3440 && tile.y == 3335) || nature_spirit >= 10
            statement(
                if (placedFaith) "You search the stone and find that it has some sort of faith symbol scratched into it.<br>This stone seems complete in some way."
                else "You search the stone and find that it has some sort of faith symbol scratched into it."
            )
        }

        objectOperate("Search", "stonedisc_ds_spirit") {
            val grey = get("ns_grey_correct", false)
            statement(
                if (grey) "You search the stone and find that it has some sort of spirit symbol scratched into it.<br>This stone seems complete in some way."
                else "You search the stone and find that it has some sort of spirit symbol scratched into it."
            )
        }

        itemOnObjectOperate("mort_myre_fungus,druidic_spell,a_used_spell", "stonedisc_ds_nature,stonedisc_ds_faith,stonedisc_ds_spirit") { interaction ->
            handlePuzzleItem(interaction.target, interaction.item.id)
        }

        objectOperate("Pick", "log_druidicspirit2") { (target) ->
            pickBloom(target, "mort_myre_fungus")
        }

        objectOperate("Pick", "branch_druidicspirit2") { (target) ->
            pickBloom(target, "mort_myre_stem")
        }

        objectOperate("Pick", "peartree_druidicspirit2") { (target) ->
            pickBloom(target, "mort_myre_pear")
        }

        objectOperate("Pray-at", "druidic_spirit_grotto_naturealtar") {
            prayNatureAltar()
        }

        itemOnObjectOperate("silver_sickle", "druidic_spirit_grotto_naturealtar,druidic_spirit_grotto") {
            if (ownsItem("silver_sickle_b")) {
                return@itemOnObjectOperate message("You already have a blessed sickle.")
            }

            anim("human_pickupfloor")
            delay(2)
            gfx("druidicspirit_bloom_player_spotanim", delay = 40, height = 200)
            anim("druidicspirit_human_bloom", delay = 30)
            inventory.replace("silver_sickle", "silver_sickle_b")
            message("You dip the sickle into the grotto water to bless it.")
            item(item = "silver_sickle_b", text = "You dip the sickle into the grotto water to bless it.")
        }

        itemOption("Read", "druidic_spell,a_used_spell") {
            statement(
                "Most of the writing is pretty uninteresting, but something inside refers to " +
                        "nature spirit. The requirements for which are,<br>'Something from nature', " +
                        "'something with faith' and 'something of the spirit-to-become freely given'." +
                        "<br>It's all pretty vague."
            )
        }

        itemOption("Read", "journal_nature_spirit") {
            item(
                item = "journal_nature_spirit",
                text = "Most of the writing is pretty uninteresting, but something inside refers to " +
                        "nature spirit. The requirements for which are,"
            )
            item(
                item = "journal_nature_spirit",
                text = "'Something from nature', 'something with faith' and 'something of the " +
                        "spirit-to-become freely given'. It's all pretty vague."
            )
        }

        itemOption("Cast", "druidic_spell") {
            castBloomFromScroll()
        }

        itemOption("Bloom", "silver_sickle_b") {
            castBloomFromSickle()
        }

        itemOption("Bloom", "silver_sickle_b", "worn_equipment") {
            castBloomFromSickle()
        }

        itemOption("Fill", "druid_pouch,druid_pouch_2") {
            fillPouch()
        }

        itemOnNPCOperate("druid_pouch_2", "ghast") { interaction ->
            applyPouchOnGhast(interaction.target)
        }

        takeable("washing_bowl") { item, _ ->
            if (item.tile == Tile(3437, 3337) && !inventory.contains("mirror")) {
                anim("human_pickuptable")
                message("You find a small mirror under the washing bowl.")
                FloorItems.add(Tile(3437, 3337), "mirror", revealTicks = 0, disappearTicks = 300, owner = this)
            }
            item.id
        }

        takeable("mirror") { item, _ ->
            if (item.tile == Tile(3437, 3337)) {
                anim("human_pickuptable")
            }
            item.id
        }

        npcDeath("ghast") {
            gfx(
                id = "ghast_spotdeath",
                delay = 20,
                height = 50
            )
            val killer = killer
            if (killer !is Player) {
                return@npcDeath
            }
            killer.exp(Skill.Prayer, 30.0)
            when (killer.nature_spirit) {
                90 -> {
                    killer.message("That's one Ghast, 2 more to kill.")
                    killer.nature_spirit = 95
                }
                95 -> {
                    killer.message("That's two Ghasts, 1 more to kill.")
                    killer.nature_spirit = 100
                }
                100 -> {
                    killer.message("That's all three ghasts!")
                    killer.nature_spirit = 105
                }
            }
        }
    }

    private suspend fun Player.enterGrotto() {
        message("You prepare to enter the Druid's grotto.")
        delay(1)
        if (nature_spirit < 60) {
            sound("ghast_appear")
            val filliman = ensureFillimanGhost(Cuboid(3440, 3336, 0, 0))
            talkWith(filliman)
            statement("A shifting apparition appears in front of you.")
            interactNpc(filliman, "Talk-to")
            return
        }
        message("You see a beautifully tended small grotto area.")
        val z = if (nature_spirit >= 110) 1 else 0
        tele(3442, 9734, z)
        if (nature_spirit == 60) {
            nature_spirit = 65
        }
    }

    private fun Player.searchGrottoInside() {
        val filliman = ensureFillimanGhost(Cuboid(3438, 9735, 3444, 9742, 0))
        sound("spirit_transform_start")
        talkWith(filliman)
    }

    private fun Player.ensureFillimanGhost(area: Area): NPC {
        NPCs.findOrNull(tile.regionLevel, "filliman_tarlock_ghost")?.let { return it }
        NPCs.findOrNull(tile.regionLevel, "filliman_tarlock_spirit")?.let { return it }
        val id = if (nature_spirit >= 70) "filliman_tarlock_spirit" else  "filliman_tarlock_ghost"
        val npc = NPCs.addRandom(id, area)
        return npc ?: NPCs.add(id, tile)
    }

    private suspend fun Player.exitGrotto() {
        message("You prepare to exit the Druid's grotto.")
        delay(1)
        tele(3440, 3337, 0)
        message("You crawl back out of the grotto.")
    }

    private suspend fun Player.handlePuzzleItem(target: GameObject, itemId: String) {
        if (nature_spirit !in 45..55) {
            message("Nothing interesting happens.")
            return
        }
        val isNatureStone = target.id == "stonedisc_ds_nature"
        val isSpiritStone = target.id == "stonedisc_ds_spirit"
        val correct = (isNatureStone && itemId == "mort_myre_fungus") ||
                (isSpiritStone && (itemId == "druidic_spell" || itemId == "a_used_spell"))
        anim("human_pickupfloor")
        inventory.remove(itemId, 1)
        FloorItems.add(target.tile, itemId, revealTicks = 0, disappearTicks = 2, owner = this)
        when {
            isNatureStone -> set("ns_brown_correct", correct)
            isSpiritStone -> set("ns_grey_correct", correct)
        }
        if (correct) {
            message("The stone seems to absorb the used ${if (isNatureStone) "fungus" else "spell scroll"}.")
        }
        delay(1)
        val filliman = NPCs.findOrNull(tile.regionLevel, "filliman_tarlock_ghost")
        if (filliman != null) {
            talkWith(filliman)
            if (correct) {
                npc<Neutral>("Aha, yes, that seems right well done!")
            } else {
                npc<Neutral>("Hmm, that doesn't seem right.")
            }
        }
    }

    private fun Player.pickBloom(target: GameObject, yieldItem: String) {
        inventory.add(yieldItem)
        anim("human_pickupfloor")
        sound("pick")
        when (yieldItem) {
            "mort_myre_stem" -> message("You take a cutting from the budding branch.")
            "mort_myre_fungus" -> message("You pick a mushroom from the log.")
            "mort_myre_pear" -> message("You pick a pear from the tree.")
        }
        target.replace(target.id.removeSuffix("2"))
        when (nature_spirit) {
            45 -> nature_spirit = 50
            80 -> nature_spirit = 85
        }
    }

    private suspend fun Player.castBloomFromScroll() {
        if (nature_spirit < 40) {
            message("You need to be blessed before you can cast this spell.")
            return
        }
        if (!atMortMyre()) {
            message("This spell has no effect outside of Mort Myre swamp.")
            return
        }

        message("You cast the spell in the swamp.")
        delay(2)
        if (nature_spirit == 40) {
            nature_spirit = 45
        }
        anim("human_casting", delay = 30)
        sprinkleBloomGfx(height = 46, delay = 30)
        inventory.remove("druidic_spell")
        inventory.add("a_used_spell")
        sound("cast_bloom", delay = 30)
        sound("bloom_mushroom", delay = 30)
        sound("bloom_mushroom", delay = 30)
        bloomNearbyPlants()
    }

    private suspend fun Player.castBloomFromSickle() {
        if (levels.get(Skill.Prayer) <= 0) {
            message("You need some prayer points to activate the power of the sickle.")
            return
        }
        if (!atMortMyre()) {
            message("This spell has no effect outside of Mort Myre swamp.")
            return
        }
        gfx("druidicspirit_bloom_player_spotanim", delay = 30, height = 200)
        anim("druidicspirit_human_bloom", delay = 10)
        sound("cast_bloom", delay = 35)
        delay(1)
        if (nature_spirit == 75) {
            nature_spirit = 80
        }
        levels.drain(Skill.Prayer, ((1..6).random()).coerceAtMost(levels.get(Skill.Prayer)))
        sprinkleBloomGfx(height = 150, delay = 15)
        bloomNearbyPlants()
    }

    private fun Player.sprinkleBloomGfx(height: Int, delay: Int) {
        val offsets = listOf(
            0 to -1, 0 to 1, -1 to -1, -1 to 1, 1 to -1, 1 to 1, -1 to 0, 1 to 0,
        )
        for ((dx, dy) in offsets) {
            areaGfx(
                id = "druidicspirit_bloom_spotanim",
                tile = Tile(tile.x + dx, tile.y + dy, tile.level),
                height = height,
                delay = delay
            )
        }
    }

    private fun Player.bloomNearbyPlants() {
        for (dx in -1..1) {
            for (dy in -1..1) {
                val at = Tile(tile.x + dx, tile.y + dy, tile.level)
                for (data in BLOOM_TABLE) {
                    val obj = GameObjects.findOrNull(at, data.unbloomedName) ?: continue
                    if ((1..4).random() != 1) {
                        continue
                    }
                    sound(data.sound, delay = 40)
                    obj.replace(data.bloomedName, ticks = 25)
                }
            }
        }
    }

    private fun Player.fillPouch() {
        val fungi = inventory.count("mort_myre_fungus")
        val pear = inventory.count("mort_myre_pear")
        val stem = inventory.count("mort_myre_stem")
        if (fungi + pear + stem < 3) {
            message("You need at least 3 of nature's harvests to add to your druid pouch.")
            return
        }
        if (!inventory.contains("druid_pouch_2")) {
            inventory.remove("druid_pouch", 1)
        }
        var added = 0
        var charges = 0
        while (inventory.contains("mort_myre_pear") && added < 3) {
            inventory.remove("mort_myre_pear", 1)
            inventory.add("druid_pouch_2", 3)
            charges += 3
            added++
        }
        while (inventory.contains("mort_myre_stem") && added < 3) {
            inventory.remove("mort_myre_stem", 1)
            inventory.add("druid_pouch_2", 2)
            charges += 2
            added++
        }
        while (added < 3 && inventory.contains("mort_myre_fungus")) {
            inventory.remove("mort_myre_fungus", 1)
            inventory.add("druid_pouch_2", 1)
            charges += 1
            added++
        }
        if (nature_spirit in 75..85) {
            nature_spirit = 90
        }
        message("You add $charges nature's ${if (charges == 1) "harvest" else "harvests"} to your druid pouch.")
    }

    private suspend fun Player.applyPouchOnGhast(npc: NPC) {
        message("The druid pouch makes the Ghast visible.")
        sound("ghast_appear")
        npc.gfx("druidpouch_impact", delay = 90)
        inventory.remove("druid_pouch_2", 1)
        if (inventory.count("druid_pouch_2") == 0) {
            inventory.add("druid_pouch")
        }
        shoot(
            id = "druid_shooting_star",
            target = npc,
            delay = 51,
            flightTime = 39,
            height = 43,
            endHeight = 31,
            curve = 16,
            offset = 64,
        )
        delay(2)
        npc.transform("ghast_level_30")
    }

    fun Player.atMortMyre(): Boolean =
        tile.x in 3400..3520 && tile.y in 3324..3455

    private fun Player.prayNatureAltar() {
        val max = levels.getMax(Skill.Prayer)
        val current = levels.get(Skill.Prayer)
        when {
            current >= max + 2 -> {
                message("You have already boosted your prayer points.")
            }
            current == max -> {
                levels.boost(Skill.Prayer, 2)
                sound("prayer_boost")
                message("You boost your prayer points.")
            }
            else -> {
                levels.boost(Skill.Prayer, 2)
                anim("human_pray")
                sound("prayer_recharge")
                message("You recharge your prayer points at the altar of nature.")
            }
        }
    }

    companion object {
        data class BloomEntry(val unbloomedName: String, val bloomedName: String, val sound: String)

        val BLOOM_TABLE: List<BloomEntry> = listOf(
            BloomEntry("log_druidicspirit", "log_druidicspirit2", "bloom_mushroom"),
            BloomEntry("branch_druidicspirit", "branch_druidicspirit2", "bloom_branch"),
            BloomEntry("peartree_druidicspirit", "peartree_druidicspirit2", "bloom_pears"),
        )
    }
}

var Player.nature_spirit: Int
    get() = get("druidspirit", 0)
    set(value) = set("druidspirit", value)

suspend fun Player.sendNatureSpiritReward() {
    val spirit = NPCs.findOrNull(tile.regionLevel, "filliman_tarlock_spirit")
    val spiritTile = spirit?.tile ?: Tile(3444, 9738, 0)

    spirit?.anim("human_casting")
    delay(2)

    sound("spirit_transform_start", delay = 30)
    val projectiles = listOf(
        Tile(3438, 9742, spiritTile.level) to 106,
        Tile(3444, 9742, spiritTile.level) to 86,
        Tile(3442, 9737, spiritTile.level) to 66,
    )
    for ((origin, flightTime) in projectiles) {
        origin.shoot(
            id = "druid_shooting_star",
            tile = spiritTile,
            delay = 30,
            flightTime = flightTime,
            height = 0,
            endHeight = 42,
            curve = 180,
            offset = 0,
        )
    }

    delay(1)

    for (height in listOf(128, 64, 0)) {
        areaGfx(
            id = "druidicspirit_effect",
            tile = spiritTile,
            delay = 96,
            height = height,
        )
    }
    sound("bloom_pears", delay = 30)
    sound("fire_bolt_all", delay = 30)
    sound("fire_bolt_all", delay = 30)
    delay(3)

    spirit?.anim("human_casting")
    delay(3)

    tele(tile.x, tile.y, 1)
    message("You see a beautifully tended small grotto area.")
    jingle("quest_complete_1")
    exp(Skill.Crafting, 3000.0)
    exp(Skill.Constitution, 2000.0)
    exp(Skill.Defence, 2000.0)
    addOrDrop("silver_sickle_b")
    inc("quest_points", 2)
    nature_spirit = 110
    clear("ns_brown_correct")
    clear("ns_grey_correct")
    refreshQuestJournal()
    questComplete(
        "Nature Spirit Quest!",
        "2 Quest Points",
        "3000 Crafting XP",
        "2000 Constitution XP",
        "2000 Defence XP",
        item = "silver_sickle_b",
    )

    longQueue("Filliman Post Quest") {
        val spirit = NPCs.findOrNull(tile.regionLevel, "filliman_tarlock_spirit") ?:
        NPCs.add("filliman_tarlock_spirit", Tile(3441, 9738, 1))

        face(spirit)
        spirit.face(this)
        talkWith(spirit)
        npc<Neutral>("Welcome to my Altar to Nature! Farewell my friend, and keep those Ghasts at bay!")
        NPCs.remove(spirit)
    }
}

// TODO grotto bridge is incorrect
// TODO fix ghasts
// TODO npc despawn timer for filliman