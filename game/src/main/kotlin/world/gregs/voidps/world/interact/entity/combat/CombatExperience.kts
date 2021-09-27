import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp
import world.gregs.voidps.engine.entity.definition.SpellDefinitions
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.utility.inject
import world.gregs.voidps.world.interact.entity.combat.CombatAttack
import world.gregs.voidps.world.interact.entity.combat.attackStyle
import world.gregs.voidps.world.interact.entity.combat.attackType

val definitions: SpellDefinitions by inject()

on<CombatAttack>({ damage > 0 }) { player: Player ->
    if (type == "spell" || type == "blaze") {
        val base = definitions.get(spell).experience
        if (player.getVar("defensive_cast", false)) {
            player.exp(Skill.Magic, base + damage / 7.5)
            player.exp(Skill.Defence, damage / 10.0)
        } else {
            player.exp(Skill.Magic, base + damage / 5.0)
        }
    } else if (type == "range") {
        if (player.attackType == "long_range") {
            player.exp(Skill.Range, damage / 5.0)
            player.exp(Skill.Defence, damage / 5.0)
        } else {
            player.exp(Skill.Range, damage / 2.5)
        }
    } else if (type == "melee") {
        when (player.attackStyle) {
            "accurate" -> player.exp(Skill.Attack, damage / 2.5)
            "aggressive" -> player.exp(Skill.Strength, damage / 2.5)
            "controlled" -> {
                player.exp(Skill.Attack, damage / 7.5)
                player.exp(Skill.Strength, damage / 7.5)
                player.exp(Skill.Defence, damage / 7.5)
            }
            "defensive" -> player.exp(Skill.Defence, damage / 2.5)
        }
    }
    player.exp(Skill.Constitution, damage / 7.5)
}