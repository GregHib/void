package content.area.kandarin.fishing_guild

import content.entity.obj.door.enterDoor
import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has

class FishingGuild : Script {
    init {
        objectOperate("Open", "gate_fishing_guild_*_closed") { (target) ->
            if (!has(Skill.Fishing, 68)) {
                npc<Neutral>("master_fisher", "Hello, only the top fishers are allowed in here. You need a fishing level of 68 to enter.")
                return@objectOperate
            }
            enterDoor(target)
        }

        objectOperate("Open", "gate_fishing_contest_closed") { (target) ->
            npc<Neutral>("morris", "Competition pass please.")
            choice {
                option<Sad>("I don't have one of them.") {
                    npc<Neutral>("morris", "Oh well. I can't let you past then.")
                }
                option<Confused>("What do I need that for?") {
                    npc<Neutral>("morris", "This is the entrance to the Hemenster fishing competition. It's a high class competition. Invitation only.")
                }
            }
        }
    }
}