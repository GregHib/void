package content.skill.magic.book.lunar

import content.entity.combat.hit.damage
import content.entity.sound.sound
import content.skill.magic.spell.removeSpellItems
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.data.definition.SpellDefinitions
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inject

class HealGroup : Script {

    val definitions: SpellDefinitions by inject()
    val players: Players by inject()

    init {
        interfaceOption("Cast", "heal_group", "lunar_spellbook") {
            val spell = component
            if (player.levels.get(Skill.Constitution) < player.levels.getMax(Skill.Constitution) * 0.11) {
                player.message("You don't have enough life points.")
                return@interfaceOption
            }
            if (!player.removeSpellItems(spell)) {
                return@interfaceOption
            }
            val definition = definitions.get(spell)
            var healed = 0
            val amount = (player.levels.get(Skill.Constitution) * 0.75).toInt() + 5
            player.anim("lunar_cast")
            player.sound(spell)
            val group = players
                .filter { other -> other != player && other.tile.within(player.tile, 1) && other.levels.getOffset(Skill.Constitution) < 0 && player["accept_aid", true] }
                .take(5)
            group.forEach { target ->
                target.gfx(spell)
                target.sound("heal_other_impact")
                player.experience.add(Skill.Magic, definition.experience)
                healed += target.levels.restore(Skill.Constitution, amount / group.size)
                target.message("You have been healed by ${player.name}.")
            }
            if (healed > 0) {
                player.damage(healed, delay = 2)
            }
        }
    }
}
