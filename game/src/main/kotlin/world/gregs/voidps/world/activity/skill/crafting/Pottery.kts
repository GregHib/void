import net.pearx.kasechange.toLowerSpaceCase
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.InterfaceOnObject
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.contain.replace
import world.gregs.voidps.engine.entity.character.face
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Level.has
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.definition.data.Pottery
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.queue.weakQueue
import world.gregs.voidps.world.interact.dialogue.type.makeAmount

val Item.pottery: Pottery
    get() = def["pottery"]

on<InterfaceOnObject>({ obj.id.startsWith("potters_wheel") && item.id == "soft_clay" }) { player: Player ->
    make("spinning")
}

on<InterfaceOnObject>({ obj.id.startsWith("potters_oven") && item.id != "soft_clay" && item.def.has("pottery") }) { player: Player ->
    make("cook_range")
}

suspend fun InterfaceOnObject.make(animation: String) {
    val pottery = item.pottery.list
    val (id, amount) = makeAmount(
        items = pottery.keys.toList(),
        type = "Make",
        maximum = 28
    )
    val current = player.inventory.count(item.id)
    if (current <= 0) {
        player.message("You need some ${item.id.toLowerSpaceCase()} in order to make a ${id.toLowerSpaceCase()}.")
        return
    }
    val data = pottery.getValue(id)
    val actualAmount = if (current < amount) current else amount
    player.make(animation, obj, item, id, data, actualAmount)
}

fun Player.make(animation: String, obj: GameObject, item: Item, id: String, data: Pottery.Ceramic, amount: Int) {
    if (amount <= 0) {
        return
    }
    val current = inventory.count(item.id)
    if (current <= 0) {
        message("You need some ${item.id.toLowerSpaceCase()} in order to make a ${id.toLowerSpaceCase()}.")
        return
    }
    face(obj)
    if (!has(Skill.Crafting, data.level)) {
        return
    }
    setAnimation(animation)
    weakQueue(3) {
        if (!inventory.replace(item.id, id)) {
            message("You need some ${item.id.toLowerSpaceCase()} in order to make a ${id.toLowerSpaceCase()}.")
            return@weakQueue
        }
        exp(Skill.Crafting, data.xp)
        make(animation, obj, item, id, data, amount - 1)
    }
}