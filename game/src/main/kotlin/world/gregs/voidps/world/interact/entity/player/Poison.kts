import world.gregs.voidps.engine.client.Colour
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.awaitInterfaces
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.delay
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.EventHandler
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.instruct.Command
import world.gregs.voidps.world.interact.entity.combat.CombatDamage
import world.gregs.voidps.world.interact.entity.combat.hit
import world.gregs.voidps.world.interact.entity.player.cure
import world.gregs.voidps.world.interact.entity.player.poisonedBy
import kotlin.random.Random

on<Registered> { player: Player ->
    player.restart("poison")
}

on<EffectStart>({ effect == "poison" }) { character: Character ->
    if (!restart) {
        if (character is Player) {
            character.message(Colour.Green { "You have been poisoned." })
        }
        delay(0) {
            damage(character)
        }
    }
    character["poison_job"] = delay(character, 30, loop = true) {
        damage(character)
    }
    if (character is Player) {
        character.setVar("poisoned", true)
    }
}

on<EffectStop>({ effect == "poison" }) { character: Character ->
    if (character is Player) {
        character.setVar("poisoned", false)
    }
    character.clear("poison_job")
    character.clear("poison_damage")
    val source: Character? = character.remove("poison_source")
    val handler: EventHandler? = character.remove("poison_source_handler")
    if (source != null && handler != null) {
        source.events.remove(handler)
    }
}

suspend fun damage(character: Character) {
    val damage = character["poison_damage", 0]
    if (damage <= 10) {
        character.cure()
        return
    }
    if (character is Player) {
        character.awaitInterfaces()
    }
    character["poison_damage"] = damage - 2
    hit(character["poison_source", character], character, damage, "poison")
}

fun isPoisoned(name: String?) = name != null && (name.endsWith("_p") || name.endsWith("_p+") || name.endsWith("_p++") || name == "emerald_bolts_e")

on<CombatDamage>({ damage > 0 && isPoisoned(weapon?.name) }) { player: Player ->
    val poison = 20 + weapon!!.name.count { it == '+' } * 10
    if (type == "range" && Random.nextDouble() < 0.125) {
        target.poisonedBy(player, if (weapon.name == "emerald_bolts_e") 50 else poison)
    } else if (type == "melee" && Random.nextDouble() < 0.25) {
        target.poisonedBy(player, poison + 20)
    }
}

on<Command>({ prefix == "poison" }) { player: Player ->
    if (player.hasEffect("poison")) {
        player.stop("poison")
    } else {
        player.poisonedBy(player, content.toIntOrNull() ?: 100)
    }
}