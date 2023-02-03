import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.InterfaceOnObject
import world.gregs.voidps.engine.entity.character.contain.add
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.contain.replace
import world.gregs.voidps.engine.entity.character.face
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.inventoryFull
import world.gregs.voidps.engine.entity.character.player.noInterest
import world.gregs.voidps.engine.entity.character.player.skill.Level
import world.gregs.voidps.engine.entity.character.player.skill.Level.has
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.definition.data.Uncooked
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.equipped
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.Objects
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.queue.weakQueue
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.engine.utility.toSentenceCase
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.world.interact.dialogue.type.makeAmount

val definitions: ItemDefinitions by inject()
val objects: Objects by inject()

on<InterfaceOnObject>({ obj.heatSource && item.def.has("cooking") }) { player: Player ->
    val definition = if (player["sinew", false]) definitions.get("sinew") else if (item.id == "sinew") return@on else item.def
    player["sinew"] = false
    val cooking: Uncooked = definition.getOrNull("cooking") ?: return@on
    val (_, amount) = makeAmount(
        listOf(item.id),
        type = cooking.type.toSentenceCase(),
        maximum = player.inventory.count(item.id),
        text = "How many would you like to ${cooking.type}?"
    )
    player.cook(item, amount, obj, cooking)
}

fun Player.cook(item: Item, count: Int, obj: GameObject, cooking: Uncooked) {
    if (count <= 0) {
        return
    }
    if (objects[obj.tile, obj.id] == null) {
        return
    }

    if (!has(Skill.Cooking, cooking.level, true)) {
        return
    }

    if (cooking.leftover.isNotEmpty() && inventory.isFull()) {
        inventoryFull()
        return
    }

    if (cooking.rangeOnly && !obj.cookingRange) {
        noInterest()
        return
    }
    weakQueue(4) {
        face(obj)
        setAnimation("cook_${if (obj.id.startsWith("fire_")) "fire" else "range"}")
        val level = levels.get(Skill.Cooking)
        val chance = when {
            obj.id == "cooking_range_lumbridge_castle" -> cooking.cooksRangeChance
            equipped(EquipSlot.Hands).id == "cooking_gauntlets" -> cooking.gauntletChance
            obj.cookingRange -> cooking.rangeChance
            else -> cooking.chance
        }
        if (Level.success(level, chance)) {
            val cooked = cooking.cooked.ifEmpty { item.id.replace("raw", "cooked") }
            if (!inventory.replace(item.id, cooked)) {
                return@weakQueue
            }
            experience.add(Skill.Cooking, cooking.xp)
            if (cooking.cookedMessage.isNotEmpty()) {
                message(cooking.cookedMessage, ChatType.Filter)
            }
        } else {
            val burnt = cooking.burnt.ifEmpty { item.id.replace("raw", "burnt") }
            if (!inventory.replace(item.id, burnt)) {
                return@weakQueue
            }
            if (cooking.burntMessage.isNotEmpty()) {
                message(cooking.burntMessage, ChatType.Filter)
            }
        }
        if (cooking.leftover.isNotEmpty() && !inventory.add(cooking.leftover)) {
            return@weakQueue
        }
        cook(item, count - 1, obj, cooking)
    }
}

val GameObject.cookingRange: Boolean
    get() = id.startsWith("cooking_range")

val GameObject.heatSource: Boolean
    get() = id.startsWith("fire_") || cookingRange