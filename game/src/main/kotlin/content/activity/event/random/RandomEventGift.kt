package content.activity.event.random

import content.entity.combat.inCombat
import content.entity.player.bank.noted
import content.entity.player.dialogue.type.skillLamp
import content.entity.player.dialogue.type.statement
import content.entity.player.inv.item.addOrDrop
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.Inventory
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.inv.sendInventory
import world.gregs.voidps.type.random

/**
 * Random event gift: the reward box every random event hands out. Opening it deals one reward per
 * category - coins scaled to the player's total level, a rune, coal, essence by Mining level, an
 * ore, bar, gem, herb, seed, charm, an experience lamp, an oddment and a mystery box - into the gift's own inventory,
 * alongside the emote unlock and save-up-for-a-costume choices, shown on the "Choose Your Reward!"
 * interface (202). The choices keep until the player selects one and confirms, consuming the gift.
 * https://runescape.wiki/w/Random_event_gift
 */
class RandomEventGift : Script {

    init {
        itemOption("Open", "random_event_gift") {
            if (inCombat) {
                message("You won't be able to choose a reward during combat.")
                return@itemOption
            }
            if (gift.isEmpty()) {
                fillGift()
            }
            set("gift_selected", 0)
            // The interface's own load script draws the reward grid from the gift inventory, so it
            // only needs the container contents up front and click masks - no options script.
            sendInventory(gift)
            open("random_event_gift_select")
        }

        interfaceOpened("random_event_gift_select") { id ->
            interfaceOptions.unlockAll(id, "rewards", 0 until gift.size * 8)
            interfaceOptions.unlockAll(id, "confirm", 0 until gift.size * 8)
        }

        interfaceOption("Select", "random_event_gift_select:rewards") {
            // The grid packs seven sub-components per reward and doesn't attach the item to clicks
            val slot = if (it.item.isEmpty()) it.itemSlot / 7 else gift.indexOf(it.item.id)
            // The emote and costume cells are drawn by the interface itself, so their inventory
            // slots stay empty
            if (slot == EMOTE_SLOT || slot == COSTUME_SLOT || (slot in gift.indices && gift[slot].isNotEmpty())) {
                set("gift_selected", slot + 1)
            }
        }

        interfaceOption("Confirm", "random_event_gift_select:confirm") {
            confirm()
        }

        itemOption("Rub", "lamp") { (item, slot) ->
            val skill = skillLamp()
            // Constitution is measured in tenths internally (level 10 = 100)
            val level = if (skill == Skill.Constitution) levels.getMax(skill) / 10 else levels.getMax(skill)
            val experience = level * XP_PER_LEVEL
            if (inventory.remove(slot, item.id)) {
                exp(skill, experience)
                statement("<blue>Your wish has been granted!<br><black>You have been awarded ${experience.toInt()} ${skill.name} experience!")
            }
        }
    }

    private val Player.gift: Inventory
        get() = inventories.inventory("random_event_gift")

    /**
     * Deal one reward into each category's slot; the arrangement mirrors the official interface
     * layout with coins first and the surprises at the bottom.
     */
    private fun Player.fillGift() {
        val mining = levels.getMax(Skill.Mining)
        gift.transaction {
            set(COINS_SLOT, Item("coins", coins()))
            set(RUNE_SLOT, roll("gift_runes"))
            set(COAL_SLOT, Item("coal", random.nextInt(3, 31)))
            set(
                ESSENCE_SLOT,
                if (mining >= 30) {
                    Item("pure_essence", random.nextInt(18, 47))
                } else {
                    Item("rune_essence", random.nextInt(14, 22))
                },
            )
            set(ORE_SLOT, roll("gift_ores"))
            set(BAR_SLOT, roll("gift_bars"))
            set(GEM_SLOT, roll("gift_gems"))
            set(HERB_SLOT, roll("gift_herbs"))
            set(SEED_SLOT, roll("gift_seeds"))
            set(CHARM_SLOT, roll("gift_charms"))
            set(LAMP_SLOT, Item("lamp"))
            set(OTHER_SLOT, roll("gift_other"))
            set(SURPRISE_SLOT, Item("mystery_box"))
            // The grid only draws cells whose slot holds an item; these two stand in for the
            // "Unlock emote!" and "Save up for a costume!" choices and are never granted.
            set(EMOTE_SLOT, Item("mime_mask"))
            set(COSTUME_SLOT, Item("zombie_mask"))
        }
    }

    /** https://runescape.wiki/w/Random_event_gift - coins scale to a third of the total level. */
    private fun Player.coins(): Int {
        val total = Skill.all.sumOf { if (it == Skill.Constitution) levels.getMax(it) / 10 else levels.getMax(it) }
        return (total * 0.33).toInt().coerceAtLeast(30)
    }

    private fun roll(table: String): Item {
        val rows = Tables.get(table).rows()
        var roll = random.nextInt(rows.sumOf { it.int("weight") })
        val row = rows.first {
            roll -= it.int("weight")
            roll < 0
        }
        return Item(row.item("item"), random.nextInt(row.int("min"), row.int("max") + 1))
    }

    private fun Player.confirm() {
        val slot = get("gift_selected", 0) - 1
        if (slot < 0) {
            message("Choose a reward first.")
            return
        }
        if (slot == EMOTE_SLOT) {
            unlockEmote()
            return
        }
        if (slot == COSTUME_SLOT) {
            saveForCostume()
            return
        }
        if (slot !in gift.indices) {
            return
        }
        val reward = gift[slot]
        if (reward.isEmpty() || !inventory.remove("random_event_gift")) {
            return
        }
        inventories.clear("random_event_gift")
        // Unstackable rewards come noted so a multi-item prize doesn't flood the inventory
        val prize = if (reward.def.stackable != 1) reward.noted ?: reward else reward
        addOrDrop(prize.id, prize.amount)
        close("random_event_gift_select")
        message("Enjoy your gift.")
    }

    private fun Player.saveForCostume() {
        if (!inventory.remove("random_event_gift")) {
            return
        }
        val points = inc("costume_points")
        inventories.clear("random_event_gift")
        close("random_event_gift_select")
        message("Enjoy your gift.")
        message("You save up for a costume; you now have $points costume point${if (points == 1) "" else "s"}.")
    }

    private fun Player.unlockEmote() {
        val locked = EMOTES.filter { !get("unlocked_emote_$it", false) }
        if (locked.isEmpty()) {
            message("You've already unlocked all of the emotes.")
            return
        }
        if (!inventory.remove("random_event_gift")) {
            return
        }
        val emote = locked.random(random)
        set("unlocked_emote_$emote", true)
        inventories.clear("random_event_gift")
        close("random_event_gift_select")
        message("Enjoy your gift.")
        message("You've unlocked the ${emote.replace('_', ' ')} emote!")
    }

    companion object {
        private const val COINS_SLOT = 0
        private const val RUNE_SLOT = 1
        private const val COAL_SLOT = 2
        private const val ESSENCE_SLOT = 3
        private const val ORE_SLOT = 4
        private const val BAR_SLOT = 5
        private const val GEM_SLOT = 6
        private const val HERB_SLOT = 7
        private const val SEED_SLOT = 8
        private const val CHARM_SLOT = 9
        private const val LAMP_SLOT = 10

        // A genie lamp grants ten experience per level in the chosen skill
        private const val XP_PER_LEVEL = 10.0

        private const val OTHER_SLOT = 24
        private const val SURPRISE_SLOT = 25
        private const val EMOTE_SLOT = 26
        private const val COSTUME_SLOT = 27

        private val EMOTES = listOf("idea", "stomp", "flap", "slap_head")
    }
}
