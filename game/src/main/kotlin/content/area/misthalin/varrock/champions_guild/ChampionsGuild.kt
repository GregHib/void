package content.area.misthalin.varrock.champions_guild

import content.entity.obj.door.enterDoor
import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message

class ChampionsGuild : Script {
    init {
        objectOperate("Open", "door_champions_guild_closed") { (target) ->
            if (get("quest_points", 0) < 32) {
                npc<Angry>("guildmaster", "You have not proved yourself worthy to enter here yet.")
                message("The door won't open - you need at least 32 Quest Points.")
                return@objectOperate
            }
            val entered = tile.y >= 3363
            enterDoor(target)
            if (entered) {
                npc<Neutral>("guildmaster", "Greetings bold adventurer. Welcome to the guild of Champions.")
            }
        }
    }
}