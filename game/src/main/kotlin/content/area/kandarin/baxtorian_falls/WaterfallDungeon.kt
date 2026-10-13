package content.area.kandarin.baxtorian_falls

import content.entity.combat.hit.damage
import content.entity.gfx.areaGfx
import content.entity.player.AdventurersLogs
import content.quest.quest
import content.quest.questComplete
import content.quest.questCompleted
import content.quest.questStage
import content.quest.refreshQuestJournal
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.jingle
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.event.AuditLog
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.engine.queue.longQueue
import world.gregs.voidps.type.Tile

class WaterfallDungeon : Script {
    init {
        itemOnObjectOperate("air_rune,water_rune,earth_rune", "pillar") { interaction ->
            if (placeRune(interaction.target.tile, interaction.item.id)) {
                message("You place the rune on the stand.")
                areaGfx("smokepuff_large", interaction.target.tile, height = 100)
                delay(2)
                message("The rune stone disappears in a puff of smoke.")
            }
            // TODO if not during quest? - Can't access without amulet
        }

        itemOnObjectOperate("glarials_amulet", "waterfall_glarial_statue") {
            if (questCompleted("waterfall_quest")) {
                return@itemOnObjectOperate
            }
            if (questStage("waterfall_quest") < 4 || get("waterfall_pillars", 0) != ALL_RUNES) {
                floodChamber("You go to place the amulet around the neck of the statue. However, water floods into the room as you do...")
            } else if (quest("waterfall_quest") == "restored_amulet" || inventory.remove("glarials_amulet")) {
                if (quest("waterfall_quest") == "entered_dungeon") {
                    set("waterfall_quest", "restored_amulet")
                }
                message("You place the amulet around the neck of the statue.")
                delay(2)
                message("You hear a loud rumble from beneath...")
                delay(2)
                message("The ground raises up before you!")
                // The cache contains a second chamber with the raised floor, offset east and south.
                tele(2603, 9914)
            }
        }

        // The chalice is above the player. Approach allows interaction with it across the ledge.
        itemOnObjectApproach("glarials_urn", "waterfall_chalice") {
            if (quest("waterfall_quest") != "restored_amulet") {
                if (!questCompleted("waterfall_quest")) {
                    floodChamber("You go to take the treasure from the chalice. However, water floods into the room as you do...")
                }
            } else if (inventory.spaces < 5) {
                message("You do not have enough space for the reward, you will need 5 free slots to accept it.")
            } else {
                message("You carefully pour the ashes into the chalice")
                delay(2)
                message("as you remove the Treasure of Baxtorian.")
                delay(2)
                message("The chalice remains standing.")
                delay(2)
                if (completeQuest()) {
                    message("Inside you find a mithril case")
                    message("containing 40 seeds,")
                    message("two diamonds and two gold bars.")
                    message("Congratulations! Quest complete!")
                }
            }
        }

        objectApproach("Take treasure", "waterfall_chalice") {
            if (questCompleted("waterfall_quest")) {
                message("The chalice only contains some old ashes.")
                return@objectApproach
            }
            floodChamber("You go to take the treasure from the chalice. However, water floods into the room as you do...")
        }
    }


    private suspend fun Player.floodChamber(text: String) {
        message(text)
        delay(2)
        damage(80)
        tele(2527, 3413)
        message("...you are washed out of the cave and down the river.")
    }

    internal fun Player.completeQuest(): Boolean {
        if (quest("waterfall_quest") != "restored_amulet" || inventory.spaces < 5 || !inventory.contains("glarials_urn")) return false
        val success = inventory.transaction {
            remove("glarials_urn")
            add("glarials_urn_empty")
            add("gold_bar", 2)
            add("diamond", 2)
            add("mithril_seeds", 40)
        }
        if (!success) return false
        set("waterfall_quest", "completed")
        exp(Skill.Attack, 13750.0)
        exp(Skill.Strength, 13750.0)
        AuditLog.event(this, "quest_completed", "waterfall_quest")
        AdventurersLogs.questCompleted(this, "waterfall_quest", points = 1)
        inc("quest_points")
        refreshQuestJournal()
        jingle("quest_complete_1")
        longQueue("quest_complete", 1) {
            questComplete(
                "Waterfall Quest",
                "1 Quest Point",
                "13,750 Attack XP",
                "13,750 Strength XP",
                "2 gold bars",
                "2 diamonds",
                "40 mithril seeds",
                "Access to the Waterfall Dungeon",
                item = "glarials_urn"
            )
        }
        return true
    }

    internal fun Player.placeRune(pillar: Tile, rune: String): Boolean {
        val index = PILLARS.indexOf(pillar)
        val runeIndex = RUNES.indexOf(rune)
        if (index == -1 || runeIndex == -1 || questStage("waterfall_quest") < 4 || questStage("waterfall_quest") >= 8) {
            return false
        }
        val bit = 1 shl (index * 3 + runeIndex)
        val placed = get("waterfall_pillars", 0)
        if (placed and bit != 0) {
            message("You have already placed this type of rune on this pillar.")
            return false
        }
        if (!inventory.remove(rune)) {
            return false
        }
        set("waterfall_pillars", placed or bit)
        return true
    }

    companion object {
        const val ALL_RUNES = (1 shl 18) - 1
        val RUNES = listOf("air_rune", "water_rune", "earth_rune")
        val PILLARS = listOf(Tile(2562, 9910), Tile(2562, 9912), Tile(2562, 9914), Tile(2569, 9910), Tile(2569, 9912), Tile(2569, 9914))
    }
}