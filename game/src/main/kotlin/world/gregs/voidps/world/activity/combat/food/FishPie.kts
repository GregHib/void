import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.activity.skill.cooking.Consume

on<Consume>({ item.id.startsWith("fish_pie") }) { player: Player ->
    player.levels.boost(Skill.Fishing, 3)
}