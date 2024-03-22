package world.gregs.voidps.network.client.instruction

import world.gregs.voidps.network.Instruction

data class QuickChatPublic(
    val script: Int,
    val file: Int,
    val data: ByteArray
) : Instruction {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as world.gregs.voidps.network.client.instruction.QuickChatPublic

        if (script != other.script) return false
        if (file != other.file) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = script
        result = 31 * result + file
        result = 31 * result + data.contentHashCode()
        return result
    }
}