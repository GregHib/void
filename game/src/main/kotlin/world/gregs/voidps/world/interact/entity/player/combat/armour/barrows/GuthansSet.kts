package world.gregs.voidps.world.interact.entity.player.combat.armour.barrows

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.playerSpawn
import world.gregs.voidps.engine.inv.itemAdded
import world.gregs.voidps.engine.inv.itemRemoved
import world.gregs.voidps.type.random
import world.gregs.voidps.world.interact.entity.combat.hit.combatAttack

playerSpawn { player: Player ->
    if (player.hasFullSet()) {
        player["guthans_set_effect"] = true
    }
}

itemRemoved("worn_equipment", "guthans_*", BarrowsArmour.slots) { player: Player ->
    player.clear("guthans_set_effect")
}

itemAdded("worn_equipment", "guthans_*", BarrowsArmour.slots) { player: Player ->
    if (player.hasFullSet()) {
        player["guthans_set_effect"] = true
    }
}

fun Player.hasFullSet() = BarrowsArmour.hasSet(this,
    "guthans_warspear",
    "guthans_helm",
    "guthans_platebody",
    "guthans_chainskirt")

combatAttack({ type == "melee" && it.contains("guthans_set_effect") && random.nextInt(4) == 0 }) { character: Character ->
    character.levels.boost(Skill.Constitution, damage)
    target.setGraphic("guthans_effect")
}