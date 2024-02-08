package world.gregs.voidps.world.map.lumbridge.combat_hall

import net.pearx.kasechange.toTitleCase
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.ItemOnNPC
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.npcLevelChange
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.suspend.approachRange
import world.gregs.voidps.world.interact.entity.combat.attackers
import world.gregs.voidps.world.interact.entity.combat.combatSwing
import world.gregs.voidps.world.interact.entity.combat.fightStyle

for (type in listOf("magic", "melee")) {
    on<NPCOption>({ target.id == "${type}_dummy" && option == "Attack" && it.fightStyle != type }, Priority.HIGH) { player: Player ->
        player.message("You can only use ${type.toTitleCase()} against this dummy.")
        player.approachRange(10, false)
        player.mode = EmptyMode
        cancel()
    }

    on<ItemOnNPC>({ approach && target.id == "${type}_dummy" && it.fightStyle != type }) { player: Player ->
        player.message("You can only use ${type.toTitleCase()} against this dummy.")
        player.approachRange(10, false)
        player.mode = EmptyMode
        cancel()
    }

    combatSwing(priority = Priority.HIGHER) { player: Player ->
        if (target is NPC && target.id == "${type}_dummy" && player.fightStyle != type) {
            player.message("You can only use ${type.toTitleCase()} against this dummy.")
            player.mode = EmptyMode
            delay = -1
        }
    }

    npcLevelChange("${type}_dummy", Skill.Constitution, Priority.HIGH) { npc: NPC ->
        if (to > 10) {
            return@npcLevelChange
        }
        npc.levels.clear()
        npc.attackers.forEach {
            it.mode = EmptyMode
        }
    }
}
