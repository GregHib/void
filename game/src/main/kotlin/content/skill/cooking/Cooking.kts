package content.skill.cooking

import net.pearx.kasechange.toSentenceCase
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.closeDialogue
import world.gregs.voidps.engine.client.ui.interact.itemOnObjectOperate
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.data.Uncooked
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.character.player.chat.noInterest
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.replace
import world.gregs.voidps.engine.queue.weakQueue
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import content.entity.player.dialogue.type.makeAmount

val definitions: ItemDefinitions by inject()
val objects: GameObjects by inject()

val GameObject.cookingRange: Boolean get() = id.startsWith("cooking_range")

itemOnObjectOperate(objects = setOf("fire_*", "cooking_range*"), def = "cooking") {
    val start = GameLoop.tick
    val definition = if (player["sinew", false]) definitions.get("sinew") else if (item.id == "sinew") return@itemOnObjectOperate else item.def
    player["sinew"] = false
    val cooking: Uncooked = definition.getOrNull("cooking") ?: return@itemOnObjectOperate
    var amount = player.inventory.count(item.id)
    if (amount != 1) {
        amount = makeAmount(
            listOf(item.id),
            type = cooking.type.toSentenceCase(),
            maximum = player.inventory.count(item.id),
            text = "How many would you like to ${cooking.type}?"
        ).second
    }
    val offset = (4 - (GameLoop.tick - start)).coerceAtLeast(0)
    player.closeDialogue()
    player.softTimers.start("cooking")
    player.cook(item, amount, target, cooking, offset)
}

fun Player.cook(item: Item, count: Int, obj: GameObject, cooking: Uncooked, offset: Int? = null) {
    if (count <= 0 || objects[obj.tile, obj.id] == null) {
        softTimers.stop("cooking")
        return
    }

    if (!has(Skill.Cooking, cooking.level, true)) {
        softTimers.stop("cooking")
        return
    }

    if (cooking.leftover.isNotEmpty() && inventory.isFull()) {
        inventoryFull()
        softTimers.stop("cooking")
        return
    }

    if (cooking.rangeOnly && !obj.cookingRange) {
        noInterest()
        softTimers.stop("cooking")
        return
    }
    face(obj)
    anim("cook_${if (obj.id.startsWith("fire_")) "fire" else "range"}")
    weakQueue("cooking", offset ?: 4) {
        val level = levels.get(Skill.Cooking)
        val chance = when {
            obj.id == "cooking_range_lumbridge_castle" -> cooking.cooksRangeChance
            equipped(EquipSlot.Hands).id == "cooking_gauntlets" -> cooking.gauntletChance
            obj.cookingRange -> cooking.rangeChance
            else -> cooking.chance
        }
        if (failedToReplace(item, cooking, Level.success(level, chance))) {
            return@weakQueue
        }
        if (cooking.leftover.isNotEmpty() && !inventory.add(cooking.leftover)) {
            return@weakQueue
        }
        cook(item, count - 1, obj, cooking)
    }
}

fun Player.failedToReplace(item: Item, raw: Uncooked, cooked: Boolean): Boolean {
    val id = if (cooked) raw.cooked else raw.burnt
    val itemId = id.ifEmpty { item.id.replace("raw", if (cooked) "cooked" else "burnt") }
    println("Replace ${item.id} $itemId")
    if (!inventory.replace(item.id, itemId)) {
        return true
    }
    experience.add(Skill.Cooking, if (cooked) raw.xp else 0.0)
    val message = if (cooked) raw.cookedMessage else raw.burntMessage
    if (message.isNotEmpty()) {
        message(message, ChatType.Filter)
    }
    return false
}