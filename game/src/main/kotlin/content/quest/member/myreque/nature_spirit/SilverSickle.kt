package content.quest.member.myreque.nature_spirit

import content.entity.gfx.areaGfx
import content.quest.quest
import content.quest.questStage
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.remove
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.random
import java.util.concurrent.TimeUnit

class SilverSickle : Script {
    init {
        itemOption("Cast", "druidic_spell") {
            castBloomFromScroll()
        }

        itemOption("Bloom", "silver_sickle_b") {
            castBloomFromSickle()
        }

        itemOption("Bloom", "silver_sickle_b", "worn_equipment") {
            castBloomFromSickle()
        }

        objectOperate("Pick", "log_druidicspirit2") { (target) ->
            pickBloom(target, "mort_myre_fungus")
        }

        objectOperate("Pick", "branch_druidicspirit2") { (target) ->
            pickBloom(target, "mort_myre_stem")
        }

        objectOperate("Pick", "peartree_druidicspirit2") { (target) ->
            pickBloom(target, "mort_myre_pear")
        }
    }

    private fun Player.pickBloom(target: GameObject, yieldItem: String) {
        inventory.add(yieldItem)
        anim("human_pickupfloor")
        sound("pick")
        when (yieldItem) {
            "mort_myre_stem" -> message("You take a cutting from the budding branch.")
            "mort_myre_fungus" -> message("You pick a mushroom from the log.")
            "mort_myre_pear" -> message("You pick a pear from the tree.")
        }
        target.remove()
        when (quest("nature_spirit")) {
            "spell_cast" -> set("nature_spirit", "has_mushroom")
            "bloomed" -> set("nature_spirit", "picked")
        }
    }

    private suspend fun Player.castBloomFromScroll() {
        if (questStage("nature_spirit") < 40) {
            message("You need to be blessed before you can cast this spell.")
            return
        }
        if (tile !in Areas["mort_myre_swamp_bloom_area"]) {
            message("This spell has no effect outside of Mort Myre swamp.")
            return
        }

        message("You cast the spell in the swamp.")
        delay(2)
        if (quest("nature_spirit") == "blessed_spell") {
            set("nature_spirit", "spell_cast")
        }
        anim("human_casting", delay = 30)
        sprinkleBloomGfx(height = 46, delay = 30)
        inventory.remove("druidic_spell")
        inventory.add("a_used_spell")
        sound("cast_bloom", delay = 30)
        sound("bloom_mushroom", delay = 30)
        sound("bloom_mushroom", delay = 30)
        bloomNearbyPlants()
    }

    private suspend fun Player.castBloomFromSickle() {
        if (!has(Skill.Prayer, 0)) {
            message("You need some prayer points to activate the power of the sickle.")
            return
        }
        if (tile !in Areas["mort_myre_swamp_bloom_area"]) {
            message("This spell has no effect outside of Mort Myre swamp.")
            return
        }
        gfx("druidicspirit_bloom_player_spotanim", delay = 30, height = 200)
        anim("druidicspirit_human_bloom", delay = 10)
        sound("cast_bloom", delay = 35)
        delay(1)
        if (quest("nature_spirit") == "natures_bounty") {
            set("nature_spirit", "bloomed")
        }
        levels.drain(Skill.Prayer, ((1..6).random()).coerceAtMost(levels.get(Skill.Prayer)))
        sprinkleBloomGfx(height = 150, delay = 15)
        bloomNearbyPlants()
    }

    private fun Player.sprinkleBloomGfx(height: Int, delay: Int) {
        for (dir in Direction.all) {
            areaGfx(
                id = "druidicspirit_bloom_spotanim",
                tile = tile.add(dir),
                height = height,
                delay = delay,
            )
        }
    }

    data class BloomEntry(val unbloomedName: String, val bloomedName: String, val sound: String)

    val bloomTypes: List<BloomEntry> = listOf(
        BloomEntry("log_druidicspirit", "log_druidicspirit2", "bloom_mushroom"),
        BloomEntry("branch_druidicspirit", "branch_druidicspirit2", "bloom_branch"),
        BloomEntry("peartree_druidicspirit", "peartree_druidicspirit2", "bloom_pears"),
    )

    private fun Player.bloomNearbyPlants() {
        for (dir in Direction.all) {
            val at = tile.add(dir)
            for (data in bloomTypes) {
                val obj = GameObjects.findOrNull(at, data.unbloomedName) ?: continue
                if (random.nextInt(4) != 0) {
                    continue
                }
                sound(data.sound, delay = 40)
                obj.replace(data.bloomedName, ticks = TimeUnit.SECONDS.toTicks(15))
            }
        }
    }
}
