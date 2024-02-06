package world.gregs.voidps.world.interact.entity.player.combat.armour.barrows

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.playerSpawn
import world.gregs.voidps.engine.inv.itemAdded
import world.gregs.voidps.engine.inv.itemRemoved
import world.gregs.voidps.type.random
import world.gregs.voidps.world.interact.entity.combat.hit.characterSpellAttack

playerSpawn { player: Player ->
    if (player.hasFullSet()) {
        player["ahrims_set_effect"] = true
    }
}

itemRemoved("worn_equipment", "ahrims_*", BarrowsArmour.slots) { player: Player ->
    player.clear("ahrims_set_effect")
}

itemAdded("worn_equipment", "ahrims_*", BarrowsArmour.slots) { player: Player ->
    if (player.hasFullSet()) {
        player["ahrims_set_effect"] = true
    }
}

fun Player.hasFullSet() = BarrowsArmour.hasSet(this,
    "ahrims_staff",
    "ahrims_hood",
    "ahrims_robe_top",
    "ahrims_robe_skirt")

characterSpellAttack { character: Character ->
    if (!character.contains("ahrims_set_effect") || random.nextInt(4) != 0) {
        return@characterSpellAttack
    }
    val drain = target.levels.drain(Skill.Strength, 5)
    if (drain < 0) {
        target.setGraphic("ahrims_effect")
    }
}