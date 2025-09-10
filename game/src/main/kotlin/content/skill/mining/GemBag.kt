package content.skill.mining

import content.entity.player.inv.inventoryItem
import content.entity.player.inv.item.destroy.canDestroy
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.ui.interact.itemOnItems
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.removeToLimit
import world.gregs.voidps.engine.inv.transact.operation.AddItemLimit.addToLimit
import world.gregs.voidps.engine.event.Script
@Script
class GemBag {

    val bagCapacity = 100
    
    val gems = setOf("uncut_sapphire", "uncut_emerald", "uncut_ruby", "uncut_diamond")
    
    init {
        inventoryItem("Inspect", "gem_bag") {
            val sapphires = player["gem_bag_sapphire", 0]
            val emeralds = player["gem_bag_emerald", 0]
            val rubies = player["gem_bag_ruby", 0]
            val diamonds = player["gem_bag_diamond", 0]
            player.message("Your gem bag has $sapphires ${"sapphire".plural(sapphires)}, $emeralds ${"emerald".plural(emeralds)}, $rubies ${"ruby".plural(rubies)}, and $diamonds ${"diamond".plural(diamonds)}.")
        }

        inventoryItem("Withdraw", "gem_bag") {
            val sapphires = player["gem_bag_sapphire", 0]
            val emeralds = player["gem_bag_emerald", 0]
            val rubies = player["gem_bag_ruby", 0]
            val diamonds = player["gem_bag_diamond", 0]
            val total = sapphires + emeralds + rubies + diamonds
            if (total == 0) {
                player.message("Your gem bag is empty.")
                return@inventoryItem
            }
            val added = mutableMapOf<String, Int>()
            val success = player.inventory.transaction {
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
                return@inventoryItem
            }
            player["gem_bag_sapphire"] = sapphires - added.getOrDefault("sapphire", 0)
            player["gem_bag_emerald"] = emeralds - added.getOrDefault("emerald", 0)
            player["gem_bag_ruby"] = rubies - added.getOrDefault("ruby", 0)
            player["gem_bag_diamond"] = diamonds - added.getOrDefault("diamond", 0)
            player.message("You withdraw some gems.")
        }

        itemOnItems(gems.toTypedArray(), arrayOf("gem_bag")) { player ->
            val sapphires = player["gem_bag_sapphire", 0]
            val emeralds = player["gem_bag_emerald", 0]
            val rubies = player["gem_bag_ruby", 0]
            val diamonds = player["gem_bag_diamond", 0]
            val total = sapphires + emeralds + rubies + diamonds
            if (total == bagCapacity) {
                player.message("Your gem bag is already full.")
                return@itemOnItems
            }
            val type = fromItem.id.removePrefix("uncut_")
            val limit = bagCapacity - total
            val removed = player.inventory.removeToLimit(fromItem.id, limit)
            if (removed == 0) {
                return@itemOnItems
            }
            player["gem_bag_$type"] = player["gem_bag_$type", 0] + removed
            player.message("You add the gems to your bag.")
            if (total + removed == bagCapacity) {
                player.message("Your gem bag is now full.")
            }
        }

        canDestroy("gem_bag") { player ->
            val sapphires = player["gem_bag_sapphire", 0]
            val emeralds = player["gem_bag_emerald", 0]
            val rubies = player["gem_bag_ruby", 0]
            val diamonds = player["gem_bag_diamond", 0]
            val total = sapphires + emeralds + rubies + diamonds
            if (total > 0) {
                player.message("You can't destroy this item with gems in it.")
                cancel()
            }
        }

    }

}
