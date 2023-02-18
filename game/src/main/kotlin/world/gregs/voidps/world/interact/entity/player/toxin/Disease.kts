package world.gregs.voidps.world.interact.entity.player.toxin

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.Yellow
import world.gregs.voidps.engine.client.ui.event.Command
import world.gregs.voidps.engine.entity.Registered
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.clear
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.entity.set
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.timer.TimerStart
import world.gregs.voidps.engine.timer.TimerStop
import world.gregs.voidps.engine.timer.TimerTick
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.world.interact.entity.combat.hit

on<Registered>({ it.diseaseCounter != 0 }) { character: Character ->
    val timers = if (character is Player) character.timers else character.softTimers
    timers.restart("disease")
}

on<TimerStart>({ timer == "disease" }) { character: Character ->
    if (!restart && character.diseaseCounter == 0) {
        (character as? Player)?.message(Yellow { "You have been diseased." })
        damage(character)
    }
    interval = 30
}

on<TimerTick>({ timer == "disease" }) { character: Character ->
    var disease = character.diseaseCounter
    if (disease < 0) {
        disease = character.diseaseCounter++
        if (disease == 0) {
            (character as? Player)?.message("<col=7f007f>Your disease resistance has worn off.</col>")
            return@on cancel()
        }
        if (disease == -1) {
            (character as? Player)?.message("<col=7f007f>Your disease resistance is about to wear off.</col>")
        }
    } else if (disease > 0) {
        disease = character.diseaseCounter--
        if (disease == 0) {
            return@on cancel()
        }
        damage(character)
    }
}

on<TimerStop>({ timer == "disease" }) { character: Character ->
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
    hit(character["disease_source", character], character, damage, "disease")
}

on<Command>({ prefix == "disease" }) { player: Player ->
    if (player.diseased) {
        player.cureDisease()
    } else {
        player.disease(player, content.toIntOrNull() ?: 100)
    }
}

on<TimerStart>({ timer == "disease" && it.equipped(EquipSlot.Hands).id == "inoculation_brace" }, Priority.HIGH) { _: Player ->
    cancel()
}

on<TimerStart>({ timer == "disease" && it.def["immune_disease", false] }, Priority.HIGH) { _: NPC ->
    cancel()
}

on<TimerStart>({ timer == "disease" && it.antiDisease }, Priority.HIGH) { _: Character ->
    cancel()
}