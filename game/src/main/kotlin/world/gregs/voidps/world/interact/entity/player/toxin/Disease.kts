package world.gregs.voidps.world.interact.entity.player.toxin

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.event.command
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.characterSpawn
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.timer.*
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.world.interact.entity.combat.hit.directHit
import kotlin.math.sign

characterSpawn { character: Character ->
    if (character.diseaseCounter != 0) {
        val timers = if (character is Player) character.timers else character.softTimers
        timers.restart("disease")
    }
}

characterTimerStart({ timer == "disease" }) { character: Character ->
    if (!restart && character.diseaseCounter == 0) {
        (character as? Player)?.message("You have been diseased.")
        damage(character)
    }
    interval = 30
}

characterTimerTick({ timer == "disease" }) { character: Character ->
    val diseased = character.diseased
    character.diseaseCounter -= character.diseaseCounter.sign
    when {
        character.diseaseCounter == 0 -> {
            if (!diseased) {
                (character as? Player)?.message("Your disease resistance has worn off.")
            }
            cancel()
            return@characterTimerTick
        }
        character.diseaseCounter == -1 -> (character as? Player)?.message("Your disease resistance is about to wear off.")
        diseased -> damage(character)
    }
}

characterTimerStop({ timer == "disease" }) { character: Character ->
    character.diseaseCounter = 0
    character.clear("disease_damage")
    character.clear("disease_source")
}

fun damage(character: Character) {
    val damage = character["disease_damage", 0]
    if (damage <= 10) {
        character.cureDisease()
        return
    }
    character["disease_damage"] = damage - 2
    val source = character["disease_source", character]
    character.directHit(source, damage, "disease")
}

command({ prefix == "disease" }) { player: Player ->
    if (player.diseased) {
        player.cureDisease()
    } else {
        player.disease(player, content.toIntOrNull() ?: 100)
    }
}

timerStart({ timer == "disease" && it.equipped(EquipSlot.Hands).id == "inoculation_brace" }, Priority.HIGH) { _: Player ->
    cancel()
}

npcTimerStart({ timer == "disease" && it.def["immune_disease", false] }, Priority.HIGH) { _: NPC ->
    cancel()
}

characterTimerStart({ timer == "disease" && it.antiDisease }, Priority.HIGH) { _: Character ->
    cancel()
}