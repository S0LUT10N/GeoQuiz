package com.bignerdranch.android.geomain

import androidx.annotation.StringRes

private const val TAG = "Question"

data class Question(@StringRes val textResId: Int, val answer: Boolean) {
    var isCheated: Boolean = false
}