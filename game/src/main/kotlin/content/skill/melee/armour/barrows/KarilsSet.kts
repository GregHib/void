package content.skill.melee.armour.barrows

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.playerSpawn
import world.gregs.voidps.engine.inv.itemAdded
import world.gregs.voidps.engine.inv.itemRemoved
import world.gregs.voidps.type.random
import content.entity.combat.hit.characterCombatAttack

playerSpawn { player ->
    if (player.hasFullSet()) {
        player["karils_set_effect"] = true
    }
}

itemRemoved("karils_*", BarrowsArmour.slots, "worn_equipment") { player ->
    player.clear("karils_set_effect")
}

itemAdded("karils_*", BarrowsArmour.slots, "worn_equipment") { player ->
    if (player.hasFullSet()) {
        player["karils_set_effect"] = true
    }
}

fun Player.hasFullSet() = BarrowsArmour.hasSet(
    this,
    "karils_crossbow",
    "karils_coif",
    "karils_top",
    "karils_skirt"
)

characterCombatAttack("karils_crossbow*", "range") { character ->
    if (damage <= 0 || target !is Player || !character.contains("karils_set_effect") || random.nextInt(4) != 0) {
        return@characterCombatAttack
    }
    if (target.levels.drain(Skill.Agility, multiplier = 0.20) < 0) {
        target.gfx("karils_effect")
    }
}