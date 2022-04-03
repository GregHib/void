import net.pearx.kasechange.toLowerSpaceCase
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.awaitDialogues
import world.gregs.voidps.engine.client.ui.dialogue.dialogue
import world.gregs.voidps.engine.client.ui.interact.InterfaceOnObject
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.face
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Level.has
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.definition.data.Pottery
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.dialogue.type.makeAmount

val Item.pottery: Pottery
    get() = def["pottery"]

on<InterfaceOnObject>({ obj.id.startsWith("potters_wheel") && item.id == "soft_clay" }) { player: Player ->
    val pottery = item.pottery.list
    player.dialogue {
        val (id, amount) = makeAmount(
            items = pottery.keys.toList(),
            type = "Make",
            maximum = 28
        )
        val current = player.inventory.getCount("soft_clay").toInt()
        if (current <= 0) {
            player.message("You need some soft clay in order to make a ${id.toLowerSpaceCase()}.")
            return@dialogue
        }
        val data = pottery.getValue(id)
        val actualAmount = if (current < amount) current else amount
        player.face(obj)
        player.action(ActionType.Pottery) {
            if (!player.has(Skill.Crafting, data.level)) {
                return@action
            }
            if (actualAmount <= 0) {
                return@action
            }
            var tick = 0
            while (isActive && player.awaitDialogues() && tick < actualAmount) {
                player.setAnimation("spinning")
                delay(3)
                if (!player.inventory.replace(item.id, id)) {
                    player.message("You need some ${item.id.toLowerSpaceCase()} in order to make a ${id.toLowerSpaceCase()}.")
                    break
                }
                player.exp(Skill.Crafting, data.xp)
                tick++
            }
        }
    }
}

on<InterfaceOnObject>({ obj.id.startsWith("potters_oven") && item.id != "soft_clay" && item.def.has("pottery") }) { player: Player ->
    val pottery = item.pottery.list
    player.dialogue {
        val (id, amount) = makeAmount(
            items = pottery.keys.toList(),
            type = "Make",
            maximum = 28
        )
        val current = player.inventory.getCount(id).toInt()
        if (current <= 0) {
            player.message("You need some ${item.id.toLowerSpaceCase()} in order to make a ${id.toLowerSpaceCase()}.")
            return@dialogue
        }
        val data = pottery.getValue(id)
        val actualAmount = if (current < amount) current else amount
        player.face(obj)
        player.action(ActionType.Pottery) {
            if (!player.has(Skill.Crafting, data.level)) {
                return@action
            }
            if (actualAmount <= 0) {
                return@action
            }
            var tick = 0
            while (isActive && player.awaitDialogues() && tick < actualAmount) {
                player.setAnimation("cook_range")
                delay(3)
                if (!player.inventory.replace(item.id, id)) {
                    player.message("You need some ${item.id.toLowerSpaceCase()} in order to make a ${id.toLowerSpaceCase()}.")
                    break
                }
                player.exp(Skill.Crafting, data.xp)
                tick++
            }
        }
    }
}

