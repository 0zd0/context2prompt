package com.ozd0.context2prompt.core

object TokenEstimator {
    /** ~4 characters/token */
    fun estimate(text: String): Int = (text.length + 3) / 4
}
