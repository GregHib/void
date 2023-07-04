package world.gregs.voidps.world.interact.entity.player.combat.magic.spell

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.data.definition.SpellDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.world.interact.entity.combat.hit
import world.gregs.voidps.world.interact.entity.player.combat.magic.Runes

val definitions: SpellDefinitions by inject()
val players: Players by inject()

on<InterfaceOption>({ id == "lunar_spellbook" && component == "heal_group" }) { player: Player ->
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
    val group = players
        .filter { other -> other != player && other.tile.within(player.tile, 1) && other.levels.getOffset(Skill.Constitution) < 0 }
        .take(5)
    group.forEach { target ->
        target.setGraphic(spell)
        player.experience.add(Skill.Magic, definition.experience)
        healed += target.levels.restore(Skill.Constitution, amount / group.size)
        target.message("You have been healed by ${player.name}.")
    }
    if (healed > 0) {
        player.hit(healed, delay = 2)
    }
}
