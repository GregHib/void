package world.gregs.voidps.world.activity.combat.consume.food

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.activity.combat.consume.Consume
import world.gregs.voidps.world.interact.entity.combat.hit

on<Consume>({ item.id == "poison_karambwan" }) { player: Player ->
    hit(player, player, 50, "poison")
}