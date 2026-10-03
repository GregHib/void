package content.area.asgarnia.falador

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.skillcapeMasterDialogue
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.inv.item.addOrDrop
import content.skill.construction.House.Companion.newHouse
import net.pearx.kasechange.toSentenceCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.chat.toDigitGroupString
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.config.RowDefinition
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

class EstateAgent : Script {
    init {
        npcOperate("Talk-to", "estate_agent*") {
            npc<Neutral>("Hello. Welcome to the ${Settings["server.name"]} Housing Agency! What can I do for you?")
            choice {
                if (!contains("house_location")) {
                    option<Quiz>("How can I get a house?") {
                        buyHouse()
                    }
                } else {
                    option<Quiz>("Can you move my house please?") {
                        moveHouse()
                    }
                    option<Quiz>("Can you redecorate my house please?") {
                        redecorateHouse()
                    }
                }
                option<Quiz>("What's that cape you are wearing?") {
                    skillcapeMasterDialogue(Skill.Construction, "a master home builder")
                }
                option<Neutral>("Never mind.")
            }
        }
    }

    private suspend fun Player.buyHouse() {
        npc<Neutral>("I can sell you a starting house in Rimmington for 1000 coins. As you increase your construction skill you will be able to have your house moved to other areas and redecorated in other styles.")
        npc<Quiz>("Do you want to buy a starter house?")
        choice {
            option<Happy>("Yes please!") {
                if (!inventory.remove("coins", 1000)) {
                    player<Sad>("I haven't got 1,000 coins on me.")
                    npc<Neutral>("Well come back when you have it then.")
                    return@option
                }
                set("house_location", "rimmington")
                newHouse()
                npc<Neutral>("Thank you. Go through the Rimmington house portal and you will find your house ready for you to start building in it.")
                npc<Neutral>("This book will help you to start building your house.")
                addOrDrop("construction_guide")
            }
            option<Neutral>("No thanks.") {
                npc<Neutral>("Well enjoy your player-owned cardboard box or wherever you're going to sleep tonight!")
            }
        }
    }

    private suspend fun Player.moveHouse() {
        npc<Neutral>("Certainly. Where would you like it moved to?")
        chooseRow("house_locations") { row ->
            val name = row.rowId.toSentenceCase()
            if (get("house_location", "") == row.rowId) {
                npc<Neutral>("Your house is already in $name!") // TODO proper messages
                return@chooseRow
            }
            if (!pay(row)) {
                return@chooseRow
            }
            set("house_location", row.rowId)
            npc<Happy>("Your house has been moved to $name.") // TODO proper messages
        }
    }

    private suspend fun Player.redecorateHouse() {
        npc<Neutral>("Certainly. My magic can rebuild the house in a completely new style! What style would you like?")
        chooseRow("house_styles") { row ->
            if (get("house_style", "basic_wood") == row.rowId) {
                npc<Neutral>("Your house is already built in that style!") // TODO proper messages
                return@chooseRow
            }
            if (!pay(row)) {
                return@chooseRow
            }
            set("house_style", row.rowId)
            npc<Happy>("Your house has been redecorated.") // TODO proper messages
        }
    }

    /**
     * Choose a row from [table], listed four at a time with their costs
     */
    private suspend fun Player.chooseRow(table: String, page: Int = 0, block: suspend Player.(RowDefinition) -> Unit) {
        val rows = Tables.get(table).rows()
        choice {
            for (row in rows.subList(page * 4, minOf(rows.size, page * 4 + 4))) {
                val name = row.stringOrNull("name") ?: row.rowId.toSentenceCase()
                option("$name (${row.int("cost").toDigitGroupString()})") {
                    block(row)
                }
            }
            if (rows.size > page * 4 + 4) {
                option("More...") {
                    chooseRow(table, page + 1, block)
                }
            } else {
                option<Neutral>("Never mind.")
            }
        }
    }

    /**
     * Checks the construction level of [row] and takes its cost
     */
    private suspend fun Player.pay(row: RowDefinition): Boolean {
        val level = row.int("level")
        if (!has(Skill.Construction, level)) {
            npc<Sad>("I'm afraid you need a Construction level of $level for that.") // TODO proper messages
            return false
        }
        val cost = row.int("cost")
        if (!inventory.remove("coins", cost)) {
            player<Sad>("I haven't got ${cost.toDigitGroupString()} coins on me.")
            npc<Neutral>("Well come back when you have it then.")
            return false
        }
        return true
    }
}
