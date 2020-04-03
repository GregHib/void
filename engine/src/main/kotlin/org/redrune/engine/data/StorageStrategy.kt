package org.redrune.engine.data

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 03, 2020
 */
interface StorageStrategy<T : Any> {
    fun load(name: String): T?
    fun save(name: String, data: T)
}