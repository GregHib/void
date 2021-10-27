import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.InterfaceOnPlayer
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.character.update.visual.setGraphic
import world.gregs.voidps.engine.entity.definition.SpellDefinitions
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.world.interact.entity.combat.hit
import world.gregs.voidps.world.interact.entity.combat.inMultiCombat
import world.gregs.voidps.world.interact.entity.player.combat.MAX_SPECIAL_ATTACK
import world.gregs.voidps.world.interact.entity.player.combat.magic.Runes
import world.gregs.voidps.world.interact.entity.player.combat.specialAttackEnergy
import world.gregs.voidps.world.interact.entity.player.energy.MAX_RUN_ENERGY
import world.gregs.voidps.world.interact.entity.player.energy.runEnergy
import kotlin.random.Random

val definitions: SpellDefinitions by inject()

on<InterfaceOnPlayer>({ id == "lunar_spellbook" && component == "energy_transfer" }) { player: Player ->
    val spell = component
    if (target.specialAttackEnergy == MAX_SPECIAL_ATTACK) {
        player.message("This player has full special attack.")
        return@on
    }
    if (player.specialAttackEnergy != MAX_SPECIAL_ATTACK) {
        player.message("You must have 100% special attack energy to transfer.")
        return@on
    }
    if (player.levels.get(Skill.Constitution) < 100) {
        player.message("You need more hitpoints to cast this spell.")
        return@on
    }
    if (!target.inMultiCombat) {
        player.message("This player is not in a multi-combat zone.")
        return@on
    }
    if (!Runes.hasSpellRequirements(player, spell)) {
        return@on
    }
    val definition = definitions.get(spell)
    player.setAnimation("lunar_cast")
    target.setGraphic(spell)
    player.experience.add(Skill.Magic, definition.experience)
    player.hit(Random.nextInt(95, 100))
    player.specialAttackEnergy = 0
    target.specialAttackEnergy = MAX_SPECIAL_ATTACK
    target.runEnergy = MAX_RUN_ENERGY
}
