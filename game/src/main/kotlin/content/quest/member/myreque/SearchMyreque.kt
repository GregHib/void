package content.quest.member.myreque

import content.entity.combat.killer
import content.entity.gfx.areaGfx
import content.entity.npc.markHint
import content.entity.obj.door.enterDoor
import content.entity.player.dialogue.type.statement
import content.quest.quest
import content.quest.questCompleted
import content.quest.questJournal
import content.quest.questStage
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.handle.interactPlayer
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.remove
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.queue.strongQueue
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.area.Cuboid

class SearchMyreque : Script {

    init {
        questJournalOpen("in_search_of_the_myreque") {
            val lines = when (quest("in_search_of_the_myreque")) {
                "unstarted" -> notStartedJournal()
                "completed" -> completedJournal()
                else -> startedJournal()
            }
            questJournal("In Search of the Myreque", lines)
        }

        objectApproach("Climb", "swamp_bridge_tree") { (target) ->
            approachRange(1)
            if (!tile.within(target.tile, 1)) {
                return@objectApproach message("You can't reach that.")
            }
            climbTree(target)
        }

        objectApproach("Walk-here", "swamp_bridge1") { (target) ->
            if (tile == target.tile) {
                return@objectApproach
            }
            if (!tile.within(target.tile, 1)) {
                return@objectApproach message("You can't reach that.")
            }
            val givesWay = target.tile in Areas["mort_myre_rope_bridge"] &&
                target.tile.y != BRIDGE_LANDING &&
                !get(rung(target.tile.y), false)
            if (!givesWay) {
                walkTo(target.tile, noCollision = true)
                return@objectApproach
            }
            target.replace("spooky_tree_base_forbridge", ticks = 200)
            statement("This bridge looks pretty old. The wooden boards break apart beneath your feet.")
        }

        objectApproach("Repair", "swamp_bridge1") {
            message("This bridge rung already looks fixed, that's why you're able to stand on it.")
        }

        objectApproach("Repair", "spooky_tree_base_forbridge") { (target) ->
            if (!tile.within(target.tile, 1)) {
                return@objectApproach message("You can't reach that.")
            }
            if (!inventory.contains("plank") || !inventory.contains("steel_nails", 75) || !inventory.contains("hammer")) {
                return@objectApproach statement("You don't have the required materials to fix this part of the bridge.")
            }
            anim("smith_item")
            inventory.remove("plank")
            inventory.remove("steel_nails", 75)
            delay(2)
            target.remove()
            set(rung(target.tile.y), true)
            if (bridgeRepaired()) {
                set("route_bridgecomplete", true)
            }
            walkTo(target.tile, noCollision = true)
        }

        objectOperate("Open", "swamp_wooden_doors_closed") { (target) ->
            arriveDelay()
            val progress = questStage("in_search_of_the_myreque")
            if (progress < myrequeStage("answered_questions")) {
                return@objectOperate statement(
                    "There seems to be a strange combination on the door. It would take ages to work it out.",
                )
            }
            if (progress == myrequeStage("answered_questions")) {
                set("in_search_of_the_myreque", "entered_hideout")
            }
            anim("human_lockedchest")
            sound("locked")
            delay(1)
            target.replace("swamp_wooden_doors_opened", ticks = 3)
            delay(2)
            sound("iron_door_open")
            tele(3500, 9812)
        }

        objectOperate("Enter", "route_cavewalltunnel") {
            tele(3491, 9824)
        }

        objectOperate("Look", "route_stalagmite_cave_entrace") {
            statement(
                "It looks like an ordinary stalagmite, however, beyond it you can see that there is a " +
                    "small cave entrance.",
            )
        }

        objectOperate("Squeeze-past", "route_stalagmite_cave_entrace") {
            statement("You try to squeeze past this stalagmite into a small cavern entrance.")
            anim("squeeze_past_stalagmite")
            delay(3)
            val progress = questStage("in_search_of_the_myreque")
            tele(3505, 9832, if (progress < myrequeStage("hellhound_summoned")) 2 else 0)
            if (progress == myrequeStage("hellhound_summoned")) {
                delay(3)
                spawnHellHound(this)
            }
        }

        objectOperate("Search", "canifis_fake_wall_closed") { (target) ->
            if (questStage("in_search_of_the_myreque") < myrequeStage("shown_way_out")) {
                return@objectOperate message("You see nothing interesting with this wall.")
            }
            message("You search the wall and find the door which Veliaf told you about.")
            if (questStage("in_search_of_the_myreque") == myrequeStage("shown_way_out")) {
                set("in_search_of_the_myreque", "in_inn_basement")
            }
            enterDoor(target)
            message("You walk through.")
        }

        objectOperate("Climb-up", "canifis_tavern_ladder") {
            message("You climb the ladder out into the outside.")
            if (questStage("in_search_of_the_myreque") == myrequeStage("in_inn_basement")) {
                set("in_search_of_the_myreque", "escaped_to_canifis")
            }
            anim("climb_up")
            delay(1)
            tele(3495, 3466)
            message("You emerge to the south of the 'Hair of the Dog' tavern in Canifis.")
        }

        objectOperate("Open", "canifis_tavern_trapdoor") {
            if (!questCompleted("in_search_of_the_myreque")) {
                return@objectOperate message("This trap door seems locked, you're not really sure where it would lead to.")
            }
            delay(1)
            tele(3477, 9845)
            message("You open the trapdoor and find yourself in the inn basement.")
        }

        npcDeath("skeleton_hellhound") {
            val killer = killer as? Player ?: return@npcDeath
            if (killer.questStage("in_search_of_the_myreque") == myrequeStage("hellhound_summoned")) {
                killer["in_search_of_the_myreque"] = "killed_hellhound"
            }
        }

        npcTimerStart("hellhound_leash") { 1 }

        npcTimerTick("hellhound_leash") {
            val owner = Players.findByAccount(get("owner", ""))
            if (owner == null || !HIDEOUT.contains(owner.tile)) {
                despawn()
                return@npcTimerTick Timer.CANCEL
            }
            Timer.CONTINUE
        }
    }

    private suspend fun Player.climbTree(target: GameObject) {
        val north = target.tile.y == NORTH_TREE
        if (north && !get(rung(BRIDGE_TOP), false)) {
            statement(
                "This bridge run looks broken; you would have nowhere to stand. The southern " +
                    "side of the rope bridge looks much less damaged.",
            )
            return
        }
        message("You climb the tree.")
        val up = if (north) tile.y >= target.tile.y else tile.y <= target.tile.y
        val landing = when {
            north && up -> Tile(target.tile.x, BRIDGE_TOP)
            north -> NORTH_BANK
            up -> Tile(target.tile.x, BRIDGE_LANDING)
            else -> SOUTH_BANK
        }
        anim(if (up) "climb_up" else "climb_down")
        delay(1)
        strongQueue("climb_bridge_tree", 1) {
            tele(landing)
        }
    }

    private fun Player.notStartedJournal(): List<String> = listOf(
        "<navy>I can start this quest by talking to a <maroon>stranger <navy>in the '<maroon>Hair",
        "<maroon>of the Dog Inn<navy>' located in the town of <maroon>Canifis <navy>in the land of",
        "<maroon>Morytania<navy>.",
        "",
        "<navy>I need to complete the following quest:",
        if (questCompleted("nature_spirit")) "<str>Nature Spirit" else "<maroon>Nature Spirit",
        "",
        "<navy>I also need to be able to defeat a level 97 foe.",
    )

    private fun completedJournal(): List<String> = listOf(
        "<str>I was set a quest by Vanstrom Klause to deliver weapons",
        "<str>to a group called the Myreque. After negotiating with a",
        "<str>boatman, fixing a broken rope bridge and answering a",
        "<str>guard's questions I eventually found the group called the",
        "<str>Myreque.",
        "",
        "<str>However, it seems that I was tricked by Vanstrom who",
        "<str>turned out to be a vampire following me to the Myreque's",
        "<str>hideout. He killed Sani and Harold, both young members of",
        "<str>the Myreque.",
        "",
        "<str>During the quest I found a quicker route between Mort'ton",
        "<str>and Canifis, and I feel that I better understand the reason",
        "<str>for the darkness of Morytania.",
        "",
        "<str>Perhaps in the future, I can offer help to the Myreque?",
        "",
        "<red>QUEST COMPLETE!",
    )

    private fun Player.startedJournal(): List<String> {
        val progress = questStage("in_search_of_the_myreque")
        val list = mutableListOf<String>()

        list += "<str>I talked to a stranger called Vanstrom Klause in the 'Hair"
        list += "<str>of the Dog Inn' located in the town of Canifis."

        if (progress < myrequeStage("delivered_weapons")) {
            list += "<maroon>Vanstrom Klause <navy>asked me to take some <maroon>weapons <navy>to a"
            list += "<navy>group called '<maroon>The Myreque<navy>'."
            list += "<navy>The weapons I need to collect are:"
            list += weaponLine("steel_longsword", 1, "Steel Longsword")
            list += weaponLine("steel_sword", 2, "Steel Sword")
            list += weaponLine("steel_dagger", 1, "Steel Dagger")
            list += weaponLine("steel_mace", 1, "Steel Mace")
            list += weaponLine("steel_warhammer", 1, "Steel Warhammer")
        } else {
            list += "<str>Vanstrom Klause asked me to take some weapons to a"
            list += "<str>group called 'The Myreque'."
        }

        if ((hasAllWeapons() || progress >= myrequeStage("delivered_weapons")) && progress < myrequeStage("refused_delivery")) {
            list += "<navy>I have all the weapons <maroon>Vanstrom <navy>asked me to get."
            list += "<navy>Vanstrom said that the <maroon>boatman <navy>in <maroon>Mort'ton <navy>should be"
            list += "<navy>able to help."
        }

        if (progress in myrequeStage("refused_delivery") until myrequeStage("persuaded_boatman")) {
            list += "<navy>The <maroon>boatman <navy>won't take me to the <maroon>Myreque's hideout<navy>."
            list += "<navy>Vanstrom did say that I have to be persuasive."
        }

        if (progress >= myrequeStage("persuaded_boatman")) {
            list += "<str>The boatman says he won't take me to the Myreque"
            list += "<str>Vanstrom did say that I have to be persuasive."

            if (progress == myrequeStage("persuaded_boatman")) {
                val hasPouch = inventory.contains("druid_pouch_2", 5) && inventory.contains("silver_sickle_b")
                if (hasPouch) {
                    list += "<str>I've persuaded the boatman to help me find the Myreque."
                    list += "<str>I have the druid pouch as a defence against the ghasts."
                    list += "<str>The boatman says I need to collect 6 wooden planks"
                    list += "<str>before he will let me use his boat."

                    if (inventory.contains("plank", 6)) {
                        list += "<navy>I've got <maroon>six planks<navy>, I should give them to the <maroon>boatman."
                    } else {
                        val needed = 6 - inventory.count("plank")
                        list += "<navy>I need to collect <maroon>$needed <navy>more <maroon>wooden planks"
                    }
                } else {
                    list += "<navy>I've <maroon>persuaded <navy>the <maroon>boatman <navy>to help me find the <maroon>Myreque."
                    list += "<navy>He won't lend the <maroon>boat <navy>unless I can defend against"
                    list += "<maroon>Ghasts<navy>."
                }
            }
        }

        if (progress in myrequeStage("gave_planks") until myrequeStage("reached_hollows")) {
            list += "<str>I've persuaded the boatman to help me find the Myreque."
            list += "<navy>The <maroon>boatman <navy>won't take me to the <maroon>Myreque <navy>but I can use"
            list += "<navy>his <maroon>boat<navy>."
        }

        if (progress in myrequeStage("reached_hollows") until myrequeStage("questioned_by_curpile")) {
            list += "<str>The boatman loaned me the use of his boat."
            list += "<navy>I borrowed the <maroon>boat <navy>and travelled to the '<maroon>Hollows<navy>' in the"
            list += "<navy>middle of <maroon>Mort Myre <navy>in search of the <maroon>Myreque."
        }

        if (progress in myrequeStage("questioned_by_curpile") until myrequeStage("answered_questions")) {
            list += "<str>I borrowed the boat and travelled to the 'Hollows' in the"
            list += "<str>middle of Mort Myre in search of the Myreque."
            list += "<str>While looking for the hideout, I came across a guard called"
            list += "<str>Curpile, he may know where the Myreque are hidden."
            list += "<navy>A guard named <maroon>Curpile <navy>asked me to answer some"
            list += "<navy>questions before he'll let me continue my search for the"
            list += "<maroon>Myreque<navy>."
        }

        if (progress in myrequeStage("answered_questions") until myrequeStage("entered_hideout")) {
            list += "<str>A guard named Curpile asked me some questions about the"
            list += "<str>Myreque which I answered, he'll unlock the door for me"
            list += "<str>now"
            list += "<navy>I need to find the <maroon>entrance <navy>to the <maroon>Myreque's hideout."
        }

        if (progress in myrequeStage("entered_hideout") until myrequeStage("met_veliaf")) {
            list += "<str>I entered an underground area, it might be the Myreque"
            list += "<str>hideout. I'd better look around fully to try and find them."
            list += "<navy>I need to look for the <maroon>members <navy>of the <maroon>Myreque <navy>now."
        }

        if (progress in myrequeStage("met_veliaf") until myrequeStage("hellhound_summoned")) {
            list += "<str>I entered an underground area, it might be the Myreque"
            list += "<str>hideout. I'd better look around fully to try and find them."
            list += "<str>I met Veliaf, he seems to be the leader of the Myreque."
            list += "<maroon>Veliaf <navy>is busy, he's asked me to introduce myself to the"
            list += "<navy>other <maroon>members <navy>of the <maroon>Myreque<navy>."
            list += "<navy>I need to talk to:"
            list += if (get("met_sani", false)) "<str>Sani Piliu" else "<navy>Sani Piliu"
            list += if (get("met_harold", false)) "<str>Harold Evans" else "<navy>Harold Evans"
            list += if (get("met_radigad", false)) "<str>Radigad Ponfit" else "<navy>Radigad Ponfit"
            list += if (get("met_polmafi", false)) "<str>Polmafi Ferdygris" else "<navy>Polmafi Ferdygris"
            list += if (get("met_ivan", false)) "<str>Ivan Strom" else "<navy>Ivan Strom"

            if (checkMembers()) {
                list += "<str>Veliaf was busy, so I introduced myself to the other"
                list += "<str>members of the Myreque."
                list += "<navy>I've introduced myself to all the members now"
                list += "<navy>perhaps I should talk to <maroon>Veliaf <navy>again"
            }

            if (progress in myrequeStage("weapons_accepted") until myrequeStage("hellhound_summoned")) {
                list += "<str>I tried talking to Veliaf but he was still busy."
                list += "<navy>I should try <maroon>talking <navy>to <maroon>Veliaf <navy>in a <maroon>few minutes <navy>when he's"
                list += "<navy>less busy."
            }
        }

        if (progress in myrequeStage("hellhound_summoned") until myrequeStage("killed_hellhound")) {
            list += "<str>Veliaf was busy, so I introduced myself to the other"
            list += "<str>members of the Myreque."
            list += "<str>While talking to Veliaf a mist cloud appeared in the room."
            list += "<str>The mist transformed into Vanstrom, he's a vampire!"
            list += "<str>Vanstrom killed Sani and Harold then summoned a hell"
            list += "<str>hound!"
            list += "<navy>I have to try and kill the <maroon>hell hound <navy>to save the <maroon>Myreque<navy>."
        }

        if (progress in myrequeStage("killed_hellhound") until myrequeStage("shown_way_out")) {
            list += "<str>I killed the hell hound and saved the rest of the Myreque."
            list += "<navy>I was tricked by <maroon>Vanstrom<navy>, he was a <maroon>vampire <navy>after all!"
            list += "<navy>Maybe someone can show me the <maroon>way out <navy>of here?"
        }

        if (progress in myrequeStage("shown_way_out") until myrequeStage("in_inn_basement")) {
            list += "<str>I was tricked by Vanstrom, he was a vampire after all!"
            list += "<str>Maybe someone can show me the way out of here?"
            list += "<navy>I talked to <maroon>Veliaf<navy>, he says that I can get into the <maroon>basement"
            list += "<navy>of the <maroon>Inn <navy>through a <maroon>secret wall<navy>."
        }

        if (progress in myrequeStage("in_inn_basement") until myrequeStage("escaped_to_canifis")) {
            list += "<str>I went through the secret wall Veliaf told me about into the"
            list += "<str>basement of the inn."
            list += "<navy>I should try to find a way up to the <maroon>surface <navy>from the"
            list += "<maroon>basement <navy>of the <maroon>Inn<navy>."
        }

        if (progress >= myrequeStage("escaped_to_canifis")) {
            list += "<str>I climbed up the ladder in the Inn basement to find my way"
            list += "<str>out."
            list += "<navy>I climbed out of the <maroon>basement <navy>of the <maroon>Inn <navy>to find myself in"
            list += "<navy>the town of <maroon>Canifis <navy>where I first met that double crossing"
            list += "<maroon>Vanstrom<navy>. I wonder if I can still find him there?"
        }

        return list
    }

    private fun Player.weaponLine(item: String, amount: Int, displayName: String): String {
        val collected = inventory.contains(item, amount) || questStage("in_search_of_the_myreque") >= myrequeStage("refused_delivery")
        return if (collected) {
            "<str>$amount x $displayName"
        } else {
            "<maroon>$amount x $displayName"
        }
    }

    private companion object {
        private const val NORTH_TREE = 3431

        private const val BRIDGE_LANDING = 3427

        private const val BRIDGE_TOP = 3430

        private val NORTH_BANK = Tile(3503, 3431)
        private val SOUTH_BANK = Tile(3502, 3425)
    }
}

fun myrequeStage(name: String): Int = questStage("in_search_of_the_myreque", name)

fun rung(y: Int): String = "bridgerung${y - 3427}"

fun Player.bridgeRepaired(): Boolean = (3428..3430).all { get(rung(it), false) }

fun Player.hasAllWeapons(): Boolean = inventory.contains("steel_longsword", "steel_dagger", "steel_mace", "steel_warhammer") &&
    inventory.contains("steel_sword", 2)

fun Player.checkMembers(): Boolean = listOf("sani", "harold", "ivan", "radigad", "polmafi").all { get("met_$it", false) }

fun spawnHellHound(player: Player): NPC {
    val hound = NPCs.add("skeleton_hellhound", HELL_HOUND_SPOT, ticks = HELL_HOUND_TICKS, owner = player)
    areaGfx("skeleton_hellhound_summon", HELL_HOUND_SPOT)
    hound.markHint(player)
    hound.softTimers.start("hellhound_leash")
    hound.interactPlayer(player, "Attack")
    player.message("The skeletal hell hound attacks.")
    return hound
}

private val HELL_HOUND_SPOT = Tile(3505, 9834, 0)

private const val HELL_HOUND_TICKS = 800

private val HIDEOUT = Cuboid(Tile(3503, 9829, 0), width = 12, height = 18, levels = 1)
