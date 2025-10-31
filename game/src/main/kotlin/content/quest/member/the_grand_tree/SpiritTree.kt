package content.quest.member.the_grand_tree

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Talk
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.statement
import content.quest.questCompleted
import content.skill.magic.spell.Teleport
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.event.interfaceOpen
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inject
import world.gregs.voidps.type.Tile

class SpiritTree : Script {

    val enums: EnumDefinitions by inject()

    init {
        objectOperate("Talk-to", "spirit_tree") {
            if (!questCompleted("the_grand_tree")) {
                statement("The tree doesn't feel like talking.")
                return@objectOperate
            }
            npc<Talk>("spirit_tree", "Hello gnome friend. Where would you like to go?")
            updatePosition(this)
            open("spirit_tree")
        }

        objectOperate("Talk-to", "spirit_tree_gnome,spirit_tree_stronghold") {
            if (!questCompleted("the_grand_tree")) {
                statement("The tree doesn't feel like talking.")
                return@objectOperate
            }
            npc<Happy>("spirit_tree_gnome", "You friend of gnome people, you friend of mine. Would you like me to take you somewhere?")
            choice {
                option<Talk>("No thanks, old tree.")
                option<Quiz>("Where can I go?") {
                    npc<Talk>("spirit_tree_gnome", "You can travel to the trees which are related to me.")
                    updatePosition(this)
                    open("spirit_tree")
                }
            }
        }

        objectOperate("Teleport", "spirit_tree*") {
            if (!questCompleted("the_grand_tree")) {
                return@objectOperate
            }
            updatePosition(this)
            open("spirit_tree")
        }

        interfaceOpen("spirit_tree") { player ->
            player.interfaceOptions.unlockAll(id, "text", 0 until 9)
        }

        interfaceOption("*", "text", "spirit_tree") {
            val enum = enums.get("spirit_tree_destination_tiles")
            val map = enum.map ?: return@interfaceOption
            var count = 0
            var index = -1
            for (key in 0 until enum.length) {
                val value = map[key] ?: continue
                if (value == player["spirit_tree_tile", -1]) {
                    continue
                }
                when {
                    key == 5 && player["spirit_tree_port_sarim", 0] != 20 -> continue
                    key == 6 && player["spirit_tree_etceteria", 0] != 20 -> continue
                    key == 7 && player["spirit_tree_brimhaven", 0] != 20 -> continue
                    key == 8 && player["spirit_tree_poison_waste", 0] < 3 -> continue
                }
                if (count == itemSlot) {
                    index = key
                    break
                }
                count++
            }
            if (index == -1) {
                return@interfaceOption
            }
            Teleport.teleport(player, Tile(map[index] as Int), "spirit_tree", sound = false)
            player.message("You feel at one with the spirit tree.")
        }
    }

    fun updatePosition(player: Player) {
        var closest = Int.MAX_VALUE
        var tile = 0
        for ((_, value) in enums.get("spirit_tree_destination_tiles").map ?: return) {
            val distance = player.tile.distanceTo(Tile(value as Int))
            if (distance < closest) {
                tile = value
                closest = distance
            }
        }
        player["spirit_tree_tile"] = tile
    }
}
