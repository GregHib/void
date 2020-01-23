package org.redrune.network.codec.file

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since January 23, 2020 2:16 a.m.
 */
data class FileRequest(val indexId: Int, val archiveId: Int, val priority: Boolean)