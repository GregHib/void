import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.InterfaceClick
import world.gregs.voidps.engine.delay
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.character.update.visual.setGraphic
import world.gregs.voidps.engine.entity.definition.SpellDefinitions
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.world.interact.entity.combat.hit
import world.gregs.voidps.world.interact.entity.player.combat.magic.Runes

val definitions: SpellDefinitions by inject()

on<InterfaceClick>({ id == "lunar_spellbook" && component == "heal_group" }) { player: Player ->
    val spell = component
    if (player.levels.get(Skill.Constitution) < player.levels.getMax(Skill.Constitution) * 0.11) {
        player.message("You don't have enough life points.")
        return@on
    }
    if (!Runes.hasSpellRequirements(player, spell)) {
        return@on
    }
    val definition = definitions.get(spell)
    var healed = 0
    val amount = (player.levels.get(Skill.Constitution) * 0.75).toInt() + 5
    player.setAnimation("lunar_cast")
    val group = player.viewport.players.current
        .filter { other -> println(other.levels.getOffset(Skill.Constitution));other != player && other.tile.within(player.tile, 1) && other.levels.getOffset(Skill.Constitution) < 0 }
        .take(5)
    group.forEach { target ->
        target.setGraphic(spell)
        player.experience.add(Skill.Magic, definition.experience)
        healed += target.levels.restore(Skill.Constitution, amount / group.size)
        target.message("You have been healed by ${player.name}.")
    }
    if (healed > 0) {
        delay(player, 2) {
            player.hit(healed)
        }
    }
}
