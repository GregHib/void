package world.gregs.voidps.tools.wiki.scrape

import net.pearx.kasechange.toSnakeCase
import world.gregs.voidps.tools.wiki.model.Wiki

object MonsterInfoBoxDumper {

    @JvmStatic
    fun main(args: Array<String>) {
//        val buffer = RunescapeWiki.export("king_black_dragon", "oldschool.runescape.wiki")
//        val text = buffer.readAllBytes().toString(Charset.defaultCharset())
        val wiki = Wiki.load("C:\\Users\\Greg\\Downloads\\test.xml")
        for (page in wiki.pages) {
            val infobox = page.getTemplateMap("Infobox Monster")!!
            val version = "1"
            println("${(infobox["name"] as String).toSnakeCase()}:")
            val id = infobox["id"] as? String ?: infobox["id$version"] as? String
            if (id != null) {
                println("  id: $id")
            }
            val hp = infobox["hitpoints"] as? String
            if (hp != null) {
                println("  hitpoints: ${hp.toInt() * 10}")
            }
            val att = infobox["att"] as? String
            if (att != null && att != "1") {
                println("  att: $att")
            }
            val str = infobox["str"] as? String
            if (str != null && str != "1") {
                println("  str: $str")
            }
            val def = infobox["def"] as? String
            if (def != null && def != "1") {
                println("  def: $def")
            }
            val mage = infobox["mage"] as? String
            if (mage != null && mage != "1") {
                println("  mage: $mage")
            }
            val range = infobox["range"] as? String
            if (range != null && range != "1") {
                println("  range: $range")
            }
            val style = infobox["attack style"] as? String
            if (style != null && style != "0") {
                println("  style: $style")
            }
            val attbns = infobox["attbns"] as? String
            if (attbns != null && attbns != "0") {
                println("  attack_bonus: $attbns")
            }
            val mbns = infobox["mbns"] as? String
            if (mbns != null && mbns != "0") {
                println("Unexpected magic bonus")
            }
            val rngbns = infobox["rngbns"] as? String
            if (rngbns != null && rngbns != "0") {
                println("Unexpected ranged bonus")
            }
            val maxHit = infobox["max hit"] as? String
            if (maxHit != null) {
                println("  max_hit_melee: $maxHit")
            }
            val aggressive = infobox["aggressive"] as? String
            if (aggressive != null && aggressive.equals("yes", true)) {
                println("  hunt_mode: aggressive")
            }
            val slayer = infobox["slayxp"] as? String
            if (slayer != null) {
                println("  slayer_xp: ${slayer.toDouble()}")
            }
            val category = infobox["cat"] as? String
            if (category != null) {
                println("  race: $category")
            }
            val poison = infobox["immunepoison"] as? String
            if (poison != null && poison.equals("yes", true)) {
                println(" immune_poison: true")
            }
            val cannon = infobox["immunecannon"] as? String
            if (cannon != null && cannon.equals("yes", true)) {
                println(" immune_cannon: true")
            }
            val members = (infobox["members"] as? String)?.toBoolean() ?: (infobox["members$version"] as? String)?.toBoolean() ?: false
            if (members) {
                println("  members: true")
            }
            val aka = infobox["aka"] as? String
            if (aka != null) {
                println("  aka: [ $aka ]")
            }
            val respawn = infobox["respawn"] as? String ?: infobox["respawn$version"] as? String
            if (respawn != null) {
                println("  respawn_delay: $respawn")
            }
            val examine = infobox["examine"] as? String
            if (examine != null) {
                println("  examine: \"$examine\"")
            }
        }
    }
}
