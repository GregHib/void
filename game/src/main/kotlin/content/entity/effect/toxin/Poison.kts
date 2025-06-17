package content.entity.effect.toxin

import content.entity.combat.hit.characterCombatAttack
import content.entity.combat.hit.directHit
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.event.adminCommand
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.characterSpawn
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.timer.characterTimerStart
import world.gregs.voidps.engine.timer.characterTimerStop
import world.gregs.voidps.engine.timer.characterTimerTick
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.random
import kotlin.math.sign

characterSpawn { character ->
    if (character.poisonCounter != 0) {
        val timers = if (character is Player) character.timers else character.softTimers
        timers.restart("poison")
    }
}

fun immune(character: Character) = character is NPC && character.def["immune_poison", false] || character is Player && character.equipped(EquipSlot.Shield).id == "anti_poison_totem"

characterTimerStart("poison") { character ->
    if (character.antiPoison || immune(character)) {
        cancel()
        return@characterTimerStart
    }
    if (!restart && character.poisonCounter == 0) {
        (character as? Player)?.message("<green>You have been poisoned.")
        damage(character)
    }
    interval = 30
}

characterTimerTick("poison") { character ->
    val poisoned = character.poisoned
    character.poisonCounter -= character.poisonCounter.sign
    when {
        character.poisonCounter == 0 -> {
            if (!poisoned) {
                (character as? Player)?.message("<purple>Your poison resistance has worn off.")
            }
            cancel()
            return@characterTimerTick
        }
        character.poisonCounter == -1 -> (character as? Player)?.message("<purple>Your poison resistance is about to wear off.")
        poisoned -> damage(character)
    }
}

characterTimerStop("poison") { character ->
    character.poisonCounter = 0
    character.clear("poison_damage")
    character.clear("poison_source")
}

fun damage(character: Character) {
    val damage = character["poison_damage", 0]
    if (damage <= 10) {
        character.curePoison()
        return
    }
    character["poison_damage"] = damage - 2
    val source = character["poison_source", character]
    character.directHit(source, damage, "poison")
}

fun isPoisoned(id: String) = id.endsWith("_p") || id.endsWith("_p+") || id.endsWith("_p++") || id == "emerald_bolts_e"

fun poisonous(source: Character, weapon: Item) = source is Player && isPoisoned(weapon.id)

characterCombatAttack { source ->
    if (damage <= 0 || !poisonous(source, weapon)) {
        return@characterCombatAttack
    }
    val poison = 20 + weapon.id.count { it == '+' } * 10
    if (type == "range" && random.nextDouble() < 0.125) {
        source.poison(target, if (weapon.id == "emerald_bolts_e") 50 else poison)
    } else if (type == "melee" && random.nextDouble() < 0.25) {
        source.poison(target, poison + 20)
    }
}

adminCommand("poison [damage]", "toggle hitting player with poison") {
    val damage = content.toIntOrNull() ?: 100
    if (player.poisoned || damage < 0) {
        player.curePoison()
    } else {
        player.poison(player, damage)
    }
}
