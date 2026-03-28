package content.area.morytania.braindeath_island

import content.entity.combat.hit.damage
import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Drunk
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Laugh
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Scared
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.Shock
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.Despawn.Companion.player
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

class FeverSpider : Script {
    init {
        npcCondition("slayer_gloves") { it is Player && it.equipped(EquipSlot.Hands).id == "slayer_gloves" }
        npcCondition("no_slayer_gloves") { it is Player && it.equipped(EquipSlot.Hands).id != "slayer_gloves" }
        npcAttack("fever_spider", "disease") {
            val damage = (it.levels.get(Skill.Constitution) / 100.0) * 12.5
            it.damage(damage.toInt())
        }
    }
}