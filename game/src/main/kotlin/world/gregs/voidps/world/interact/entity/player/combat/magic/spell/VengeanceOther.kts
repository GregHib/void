import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.InterfaceOnPlayer
import world.gregs.voidps.engine.client.variable.hasVar
import world.gregs.voidps.engine.client.variable.setVar
import world.gregs.voidps.engine.data.definition.extra.SpellDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.world.interact.entity.player.combat.magic.Runes

val definitions: SpellDefinitions by inject()

on<InterfaceOnPlayer>({ id == "lunar_spellbook" && component == "vengeance_other" }) { player: Player ->
    val spell = component
    if (target.hasVar("vengeance")) {
        player.message("This player already has vengeance cast.")
        return@on
    }
    if (player.clocks.contains("vengeance_delay")) {
        player.message("You can only cast vengeance spells once every 30 seconds.")
        return@on
    }
    if (!Runes.hasSpellRequirements(player, spell)) {
        return@on
    }
    val definition = definitions.get(spell)
    player.setAnimation("lunar_cast")
    target.setGraphic(spell)
    player.experience.add(Skill.Magic, definition.experience)
    player.setVar("vengeance", true)
    player.clocks.start("vengeance_delay", definition["delay_ticks"])
}