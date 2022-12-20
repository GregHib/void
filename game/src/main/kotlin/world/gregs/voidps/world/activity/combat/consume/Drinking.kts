package world.gregs.voidps.world.activity.combat.consume

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.hasEffect
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.utility.plural

on<Consume>({ item.id.endsWith("_4") || item.id.endsWith("_3") || item.id.endsWith("_2") || item.id.endsWith("_1") }, Priority.LOWER) { player: Player ->
    val doses = item.id.last().digitToInt()
    if (doses == 1) {
        player.message("You have finished your potion.")
        if (player.hasEffect("smash_vials")) {
            player.inventory.clear(slot)
            player.message("You quickly smash the empty vial using the tick a Barbarian taught you.")
        }
    } else {
        player.message("You have ${doses - 1} ${"dose".plural(doses - 1)} of the potion left.")
    }
}