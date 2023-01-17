import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.Green
import world.gregs.voidps.engine.client.ui.event.Command
import world.gregs.voidps.engine.client.ui.menu
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.Unregistered
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.tick.Job
import world.gregs.voidps.engine.tick.delay
import world.gregs.voidps.world.interact.entity.combat.CombatHit
import world.gregs.voidps.world.interact.entity.combat.hit
import world.gregs.voidps.world.interact.entity.player.cure
import world.gregs.voidps.world.interact.entity.player.poisonedBy
import kotlin.random.Random

on<EffectStart>({ effect == "poison" }) { character: Character ->
    if (!restart) {
        if (character is Player) {
            character.message(Green { "You have been poisoned." })
        }
        character.delay(0) {
            damage(character)
        }
    }
    character["poison_job"] = character.delay(30, loop = true) {
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
    character.remove<Job>("poison_job")?.cancel()
    character.clear("poison_damage")
    character.clear("poison_source")
}

on<Unregistered>({ it.contains("poisons") }) { character: Character ->
    val poisons: Set<Character> = character.remove("poisons") ?: return@on
    for (poison in poisons) {
        poison.clear("poison_source")
    }
}

fun damage(character: Character) {
    val damage = character["poison_damage", 0]
    if (damage <= 10) {
        character.cure()
        return
    }
    if (character is Player && character.menu != null) {
        return
    }
    character["poison_damage"] = damage - 2
    hit(character["poison_source", character], character, damage, "poison")
}

fun isPoisoned(id: String?) = id != null && (id.endsWith("_p") || id.endsWith("_p+") || id.endsWith("_p++") || id == "emerald_bolts_e")

fun poisonous(source: Character, weapon: Item?) = source is Player && isPoisoned(weapon?.id)

on<CombatHit>({ damage > 0 && poisonous(source, weapon) }) { target: Character ->
    val poison = 20 + weapon!!.id.count { it == '+' } * 10
    if (type == "range" && Random.nextDouble() < 0.125) {
        target.poisonedBy(source, if (weapon.id == "emerald_bolts_e") 50 else poison)
    } else if (type == "melee" && Random.nextDouble() < 0.25) {
        target.poisonedBy(source, poison + 20)
    }
}

on<Command>({ prefix == "poison" }) { player: Player ->
    if (player.hasEffect("poison")) {
        player.stop("poison")
    } else {
        player.poisonedBy(player, content.toIntOrNull() ?: 100)
    }
}