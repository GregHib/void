package content.area.misthalin.varrock.museum

import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Laugh
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import java.io.File

class Archaeologist : Script {
    init {
        npcOperate("Talk-to", "marius_giste,barnabus_hurma,caden_azro,thias_leacke,sinco_doar,tinse_torpe") {
//            npc<Happy>("Hello! You're that student who recently finished all three Earth Sciences exams, aren't you? Come to help us out?")
            npc<Happy>("Greetings! Have you come to give us a hand?")
            choice {
                option<Happy>("Yes, how can I help out?") {
                    npc<Sad>("Well, you'll need to get your equipment first - it's all there on the tool rack. Use what you learned from your Earth Sciences exams. You'll need to be wearing your leather gloves and boots as well as have access to")
                    npc<Sad>("your trowel, rock pick and specimen brush.")
                }
                option<Happy>("I found something interesting.") {
                    npc<Happy>("Oh? Let's take a look...")
                    player<Confused>("Err... I seem to have lost it. Sorry.")
                    npc<Laugh>("Sounds like you never had it!")
                }
                option("No thanks.")
            }
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val string = "jagdx\\IDirect3D.kt"
            val file = File("C:\\Users\\Greg\\IdeaProjects\\void-client\\src\\commonMain\\kotlin\\${string}")
            if (file.isFile) {
                file.writeText(
                    stripMethodBodies(file)
                        .replace("private ", "")
                        .replace("protected ", "")
                        .replace("class ", "expect class ")
                        .replace("object ", "expect object ")
                        .replace("@Synchronized", "/*@Synchronized*/")
                        .replace("external ", "")
                )
            }
            val actual = File("C:\\Users\\Greg\\IdeaProjects\\void-client\\src\\jvmMain\\kotlin\\${string}")
            if (actual.isFile) {
                actual.writeText(
                    actual.readText()
                        .replace("    private external fun", "    /*private*/ actual external fun")
                        .replace("    external override fun", "    actual external override fun")
                        .replace("    external fun", "    actual external fun")
                        .replace("    override fun", "    actual override fun")
                        .replace("    protected", "    /*protected*/ actual")
                        .replace("    private fun", "    /*private*/ actual fun")
                        .replace("    fun", "    actual fun")
//                            .replace("    var", "    actual var")
                        .replace("actual actual ", "actual ")
                        .replace("class ", "actual class ")
                        .replace("object ", "actual object ")
                )
            }
        }

        fun stripMethodBodies(filePath: File): String {
            val sb = StringBuilder(filePath.readText())
            var i = 0
            while (i < sb.length) {
                // naive detection: a "fun " keyword not preceded by an identifier char
                if (sb.startsWith("fun ", i) && (i == 0 || !sb[i - 1].isLetterOrDigit())) {
                    var j = i
                    var parenDepth = 0
                    var braceIdx = -1

                    // scan forward through the signature to find the body's opening '{'
                    while (j < sb.length) {
                        when (sb[j]) {
                            '(' -> parenDepth++
                            ')' -> parenDepth--
                            '{' -> if (parenDepth == 0) {
                                braceIdx = j; break
                            }
                            ';' -> if (parenDepth == 0) break // no body (declaration only)
                        }
                        j++
                    }

                    if (braceIdx != -1) {
                        // find the matching closing brace by depth counting
                        var depth = 1
                        var k = braceIdx + 1
                        while (k < sb.length && depth > 0) {
                            when (sb[k]) {
                                '{' -> depth++
                                '}' -> depth--
                            }
                            k++
                        }
                        val closeIdx = k - 1
                        // delete from the opening '{' through the closing '}' inclusive
                        sb.delete(braceIdx, closeIdx + 1)
                        i = braceIdx // continue scanning right where the braces used to be
                        continue
                    }
                }
                i++
            }
            return sb.toString()
        }
    }
}
