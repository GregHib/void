import world.gregs.voidps.engine.client.Colour
import world.gregs.voidps.engine.client.ui.awaitInterfaces
import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.delay
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.update.visual.Hit
import world.gregs.voidps.engine.entity.character.update.visual.addHit
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.network.encode.message
import world.gregs.voidps.network.instruct.Command
import world.gregs.voidps.world.interact.entity.player.cure
import world.gregs.voidps.world.interact.entity.player.poison

on<EffectStart>({ effect == "poison" }) { player: Player ->
    player.message(Colour.Green.wrap("You have been poisoned."))
    delay(0) {
        damage(player)
    }
    player["poison_job"] = delay(player, 30, loop = true) {
        damage(player)
    }
    player.setVar("poisoned", true)
}

on<EffectStop>({ effect == "poison" }) { player: Player ->
    player.setVar("poisoned", false)
    player.clear("poison_job")
    player.clear("poison_damage")
}

suspend fun damage(player: Player) {
    val damage = player["poison_damage", 0]
    if (damage <= 10) {
        player.cure()
        return
    }
    player.awaitInterfaces()
    val hp = player.levels.get(Skill.Constitution) * 10
    val max = player.levels.getMax(Skill.Constitution) * 10
    player["poison_damage"] = damage - 2
    // TODO proper damage system
    //  hp variable + level should be linked bi-dir
    player.addHit(Hit(damage, Hit.Mark.Poison, (((hp - damage) / max.toDouble()) * 255).toInt()))
    val lp = player.getVar<Int>("life_points") - damage
    player.setVar("life_points", lp)
    player.levels.drain(Skill.Constitution, damage / 10)
}

on<Command>({ prefix == "poison" }) { player: Player ->
    if (player.hasEffect("poison")) {
        player.stop("poison")
    } else {
        player.poison(content.toIntOrNull() ?: 100)
    }
}