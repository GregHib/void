package org.redrune.network.codec.file

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 23, 2020 3:11 a.m.
 */
data class FileContents(
    val index: Int,
    val archive: Int,
    val settings: Int,
    val length: Int,
    val data: ByteArray,
    val size: Int,
    val encryption: Int
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FileContents

        if (index != other.index) return false
        if (archive != other.archive) return false
        if (settings != other.settings) return false
        if (length != other.length) return false
        if (!data.contentEquals(other.data)) return false
        if (size != other.size) return false
        if (encryption != other.encryption) return false

        return true
    }

    override fun hashCode(): Int {
        var result = index
        result = 31 * result + archive
        result = 31 * result + settings
        result = 31 * result + length
        result = 31 * result + data.contentHashCode()
        result = 31 * result + size
        result = 31 * result + encryption
        return result
    }
}