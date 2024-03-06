package world.gregs.voidps.world.interact.entity.player.combat.range.special

import world.gregs.voidps.engine.client.ui.chat.toInt
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.characterLevelChange
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.distanceTo
import world.gregs.voidps.world.interact.entity.combat.attackType
import world.gregs.voidps.world.interact.entity.combat.combatSwing
import world.gregs.voidps.world.interact.entity.combat.hit.Hit
import world.gregs.voidps.world.interact.entity.combat.hit.characterCombatHit
import world.gregs.voidps.world.interact.entity.combat.hit.combatAttack
import world.gregs.voidps.world.interact.entity.combat.hit.hit
import world.gregs.voidps.world.interact.entity.combat.weapon
import world.gregs.voidps.world.interact.entity.proj.shoot
import world.gregs.voidps.world.interact.entity.sound.playSound

combatSwing("seercull", style = "range", special = true) { player ->
    player.setAnimation("bow_accurate")
    player.setGraphic("seercull_special_shoot")
    player.playSound("seercull_special")
    player.shoot(id = "seercull_special_arrow", target = target)
    val distance = player.tile.distanceTo(target)
    player.hit(target, delay = Hit.bowDelay(distance))
    delay = player.weapon.def["attack_speed", 4] - (player.attackType == "rapid").toInt()
}

characterCombatHit("seercull", "range") { character ->
    character.setGraphic("seercull_special_hit")
}

combatAttack("seercull*", special = true) {
    if (target["soulshot", false]) {
        return@combatAttack
    }
    target["soulshot"] = true
    target.levels.drain(Skill.Magic, damage / 10)
}

characterLevelChange(Skill.Magic) { character ->
    if (character["soulshot", false] && to >= character.levels.getMax(skill)) {
        character.clear("soulshot")
    }
}