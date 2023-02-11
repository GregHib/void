package world.gregs.voidps.world.activity.skill.crafting

import net.pearx.kasechange.toLowerSpaceCase
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.*
import world.gregs.voidps.engine.client.ui.event.InterfaceRefreshed
import world.gregs.voidps.engine.client.ui.interact.InterfaceOnObject
import world.gregs.voidps.engine.contain.inventory
import world.gregs.voidps.engine.contain.remove
import world.gregs.voidps.engine.contain.replace
import world.gregs.voidps.engine.data.definition.data.Jewellery
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerContext
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.members
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.queue.weakQueue
import world.gregs.voidps.world.activity.skill.slayer.unlocked
import world.gregs.voidps.world.interact.dialogue.type.intEntry
import kotlin.math.min

val moulds = listOf("ring", "necklace", "amulet_unstrung", "bracelet")
val gems = listOf("gold", "sapphire", "emerald", "ruby", "diamond", "dragonstone", "onyx", "enchanted_gem")

val Item.jewellery: Jewellery?
    get() = def.getOrNull("jewellery")

on<InterfaceOnObject>({ obj.id.startsWith("furnace") && item.id.endsWith("_mould") }) { player: Player ->
    player.open("make_mould${if (World.members) "_slayer" else ""}")
}

on<InterfaceRefreshed>({ id.startsWith("make_mould") }) { player: Player ->
    for (type in moulds) {
        val showText = !player.inventory.contains("${type}_mould")
        player.interfaces.sendVisibility(id, "${type}_text", showText)
        for (i in gems.indices) {
            if (showText) {
                player.interfaces.sendVisibility(id, "make_${type}_options_$i", false)
            } else {
                var item = Item("${if (player.inventory.contains("gold_bar") && (i == 0 || player.inventory.contains(gems[i]))) gems[i] else "blank"}_$type")
                if (item.id == "enchanted_gem_ring" && player.unlocked("ring_bling")) {
                    item = Item("ring_of_slaying_8")
                }
                val jewellery = item.jewellery
                if (jewellery == null || !player.has(Skill.Crafting, jewellery.level)) {
                    item = Item("blank_$type")
                }
                player.interfaces.sendVisibility(id, "make_${type}_options_$i", !item.id.startsWith("blank"))
                player.interfaces.sendItem(id, "make_${type}_$i", item)
            }
        }
    }
}

on<InterfaceOption>({ id.startsWith("make_mould") && component.startsWith("make_") && option != "Make X" }) { player: Player ->
    val amount = when (option) {
        "Make 1" -> 1
        "Make 5" -> 5
        "Make All" -> Int.MAX_VALUE
        else -> return@on
    }
    make(component, amount)
}

on<InterfaceOption>({ id.startsWith("make_mould") && component.startsWith("make_") && option == "Make X" }) { player: Player ->
    val amount = intEntry("Enter amount:")
    make(component, amount)
}

fun PlayerContext.make(component: String, amount: Int) {
    val type = component.split("options_").first().removePrefix("make_").removeSuffix("_")
    val index = component.split("_").last().toInt()
    val gem = gems[index]
    val item = Item(if (gem == "enchanted_gem" && type == "ring") "ring_of_slaying_8" else "${gem}_$type")
    val goldBars = player.inventory.count("gold_bar")
    val gems = if (gem == "gold") goldBars else player.inventory.count(gem)
    val current = min(goldBars, gems)
    val actualAmount = if (current < amount) current else amount
    player.closeInterface()
    player.make(item, gem, actualAmount)
}

fun Player.make(item: Item, gem: String, amount: Int) {
    if (amount <= 0) {
        return
    }
    val data = item.jewellery ?: return
    if (!has(Skill.Crafting, data.level)) {
        return
    }
    if (!inventory.contains("gold_bar")) {
        message("You need some gold bars in order to make a ${item.id.toLowerSpaceCase()}.")
        return
    }
    setAnimation("cook_range")
    weakQueue(3) {
        if (gem != "gold" && !inventory.remove(gem)) {
            message("You need some ${gem.toLowerSpaceCase()} in order to make a ${item.id.toLowerSpaceCase()}.")
            return@weakQueue
        }
        inventory.replace("gold_bar", item.id)
        exp(Skill.Crafting, data.xp)
        make(item, gem, amount - 1)
    }
}