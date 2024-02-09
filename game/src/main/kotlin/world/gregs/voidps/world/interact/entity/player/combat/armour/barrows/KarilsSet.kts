package world.gregs.voidps.world.interact.entity.player.combat.armour.barrows

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.playerSpawn
import world.gregs.voidps.engine.inv.itemAdded
import world.gregs.voidps.engine.inv.itemRemoved
import world.gregs.voidps.type.random
import world.gregs.voidps.world.interact.entity.combat.hit.characterCombatAttack

playerSpawn { player: Player ->
    if (player.hasFullSet()) {
        player["karils_set_effect"] = true
    }
}

itemRemoved("karils_*", BarrowsArmour.slots, "worn_equipment") { player: Player ->
    player.clear("karils_set_effect")
}

itemAdded("karils_*", BarrowsArmour.slots, "worn_equipment") { player: Player ->
    if (player.hasFullSet()) {
        player["karils_set_effect"] = true
    }
}

fun Player.hasFullSet() = BarrowsArmour.hasSet(this,
    "karils_crossbow",
    "karils_coif",
    "karils_top",
    "karils_skirt")

characterCombatAttack { character: Character ->
    if (type != "range" || damage <= 0 || target !is Player || !weapon.id.startsWith("karils_crossbow") || !character.contains("karils_set_effect") || random.nextInt(4) != 0) {
        return@characterCombatAttack
    }
    if (target.levels.drain(Skill.Agility, multiplier = 0.20) < 0) {
        target.setGraphic("karils_effect")
    }
}