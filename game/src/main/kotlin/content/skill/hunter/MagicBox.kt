package content.skill.hunter

import content.entity.effect.transform
import content.entity.player.bank.BankDeposit
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.inv.item.drop
import net.pearx.kasechange.toLowerSpaceCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.config.RowDefinition
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.obj.*
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.inv.replace

class MagicBox : Script {
    init {
        itemOption("Activate", "magic_box") {
            layTrap(null)
        }

        floorItemOperate("Lay") { (item) ->
            if (item.id == "magic_box") {
                layTrap(item)
            }
        }

        objectOperate("Deactivate", "magic_box,magic_box_fail") { (target) ->
            dismantleTrap(target, null)
        }

        objectOperate("Retrieve", "magic_box_caught") { (target) ->
            dismantleTrap(target, creature = Rows.get("creatures.imp"))
        }

        objectOperate("Investigate", "magic_box") { (target) ->
            val npc = NPCs.find(target.tile, "hunting_imptrap_npc")
            if (npc["owner", ""] == accountName) {
                message("This is your magic box, ready to catch an imp.")
            } else {
                message("This isn't your magic box.")
            }
        }

        huntNPC("magic_box") { target ->
            if (transform.endsWith("_off")) {
                return@huntNPC
            }
            val creature = Rows.getOrNull("creatures.${target.id}") ?: return@huntNPC
            val account: String = get("owner") ?: return@huntNPC
            val player = Players.findByAccount(account) ?: return@huntNPC
            if (!player.has(Skill.Hunter, creature.int("level"))) {
                return@huntNPC
            }
            if (tile.distanceTo(target.tile) > 2) {
                return@huntNPC
            }
            transform("${id}_off")
            val chance = Traps.chance(this, creature)
            val success = Level.success(player.levels.get(Skill.Hunter), chance)
            target.walkToDelay(tile)
            target.walkOverDelay(tile)
            despawn(100)
            val trap = GameObjects.getLayer(tile, ObjectLayer.GROUND) ?: return@huntNPC
            target.anim(if (success) creature.anim("catch_anim") else creature.anim("fail_anim"))
            target.gfx("imp")
            target.delay(1)
            if (!success) {
                trap.replace("magic_box_fail")
                return@huntNPC
            }
            target.levels.set(Skill.Constitution, 0)
            val catching = trap.replace("magic_box_catching")
            delay(1)
            catching.replace("magic_box_caught")
            player.message("Something has been caught in your trap!")
        }

        npcDespawn("hunting_imptrap_npc") {
            val player = owner ?: return@npcDespawn
            val trap = GameObjects.getLayer(tile, ObjectLayer.GROUND) ?: return@npcDespawn
            player.dec("trap_count")
            GameObjects.remove(trap)
            val drop = if (lifecycle == 0) {
                player.message("The magic box that you activated has stopped working.")
                true
            } else {
                player["logged_out", false]
            }
            if (drop) {
                player.drop(trap.tile, "magic_box")
            }
        }

        itemOption("Talk-to", "imp_in_a_box_2,imp_in_a_box_1") {
            impDialogue()
        }

        itemOption("Bank", "imp_in_a_box_2,imp_in_a_box_1") {
            open("imp_box")
        }

        interfaceOpened("imp_box") {
            interfaces.sendText("imp_box", "text", depositText())
            interfaceOptions.send("imp_box", "inventory")
            interfaceOptions.unlockAll("imp_box", "inventory", 0 until 28)
        }

        interfaceOption("Deposit", "imp_box:inventory") { (item, slot) ->
            depositItem(item, slot)
        }

        interfaceOption("Close", "imp_box:close") {
            close("imp_box")
        }

        itemOnItem("*", "imp_in_a_box_2,imp_in_a_box_1") { item, box ->
            if (item.id.startsWith("imp_in_a_box") || item.id.startsWith("magic_box")) {
                message("The imp refuses to take that to your bank.")
                return@itemOnItem
            }
            BankDeposit.deposit(this, inventory, item, 1, check = false)
            if (box.id == "imp_in_a_box_2") {
                inventory.replace("imp_in_a_box_2", "imp_in_a_box_1")
                message("The imp takes the item to your bank.")
            } else {
                inventory.replace("imp_in_a_box_1", "magic_box")
                message("The imp takes the item to your bank and escapes from the box.")
            }
        }
    }

    private fun Player.depositText() = if (inventory.contains("imp_in_a_box_2")) {
        "Select an item or stack of items to deposit. <br>You can deposit up to 2 items or stacks."
    } else {
        "Select an item or stack of items to deposit. <br>You can deposit 1 more item or stack."
    }

    private fun Player.depositItem(item: Item, slot: Int) {
        if (item.id.startsWith("imp_in_a_box") || item.id.startsWith("magic_box")) {
            message("A magical force prevents you from banking this item.")
            return
        }
        BankDeposit.deposit(this, inventory, item, item.amount, slot, check = false)
        if (inventory.contains("imp_in_a_box_2")) {
            inventory.replace("imp_in_a_box_2", "imp_in_a_box_1")
            interfaces.sendText("imp_box", "text", depositText())
        } else if (inventory.contains("imp_in_a_box_1")) {
            inventory.replace("imp_in_a_box_1", "magic_box")
            close("imp_box")
            message("The imp takes your items to the bank and escapes from the box.")
        } else {
            close("imp_box")
        }
    }

    private suspend fun Player.impDialogue() {
        player<Quiz>("Hey imp, are you still there?")
        npc<Neutral>("imp", "Of course I can hear ya, ya great big ape. You know, there's not even enuf space to swing Bob about in 'ere. How about a breather? You know, stretch me pins for a bit?")
        impOptions()
    }

    private suspend fun Player.impOptions() {
        choice {
            option<Neutral>("No, I'm going to keep you in there.") {
                player<Neutral>("No, I'm going to keep you in there. I might keep you as a pet.")
                npc<Neutral>("imp", "Pet!! Nah mate. I fink you'd find dat you'd be my pet!! We is not makin good pets.")
                player<Quiz>("Really? Why not?")
                npc<Neutral>("imp", "Coz...errr...")
                npc<Neutral>("imp", "We bite! Yeah we is biting and...and...er...")
                npc<Neutral>("imp", "We is fire risk! Yeah dat's it! We be burning down your housey and stealin' all ya shiny gems. Oh, and da beads!! Mmmm, beads.")
                player<Quiz>("Fire risk? How does that work?")
                npc<Neutral>("imp", "Is those wizzies. Dey don't like de imps so dey make us go BOOOM!!")
            }
            option<Neutral>("It's not that bad.") {
                player<Neutral>("It's not that bad. You've got four big windows, charming company...er...")
                npc<Neutral>("imp", "Yeah, we's love tiny, crampt space. It be magical. But I is a busy imp, innit? Dragons needin' ticklin', shiny relics needin' stealin', you know how it goes.")
                npc<Neutral>("imp", "So, if you's know whas good for ya, you'd be lettin' me go, right?")
                impOptions()
            }
            option<Quiz>("Don't I get three wishes?") {
                player<Quiz>("Don't I get three wishes?")
                npc<Neutral>("imp", "Nah, mate. Dunno what you're chirpin' about.")
                player<Neutral>("Well, you're a magical creature aren't you? Surely I get some wishes for capturing you, or releasing you, or something?")
                npc<Neutral>("imp", "I'm finking dat you be a bit confoosed. I is an imp, not some namby-pamby genie or some kinda fairy. Ye can tell by the horns.")
                npc<Neutral>("imp", "Sayin' dat, I don't fancy being cooped up like one of me uncle's pigeons. Tell you what, is there anything you need deliverin' to the bank?")
                npc<Neutral>("imp", "I may not be no cunjerer or sommink like dat, but I can get about nice an quick like. If you let me scarper, I'll take a couple of fings to the bank for ya. You game?")
                bankOptions()
            }
        }
    }

    private suspend fun Player.bankOptions() {
        choice {
            option<Happy>("Okay, that sounds fair.") {
                open("imp_box")
            }
            option<Quiz>("Surely it should be three items?") {
                player<Quiz>("Surely it should be three items? Then it's one item per wish.")
                npc<Neutral>("imp", "I've already told ya, I ain't no bloomin' fairy. Besides, you know wot dey say, three's a crowd innit? I don't fink I can hop about carryin' more dan 2 fings.")
                bankOptions()
            }
            option<Neutral>("I've got nothing I need banking right now.") {
                player<Neutral>("I've got nothing I need banking right now.")
                npc<Neutral>("imp", "Great, just blinkin great, dat is. I'll just sit about countin' zombie sheep then. One...two...two and a bit...three and a bit more... I don't fink sheep 'ave dat many legs...")
            }
        }
    }

    private suspend fun Player.layTrap(floorItem: FloorItem?) {
        val trap = Rows.getOrNull("traps.magic_box") ?: return
        val level = levels.get(Skill.Hunter)
        if (!has(Skill.Hunter, trap.int("level"), message = true)) {
            return
        }
        if (Areas.get(tile.zone).any { it.tags.contains("bank") } || GameObjects.getLayer(tile, ObjectLayer.GROUND) != null) {
            message("You can't lay a trap here.", ChatType.Filter)
            return
        }
        val max = Traps.max(level, trap.int("max"))
        val trapCount = get("trap_count", 0)
        if (trapCount >= max) {
            message("You may setup only $max ${"trap".plural(max)} at a time at your Hunter level.")
            return
        }
        arriveDelay()
        message("You begin setting up ${if (max == 1) "the" else "a"} trap.", ChatType.Filter)
        anim("lay_trap")
        sound("lay_box_trap")
        delay(3)
        if (floorItem != null) {
            FloorItems.remove(floorItem)
        } else {
            inventory.remove("magic_box")
        }
        inc("trap_count")
        NPCs.add("hunting_imptrap_npc", tile, ticks = 100, owner = this)
        val obj = GameObjects.add("magic_box", tile)
        stepAway(obj)
    }

    private suspend fun Player.dismantleTrap(target: GameObject, creature: RowDefinition?) {
        val npc = NPCs.findOrNull(target.tile, "hunting_imptrap_npc") ?: return
        if (npc["owner", ""] != accountName) {
            message("This is not your trap.")
            return
        }
        val loot = creature?.itemList("loot") ?: emptyList()
        val items = if (loot.isEmpty()) listOf("magic_box") else emptyList()
        val size = items.size + loot.size
        if (inventory.spaces < size) {
            val slots = size - inventory.spaces
            message("You don't have enough inventory space. You need $slots more free ${"slot".plural(slots)}.")
            return
        }
        anim("take_trap")
        sound("trap_dismantle", delay = 25)
        delay(2)
        collapse(npc, target)
        for (item in items) {
            inventory.add(item)
        }
        if (creature != null) {
            for (item in loot) {
                inventory.add(item)
            }
            exp(Skill.Hunter, creature.int("xp") / 10.0)
            message("You've caught an ${creature.rowId.toLowerSpaceCase()}!", ChatType.Filter)
        } else {
            message("You dismantle the trap.", ChatType.Filter)
        }
    }

    private fun Player.collapse(npc: NPC, target: GameObject) {
        dec("trap_count")
        NPCs.remove(npc)
        GameObjects.remove(target)
    }
}
