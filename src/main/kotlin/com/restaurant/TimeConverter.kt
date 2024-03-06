package com.restaurant

class TimeConverter(private val multiplier: Long) {
    fun minToMs(mins: Int): Long {
        return mins * 60 * 1000L / multiplier
    }

    companion object {
        private var inst: TimeConverter? = null
        fun get() = inst!!
        fun init(multiplier: Long) {
            inst = TimeConverter(multiplier)
        }
    }
}