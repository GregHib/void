import world.gregs.voidps.engine.client.Colour
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.delay
import world.gregs.voidps.engine.entity.EffectStart
import world.gregs.voidps.engine.entity.EffectStop
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.start
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.activity.combat.consume.Consume

on<Consume>({ item.id.startsWith("antifire") || item.id.startsWith("antifire_mix") }) { player: Player ->
    player.start("fire_resistance", persist = true)
}

on<Consume>({ item.id.startsWith("super_antifire") }) { player: Player ->
    player.start("fire_immunity", persist = true)
}

on<EffectStart>({ effect == "fire_resistance" || effect == "fire_immunity" }) { player: Player ->
    val remaining = ticks - if (effect == "fire_immunity") 10 else 20
    if (remaining <= 0) {
        return@on
    }
    delay(player, remaining) {
        player.message(Colour.ChatColour.WarningRed { "Your resistance to dragonfire is about to run out." })
    }
}

on<EffectStop>({ effect == "fire_immunity" }) { player: Player ->
    player.message(Colour.ChatColour.WarningRed { "Your resistance to dragonfire has run out." })
}