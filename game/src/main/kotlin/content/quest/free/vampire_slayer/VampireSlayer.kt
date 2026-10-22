package content.quest.free.vampire_slayer

import content.entity.combat.killer
import content.entity.player.bank.bank
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.quest.quest
import content.quest.questComplete
import content.quest.questJournal
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.handle.interactPlayer
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.jingle
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.carriesItem
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random

private const val QUEST = "vampire_slayer"
private const val COUNT = "count_draynor_normal"
private const val ACTIVE_COUNT = "vampire_slayer_count"
private const val COUNT_TIMER = "vampire_slayer_count"

/** Revision-634 implementation; see docs/quest-contributing.md for references and revision differences. */
class VampireSlayer : Script {
    init {
        npcOperate("Talk-to", "morgan") {
            when (quest(QUEST)) {
                "unstarted" -> {
                    npc<Neutral>("Please help! A vampyre is terrorising Draynor Village.")
                    player<Neutral>("Where can I find it?")
                    npc<Neutral>("Count Draynor lives in the cellar of the manor to the north.")
                    choice {
                        option("I'll help you.") { accept() }
                        option("Do you have any advice?") { accept() }
                        option("Vampyres sound too dangerous.") {
                            npc<Neutral>("Please come back if you change your mind.")
                        }
                    }
                }
                "completed" -> npc<Happy>("Thank you for saving our village from Count Draynor!")
                else -> npc<Neutral>("Speak to Dr Harlow at the Blue Moon Inn in Varrock. Tell him Morgan sent you.")
            }
        }

        objectOperate("Open", "morgan_garlic_drawers") {
            if (inventory.add("garlic")) {
                statement("You search the drawers and find a clove of garlic.")
            } else {
                message("You don't have enough inventory space to take the garlic.")
            }
        }

        objectOperate("Open", "coffin") { (target) ->
            // Object 158 is also used elsewhere; only this coffin belongs to the quest.
            if (target.tile != Tile(3080, 9785)) {
                message("There is nothing of interest inside.")
                return@objectOperate
            }
            when (quest(QUEST)) {
                "completed" -> message("The coffin is empty apart from a pillow.")
                "armed" -> {
                    val existing = get<NPC>(ACTIVE_COUNT)
                    if (existing != null && existing.index != -1 && !existing.hide) {
                        message("Count Draynor is already out of his coffin!")
                        return@objectOperate
                    }
                    val count = NPCs.add(COUNT, Tile(3079, 9785), ticks = -1, owner = this)
                    set(ACTIVE_COUNT, count)
                    count.softTimers.start(COUNT_TIMER)
                    count.interactPlayer(this, "Attack")
                    message("Count Draynor emerges from the coffin!")
                }
                else -> message("You should learn how to defeat the vampyre before disturbing his coffin.")
            }
        }

        npcTimerStart(COUNT_TIMER) { 2 }
        npcTimerTick(COUNT_TIMER) {
            val owner = Players.findByAccount(get("owner", ""))
            if (owner == null || owner.dead || owner.quest(QUEST) != "armed" || owner.tile.level != tile.level || owner.tile.distanceTo(tile) > 16) {
                NPCs.remove(this)
                Timer.CANCEL
            } else {
                // Garlic suppresses the Count's regeneration; life points are ten times pre-2010 HP.
                if (random.nextInt(7) == 0) {
                    levels.set(Skill.Constitution, (levels.get(Skill.Constitution) + if (owner.carriesItem("garlic")) 10 else 100).coerceAtMost(350))
                }
                Timer.CONTINUE
            }
        }

        npcCanDie(COUNT) {
            val owner = Players.findByAccount(get("owner", ""))
            if (owner == null || owner.quest(QUEST) != "armed" || !owner.hasVampireTools()) {
                // Re-check at impact, including hits already in flight when a tool is lost.
                levels.set(Skill.Constitution, 350)
                owner?.message("The vampyre regenerates. You need a stake and hammer to finish him off.")
                false
            } else {
                true
            }
        }

        npcDeath(COUNT) { death ->
            death.respawn = false
            death.dropItems = false
            val player = killer as? Player ?: return@npcDeath
            if (player.accountName != get<String>("owner") || player.quest(QUEST) != "armed" || !player.hasVampireTools()) {
                return@npcDeath
            }
            if (!player.consumeStake()) return@npcDeath
            player.set(QUEST, "completed")
            player.inc("quest_points", 3)
            player.exp(Skill.Attack, 4825.0)
            player.clear(ACTIVE_COUNT)
            player.message("You drive the stake through Count Draynor's heart.")
            player.message("Congratulations, you've completed a quest: <col=081190>Vampire Slayer</col>.")
            player.queue("vampire_slayer_reward", 8) {
                jingle("quest_complete_1")
                questComplete("Vampire Slayer", "3 Quest Points", "4,825 Attack XP.", item = "stake")
            }
        }

        npcDespawn(COUNT) {
            val owner = Players.findByAccount(get("owner", ""))
            if (owner?.get<NPC>(ACTIVE_COUNT) === this) owner.clear(ACTIVE_COUNT)
        }
        playerDespawn {
            NPCs.remove(get<NPC>(ACTIVE_COUNT))
            clear(ACTIVE_COUNT)
        }
        playerDeath {
            NPCs.remove(get<NPC>(ACTIVE_COUNT))
            clear(ACTIVE_COUNT)
        }

        questJournalOpen(QUEST) {
            val lines = when (quest(QUEST)) {
                "completed" -> listOf("<str>I defeated Count Draynor and saved Draynor Village.", "", "<red>QUEST COMPLETE!")
                "armed" -> buildList {
                    add("<navy>I must defeat <maroon>Count Draynor<navy> in the cellar of <maroon>Draynor Manor<navy>.")
                    add("<navy>I need a <maroon>stake<navy> and <maroon>hammer<navy> to finish him off.")
                    if (!hasVampireTools()) add("<navy>Dr Harlow can replace my missing stake or stake hammer.")
                    add("<navy>Garlic from Morgan's upstairs drawers will weaken him.")
                }
                "started" -> listOf("<navy>Morgan asked me to defeat the vampyre in <maroon>Draynor Manor<navy>.", "<navy>I should speak to <maroon>Dr Harlow<navy> in the <maroon>Blue Moon Inn<navy> in Varrock.", "<navy>He may be more helpful after a <maroon>beer<navy>.")
                else -> listOf("<navy>I can start this quest by speaking to <maroon>Morgan<navy> in", "<navy>his house in <maroon>Draynor Village<navy>.", "<navy>I will need to defeat a level 28 enemy.")
            }
            questJournal("Vampire Slayer", lines)
        }
    }

    private suspend fun Player.accept() {
        npc<Neutral>("My friend Dr Harlow knows how to fight vampyres. Find him at the Blue Moon Inn in Varrock and tell him Morgan sent you.")
        set(QUEST, "started")
    }
}

/** Returns false when Harlow should use his ordinary drinking conversation. */
suspend fun Player.harlowVampireSlayer(): Boolean {
    when (quest(QUEST)) {
        "started" -> {
            player<Neutral>("Morgan sent me. Count Draynor is terrorising his village.")
            npc<Neutral>("A vampyre, eh? Bring me a beer and I'll tell you how to deal with him.")
            if (!inventory.contains("beer")) return true
            // Require room for every missing tool before taking the player's payment.
            val tools = missingVampireTools()
            if (inventory.spaces + 1 < tools.size) {
                npc<Neutral>("Make some room in your inventory for a stake and hammer first.")
                return true
            }
            inventory.remove("beer")
            tools.forEach { inventory.add(it) }
            set(QUEST, "armed")
            statement("You give Dr Harlow a beer. He gives you the tools you need.")
            npc<Neutral>("When he's weakened, drive a wooden stake through his heart with a hammer. Keep the stake and hammer with you.")
            npc<Neutral>("Garlic will weaken him too. Look in the drawers upstairs in Morgan's house.")
        }
        "armed" -> {
            val tools = missingVampireTools()
            if (inventory.spaces < tools.size) {
                npc<Neutral>("Make room in your inventory and I'll replace your missing tools.")
            } else {
                tools.forEach { inventory.add(it) }
                if (tools.isNotEmpty()) statement("Dr Harlow replaces your missing tools.")
                npc<Neutral>("Take your stake and hammer to the cellar of Draynor Manor. Garlic will help weaken the Count.")
            }
        }
        else -> return false
    }
    return true
}

private fun Player.missingVampireTools(): List<String> = listOf("stake", "stake_hammer").filterNot { carriesItem(it) || bank.contains(it) }

internal fun Player.hasVampireTools(): Boolean = carriesItem("stake") && (carriesItem("stake_hammer") || carriesItem("hammer"))

private fun Player.consumeStake(): Boolean {
    if (inventory.remove("stake")) return true
    return equipment.remove("stake")
}
