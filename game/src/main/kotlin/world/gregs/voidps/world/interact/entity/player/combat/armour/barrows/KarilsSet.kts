package world.gregs.voidps.world.interact.entity.player.combat.armour.barrows

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.playerSpawn
import world.gregs.voidps.engine.inv.itemChanged
import world.gregs.voidps.type.random
import world.gregs.voidps.world.interact.entity.combat.hit.combatAttack

playerSpawn { player: Player ->
    if (player.hasFullSet()) {
        player["karils_set_effect"] = true
    }
}

itemChanged({ inventory == "worn_equipment" && BarrowsArmour.isSlot(index) && it.contains("karils_set_effect") && !isKarils(item) }) { player: Player ->
    player.clear("karils_set_effect")
}

itemChanged({ inventory == "worn_equipment" && BarrowsArmour.isSlot(index) && !it.contains("karils_set_effect") && isKarils(item) && it.hasFullSet() }) { player: Player ->
    player["karils_set_effect"] = true
}

fun isKarils(item: Item) = item.id.startsWith("karils_")

fun Player.hasFullSet() = BarrowsArmour.hasSet(this,
    "karils_crossbow",
    "karils_coif",
    "karils_top",
    "karils_skirt")

combatAttack({ type == "range" && damage > 0 && target is Player && weapon.id.startsWith("karils_crossbow") && it.contains("karils_set_effect") && random.nextInt(4) == 0 }) { _: Character ->
    if (target.levels.drain(Skill.Agility, multiplier = 0.20) < 0) {
        target.setGraphic("karils_effect")
    }
}