package world.gregs.voidps.world.interact.entity.player.combat.consume.food

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.world.interact.entity.combat.hit.directHit
import world.gregs.voidps.world.interact.entity.player.combat.consume.consume

consume("poison_karambwan") { player: Player ->
    player.directHit(50, "poison")
}