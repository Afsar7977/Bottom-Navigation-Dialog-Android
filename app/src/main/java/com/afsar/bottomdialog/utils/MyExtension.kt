package com.afsar.bottomdialog.utils

import android.app.Activity
import android.content.Context
import android.os.Build
import android.text.*
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.afsar.bottomdialog.R
import com.afsar.bottomdialog.utils.CommonUtils.A1C_DIGIT_PATTERN
import com.afsar.bottomdialog.utils.CommonUtils.DECIMAL_CHECK_PATTERN
import com.afsar.bottomdialog.utils.CommonUtils.EMAIL_ADDRESS_PATTERN
import com.afsar.bottomdialog.utils.CommonUtils.PASSWORD_PATTERN
import com.afsar.bottomdialog.utils.CommonUtils.TWO_DECIMAL_DIGIT_PATTERN
import com.google.android.material.snackbar.Snackbar
import java.text.DecimalFormat
import java.util.regex.Pattern

fun View.showSnakbar(mMessage: String) {
    Snackbar.make(this, mMessage, Snackbar.LENGTH_LONG)
        .setAction("Action", null)
        .show()
}

fun Context.showToast(msg: String) {
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

fun String.isEmailValid(): Boolean {
    return /*!TextUtils.isEmpty(this) &&*/ EMAIL_ADDRESS_PATTERN.matcher(this).matches()
}

fun String.isPassWordValid(): Boolean {
    return this.length >= 8
}

fun String.isPassWordValid(view: View): Boolean {
    if (this.isBlank()) {
        view.showSnakbar(view.context.getString(R.string.password_blank_validation_msg))
        return false
    }
    if (!PASSWORD_PATTERN.matcher(this).matches()) {
        view.showSnakbar(view.context.getString(R.string.password_validation_msg))
        return false
    }
    return true
}

fun String.isStringContainsDecimal() : Boolean {
    if (!DECIMAL_CHECK_PATTERN.matcher(this).matches()) {
        return false
    }
    return true
}

fun String.isHealthLevelValid(): Boolean {
    if (!TWO_DECIMAL_DIGIT_PATTERN.matcher(this).matches()) {
        return false
    }
    return true
}

fun String.getNonDecimalString(): String {
    val loDecimalFormat = DecimalFormat("#")
    return loDecimalFormat.format(this.toDouble())
}

fun Activity.getSoftButtonsBarSizePort(): Int {
    // getRealMetrics is only available with API 17 and +
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
        val metrics = DisplayMetrics()
        this.windowManager.defaultDisplay.getMetrics(metrics)
        val usableHeight = metrics.heightPixels
        this.windowManager.defaultDisplay.getRealMetrics(metrics)
        val realHeight = metrics.heightPixels
        return if (realHeight > usableHeight) realHeight - usableHeight else 0
    }
    return 0
}

fun Activity.addSoftButtonsBarSizeMargin(view: View) {
    val param = view.layoutParams as ViewGroup.MarginLayoutParams
    param.bottomMargin = param.bottomMargin + this.getSoftButtonsBarSizePort()
    view.requestLayout()
}


fun Activity.showKeyboard() {
    val inputMethodManager =
        this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
}

fun Activity.hideKeyboard() {
    val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    var view = currentFocus
    if (view == null) {
        view = View(this)
    }
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

fun View.hideKeyboard() {
    val imm = this.context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(this.windowToken, 0)
}