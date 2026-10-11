package content.area.kandarin.yanille

import content.entity.obj.door.enterDoor
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has

class MagicGuild : Script {
    init {
        objectOperate("Open", "magic_guild_door_closed,magic_guild_door_2_closed") { (target) ->
            if (!has(Skill.Magic, 66)) {
                npc<Neutral>("You need a magic level of 66 for admittance to the guild. The magical energy inside the guild is unsafe for anyone below that level. For any other business please ring for attention.")
                return@objectOperate
            }
            enterDoor(target)
        }
    }
}
