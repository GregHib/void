package content.skill.mining

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.removeToLimit
import world.gregs.voidps.engine.inv.transact.operation.AddItemLimit.addToLimit

class GemBag : Script {

    val bagCapacity = 100

    init {
        itemOption("Inspect", "gem_bag") {
            val sapphires = get("gem_bag_sapphire", 0)
            val emeralds = get("gem_bag_emerald", 0)
            val rubies = get("gem_bag_ruby", 0)
            val diamonds = get("gem_bag_diamond", 0)
            message("Your gem bag has $sapphires ${"sapphire".plural(sapphires)}, $emeralds ${"emerald".plural(emeralds)}, $rubies ${"ruby".plural(rubies)}, and $diamonds ${"diamond".plural(diamonds)}.")
        }

        itemOption("Withdraw", "gem_bag") {
            val sapphires = get("gem_bag_sapphire", 0)
            val emeralds = get("gem_bag_emerald", 0)
            val rubies = get("gem_bag_ruby", 0)
            val diamonds = get("gem_bag_diamond", 0)
            val total = sapphires + emeralds + rubies + diamonds
            if (total == 0) {
                message("Your gem bag is empty.")
                return@itemOption
            }
            val added = mutableMapOf<String, Int>()
            val success = inventory.transaction {
                if (sapphires > 0) {
                    added["sapphire"] = addToLimit("uncut_sapphire", sapphires)
                }
                if (emeralds > 0) {
                    added["emerald"] = addToLimit("uncut_emerald", emeralds)
                }
                if (rubies > 0) {
                    added["ruby"] = addToLimit("uncut_ruby", rubies)
                }
                if (diamonds > 0) {
                    added["diamond"] = addToLimit("uncut_diamond", diamonds)
                }
            }
            if (!success) {
                return@itemOption
            }
            set("gem_bag_sapphire", sapphires - added.getOrDefault("sapphire", 0))
            set("gem_bag_emerald", emeralds - added.getOrDefault("emerald", 0))
            set("gem_bag_ruby", rubies - added.getOrDefault("ruby", 0))
            set("gem_bag_diamond", diamonds - added.getOrDefault("diamond", 0))
            message("You withdraw some gems.")
        }

        itemOnItem("uncut_sapphire,uncut_emerald,uncut_ruby,uncut_diamond", "gem_bag") { fromItem, _ ->
            val sapphires = get("gem_bag_sapphire", 0)
            val emeralds = get("gem_bag_emerald", 0)
            val rubies = get("gem_bag_ruby", 0)
            val diamonds = get("gem_bag_diamond", 0)
            val total = sapphires + emeralds + rubies + diamonds
            if (total == bagCapacity) {
                message("Your gem bag is already full.")
                return@itemOnItem
            }
            val type = fromItem.id.removePrefix("uncut_")
            val limit = bagCapacity - total
            val removed = inventory.removeToLimit(fromItem.id, limit)
            if (removed == 0) {
                return@itemOnItem
            }
            set("gem_bag_$type", get("gem_bag_$type", 0) + removed)
            message("You add the gems to your bag.")
            if (total + removed == bagCapacity) {
                message("Your gem bag is now full.")
            }
        }

        destructible("gem_bag") {
            val sapphires = get("gem_bag_sapphire", 0)
            val emeralds = get("gem_bag_emerald", 0)
            val rubies = get("gem_bag_ruby", 0)
            val diamonds = get("gem_bag_diamond", 0)
            val total = sapphires + emeralds + rubies + diamonds
            if (total > 0) {
                message("You can't destroy this item with gems in it.")
                false
            } else {
                true
            }
        }
    }
}
