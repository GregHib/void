import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.activity.skill.cooking.Consume

on<Consume>({ item.id == "rocktail" }) { player: Player ->
    val range = item.def.getOrNull("heals") as? IntRange ?: return@on
    val amount = range.random()
    player.levels.boost(Skill.Constitution, amount, stack = true, maximum = 100)
    cancel = true
}