package world.gregs.voidps.world.activity.skill.crafting

import net.pearx.kasechange.toLowerSpaceCase
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.itemOnObjectOperate
import world.gregs.voidps.engine.data.definition.data.Pottery
import world.gregs.voidps.engine.entity.character.face
import world.gregs.voidps.engine.entity.character.mode.interact.TargetObjectContext
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.replace
import world.gregs.voidps.engine.queue.weakQueue
import world.gregs.voidps.world.interact.dialogue.type.makeAmount

val Item.pottery: Pottery
    get() = def["pottery"]

itemOnObjectOperate("soft_clay", "potters_wheel*") {
    make("spinning", item)
}

itemOnObjectOperate(obj = "pottery_oven*", def = "pottery") {
    if (item.id != "soft_clay") {
        make("cook_range", item)
    }
}

objectOperate("Fire", "pottery_oven*", arrive = false) {
    val item = player.inventory.items.firstOrNull { it.def.contains("pottery") && it.id != "soft_clay" } ?: return@objectOperate
    make("cook_range", item)
}

suspend fun TargetObjectContext.make(animation: String, item: Item) {
    val pottery = item.pottery.map
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
    player.make(animation, target, item, id, data, actualAmount)
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
    weakQueue("make_pottery", 3) {
        if (!inventory.replace(item.id, id)) {
            message("You need some ${item.id.toLowerSpaceCase()} in order to make a ${id.toLowerSpaceCase()}.")
            return@weakQueue
        }
        exp(Skill.Crafting, data.xp)
        make(animation, obj, item, id, data, amount - 1)
    }
}