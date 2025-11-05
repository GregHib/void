package content.skill.crafting

import content.entity.player.dialogue.type.makeAmount
import net.pearx.kasechange.toLowerSpaceCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.data.Pottery
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.replace
import world.gregs.voidps.engine.queue.weakQueue

class Pottery : Script {

    init {
        itemOnObjectOperate("soft_clay", "potters_wheel*", arrive = false) { (target, item) ->
            make(target, "spinning", item)
        }

        itemOnObjectOperate(obj = "pottery_oven*", arrive = false) { (target, item) ->
            if (!item.def.contains("pottery")) {
                return@itemOnObjectOperate
            }
            if (item.id != "soft_clay") {
                make(target, "cook_range", item)
            }
        }

        objectOperate("Fire", "pottery_oven*", arrive = false) { (target) ->
            val item = inventory.items.firstOrNull { it.def.contains("pottery") && it.id != "soft_clay" } ?: return@objectOperate
            make(target, "cook_range", item)
        }
    }

    suspend fun Player.make(target: GameObject, animation: String, item: Item) {
        val pottery = item.def.getOrNull<Pottery>("pottery")?.map ?: return
        val (id, amount) = makeAmount(
            items = pottery.keys.toList(),
            type = "Make",
            maximum = 28,
        )
        val current = inventory.count(item.id)
        if (current <= 0) {
            message("You need some ${item.id.toLowerSpaceCase()} in order to make a ${id.toLowerSpaceCase()}.")
            return
        }
        val data = pottery.getValue(id)
        val actualAmount = if (current < amount) current else amount
        softTimers.start("pottery")
        make(animation, target, item, id, data, actualAmount)
    }

    fun Player.make(animation: String, obj: GameObject, item: Item, id: String, data: Pottery.Ceramic, amount: Int) {
        if (amount <= 0) {
            softTimers.stop("pottery")
            return
        }
        val current = inventory.count(item.id)
        if (current <= 0) {
            message("You need some ${item.id.toLowerSpaceCase()} in order to make a ${id.toLowerSpaceCase()}.")
            softTimers.stop("pottery")
            return
        }
        face(obj)
        if (!has(Skill.Crafting, data.level)) {
            message("You need a Crafting level of ${data.level} to make a ${id.toLowerSpaceCase()}.")
            softTimers.stop("pottery")
            return
        }
        anim(animation)
        weakQueue("make_pottery", 3) {
            if (!inventory.replace(item.id, id)) {
                message("You need some ${item.id.toLowerSpaceCase()} in order to make a ${id.toLowerSpaceCase()}.")
                softTimers.stop("pottery")
                return@weakQueue
            }
            player.sound("pottery")
            exp(Skill.Crafting, data.xp)
            make(animation, obj, item, id, data, amount - 1)
            message("You make the clay into a ${id.toLowerSpaceCase()}.")
        }
    }
}
