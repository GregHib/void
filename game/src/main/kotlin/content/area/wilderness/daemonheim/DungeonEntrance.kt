package content.area.wilderness.daemonheim

import content.area.wilderness.daemonheim.DungeoneeringParty.Companion.dungeonLeader
import content.area.wilderness.daemonheim.DungeoneeringParty.Companion.dungeonMembers
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.statement
import content.entity.player.modal.Tab
import content.entity.player.modal.tab
import content.quest.largeInstance
import content.quest.smallInstance
import content.skill.dungeoneering.DungeonGenerator
import content.skill.dungeoneering.DungeonMap
import content.skill.dungeoneering.DungeonSize
import content.skill.dungeoneering.DungeonStartingItems
import content.skill.magic.spell.spellBook
import content.skill.summoning.pet.dismissPet
import content.skill.summoning.pet.pet
import net.pearx.kasechange.toTitleCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.command.adminCommand
import world.gregs.voidps.engine.client.command.intArg
import world.gregs.voidps.engine.client.command.stringArg
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerRights
import world.gregs.voidps.engine.entity.character.player.hasRights
import world.gregs.voidps.engine.entity.character.player.isAdmin
import world.gregs.voidps.engine.inv.carriesItem
import world.gregs.voidps.engine.inv.clear
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.queue.engineQueue
import world.gregs.voidps.engine.queue.strongQueue

class DungeonEntrance : Script {
    init {
        objectOperate("Climb-down", "daemonheim_dungeon_entrance") {
            if (!carriesItem("ring_of_kinship")) {
                item("ring_of_kinship", "To join or create a party, you need a ring of kinship. You can get one form the dungeoneering tutor, on the right of the entrance to Daemonheim castle.")
                return@objectOperate
            }
            if (!DungeoneeringParty.inParty(this)) {
                choice("Would you like to start a party?") {
                    option("Yes.") {
                        DungeoneeringParty.setLeader(this)
                        open("dungeoneering_party")
                        tab(Tab.QuestJournals)
                        // Temp
                        for (member in dungeonMembers) {
                            member["dungeoneering_party_floor"] = 1
                            member["dungeoneering_party_complexity"] = 1
                        }
                    }
                    option("No.") {
                        message("You must be in a party to enter a dungeon.")
                        return@option
                    }
                }
                return@objectOperate
            }
            if (this != dungeonLeader) {
                message("<red_orange>Only the party leader can start a dungeon.")
                return@objectOperate
            }
            if (!allMembersCanEnter()) {
                return@objectOperate
            }
            if (dungeonMembers.size == 1) {
                dungeonSize(1)
                return@objectOperate
            }

            // https://youtu.be/tg1Kf4iAkN4?t=243
            statement("Please select the number of players you want the dungeon to be </col>designed for. <col=531e13>You may not be able to complete a dungeon if too many people leave.")
            val partySize = dungeonMembers.size
            choice {
                for (i in 1..partySize) {
                    option("$i${if (i == partySize) " (recommended)" else ""}") {
                        dungeonSize(i)
                    }
                }
            }
//            item("ring_of_kinship", 300, "You have unlocked more features and opportunities within Daemonheim. You can now reach complexity level 2.")
        }

        adminCommand("start_dungeon", intArg("floor", optional = true), stringArg("size", autofill = setOf("small", "medium", "large"), optional = true), intArg("complexity", optional = true)) { args ->
            val floor = args.getOrNull(0)?.toInt() ?: 1
            val size = DungeonSize.valueOf((args.getOrNull(1) ?: "small").toTitleCase())
            val complexity = args.getOrNull(2)?.toInt() ?: 1
            if (!DungeoneeringParty.inParty(this)) {
                DungeoneeringParty.setLeader(this)
                open("dungeoneering_party")
                tab(Tab.QuestJournals)
            }
            for (member in dungeonMembers) {
                member["dungeoneering_party_floor"] = floor
                member["dungeoneering_party_complexity"] = complexity
            }
            if (!allMembersCanEnter()) {
                return@adminCommand
            }
            generate(size, dungeonMembers.size)
        }
    }

    private fun Player.allMembersCanEnter(): Boolean {
        for (member in dungeonMembers) {
            if (!member.canEnter()) {
                if (member == this) {
                    // https://youtu.be/1e4dfeuKsdg?t=133
                    message("You are carrying items that cannot be taken into Daemonheim.")
                } else {
                    message("A member of your party has items that cannot be taken into Daemonheim.") // TODO proper message
                }
                return false
            }
            if (pet != null) {
                if (member == this) {
                    message("You cannot bring a familiar into Daemonheim.") // TODO proper message
                } else {
                    message("A member of your party has a familiar that cannot be taken into Daemonheim.") // TODO proper message
                }
                return false
            }
        }
        return true
    }

    private fun Player.canEnter(): Boolean {
        for (item in inventory.items) {
            if (item.isNotEmpty() && item.id != "ring_of_kinship") {
                return false
            }
        }
        for (item in equipment.items) {
            if (item.isNotEmpty() && item.id != "ring_of_kinship") {
                return false
            }
        }
        return true
    }

    private suspend fun Player.dungeonSize(playerCount: Int) {
        if (get("dungeoneering_party_complexity", 1) == 1) {
            generate(DungeonSize.Small, playerCount)
            return
        }
        choice("What size of dungeon would you like?") {
            option("Small.") {
                generate(DungeonSize.Small, playerCount)
            }
            option("Medium.") {
                generate(DungeonSize.Medium, playerCount)
            }
            option("Large.") {
                generate(DungeonSize.Large, playerCount)
            }
        }
    }

    private fun Player.generate(size: DungeonSize, playerCount: Int) {
        val floor = get("dungeoneering_party_floor", 1)
        val complexity = get("dungeoneering_party_complexity", 1)
        if (!isAdmin()) {
            if (floor > 11) {
                message("<red>Floor $floor dungeons are not currently implemented.")
                return
            }
            if (complexity != 1) {
                message("<red>Complexity $complexity dungeons are not currently implemented.")
                return
            }
        }
        val generator = DungeonGenerator(
            size = size,
            floor = floor,
            complexity = complexity,
            playerCount = playerCount,
        )

        val start = System.currentTimeMillis()
        val skills = DungeonMap.maxSkills(dungeonMembers)
        val dungeon = generator.generate(skills)
        if (dungeon == null) {
            message("<red_orange>Failed to generate ${size.name.lowercase()} c$complexity:f$floor dungeon.")
            return
        }
        set("dungeon", dungeon)
        if (get("debug", false)) {
            println("Dungeon generation took ${System.currentTimeMillis() - start}ms")
            dungeon.prettyPrint()
        }
        val instance = when (size) {
            DungeonSize.Small -> smallInstance(logout = false)
            DungeonSize.Medium -> smallInstance(logout = false)
            DungeonSize.Large -> largeInstance()
        }
        dungeon.region = instance
        for (member in dungeonMembers) {
            dungeon.players.add(member.index)
            member["delay"] = 3
        }
        val guideMode = get("dungeoneering_guide_mode", false)
        val startRoom = dungeon.start()
        startRoom.open(this, dungeon)
        val tile = dungeon.startTile()
        strongQueue("enter_dungeon", 2) {
            for (member in dungeonMembers) {
                member["show_daemonheim_map"] = true
                member["dungeoneering_party_size"] = size.name
                member["dungeon_deaths"] = 0
                member["in_dungeoneering"] = true
                member["in_multi_combat"] = true
                member["dungeoneering_stored_kinship"] = member.carriesItem("ring_of_kinship")
                member["dungeoneering_stored_spellbook"] = member.spellBook
                member.open("dungeoneering_spellbook")
                member.levels.clear()
                member.inventory.clear()
                member.equipment.clear()
                member.dismissPet()
                DungeonStartingItems.spawn(dungeon, complexity)
                member.open("rand_overlay")
                member.message("")
                member.message("- Welcome to Daemonheim -")
                member.message("Floor <purple>$floor</col>    Complexity <purple>$complexity")
                member.message("Dungeon Size: <purple>$size")
                member.message("Party Size:Difficulty <purple>${dungeonMembers.size}:${dungeon.playerCount}")
                member.message("<purple>Guide Mode ${if (guideMode) "ON" else "OFF"}")
                member.message("")
                if (guideMode) {
                    member.engineQueue("dungeon_start") {
                        when (floor) {
                            1 -> {
                                statement("You have just entered a dungeon. In the starting room, you'll find a smuggler to trade and store items with, as well as some starting supplies in your backpack and around the room.")
                                statement("If you want to leave, there is a ladder that will take you back to the surface. For more information, speak to the smuggler.")
                                item("katagon_platebody", "Some equipment has been allocated to you. To find out more about an item, use it on the smuggler.")
                                item("katagon_platebody", "If you like the equipment, you can keep it by right-clicking 'Bind' on it. To find out more about the bind system, open the bind setup by right-clicking the smuggler.")
                            }
                            2 -> item("heim_crab", "Equipment and food have different 'tiers', indicating how good they are (tier 1 being the lowest, tier 11 being the highest). Examine them to find out!")
                            3 -> statement("Did you know that your whole party can read your chat, regardless of how far away they are?")
                        }
                    }
                }
                // TODO not clear which dialogue interface was used https://youtu.be/yCSJaU4azVA?t=384
                member.tele(tile)
            }
        }
    }
}
