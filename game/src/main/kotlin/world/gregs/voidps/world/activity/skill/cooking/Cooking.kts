import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.awaitDialogues
import world.gregs.voidps.engine.client.ui.interact.InterfaceOnObject
import world.gregs.voidps.engine.entity.character.clearAnimation
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
import world.gregs.voidps.engine.entity.item.equipped
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.Objects
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.engine.utility.toSentenceCase
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.world.interact.dialogue.type.makeAmount

val definitions: ItemDefinitions by inject()
val objects: Objects by inject()

on<InterfaceOnObject>({ obj.heatSource && item.def.has("cooking") }) { player: Player ->
    player.action(ActionType.Cooking) {
        player.awaitDialogues()
        val definition = if (player["sinew", false]) definitions.get("sinew") else item.def
        player["sinew"] = false
        val cooking: Uncooked = definition.getOrNull("cooking") ?: return@action
        val (_, amount) = player.makeAmount(
            listOf(item.id),
            type = cooking.type.toSentenceCase(),
            maximum = player.inventory.count(item.id),
            text = "How many would you like to ${cooking.type}?"
        )

        if (amount <= 0) {
            return@action
        }

        try {
            var tick = 0
            while (isActive && tick < amount && player.awaitDialogues()) {
                if (objects[obj.tile, obj.id] == null) {
                    break
                }

                if (!player.has(Skill.Cooking, cooking.level, true)) {
                    break
                }

                if (cooking.leftover.isNotEmpty() && player.inventory.isFull()) {
                    player.inventoryFull()
                    break
                }

                if (cooking.rangeOnly && !obj.cookingRange) {
                    player.noInterest()
                    break
                }

                player.face(obj)
                player.setAnimation("cook_${if (obj.id.startsWith("fire_")) "fire" else "range"}")
                delay(when (tick) {
                    0 -> 1
                    1 -> 3
                    else -> 4
                })
                val level = player.levels.get(Skill.Cooking)
                val chance = when {
                    obj.id == "cooking_range_lumbridge_castle" -> cooking.cooksRangeChance
                    player.equipped(EquipSlot.Hands).id == "cooking_gauntlets" -> cooking.gauntletChance
                    obj.cookingRange -> cooking.rangeChance
                    else -> cooking.chance
                }

                tick++
                if (Level.success(level, chance)) {
                    val cooked = cooking.cooked.ifEmpty { item.id.replace("raw", "cooked") }
                    player.inventory.replace(item.id, cooked)
                    player.experience.add(Skill.Cooking, cooking.xp)
                    if (cooking.cookedMessage.isNotEmpty()) {
                        player.message(cooking.cookedMessage, ChatType.Filter)
                    }
                } else {
                    val burnt = cooking.burnt.ifEmpty { item.id.replace("raw", "burnt") }
                    player.inventory.replace(item.id, burnt)
                    if (cooking.burntMessage.isNotEmpty()) {
                        player.message(cooking.burntMessage, ChatType.Filter)
                    }
                }
                if (cooking.leftover.isNotEmpty()) {
                    player.inventory.add(cooking.leftover)
                }
            }
        } finally {
            player.clearAnimation()
        }
    }
}

val GameObject.cookingRange: Boolean
    get() = id.startsWith("cooking_range")

val GameObject.heatSource: Boolean
    get() = id.startsWith("fire_") || cookingRange