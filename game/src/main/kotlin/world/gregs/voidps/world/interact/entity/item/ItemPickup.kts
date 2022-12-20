package world.gregs.voidps.world.interact.entity.item

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.entity.character.contain.add
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.contain.transact.TransactionError
import world.gregs.voidps.engine.entity.character.face
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.cantReach
import world.gregs.voidps.engine.entity.character.player.inventoryFull
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.hasEffect
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.entity.item.floor.FloorItemClick
import world.gregs.voidps.engine.entity.item.floor.FloorItemOption
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.Delta
import world.gregs.voidps.engine.map.collision.CollisionFlag
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.map.collision.blocked
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.world.interact.entity.sound.playSound
import kotlin.math.abs

val items: FloorItems by inject()
val collisions: Collisions by inject()
val logger = InlineLogger()

on<FloorItemClick>({ player -> option == "Take" && player.hasEffect("freeze") }) { player: Player ->
    take(player, floorItem, true)
}

on<FloorItemOption>({ option == "Take" }) { player: Player ->
    take(player, floorItem, partial || collisions.check(floorItem.tile, CollisionFlag.BLOCKED))
}

fun take(player: Player, item: FloorItem, nearby: Boolean) {
    if (nearby) {
        val delta = item.tile.delta(player.tile)
        if (delta.isDiagonal() || abs(delta.x) > 1 && abs(delta.y) > 1) {
            player.cantReach()
            return
        }
        val dir = delta.toDirection()
        if (player.blocked(dir)) {
            player.cantReach()
            return
        }
        if (delta != Delta.EMPTY) {
            player.face(dir)
            player.setAnimation("take")
        }
    }

    if (player.inventory.isFull() && (!player.inventory.stackable(item.id) || !player.inventory.contains(item.id))) {
        player.inventoryFull()
    } else if (items.remove(item)) {
        player.playSound("pickup_item")
        if (!player.inventory.add(item.id, item.amount)) {
            when (player.inventory.transaction.error) {
                is TransactionError.Full -> player.inventoryFull()
                else -> logger.warn { "Error picking up item $item ${player.inventory.transaction.error}" }
            }
        }
    }
}