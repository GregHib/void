package content.skill.crafting

import com.github.michaelbull.logging.InlineLogger
import content.entity.player.dialogue.type.intEntry
import net.pearx.kasechange.toLowerSpaceCase
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.ui.closeMenu
import world.gregs.voidps.engine.client.ui.event.InterfaceRefreshed
import world.gregs.voidps.engine.client.ui.event.interfaceClose
import world.gregs.voidps.engine.client.ui.event.interfaceRefresh
import world.gregs.voidps.engine.client.ui.interact.itemOnObjectOperate
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.data.Jewellery
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Context
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.engine.inv.transact.operation.ReplaceItem.replace
import world.gregs.voidps.engine.queue.weakQueue

@Script
class Jewellery {

    val moulds = listOf("ring", "necklace", "amulet_unstrung", "bracelet")
    val gems = listOf("gold", "sapphire", "emerald", "ruby", "diamond", "dragonstone", "onyx", "enchanted_gem")

    val logger = InlineLogger()

    val Item.jewellery: Jewellery?
        get() = def.getOrNull("jewellery")

    init {
        itemOnObjectOperate("*_mould", "furnace*", arrive = false) {
            player.open("make_mould${if (World.members) "_slayer" else ""}")
        }

        interfaceRefresh("make_mould*") { player ->
            makeMould(player)
        }

        interfaceOption("Make *", "make*", "make_mould*") {
            val amount = when (option) {
                "Make 1" -> 1
                "Make 5" -> 5
                "Make All" -> Int.MAX_VALUE
                "Make X" -> intEntry("Enter amount:")
                else -> return@interfaceOption
            }
            make(component, amount)
        }

        interfaceClose("make_mould*") { player ->
            player.sendScript("clear_dialogues")
        }
    }

    fun InterfaceRefreshed.makeMould(player: Player) {
        for (type in moulds) {
            val showText = !player.inventory.contains("${type}_mould")
            player.interfaces.sendVisibility(id, "${type}_text", showText)
            for (gem in gems) {
                if (showText) {
                    player.interfaces.sendVisibility(id, "make_${type}_option_$gem", false)
                } else {
                    var item = Item("${if (player.inventory.contains("gold_bar") && (gem == "gold" || player.inventory.contains(gem))) gem else "blank"}_$type")
                    if (item.id == "enchanted_gem_ring" && player.contains("ring_bling")) {
                        item = Item("ring_of_slaying_8")
                    }
                    val jewellery = item.jewellery
                    if (jewellery == null || !player.has(Skill.Crafting, jewellery.level)) {
                        item = Item("blank_$type")
                    }
                    player.interfaces.sendVisibility(id, "make_${type}_option_$gem", !item.id.startsWith("blank"))
                    player.interfaces.sendItem(id, "make_${type}_$gem", item)
                }
            }
        }
    }

    fun Context<Player>.make(component: String, amount: Int) {
        val split = component.removePrefix("make_").split("_option_")
        val type = split.first()
        val gem = split.last()
        val item = Item(if (gem == "enchanted_gem" && type == "ring") "ring_of_slaying_8" else "${gem}_$type")
        player.closeMenu()
        player.make(item, gem, amount)
    }

    fun Player.make(item: Item, gem: String, amount: Int) {
        if (amount <= 0) {
            return
        }
        val data = item.jewellery ?: return
        if (!has(Skill.Crafting, data.level, message = true)) {
            return
        }
        if (!inventory.contains("gold_bar")) {
            message("You need some gold bars in order to make a ${item.id.toLowerSpaceCase()}.")
            return
        }
        if (gem != "gold" && !inventory.contains(gem)) {
            message("You need some ${gem.toLowerSpaceCase()} in order to make a ${item.id.toLowerSpaceCase()}.")
            return
        }
        anim("cook_range")
        weakQueue("make_jewllery", 3) {
            inventory.transaction {
                if (gem != "gold") {
                    remove(gem)
                }
                replace("gold_bar", item.id)
            }
            when (inventory.transaction.error) {
                TransactionError.None -> {
                    exp(Skill.Crafting, data.xp)
                    make(item, gem, amount - 1)
                }
                else -> logger.warn { "Error crafting jewellery ${inventory.transaction.error} ${player.name} $item $gem $amount ${player.inventory.items.toList()}" }
            }
        }
    }
}
