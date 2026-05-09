package content.quest.member.the_grand_tree

import content.activity.evil_tree.EvilTree
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Teleport
import world.gregs.voidps.type.Tile

class SpiritTree : Script {

    init {
        objectOperate("Talk-to", "spirit_tree,spirit_tree_fullygrown,spirit_tree_gnome,spirit_tree_stronghold") {
            if (!questCompleted("the_grand_tree")) {
                statement("The tree doesn't feel like talking.")
                return@objectOperate
            }
            npc<Neutral>(
                "spirit_tree", "If you are a friend of the gnome people, you are a friend of mine. Do you wish to travel, or are you ${
                    when {
                        EvilTree.tree.id == "strange_sapling" -> "interested in the strange sapling?"
                        EvilTree.tree.id.endsWith("_stump") -> "to ask about the evil tree?"
                        else -> "to help dispatch the evil tree?"
                    }
                }"
            )
            choice("What would you like to ask about?") {
                option("Travel.") {
                    updatePosition(this)
                    open("spirit_tree")
                }
                if (EvilTree.isSapling()) {
                    option("Strange sapling.") {
                        npc<Neutral>("I can help you to find the strange sapling, but my knowledge outside of the anima mundi is limited.")
                        npc<Neutral>("It can be found ${Tables.string("evil_tree_place.${EvilTree.place}.hint")}.")
                    }
                } else {
                    option("Evil tree.") {
                        if (EvilTree.tree.id.endsWith("_stump")) {
                            npc<Neutral>("spirit_tree", "The taint of the evil tree is not currently on the land. There won't be an evil tree for a long time.")
                            npc<Neutral>("spirit_tree", "The taint of the evil tree is not currently on the land. There will be an evil tree in approximately [number] hour [number] minutes.")
                            return@option
                        }
                        npc<Quiz>("spirit_tree", "Would you like me to teleport you directly there?")
                        choice {
                            option("Yes please.") {
                                Teleport.teleport(this, EvilTree.spawnTile.add(-1, -1), "spirit_tree", sound = false)
                            }
                            option<Quiz>("What is this 'evil tree'?") {
                                npc<Neutral>("spirit_tree", "It is an abomination of nature that must be destroyed as quickly as possible. We do not know where it will appear, but, when it does, you should go to it immediately and help out!")
                            }
                            option("I've changed my mind, I want to stay here.")
                        }
                    }
                }
                option("Nothing.") {
                    player<Neutral>("Nothing, thanks.")
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

        interfaceOpened("spirit_tree") { id ->
            interfaceOptions.unlockAll(id, "text", 0 until 9)
        }

        interfaceOption(id = "spirit_tree:text") { (_, itemSlot) ->
            val enum = EnumDefinitions.get("spirit_tree_destination_tiles")
            val map = enum.map ?: return@interfaceOption
            var count = 0
            var index = -1
            for (key in 0 until enum.length) {
                val value = map[key] ?: continue
                if (value == get("spirit_tree_tile", -1)) {
                    continue
                }
                when (key) {
                    5 if get("farming_spirit_tree_patch_port_sarim", "weeds_3") != "spirit_life1" -> continue
                    6 if get("farming_spirit_tree_patch_etceteria", "weeds_3") != "spirit_life1" -> continue
                    7 if get("farming_spirit_tree_patch_brimhaven", "weeds_3") != "spirit_life1" -> continue
                    8 if get("spirit_tree_poison_waste", 0) < 3 -> continue
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
            Teleport.teleport(this, Tile(map[index] as Int), "spirit_tree", sound = false)
            message("You feel at one with the spirit tree.")
        }
    }

    fun updatePosition(player: Player) {
        var closest = Int.MAX_VALUE
        var tile = 0
        for ((_, value) in EnumDefinitions.get("spirit_tree_destination_tiles").map ?: return) {
            val distance = player.tile.distanceTo(Tile(value as Int))
            if (distance < closest) {
                tile = value
                closest = distance
            }
        }
        player["spirit_tree_tile"] = tile
    }
}
