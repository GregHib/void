import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.activity.combat.consume.Consume
import kotlin.random.Random

on<Consume>({ item.id == "spicy_stew" }) { player: Player ->
    if (Random.nextInt(100) > 5) {
        player.levels.boost(Skill.Cooking, 6)
    } else {
        player.levels.drain(Skill.Cooking, 6)
    }
}