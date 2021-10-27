import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.InterfaceOnPlayer
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

on<InterfaceOnPlayer>({ id == "lunar_spellbook" && component == "heal_other" }) { player: Player ->
    val spell = component
    if (target.levels.getOffset(Skill.Constitution) >= 0) {
        player.message("This player does not need healing.")
        return@on
    }
    if (player.levels.get(Skill.Constitution) < player.levels.getMax(Skill.Constitution) * 0.11) {
        player.message("You don't have enough life points.")
        return@on
    }
    if (!Runes.hasSpellRequirements(player, spell)) {
        return@on
    }
    val definition = definitions.get(spell)
    val amount = (player.levels.get(Skill.Constitution) * 0.75).toInt() + 1
    player.setAnimation("lunar_cast")
    target.setGraphic(spell)
    player.experience.add(Skill.Magic, definition.experience)
    val restored = target.levels.restore(Skill.Constitution, amount)
    target.message("You have been healed by ${player.name}.")
    delay(player, 2) {
        player.hit(restored)
    }
}
