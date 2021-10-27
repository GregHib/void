import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.character.update.visual.setGraphic
import world.gregs.voidps.engine.entity.definition.SpellDefinitions
import world.gregs.voidps.engine.entity.hasEffect
import world.gregs.voidps.engine.entity.stop
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.world.interact.entity.player.combat.magic.Runes

val definitions: SpellDefinitions by inject()

on<InterfaceOption>({ id == "lunar_spellbook" && component == "cure_group" }) { player: Player ->
    val spell = component
    if (!Runes.hasSpellRequirements(player, spell)) {
        return@on
    }
    val definition = definitions.get(spell)
    player.setAnimation("lunar_cast_group")
    player.experience.add(Skill.Magic, definition.experience)
    player.viewport.players.current
        .filter { other -> other.tile.within(player.tile, 1) && other.hasEffect("poison") }
        .forEach { target ->
            target.setGraphic(spell)
            target.stop("poison")
            target.message("You have been cured by ${player.name}")
        }
}
