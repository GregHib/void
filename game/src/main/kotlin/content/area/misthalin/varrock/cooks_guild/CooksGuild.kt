package content.area.misthalin.varrock.cooks_guild

import content.entity.obj.door.enterDoor
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

class CooksGuild : Script {
    init {
        objectOperate("Open", "door_cooks_guild_closed") { (target) ->
            if (levels.get(Skill.Cooking) < 32) {
                npc<Quiz>("head_chef", "You need a cooking level of 32 to come in here.")
                return@objectOperate
            }
            if (equipped(EquipSlot.Hat).id != "chefs_hat" && equipped(EquipSlot.Hat).id != "cooking_hood" && equipped(EquipSlot.Cape).id != "cooking_cape" && equipped(EquipSlot.Cape).id != "cooking_cape_t") {
                npc<Neutral>("head_chef", "You can't come in here unless you're wearing a chef's hat, or something like that.")
                return@objectOperate
            }
            // https://youtu.be/uPZANIKxM7c?si=nLgUxCiqTr48rKCk&t=98
//            npc<Happy>("My word! A master explorer of Varrock! Come in, come in! You are more than welcome in here, my friend!")
            enterDoor(target)
        }

        objectOperate("Open", "door_cooks_guild_achievement_closed") { (target) ->
            if (levels.get(Skill.Cooking) < 99) {
                statement("You must have completed the Varrock Achievement Diary Hard tasks or master the Cooking skill to gain entry.")
                return@objectOperate
            }
            enterDoor(target)
        }
    }
}
