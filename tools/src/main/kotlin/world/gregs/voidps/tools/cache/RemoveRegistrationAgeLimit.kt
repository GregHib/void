package world.gregs.voidps.tools.cache

import com.displee.cache.CacheLibrary
import world.gregs.voidps.buffer.read.ArrayReader
import world.gregs.voidps.buffer.write.ArrayWriter
import world.gregs.voidps.cache.Index
import world.gregs.voidps.cache.definition.data.ClientScriptDefinition
import world.gregs.voidps.cache.definition.decoder.ClientScriptDecoder
import world.gregs.voidps.cache.definition.encoder.ClientScriptEncoder
import world.gregs.voidps.engine.data.Settings

/**
 * Lets players under 13 register from the login screen.
 *
 * The "Continue" button on the create account form (client script [SCRIPT_ID]) sends anyone who enters an age
 * under 13 to a herotopia.ws redirect (script 4038) and flags the client as underage (op 5626) instead of
 * sending the create account packet (op 5605). Nothing restores the button afterwards, so it sits on
 * "Please wait..." until the client is restarted. Removing the branch sends every age to the server.
 */
object RemoveRegistrationAgeLimit {
    const val SCRIPT_ID = 2967

    /**
     * Instruction and operand pairs of the age check
     */
    val AGE_CHECK = listOf(
        42 to 1407, // push age (varc 1407)
        0 to 13,
        9 to 1, // if age < 13 skip the jump
        6 to 3, // jump to the underage flag check
        5626 to 0, // flag the client as underage
        40 to 4038, // underage redirect
        6 to 15, // return
        5625 to 0, // push underage flag
        0 to 1,
        8 to 1, // if flagged skip the jump
        6 to 2, // jump to sending the packet
        40 to 4038, // underage redirect
        6 to 9, // return
    )

    private val BRANCHES = setOf(6, 7, 8, 9, 10, 31, 32)
    private const val SWITCH = 51

    fun convert(cache: CacheLibrary) {
        val script = readScript(cache, SCRIPT_ID)
        if (!remove(script)) {
            println("Registration age limit already removed from client script $SCRIPT_ID.")
            return
        }
        writeScript(cache, script)
        cache.update()
        println("Removed registration age limit from client script $SCRIPT_ID.")
    }

    /**
     * Removes the [AGE_CHECK] from [script]
     * @return false if the script doesn't contain the check
     */
    fun remove(script: ClientScriptDefinition): Boolean {
        val start = find(script) ?: return false
        removeInstructions(script, start, start + AGE_CHECK.size)
        return true
    }

    private fun find(script: ClientScriptDefinition): Int? {
        val instructions = script.instructions
        val operands = script.intOperands ?: return null
        for (start in 0..instructions.size - AGE_CHECK.size) {
            val match = AGE_CHECK.withIndex().all { (offset, pair) ->
                instructions[start + offset] == pair.first && operands[start + offset] == pair.second
            }
            if (match) {
                return start
            }
        }
        return null
    }

    /**
     * Removes instructions [start] until [end] re-targeting every branch and switch case which jumps over them
     */
    fun removeInstructions(script: ClientScriptDefinition, start: Int, end: Int) {
        val removed = end - start
        val instructions = script.instructions
        val operands = script.intOperands!!
        fun shift(index: Int) = if (index >= end) index - removed else index
        fun offset(from: Int, offset: Int): Int {
            val target = from + offset + 1
            // Jumping to the first removed instruction continues after the removed range
            check(target <= start || target >= end) { "Instruction $from jumps into removed range $start..$end." }
            return shift(target) - shift(from) - 1
        }
        for (index in instructions.indices) {
            if (index in start until end) {
                continue
            }
            when (instructions[index]) {
                in BRANCHES -> operands[index] = offset(index, operands[index])
                SWITCH -> {
                    val table = script.switchStatementIndices!!
                    table[operands[index]] = table[operands[index]].map { (value, jump) -> value to offset(index, jump) }
                }
            }
        }
        val keep = instructions.indices.filter { it < start || it >= end }
        script.instructions = IntArray(keep.size) { instructions[keep[it]] }
        script.intOperands = IntArray(keep.size) { operands[keep[it]] }
        val strings = script.stringOperands
        if (strings != null) {
            script.stringOperands = Array(keep.size) { strings[keep[it]] }
        }
    }

    private fun readScript(cache: CacheLibrary, scriptId: Int): ClientScriptDefinition {
        val decoder = ClientScriptDecoder()
        val data = cache.data(Index.CLIENT_SCRIPTS, scriptId)!!
        val script = ClientScriptDefinition()
        script.id = scriptId
        decoder.readLoop(script, ArrayReader(data))
        return script
    }

    private fun writeScript(cache: CacheLibrary, script: ClientScriptDefinition) {
        val encoder = ClientScriptEncoder()
        val writer = ArrayWriter(4096)
        with(encoder) {
            writer.encode(script)
        }
        cache.put(Index.CLIENT_SCRIPTS, script.id, writer.toArray())
    }

    @JvmStatic
    fun main(args: Array<String>) {
        Settings.load()
        val cache = CacheLibrary(args.firstOrNull() ?: Settings["storage.cache.path"])
        convert(cache)
    }
}
