package world.gregs.voidps.world.interact.entity.player.toxin

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.Green
import world.gregs.voidps.engine.client.ui.event.Command
import world.gregs.voidps.engine.client.variable.clearVar
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.timer.TimerStart
import world.gregs.voidps.engine.timer.TimerStop
import world.gregs.voidps.engine.timer.TimerTick
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.world.interact.entity.combat.CombatHit
import world.gregs.voidps.world.interact.entity.combat.hit
import kotlin.math.sign
import kotlin.random.Random

on<Registered>({ it.poisonCounter != 0 }) { character: Character ->
    val timers = if (character is Player) character.timers else character.softTimers
    timers.restart("poison")
}

on<TimerStart>({ timer == "poison" }) { character: Character ->
    if (!restart && character.poisonCounter == 0) {
        (character as? Player)?.message(Green { "You have been poisoned." })
        damage(character)
    }
    interval = 30
}

on<TimerTick>({ timer == "poison" }) { character: Character ->
    val poisoned = character.poisoned
    character.poisonCounter -= character.poisonCounter.sign
    when {
        character.poisonCounter == 0 -> {
            if (!poisoned) {
                (character as? Player)?.message("<col=7f007f>Your poison resistance has worn off.</col>")
            }
            return@on cancel()
        }
        character.poisonCounter == -1 -> (character as? Player)?.message("<col=7f007f>Your poison resistance is about to wear off.</col>")
        poisoned -> damage(character)
    }
}

on<TimerStop>({ timer == "poison" }) { character: Character ->
    character.poisonCounter = 0
    character.clearVar("poison_damage")
    character.clearVar("poison_source")
}

fun damage(character: Character) {
    val damage = character["poison_damage", 0]
    if (damage <= 10) {
        character.curePoison()
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
        source.poison(target, if (weapon.id == "emerald_bolts_e") 50 else poison)
    } else if (type == "melee" && Random.nextDouble() < 0.25) {
        source.poison(target, poison + 20)
    }
}

on<Command>({ prefix == "poison" }) { player: Player ->
    if (player.poisoned) {
        player.curePoison()
    } else {
        player.poison(player, content.toIntOrNull() ?: 100)
    }
}

on<TimerStart>({ timer == "poison" && it.equipped(EquipSlot.Shield).id == "anti_poison_totem" }, Priority.HIGH) { _: Player ->
    cancel()
}

on<TimerStart>({ timer == "poison" && it.def["immune_poison", false] }, Priority.HIGH) { _: NPC ->
    cancel()
}

on<TimerStart>({ timer == "poison" && it.antiPoison }, Priority.HIGH) { _: Character ->
    cancel()
}