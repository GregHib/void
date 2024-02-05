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
    player["guthans_set_effect"] = true
}

itemChanged({ inventory == "worn_equipment" && BarrowsArmour.isSlot(index) && it.contains("guthans_set_effect") && !isGuthans(item) }) { player: Player ->
    player.clear("guthans_set_effect")
}

itemChanged({ inventory == "worn_equipment" && BarrowsArmour.isSlot(index) && !it.contains("guthans_set_effect") && isGuthans(item) && it.hasFullSet() }) { player: Player ->
    player["guthans_set_effect"] = true
}

fun isGuthans(item: Item) = item.id.startsWith("guthans_")

fun Player.hasFullSet() = BarrowsArmour.hasSet(this,
    "guthans_warspear",
    "guthans_helm",
    "guthans_platebody",
    "guthans_chainskirt")

combatAttack({ type == "melee" && it.contains("guthans_set_effect") && random.nextInt(4) == 0 }) { character: Character ->
    character.levels.boost(Skill.Constitution, damage)
    target.setGraphic("guthans_effect")
}