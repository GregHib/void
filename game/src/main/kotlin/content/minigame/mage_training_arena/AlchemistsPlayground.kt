package content.minigame.mage_training_arena

import content.entity.player.bank.bank
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.entity.player.inv.item.addOrDrop
import content.entity.player.modal.Tab
import content.entity.player.modal.tab
import content.skill.magic.Magic
import content.skill.magic.spell.SpellRunes
import content.skill.magic.spell.SpellRunes.removeItems
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.config.RowDefinition
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.event.AuditLog
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.type.random

/**
 * Eight cupboards whose contents rotate; alching the arena items makes arena coins that are
 * deposited for points, with a share banked as real coins on the way out.
 *
 * The Alchemy Guardian is the room's controller: its timer rotates the cupboards and it holds the
 * current item values, rotation and free item.
 */
class AlchemistsPlayground : Script {

    private val rows: List<RowDefinition>
        get() = Tables.get("mta_alchemist_items").rows()

    private val items: List<String>
        get() = rows.map { it.item("item") }

    init {
        objectOperate("Search", "cupboard_mage_training_arena_*") { (target) ->
            val slot = target.id.removePrefix("cupboard_mage_training_arena_").removeSuffix("_open").toInt() - 1
            if (!target.id.endsWith("_open")) {
                anim("mta_search_cupboard")
                target.replace("${target.id}_open", ticks = 35)
            }
            val item = guardian(this)?.let { itemAt(it, slot) }
            if (item == null) {
                message("The cupboard is empty.")
                return@objectOperate
            }
            if (!inventory.add(item)) {
                message("You have no free space to hold any more items.")
                return@objectOperate
            }
            message("You found: ${ItemDefinitions.get(item).name}")
        }

        onItem("modern_spellbook:*_level_alchemy", items.joinToString(",")) { item, id ->
            if (!MageTrainingArena.inRoom(this, "alchemist")) {
                message("This item isn't yours to alch, it belongs to the arena!")
                return@onItem
            }
            val spell = id.substringAfter(":")
            val level = SpellRunes.magicLevel("modern_spellbook", spell) ?: 0
            if (!has(Skill.Magic, level, message = true)) {
                return@onItem
            }
            val guardian = guardian(this) ?: return@onItem
            val value: Int = guardian["mta_value_${item.id}", 0]
            if (value <= 0) {
                return@onItem
            }
            tab(Tab.Inventory)
            val eject = Tables.int("mta_alchemist.settings.eject_amount")
            if (inventory.count("coins_mage_training_arena") + value > eject) {
                message("Warning: You can't deposit more than $eject coins at a time.")
            }
            inventory.transaction {
                if (item.id != guardian["mta_free_item", ""]) {
                    removeItems(this@onItem, spell)
                }
                remove(item.id)
                add("coins_mage_training_arena", value)
            }
            when (inventory.transaction.error) {
                is TransactionError.Full -> inventoryFull("room in your inventory")
                TransactionError.None -> {
                    val row = Rows.get("spells.$spell")
                    anim(Magic.animation(this, row))
                    gfx(Magic.graphic(this, row))
                    sound(spell)
                    exp(Skill.Magic, row.int("xp") / 10.0)
                    tab(Tab.MagicSpellbook)
                }
                else -> return@onItem
            }
        }

        objectOperate("Deposit", "coin_collector") {
            val deposit = inventory.count("coins_mage_training_arena")
            if (deposit == 0) {
                statement("You don't have any coins to deposit.")
                return@objectOperate
            }
            val eject = Tables.int("mta_alchemist.settings.eject_amount")
            if (deposit >= eject) {
                tele(MageTrainingArena.lobby)
                statement("You have been ejected from the arena! You were warned not to deposit more than $eject coins at once.")
                return@objectOperate
            }
            if (!inventory.remove("coins_mage_training_arena", deposit)) {
                return@objectOperate
            }
            val points = deposit / 100
            val xp = deposit * 2
            val reward = inc("mage_training_arena_alchemist_reward", points * 10, max = Tables.int("mta_alchemist.settings.reward_cap"))
            PizazzPoints.add(this, "alchemist", points)
            exp(Skill.Magic, xp.toDouble())
            AuditLog.event(this, "mta_alchemist_deposit", deposit, points, reward)
            statement("You've just deposited $deposit coins, earning you $points Alchemist Pizazz Points and $xp magic XP. So far you're taking $reward coins as a reward when you leave!")
        }

        npcOperate("Talk-to", "alchemy_guardian") {
            player<Neutral>("Hi.")
            npc<Neutral>("Greetings young one. What wisdom do you seek?")
            guardianMenu()
        }

        entered("mage_training_arena_alchemists_playground") {
            val guardian = guardian(this) ?: return@entered
            refresh(this, guardian)
        }

        exited("mage_training_arena_alchemists_playground") {
            payout(this)
        }

        npcSpawn("alchemy_guardian") {
            shuffle(this)
            softTimers.start("mta_alchemist")
        }

        npcTimerStart("mta_alchemist") {
            Tables.int("mta_alchemist.settings.interval")
        }

        npcTimerTick("mta_alchemist") {
            rotate(this)
            Timer.CONTINUE
        }
    }

    private suspend fun Player.guardianMenu() {
        choice {
            option<Quiz>("What do I have to do in this room?") {
                npc<Neutral>("In this room you will see various cupboards. It is your task to search the cupboards to find items to turn into gold using your low or high alchemy spells. You must deposit the money in the receptacle at the end of the")
                npc<Neutral>("hall in order to receive your Alchemist Pizazz Points, otherwise the money will be taken from you as you leave through the portal. This money is used for the upkeep of the training arena as well as magic shops all")
                npc<Neutral>("around Gielinor. Keep an eye on the cost of each items as these will change from time-to-time, as will the location of the items. Occasionally one of the items will be indicated as costing no runestones to convert to money.")
                guardianMenu()
            }
            option<Quiz>("What are the rewards?") {
                npc<Neutral>("You will get experience from casting the alchemist spells, as well as 1 Alchemist Pizazz Point for every 100 coins you deposit, and 10% of the coins you deposit will be given to you as you leave. Keep in mind that you")
                npc<Neutral>("will not be able to take more than 1000 coins back out with you.")
                guardianMenu()
            }
            option<Quiz>("Got any tips that may help me?") {
                npc<Neutral>("You must remember to keep an eye on the various costs of the items. If you watch the movements of the other players, you might be able to guess which are the best places to visit. You will get 1 Pizazz Point for")
                npc<Neutral>("every 100 coins, so if you have 190 coins, why not get an extra 10?")
                player<Quiz>("I see.")
                npc<Neutral>("Oh, and a word of warning: should you decide to leave this room by a method other than the exit portals, you will be teleported to the entrance and have any items that you picked up in the room removed.")
                guardianMenu()
            }
            option<Neutral>("Thanks, bye!") {
                npc<Neutral>("use what you've learned, young one.")
            }
        }
    }

    /**
     * Converts leftover arena coins into points on logout and banks the accumulated reward.
     */
    private fun payout(player: Player) {
        val coins = player.inventory.count("coins_mage_training_arena")
        if (coins > 0) {
            if (player["logged_out", false]) {
                PizazzPoints.add(player, "alchemist", coins / 100)
            }
            player.inventory.remove("coins_mage_training_arena", coins)
        }
        val reward = player["mage_training_arena_alchemist_reward", 0]
        if (reward <= 0) {
            return
        }
        player["mage_training_arena_alchemist_reward"] = 0
        val banked = player.bank.transaction { add("coins", reward) }
        if (!banked) {
            player.addOrDrop("coins", reward)
        }
        AuditLog.event(player, "mta_alchemist_reward", reward, banked)
        player.message("You've been awarded $reward coins straight into your bank as a reward!")
    }

    private fun guardian(player: Player): NPC? = NPCs.findOrNull(player.tile.regionLevel, "alchemy_guardian")

    /**
     * Which item a cupboard holds after the guardian's current rotation; slots past the item count are empty.
     */
    private fun itemAt(guardian: NPC, slot: Int): String? {
        val index = (slot - guardian["mta_rotation", 0]).mod(8)
        return rows.firstOrNull { it.int("slot") == index }?.item("item")
    }

    private fun shuffle(guardian: NPC) {
        val list = Tables.intList("mta_alchemist.settings.values").shuffled(random)
        for ((index, item) in items.withIndex()) {
            guardian["mta_value_$item"] = list[index % list.size]
        }
    }

    private fun rotate(guardian: NPC) {
        guardian["mta_rotation"] = (guardian["mta_rotation", 0] + 1) % 8
        shuffle(guardian)
        if (guardian.contains("mta_free_item")) {
            guardian.clear("mta_free_item")
            guardian.say("The costs are changing!")
        } else {
            val row = rows.random(random)
            guardian["mta_free_item"] = row.item("item")
            val name = row.string("name")
            guardian.say("The $name ${if (name.endsWith("s")) "are" else "is"} free to convert!")
        }
        for (player in Players) {
            if (MageTrainingArena.inRoom(player, "alchemist")) {
                refresh(player, guardian)
            }
        }
    }

    private fun refresh(player: Player, guardian: NPC) {
        val overlay = "mage_training_arena_alchemist"
        val free: String = guardian["mta_free_item", ""]
        for (item in items) {
            player.interfaces.sendText(overlay, "value_$item", guardian["mta_value_$item", 0].toString())
            player.interfaces.sendVisibility(overlay, "free_$item", item == free)
        }
    }
}
