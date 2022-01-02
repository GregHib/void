import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.activity.combat.consume.Consume

on<Consume>({ item.id == "holy_biscuits" }) { player: Player ->
    player.levels.restore(Skill.Prayer, 10)
}