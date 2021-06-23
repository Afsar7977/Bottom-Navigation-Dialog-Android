/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019 https://www.spaceotechnologies.com
 *
 * Permissions is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without
 * limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
 * Software, and to permit persons to whom the Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions
 * of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT
 * OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package com.afsar.bottomdialog.base

import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.PersistableBundle
import android.view.LayoutInflater
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.afsar.bottomdialog.R

abstract class BaseActivity : AppCompatActivity() {

    var moSharedPreferenceData: SharedPreferences? = null
    private var mProgressDialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        moSharedPreferenceData = getSharedPreferences(
            SHAREDPREFERENCE,
            Context.MODE_PRIVATE
        )
    }

    fun showProgressDialog(
        mContext: Context,
        title: String = "",
        message: String = ""
    ) {
        mProgressDialog = Dialog(mContext)
        val inflate = LayoutInflater.from(mContext).inflate(R.layout.progress, null)
        mProgressDialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        mProgressDialog!!.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        mProgressDialog!!.setContentView(inflate)
        mProgressDialog!!.setCancelable(false)
        mProgressDialog!!.show()
    }

    fun hideProgressDialog() {
        mProgressDialog?.let {
            if (it.isShowing) {
                it.dismiss()
                mProgressDialog = null
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        hideProgressDialog()
    }

    companion object {
        val SHAREDPREFERENCE = "sharedPreferences"
    }


}
