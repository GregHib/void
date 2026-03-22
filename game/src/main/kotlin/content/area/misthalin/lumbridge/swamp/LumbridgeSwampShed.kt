package content.area.misthalin.lumbridge.swamp

import content.entity.obj.door.enterDoor
import content.entity.player.dialogue.type.choice
import content.quest.quest
import content.quest.questComplete
import content.quest.refreshQuestJournal
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.jingle
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Teleport
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.event.AuditLog
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.carriesItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Tile

class LumbridgeSwampShed : Script {
    init {
        objectOperate("Open", "zanaris_door_closed") { (target) ->
            val stage = quest("lost_city")
            val enter = equipped(EquipSlot.Weapon).id == "dramen_staff" && (stage == "spirit_killed" || stage == "enter_shed" || stage == "completed")
            if (enter) {
                message("The world starts to shimmer...", type = ChatType.Game)
            }
            enterDoor(target)
            if (enter) {
                Teleport.teleport(this, Tile(2452, 4473), "fairy")
            }
        }

        objectOperate("Take", "tools") {
            if (inventory.isFull()) {
                message("You haven't got room to hold them.")
                return@objectOperate
            }
            val hasRake = carriesItem("rake")
            val hasSpade = carriesItem("spade")
            if (hasRake && hasSpade) {
                message("You've already got a spade and a rake.")
                return@objectOperate
            }
            choice("Which would you like to take?") {
                if (!hasRake) {
                    option("Rake") {
                        inventory.add("rake")
                        message("You 'borrow' a rake.", type = ChatType.Filter)
                    }
                }
                if (!hasSpade) {
                    option("Spade") {
                        inventory.add("spade")
                        message("You 'borrow' a spade.", type = ChatType.Filter)
                    }
                }
                if (inventory.spaces >= 2 && !hasRake && !hasSpade) {
                    option("Both") {
                        inventory.add("rake", "spade")
                        message("You 'borrow' a rake and a spade.", type = ChatType.Filter)
                    }
                }
            }
        }

        teleportLand("fairy") {
            val stage = quest("lost_city")
            if (stage == "spirit_killed" || stage == "enter_shed") {
                questComplete()
            }
        }
    }

    fun Player.questComplete() {
        AuditLog.event(this, "quest_completed", "lost_city")
        set("lost_city", "completed")
        jingle("quest_complete_1")
        refreshQuestJournal()
        inc("quest_points", 3)
        softQueue("quest_complete", 1) {
            message("Congratulations, Quest complete!")
            questComplete(
                "Lost City",
                "3 Quest Points",
                "Access to Zanaris",
                item = "dramen_staff",
            )
        }
    }
}
