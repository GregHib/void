import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.combat.CombatHit
import world.gregs.voidps.world.interact.entity.combat.ammo
import world.gregs.voidps.world.interact.entity.combat.hit
import kotlin.random.Random

on<CombatHit>({ source is Player && source.ammo == "saradomin_arrows" }) { character: Character ->
    val chance = if (weapon?.name == "saradomin_bow") 0.2 else 0.1
    if (Random.nextDouble() < chance) {
        // water_strike
        hit(source, character, hit(source, character, type, weapon), "spell", weapon)
    }
}

on<CombatHit>({ source is Player && source.ammo == "guthix_arrows" }) { character: Character ->
    val chance = if (weapon?.name == "guthix_bow") 0.2 else 0.1
    if (Random.nextDouble() < chance) {
        // earth_strike
        hit(source, character, hit(source, character, type, weapon), "spell", weapon)
    }
}

on<CombatHit>({ source is Player && source.ammo == "zamorak_arrows" }) { character: Character ->
    val chance = if (weapon?.name == "zamorak_bow") 0.2 else 0.1
    if (Random.nextDouble() < chance) {
        // fire_strike
        hit(source, character, hit(source, character, type, weapon), "spell", weapon)
    }
}