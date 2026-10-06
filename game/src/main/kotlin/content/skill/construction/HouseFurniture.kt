package content.skill.construction

import content.entity.player.dialogue.type.choice
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.an
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.inv.replace
import world.gregs.voidps.type.random
import java.time.LocalTime
import java.time.ZoneOffset

/**
 * Furniture which can be used once built: storage to take items from, barrels to pour drinks, fireplaces and lamps to light and clocks to read.
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

        objectOperate("Light", LIGHTS) { (target) ->
            light(target)
        }

        itemOnObjectOperate("tinderbox,logs,clean_marrentill", LIGHTS) { (target, item) ->
            if (item.id == "tinderbox" || item.id == Tables.itemOrNull("house_lights.${target.id}.item")) {
                light(target)
            }
        }

        objectOperate("Read", CLOCKS) {
            message(clockTime(LocalTime.now(ZoneOffset.UTC).minute))
        }
    }

    /**
     * Lights [target] with a tinderbox and its item, burning out after a random time depending on Firemaking level if it has ticks
     */
    private fun Player.light(target: GameObject) {
        val row = Rows.getOrNull("house_lights.${target.id}") ?: return
        if (get("house_build_mode", false)) {
            message("You can't light the fire in building mode.")
            return
        }
        if (!has(Skill.Firemaking, row.int("level"), message = true)) {
            return
        }
        if (!inventory.contains("tinderbox")) {
            message("You need a tinderbox to light that.") // TODO proper message
            return
        }
        val item = row.itemOrNull("item")
        if (item != null && !inventory.remove(item)) {
            message("You need ${ItemDefinitions.get(item).name.lowercase()} to light that.") // TODO proper message
            return
        }
        val ticks = row.int("ticks")
        val level = levels.get(Skill.Firemaking)
        target.replace(row.obj("lit"), ticks = if (ticks == 0) -1 else ticks + level + random.nextInt(level))
        anim(row.string("anim"))
        exp(Skill.Firemaking, row.int("xp") / 10.0)
    }

    private suspend fun Player.take(items: List<String>) {
        val index = pick(items.map { Rows.getOrNull("house_storage_items.$it")?.string("option") ?: ItemDefinitions.get(it).name })
        if (index == -1) {
            return
        }
        val item = items[index]
        if (!inventory.add(item)) {
            inventoryFull()
            return
        }
        val name = ItemDefinitions.get(item).name.lowercase()
        message(Rows.getOrNull("house_storage_items.$item")?.stringOrNull("message") ?: "You take${name.an()} $name.".replace("  ", " "))
    }

    companion object {
        private const val STORAGE = "wooden_larder,oak_larder,teak_larder,wooden_shelves_*,oak_shelves_*,teak_shelves_*,tool_store_*,glove_rack,weapons_rack,extra_weapons_rack"
        private const val BARRELS = "beer_barrel,cider_barrel,asgarnian_ale_barrel,greenmans_ale_barrel,dragon_bitter_barrel,chefs_delight_barrel"
        private const val LIGHTS = "clay_fireplace,stone_fireplace,marble_fireplace,wooden_torches,steel_torches,steel_candlesticks,gold_candlesticks,incense_burners,mahogany_burners,marble_burners"
        private val burners = setOf("incense_burners_lit", "mahogany_burners_lit", "marble_burners_lit")
        private const val CLOCKS = "oak_clock,teak_clock,gilded_clock"

        /**
         * Pick one of [options], four at a time with a "More..." option when there are too many for one choice.
         * Returns the index picked or -1 if none were.
         */
        suspend fun Player.pick(options: List<String>, offset: Int = 0): Int {
            if (options.size == 1) {
                return 0
            }
            val remaining = options.size - offset
            val count = if (remaining > 5) 4 else remaining
            val lines = options.subList(offset, offset + count)
            val choice = choice(if (count < remaining) lines + "More..." else lines)
            return when {
                choice == -1 -> -1
                choice > count -> pick(options, offset + count)
                else -> offset + choice - 1
            }
        }

        /**
         * Multiplier of bones experience offered on a house [altar], half again for each lit incense burner in its room up to two
         * https://oldschool.runescape.wiki/w/Chapel
         */
        fun altarBonus(altar: GameObject): Double? {
            val bonus = Tables.intOrNull("house_altars.${altar.id}.bonus") ?: return null
            val lit = altar.tile.zone.toCuboid().sumOf { tile -> GameObjects.at(tile).count { it.id in burners } }
            return (bonus + lit.coerceAtMost(2) * 50) / 100.0
        }

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
