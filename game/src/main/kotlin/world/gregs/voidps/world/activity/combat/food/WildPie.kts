import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.activity.skill.cooking.Consume

on<Consume>({ item.id.startsWith("wild_pie") }) { player: Player ->
    player.levels.boost(Skill.Slayer, 4)
    player.levels.boost(Skill.Range, 4)
}