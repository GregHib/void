package content.area.asgarnia.falador

import content.skill.magic.spell.spell
import content.skill.melee.weapon.attackRange
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.InterfaceOnItem
import world.gregs.voidps.engine.client.ui.interact.InterfaceOnNPC
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.event.Option
import world.gregs.voidps.engine.event.UseOn
import world.gregs.voidps.type.Tile

private val acceptedTiles = listOf(
    Tile(3005, 3376, 0),
    Tile(2999, 3375, 0),
    Tile(2996, 3377, 0),
    Tile(2989, 3378, 0),
)

@Option("Climb", "giant_mole_lair_escape_rope")
fun ObjectOption<Player>.exitMoleLair() {
    player.anim("climb_up")
    player.tele(acceptedTiles.random())
}

@Option(option = "Examine")
fun ObjectOption<Player>.examineObject() {
    player.message(def.getOrNull("examine") ?: return, ChatType.ObjectExamine)
}

@UseOn(use = ["modern_spellbook"], on = ["superheat_item"])
fun InterfaceOnItem.superHeat() {

}

@UseOn(on = ["*_spellbook"])
suspend fun InterfaceOnNPC.examineObject() {
    if (!player.has(Skill.Slayer, target.def["slayer_level", 0])) {
        player.message("You need a higher slayer level to know how to wound this monster.")
        cancel()
        return
    }
    approachRange(8, update = false)
    player.spell = component
    if (target.id.endsWith("_dummy")/* && !handleCombatDummies()*/) {
        player.clear("spell")
        return
    }
    player["attack_speed"] = 5
    player["one_time"] = true
    player.attackRange = 8
    player.face(target)
//    combatInteraction(player, target)
    cancel()
}