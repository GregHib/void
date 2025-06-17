package content.area.morytania.slayer_tower

import content.entity.combat.attackers
import content.entity.combat.hit.damage
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.itemOnNPCOperate
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.npcLevelChange
import world.gregs.voidps.engine.inv.inventory

npcLevelChange("gargoyle", Skill.Constitution) { npc ->
    if (to <= 90) {
        for (attacker in npc.attackers) {
            attacker.mode = EmptyMode
            if (attacker is Player && attacker["killing_blow", false]) {
                smash(attacker, npc)
            }
        }
    }
}

npcOperate("Smash", "gargoyle") {
    smash(player, target)
}

itemOnNPCOperate("rock_hammer", "gargoyle") {
    smash(player, target)
}

fun smash(player: Player, target: NPC) {
    if (!player.inventory.contains("rock_hammer")) {
        player.message("You need a rock hammer to do that.") // TODO proper message
        return
    }
    player.anim("pie_accurate") // TODO proper anim
    target.gfx("skip_water_splash")
    val hitpoints = target.levels.get(Skill.Constitution)
    if (hitpoints >= 90) {
        player.message("The gargoyle isn't weak enough to be harmed by the hammer.")
        return
    }
    player.message("The gargoyle cracks apart.")
    target.damage(hitpoints, source = player)
}
