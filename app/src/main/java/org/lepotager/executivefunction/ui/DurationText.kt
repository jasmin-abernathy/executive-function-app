package org.lepotager.executivefunction.ui

/** Compact duration labels for pickers: 75 -> "1 h 15 min". */
internal object DurationText {
    fun minutes(value: Int): String {
        require(value >= 0)
        if (value < 60) return "$value min"
        val hours = value / 60
        val minutes = value % 60
        return if (minutes == 0) "$hours h" else "$hours h $minutes min"
    }
}
