package world.gregs.voidps.world.interact.entity.player

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.Green
import world.gregs.voidps.engine.client.ui.event.Command
import world.gregs.voidps.engine.client.ui.menu
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.timer.TimerStart
import world.gregs.voidps.engine.timer.TimerStop
import world.gregs.voidps.engine.timer.TimerTick
import world.gregs.voidps.world.interact.entity.combat.CombatHit
import world.gregs.voidps.world.interact.entity.combat.hit
import kotlin.random.Random

on<TimerStart>({ timer == "poison" }) { player: Player ->
    if (!restart) {
        player.message(Green { "You have been poisoned." })
        damage(player)
    }
    player.setVar("poisoned", true)
}

on<TimerStart>({ timer == "poison" }) { npc: NPC ->
    if (!restart) {
        damage(npc)
    }
}

on<TimerStop>({ timer == "poison" }) { character: Character ->
    if (character is Player) {
        character.setVar("poisoned", false)
    }
    character.clear("poison_damage")
    character.clear("poison_source")
}

on<Unregistered>({ it.contains("poisons") }) { character: Character ->
    val poisons: Set<Character> = character.remove("poisons") ?: return@on
    for (poison in poisons) {
        poison.clear("poison_source")
    }
}

on<TimerTick>({ timer == "poison" }) { character: Character ->
    damage(character)
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
    if (player.poisoned()) {
        player.cure()
    } else {
        player.poisonedBy(player, content.toIntOrNull() ?: 100)
    }
}