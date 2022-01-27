package world.gregs.voidps.network.instruct

import world.gregs.voidps.network.Instruction

data class QuickChatPrivate(
    val name: String,
    val file: Int,
    val data: ByteArray
) : Instruction {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as QuickChatPrivate

        if (name != other.name) return false
        if (file != other.file) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + file
        result = 31 * result + data.contentHashCode()
        return result
    }
}