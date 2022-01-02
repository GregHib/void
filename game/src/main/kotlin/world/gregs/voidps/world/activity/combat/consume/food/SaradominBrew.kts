import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.activity.combat.consume.Consume

on<Consume>({ item.id.startsWith("saradomin_brew") }) { player: Player ->
    player.levels.restore(Skill.Constitution, 20, 0.15)
    player.levels.restore(Skill.Defence, 20, 0.15)
    player.levels.drain(Skill.Attack, 20, 0.10)
    player.levels.drain(Skill.Strength, 20, 0.15)
    player.levels.drain(Skill.Magic, 20, 0.15)
    player.levels.drain(Skill.Defence, 20, 0.15)
    cancel = true
}