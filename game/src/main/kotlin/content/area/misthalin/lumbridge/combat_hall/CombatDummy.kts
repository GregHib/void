package content.area.misthalin.lumbridge.combat_hall

import net.pearx.kasechange.toTitleCase
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.ItemOnNPC
import world.gregs.voidps.engine.client.ui.interact.itemOnNPCApproach
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.npcApproach
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.CurrentLevelChanged
import world.gregs.voidps.engine.entity.character.player.skill.level.npcLevelChange
import world.gregs.voidps.world.interact.entity.combat.attackers
import world.gregs.voidps.world.interact.entity.combat.combatPrepare
import world.gregs.voidps.world.interact.entity.combat.fightStyle

npcApproach("Attack", "magic_dummy", "melee_dummy", override = false) {
    val type = target.id.removeSuffix("_dummy")
    if (player.fightStyle == type) {
        return@npcApproach
    }
    player.message("You can only use ${type.toTitleCase()} against this dummy.")
    approachRange(10, false)
    player.mode = EmptyMode
    cancel()
}

val itemOnHandler: suspend ItemOnNPC.() -> Unit = handler@{
    val type = target.id.removeSuffix("_dummy")
    if (player.fightStyle == type || type == "magic" && id.endsWith("_spellbook")) {
        return@handler
    }
    player.message("You can only use ${type.toTitleCase()} against this dummy.")
    approachRange(10, false)
    player.mode = EmptyMode
    cancel()
}
itemOnNPCApproach(npc = "melee_dummy", override = false, handler = itemOnHandler)
itemOnNPCApproach(npc = "magic_dummy", override = false, handler = itemOnHandler)

val levelHandler: suspend CurrentLevelChanged.(NPC) -> Unit = handler@{ npc ->
    if (to > 10) {
        return@handler
    }
    npc.levels.clear()
    for (attacker in npc.attackers) {
        attacker.mode = EmptyMode
    }
}
npcLevelChange("melee_dummy", Skill.Constitution, handler = levelHandler)
npcLevelChange("magic_dummy", Skill.Constitution, handler = levelHandler)

combatPrepare("magic") { player ->
    if (target is NPC && target.id == "magic_dummy" && player.fightStyle != "magic") {
        player.message("You can only use Magic against this dummy.")
        cancel()
    }
}

combatPrepare("melee") { player ->
    if (target is NPC && target.id == "melee_dummy" && player.fightStyle != "melee") {
        player.message("You can only use Melee against this dummy.")
        cancel()
    }
}