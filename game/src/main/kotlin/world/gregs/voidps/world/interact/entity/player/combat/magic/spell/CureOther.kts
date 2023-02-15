import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.InterfaceOnPlayer
import world.gregs.voidps.engine.data.definition.extra.SpellDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.world.interact.entity.player.combat.magic.Runes
import world.gregs.voidps.world.interact.entity.player.cure
import world.gregs.voidps.world.interact.entity.player.poisoned

val definitions: SpellDefinitions by inject()

on<InterfaceOnPlayer>({ id == "lunar_spellbook" && component == "cure_other" }) { player: Player ->
    val spell = component
    if (!target.poisoned()) {
        player.message("This player is not poisoned.")
        return@on
    }
    if (!Runes.hasSpellRequirements(player, spell)) {
        return@on
    }
    val definition = definitions.get(spell)
    player.setAnimation("lunar_cast")
    target.setGraphic(spell)
    player.experience.add(Skill.Magic, definition.experience)
    target.cure()
    target.message("You have been cured by ${player.name}.")
}
