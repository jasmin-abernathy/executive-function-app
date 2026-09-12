package org.lepotager.executivefunction.domain

/** Transparent user-controlled filters; unknown duration never means impossible. */
object TaskEligibility {
    fun accepts(requiredEnergy: Int, availableEnergy: Int?, suggestedMs: Long?, availableMinutes: Int?, context: String, selectedContext: String): Boolean {
        if(availableEnergy != null && requiredEnergy > availableEnergy) return false
        if(availableMinutes != null && suggestedMs != null && suggestedMs > availableMinutes.toLong()*60_000) return false
        if(selectedContext.isNotBlank() && context.isNotBlank() && !context.trim().equals(selectedContext.trim(),ignoreCase=true)) return false
        return true
    }
}
