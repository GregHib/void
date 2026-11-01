package content.minigame.great_orb_project

import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.equip.Equipping
import content.entity.player.inv.item.addOrDrop
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.addToLimit
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItemLimit.removeToLimit
import world.gregs.voidps.engine.queue.strongQueue
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Tile

class GreatOrbProject : Script {
    init {
        exited("runecrafting_guild") {
            if (tile !in Areas["air_altar"]) {
                close("rc_guild_overlay")
            }
        }

        itemOption("Make-barrier", "yellow_barrier_generator,green_barrier_generator") {
            message("You cannot create a barrier here.")
        }

        playerDespawn {
            leave(this)
        }

        itemAdded("*_attractor", "worn_equipment") {
            set("gop_wand", "attractor")
        }

        itemAdded("*_repeller", "worn_equipment") {
            set("gop_wand", "repeller")
        }

        npcApproach("Repel", "yellow_orb,green_orb") { (target) ->
            val team = team ?: return@npcApproach
            if (team.disableWands) {
                val colour = get("gop_team", "")
                val targetColour = target.id.removeSuffix("_orb")
                if (colour != targetColour) {
                    message("The $targetColour orbs have stopped responding to $colour wands.")
                    return@npcApproach
                }
            }
//            message("You fail to remove the barrier.") // https://youtu.be/01EWnc60K6o?t=235
//            message("You remove the barrier.") // https://youtu.be/01EWnc60K6o?t=235
        }

        npcApproach("Attract", "yellow_orb,green_orb") {
        }

        npcApproach("Change-wand", "yellow_orb,green_orb") {
            val wand = equipped(EquipSlot.Weapon).id
            val item = Item(if (wand.endsWith("_attractor")) wand.replace("_attractor", "_repeller") else wand.replace("_repeller", "_attractor"))
            val index = inventory.indexOf(item.id)
            if (index == -1) {
                return@npcApproach
            }
            Equipping.equip(this, item, index)
            steps.clear() // TODO correct?
            message("Swapping wands.")
        }

        destroyed("*_repeller,*_attractor,*_barrier_generator") {
            leave(this)
        }

        /*
            lobby portal = rcguild_portal_teleporter

            https://youtu.be/On1sAphh1Bo?t=83
            "Waiting for orbs to appear."
            npc<Neutral>("wizard_acantha", "Stop dawdling and enter the portal to the Air Altar.")
            message("You enter the portal.")

            TODO questions:
                - Are projectiles from player -> orb or orb -> player or depends on attract/repell?

             https://youtu.be/Uaii7rWxnP4?t=263
            message("You cannot create a barrier this close to the altar.")
            message("<red>Warning<black>: Your activity bar is getting low! Help out with the projec to refill it!")


            Abandoning the game at the beginning will result in a 10-minute time penalty for F2P and 15-minute penalty for P2P preventing players from joining a new game. This penalty will decrease to a minimum of 1 minute as the game progresses through the altars.

            If fewer than 4 people remain when a player leaves, all players will be removed from the game, although only the player who intentionally left will be penalised.

         */
        npcSpawn("wizard_elriss") {
            softTimers.start("great_orb_project")
        }

        npcTimerStart("great_orb_project") { 15 }

        val portals = mutableListOf<NPC>()

        npcTimerTick("great_orb_project") {
            when (inc("tick_counter")) {
                0 -> {
                    if (lobby.ready()) {
                        val index = teamIndex++
                        if (teamIndex >= teams.size) {
                            teamIndex = 0
                        }
                        val team = teams[index]
                        team.yellow.addAll(lobby.yellow)
                        team.green.addAll(lobby.green)
                        lobby.clear()
                        portals.add(NPCs.add("rcguild_portal_teleporter", Tile(1696, 5373, 2)))
                        team.forEach { green ->
                            interfaces.sendText("rc_guild_overlay", "text", "Waiting for orbs to appear.")
                            set("gop_team_index", index)
                            strongQueue("gop_start") {
                                if (green) {
                                    npc<Neutral>("wizard_acantha", "Stop dawdling and enter the portal to the Air Altar.")
                                } else {
                                    // TODO
                                }
                            }
                        }
                    }
                }
                25 -> { // 15 seconds
                    for (portal in portals) {
                        NPCs.remove(portal)
                    }
                    for (team in teams) {
                        team.forEach {
                            interfaces.sendVisibility("rc_guild_overlay", "text_layer", false)
                        }
                    }
                    lobby.teleAll(Areas["air_altar"], Tile(2841, 4829, 0)) // TODO drag happens 30s after start of next game
                    // start spawning orbs
                    // TODO are orbs spawned on a cadence or do respawn once caught?
                    //  Is there a fixed number of orbs in play?
                    //  Is there a limit on how many can be caught?

                    // TODO prevent interaction 30s before end of game
                }
                75 -> { // 30s after start
                    for (team in teams) {
                        // TODO drag to the correct location
                        team.teleAll(Areas["air_altar"], Tile(2841, 4829, 0))
                    }
                }
                175 -> { // 30s before end
                    for (team in teams) {
                        team.disableWands = true
                    }
                }
                225 -> { // 2 minutes
                    for (team in teams) {
                        if (team.altar++ >= ALTARS) { // last
                            //  give reward
                            team.forEach { green ->
                                if (get("orbs_moved", 0) <= 0) {
                                    // TODO message no activity
                                    return@forEach
                                }
                                var tokens = 0
                                val ties = ALTARS - (team.greenWins + team.yellowWins)
                                tokens += ties * 25
                                if (green) {
                                    tokens += team.greenWins * 100
                                    tokens += team.yellowWins * 10
                                    if (team.greenWins > team.yellowWins) {
                                        tokens += if (World.members) 200 else 150
                                    }
                                } else {
                                    tokens += team.yellowWins * 100
                                    tokens += team.greenWins * 10
                                    if (team.yellowWins > team.greenWins) {
                                        tokens += if (World.members) 200 else 150
                                    }
                                }
                                if (team.yellowWins == team.greenWins) {
                                    tokens += if (World.members) 100 else 75
                                }
                                strongQueue("gop_reward") {
                                    reward()
                                }
                            }
                            team.teleAll(Areas["runecrafting_guild"], Tile(1696, 5464, 2))
                            team.clear()
                        } else {
                            // give essence
                            team.forEach { green ->
                                val essence = if (green) {
                                    (team.greenScore - (team.yellowScore / 2)) + 1
                                } else {
                                    (team.yellowScore - (team.greenScore / 2)) + 1
                                }
                                val type = if (World.members) "pure_essence" else "rune_essence"
                                val added = inventory.addToLimit(type, essence)
                                if (added != essence) {
                                    for (i in added until essence) {
                                        addOrDrop(type)
                                    }
                                }
                            }
                            portals.add(NPCs.add("rcguild_portal", Tile(2844, 4828))) // TODO proper next tile
                        }
                    }
                    clear("tick_counter")
                }
            }
            Timer.CONTINUE
        }


    }

    val Player.team: GreatOrbProjectTeam?
        get() = teams.getOrNull(get("gop_team", -1))

    fun Player.reward() {
        /*
            npc<Happy>("wizard_vief", "Excellent! We claimed 5 altars and Acantha's followers only got 3! This should really put our efforts beyond that of Wizard Acantha's.")
            message("You did not have enough players to continue the game.")
            message("Wizard Vief gives you $tokens tokens.", Filter)
            npc<Unsure>("wizard_vief", "Hmmm, our team did well, but you didn't make much of an effort did you? You must contribute at each altar!") // https://youtu.be/On1sAphh1Bo?t=83

            https://youtu.be/01EWnc60K6o?t=419
            npc<Upset>("wizard_acantha", "A stalemate? I could have done better blindfolded. How can you expect to save the altars when both teams claimed 2 altars?")

         */
    }

    companion object {
        private val lobby = GreatOrbProjectTeam()

        private const val MAX_ACTIVITY = 1000
        private val ALTARS = if (World.members) 8 else 6

        var teamIndex = 0
        val teams = Array(if (World.members) 4 else 3) { GreatOrbProjectTeam() }

        fun cantJoin(player: Player): Boolean = player.inventory.spaces < 2 || player.equipped(EquipSlot.Hat).isNotEmpty() || player.equipped(EquipSlot.Hands).isNotEmpty()

        fun join(player: Player, team: String) {
            // TODO team full
            val success = player.equipment.transaction {
                set(EquipSlot.Hat.index, Item("runecrafter_hat_the_great_orb_project_$team"))
                set(EquipSlot.Weapon.index, Item("${team}_attractor"))
            }
            if (!success) {
                return
            }
            val lobby = if (team == "yellow") lobby.yellow else lobby.green
            lobby.add(player.accountName)
            player["gop_team"] = team
            player.open("rc_guild_overlay")
            player["gop_activity"] = MAX_ACTIVITY
            player.interfaces.sendVisibility("rc_guild_overlay", "green_layer", team == "green")
            player.interfaces.sendVisibility("rc_guild_overlay", "yellow_layer", team == "yellow")
            player.interfaces.sendVisibility("rc_guild_overlay", "text_layer", true)
            player.interfaces.sendText("rc_guild_overlay", "text_layer", "Waiting for players to join")
            player.inventory.transaction {
                add("${team}_repeller")
                add("${team}_barrier_generator")
            }
        }

        fun leave(player: Player) {
            if (player.tile !in Areas["runecrafting_guild"]) {
                player.tele(1696, 5464, 2)
            }
            val team = player.remove<String>("gop_team")
            val lobby = if (team == "yellow") lobby.yellow else lobby.green
            lobby.remove(player.accountName)
            player.clear("gop_wand")
            player.clear("gop_activity")
            player.close("rc_guild_overlay")
            player.equipment.transaction {
                removeToLimit("green_repeller")
                removeToLimit("yellow_repeller")
                removeToLimit("green_attractor")
                removeToLimit("yellow_attractor")
                removeToLimit("runecrafter_hat_the_great_orb_project_green")
                removeToLimit("runecrafter_hat_the_great_orb_project_yellow")
            }
            player.inventory.transaction {
                removeToLimit("green_repeller")
                removeToLimit("yellow_repeller")
                removeToLimit("green_attractor")
                removeToLimit("yellow_attractor")
                removeToLimit("green_barrier_generator")
                removeToLimit("yellow_barrier_generator")
            }
        }
    }
}
