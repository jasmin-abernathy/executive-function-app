package org.lepotager.executivefunction.domain

object DieMotion {
    fun durationMillis(calm: Boolean, systemAnimations: Boolean): Int =
        if (calm || !systemAnimations) 0 else 500
}
