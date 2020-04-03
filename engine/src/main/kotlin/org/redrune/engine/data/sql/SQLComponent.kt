package org.redrune.engine.data.sql

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 03, 2020
 */
interface SQLComponent<T : Any> {
    fun create(): T
    fun load(): T
    fun save(data: T)
}