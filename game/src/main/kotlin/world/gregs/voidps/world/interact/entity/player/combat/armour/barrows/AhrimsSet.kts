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

playerSpawn({ it.hasFullSet() }) { player: Player ->
    player["ahrims_set_effect"] = true
}

itemChanged({ inventory == "worn_equipment" && BarrowsArmour.isSlot(index) && it.contains("ahrims_set_effect") && !isAhrims(item) }) { player: Player ->
    player.clear("ahrims_set_effect")
}

itemChanged({ inventory == "worn_equipment" && BarrowsArmour.isSlot(index) && !it.contains("ahrims_set_effect") && isAhrims(item) && it.hasFullSet() }) { player: Player ->
    player["ahrims_set_effect"] = true
}

fun isAhrims(item: Item) = item.id.startsWith("ahrims_")

fun Player.hasFullSet() = BarrowsArmour.hasSet(this,
    "ahrims_staff",
    "ahrims_hood",
    "ahrims_robe_top",
    "ahrims_robe_skirt")

combatAttack({ type == "magic" && it.contains("ahrims_set_effect") && damage > 0 && random.nextInt(4) == 0 }) { _: Character ->
    val drain = target.levels.drain(Skill.Strength, 5)
    if (drain < 0) {
        target.setGraphic("ahrims_effect")
    }
}