import world.gregs.voidps.engine.client.Colour
import world.gregs.voidps.engine.client.ui.awaitInterfaces
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.delay
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.update.visual.Hit
import world.gregs.voidps.engine.entity.character.update.visual.hit
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.encode.message
import world.gregs.voidps.network.instruct.Command
import world.gregs.voidps.world.interact.entity.player.cure
import world.gregs.voidps.world.interact.entity.player.poison

on<EffectStart>({ effect == "poison" }) { character: Character ->
    if (character is Player) {
        character.message(Colour.Green.wrap("You have been poisoned."))
    }
    delay(0) {
        damage(character)
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
    character.hit(character, damage, Hit.Mark.Poison)
    character.levels.drain(Skill.Constitution, damage)
}

on<Command>({ prefix == "poison" }) { player: Player ->
    if (player.hasEffect("poison")) {
        player.stop("poison")
    } else {
        player.poison(content.toIntOrNull() ?: 100)
    }
}