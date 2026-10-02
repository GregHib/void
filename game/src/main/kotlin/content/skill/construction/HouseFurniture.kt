package content.skill.construction

import content.entity.player.dialogue.type.choice
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.contains
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.inv.replace
import java.time.LocalTime
import java.time.ZoneOffset

/**
 * Furniture which can be used once built: storage to take items from, barrels to pour drinks, fireplaces to light and clocks to read.
 */
class HouseFurniture : Script {
    init {
        objectOperate("Search", STORAGE) { (target) ->
            val items = Tables.itemListOrNull("house_storage.${target.id}.items") ?: return@objectOperate
            take(items)
        }

        itemOnObjectOperate("beer_glass", BARRELS) { (target) ->
            val row = Rows.getOrNull("house_barrels.${target.id}") ?: return@itemOnObjectOperate
            val drink = row.item("drink")
            if (!inventory.replace("beer_glass", drink)) {
                return@itemOnObjectOperate
            }
            anim(row.string("anim"))
            message("You fill the glass with ${ItemDefinitions.get(drink).name.lowercase()}.") // TODO proper message
        }

        objectOperate("Light", FIREPLACES) { (target) ->
            val row = Rows.getOrNull("house_fireplaces.${target.id}") ?: return@objectOperate
            // Lit fireplaces can't be removed
            if (get("house_build_mode", false)) {
                message("You can't light the fireplace in building mode.") // TODO proper message
                return@objectOperate
            }
            if (!inventory.contains("tinderbox")) {
                message("You need a tinderbox to light the fireplace.") // TODO proper message
                return@objectOperate
            }
            if (!inventory.remove("logs")) {
                message("You need some logs to light the fireplace.") // TODO proper message
                return@objectOperate
            }
            target.replace(row.obj("lit"))
            anim("light_fireplace")
            exp(Skill.Firemaking, row.int("xp") / 10.0)
        }

        objectOperate("Read", CLOCKS) {
            message(clockTime(LocalTime.now(ZoneOffset.UTC).minute))
        }
    }

    /**
     * Pick one of [items] to take, four at a time when there are too many for one choice
     */
    private suspend fun Player.take(items: List<String>) {
        val page = if (items.size > 5) items.take(4) else items
        choice {
            for (item in page) {
                option(ItemDefinitions.get(item).name) {
                    if (!inventory.add(item)) {
                        inventoryFull()
                    }
                }
            }
            if (page.size < items.size) {
                option("More...") {
                    take(items.drop(page.size))
                }
            }
        }
    }

    companion object {
        private const val STORAGE = "wooden_larder,oak_larder,teak_larder,wooden_shelves_*,oak_shelves_*,teak_shelves_*,tool_store_*,glove_rack,weapons_rack,extra_weapons_rack"
        private const val BARRELS = "beer_barrel,cider_barrel,asgarnian_ale_barrel,greenmans_ale_barrel,dragon_bitter_barrel,chefs_delight_barrel"
        private const val FIREPLACES = "clay_fireplace,stone_fireplace,marble_fireplace"
        private const val CLOCKS = "oak_clock,teak_clock,gilded_clock"

        private val numbers = listOf("", "five", "ten", "a quarter", "twenty", "twenty-five", "half")

        /**
         * The time to the nearest five minutes, it's always Rune o'clock
         * https://oldschool.runescape.wiki/w/Oak_clock
         */
        fun clockTime(minute: Int): String {
            val rounded = (minute + 2) / 5 % 12
            return when {
                rounded == 0 -> "It's Rune o'clock."
                rounded <= 6 -> "It's ${numbers[rounded]} past Rune."
                else -> "It's ${numbers[12 - rounded]} to Rune."
            }
        }
    }
}
