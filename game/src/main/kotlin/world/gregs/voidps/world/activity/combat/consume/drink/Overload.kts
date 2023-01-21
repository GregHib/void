import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.WarningRed
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.timer.Job
import world.gregs.voidps.engine.timer.timer
import world.gregs.voidps.world.activity.combat.consume.Consumable
import world.gregs.voidps.world.activity.combat.consume.Consume
import world.gregs.voidps.world.interact.entity.combat.hit

on<Consumable>({ item.id.startsWith("overload") }) { player: Player ->
    if (player.hasEffect("overload")) {
        player.message("You may only use this potion every five minutes.")
        cancelled = true
    } else if (player.levels.get(Skill.Constitution) < 500) {
        player.message("You need more than 500 life points to survive the power of overload.")
        cancelled = true
    }
}

on<Consume>({ item.id.startsWith("overload") }) { player: Player ->
    player.start("overload", 501, persist = true)
}

fun inWilderness() = false

on<EffectStart>({ effect == "overload" }) { player: Player ->
    if (!restart) {
        var count = 0
        player["overload_hits"] = player.timer(2, true) {
            hit(player, player, 100)
            player.setAnimation("overload")
            player.setGraphic("overload")
            if (++count >= 5) {
                player.remove<Job>("overload_hits")?.cancel()
            }
        }
    }
    player["overload_job"] = player.timer(25, true) {
        if (inWilderness()) {
            player.levels.boost(Skill.Attack, 5, 0.15)
            player.levels.boost(Skill.Strength, 5, 0.15)
            player.levels.boost(Skill.Defence, 5, 0.15)
            player.levels.boost(Skill.Magic, 5, 0.15)
            player.levels.boost(Skill.Ranged, 5, 0.15)
        } else {
            player.levels.boost(Skill.Attack, 5, 0.22)
            player.levels.boost(Skill.Strength, 5, 0.22)
            player.levels.boost(Skill.Defence, 5, 0.22)
            player.levels.boost(Skill.Magic, 7)
            player.levels.boost(Skill.Ranged, 4, 0.1923)
        }
    }
}

on<EffectStop>({ effect == "overload" }) { player: Player ->
    reset(player, Skill.Attack)
    reset(player, Skill.Strength)
    reset(player, Skill.Defence)
    reset(player, Skill.Magic)
    reset(player, Skill.Ranged)
    player.levels.restore(Skill.Constitution, 500)
    player.remove<Job>("overload_job")?.cancel()
    player.message(WarningRed { "The effects of overload have worn off and you feel normal again." })
}

fun reset(player: Player, skill: Skill) {
    if (player.levels.getOffset(skill) > 0) {
        player.levels.clear(skill)
    }
}