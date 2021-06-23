package com.afsar.bottomdialog.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.*
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.database.Cursor
import android.graphics.*
import android.location.Address
import android.location.Geocoder
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.*
import android.telephony.TelephonyManager
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannedString
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.DisplayMetrics
import android.util.Log
import android.util.Log.d
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.RequiresPermission
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.pm.PackageInfoCompat
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import com.afsar.bottomdialog.MainActivity
import com.afsar.bottomdialog.R
import com.google.android.material.snackbar.Snackbar
import de.hdodenhof.circleimageview.CircleImageView
import org.jetbrains.anko.doAsync
import java.io.*
import java.net.NetworkInterface
import java.net.URL
import java.net.URLConnection
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

object CommonUtils {


    const val COMMONDATEFORMATE = "dd/MM/yyyy HH:mm:ss"
    const val COMMONCALDATEFORMAT = "dd/MM/yyyy"
    const val DATEFORMAT = "dd/MM/yyyy"
    const val SIMPLEDATEFORMAT = "MM-dd-yyyy"
    const val YEARMONTHFORMAT = "MMM dd, yyyy"
    const val MONTHDATEFORMAT = "MMM dd"
    const val OPEN_MAIL_REQUEST_CODE = 102
    const val SHAREDPREFERENCE = "sharedPreferences"

    val VISIBILITY_FLAGS = intArrayOf(View.VISIBLE, View.INVISIBLE, View.GONE)
    var HELPER_ID = ""
    val EMAIL_ADDRESS_PATTERN: Pattern = Pattern.compile(
        "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                "\\@" +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                "(" +
                "\\." +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                ")+"
    )

    val PASSWORD_PATTERN: Pattern =
        Pattern.compile("((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%]).{8,15})")

    val TWO_DECIMAL_DIGIT_PATTERN: Pattern =
        Pattern.compile("^\\d{0,3}(\\.\\d{1,2})?\$")

    val A1C_DIGIT_PATTERN: Pattern =
        Pattern.compile("^(?:100(?:\\d0(?:0)?)?|\\d{1,2}(?:\\d{1,2})?)$")

    val DECIMAL_CHECK_PATTERN: Pattern =
        Pattern.compile("^\\d+\\.\\d+")

    object TabPositions {
        val HOME = 0
        val HELPER = 1
        val RULES = 2
        val MORE = 3
        val HEALTH = 4
        val ACCOUNT = 5
        val PATIENTS = 6
        val SUMMARY = 7
        val PROFILE = 8
        val PATIENTS_UPGRADE = 9
    }

    /**
     * old method but working perfactly (https://stackoverflow.com/questions/3407256/height-of-status-bar-in-android)
     */
    fun Activity.getStatusBarHeight(): Int {
        var result = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        return result
    }


    fun Context.checkInternetConnected(): Boolean {
        var isConnected = false
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            cm?.run {
                cm.getNetworkCapabilities(cm.activeNetwork)?.run {
                    isConnected = when {
                        hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                        hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                        hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                        else -> false
                    }
                }
            }
        } else {
            cm?.run {
                @Suppress("DEPRECATION")
                cm.activeNetworkInfo?.run {
                    if (type == ConnectivityManager.TYPE_WIFI) {
                        isConnected = true
                    } else if (type == ConnectivityManager.TYPE_MOBILE) {
                        isConnected = true
                    }
                }
            }
        }
        return isConnected
    }


    fun Context.isInternetConnected(isShowDialog: Boolean = false): Boolean {
        var isConnected = false
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            cm?.run {
                cm.getNetworkCapabilities(cm.activeNetwork)?.run {
                    isConnected = when {
                        hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                        hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                        hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                        else -> false
                    }
                }
            }
        } else {
            cm?.run {
                @Suppress("DEPRECATION")
                cm.activeNetworkInfo?.run {
                    if (type == ConnectivityManager.TYPE_WIFI) {
                        isConnected = true
                    } else if (type == ConnectivityManager.TYPE_MOBILE) {
                        isConnected = true
                    }
                }
            }
        }
        if (!isConnected && isShowDialog) {
            showOkDialog(
                this, getString(R.string.app_name),
                getString(R.string.default_internet_message)
            )

        }
        return isConnected
    }

    fun showOkDialog(
        context: Context,
        title: String = context.getString(R.string.app_name),
        message: String,
        isFinish: Boolean = false
    ) {
        val alertDialog = AlertDialog.Builder(context)
        alertDialog.setTitle(title)
        alertDialog.setMessage(message)
        alertDialog.setPositiveButton("Ok") { dialog, _ ->
            if (isFinish) {
                val activity = context as Activity
                activity.finish()
            } else {
                dialog.dismiss()
            }

        }
        alertDialog.show()
    }

    fun getAppVersion(context: Context): Int {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                return context.packageManager.getPackageInfo(context.packageName, 0)
                    .longVersionCode.toInt()
            else
                return context.packageManager.getPackageInfo(context.packageName, 0).versionCode
        } catch (e: PackageManager.NameNotFoundException) {
            // should never happen
            throw RuntimeException("Could not get package name: $e")
        }
    }

    fun getDeviceId(mContext: Context): String {
        return Settings.Secure.getString(mContext.contentResolver, Settings.Secure.ANDROID_ID)
    }

    val deviceModel: String
        get() {
            val manufacturer = Build.MANUFACTURER
            val model = Build.MODEL
            return if (model.startsWith(manufacturer)) {
                capitalize(model)
            } else {
                capitalize(manufacturer) + " " + model
            }
        }

    private fun capitalize(deviceModel: String): String {
        return deviceModel.substring(0, 1).toUpperCase(Locale.getDefault()) + deviceModel.substring(
            1
        )
    }

    val deviceOSVersion = Build.VERSION.SDK_INT.toString()

    //--- get IMEI number of Device
    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    fun getIMEINumber(mContext: Context): String? {
        val telephonyManager =
            mContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return if (ActivityCompat.checkSelfPermission(
                mContext,
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            null
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                telephonyManager.imei
            } else {
                telephonyManager.deviceId
            }
        }
    }

    fun getCountryCode(foContext: Context) {
        val tm: TelephonyManager = foContext
            .getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val country = tm.networkCountryIso
        d("country", country)
    }

    //--- get ip address of mobile
    val ipAddress: String?
        @Throws(Exception::class)
        get() {
            val en = NetworkInterface.getNetworkInterfaces()
            while (en.hasMoreElements()) {
                val intf = en.nextElement()
                val enumIpAddr = intf.inetAddresses
                while (enumIpAddr.hasMoreElements()) {
                    val inetAddress = enumIpAddr.nextElement()
                    if (!inetAddress.isLoopbackAddress) {
                        return inetAddress.hostAddress
                    }
                }
            }
            return null
        }


    fun showSettingsSnackBar(
        rootLayout: CoordinatorLayout,
        settingsText: String = "Settings",
        message: String
    ) {
        val snackbar = Snackbar
            .make(rootLayout, message, Snackbar.LENGTH_LONG)
            .setAction("Settings") {
                val intent = Intent()
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                val uri = Uri.fromParts("package", rootLayout.context.packageName, null)
                intent.data = uri
                rootLayout.context.startActivity(intent)
            }
        snackbar.show()
    }


    private fun showSettingsDialog(
        mContext: Context,
        settingsText: String? = "Settings",
        cancelText: String? = "Cancel",
        title: String,
        message: String
    ) {
        val alertSettings = AlertDialog.Builder(mContext)
        alertSettings.setTitle(title)
        alertSettings.setMessage(message)
        alertSettings.setPositiveButton("Settings") { _, _ ->
            val intent = Intent()
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            val uri = Uri.fromParts("package", mContext.packageName, null)
            intent.data = uri
            mContext.startActivity(intent)
        }
        alertSettings.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        alertSettings.show()
    }

    fun dpToPx(context: Context, dp: Int) = (dp * context.getPixelScaleFactor()).roundToInt()

    fun pxToDp(context: Context, px: Int) = (px / context.getPixelScaleFactor()).roundToInt()

    private fun Context.getPixelScaleFactor(): Float {
        val displayMetrics = resources.displayMetrics
        return displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT
    }

    fun hideSoftKeyboard(activity: Activity) {
        val inputMethodManager =
            activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                inputMethodManager.hideSoftInputFromWindow(
                    Objects.requireNonNull(
                        activity.currentFocus!!.windowToken
                    ), 0
                )
            } else {
                if (activity.currentFocus != null) inputMethodManager.hideSoftInputFromWindow(
                    activity.currentFocus!!.windowToken,
                    0
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun Activity.showSoftKeyboard(mView: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInputFromWindow(
            mView.applicationWindowToken,
            InputMethodManager.SHOW_FORCED,
            0
        )
    }

    fun Activity.showToast(message: String) =
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    fun Activity.showLongToast(message: String) =
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()


    fun showSnakbar(rootView: View, mMessage: String) =
        Snackbar.make(rootView, mMessage, Snackbar.LENGTH_SHORT).show()

    fun showLongSnakbar(rootView: View, mMessage: String) =
        Snackbar.make(rootView, mMessage, Snackbar.LENGTH_LONG).show()

    fun isValidEmailString(fsEmail: String): Boolean {
        val pattern = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}$")
        val matcher = pattern.matcher(fsEmail)
        return matcher.matches()
    }

    @VisibleForTesting
    fun String.isValidPhoneNumber(): Boolean {
        return Pattern.compile("^\\+(?:[0-9] ?){6,14}[0-9]\$").matcher(this)
            .matches()
    }

    fun getMonthsAgo(monthAgo: Int): String {

        val dateFormat = SimpleDateFormat("MM/dd/yyyy")
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -monthAgo)
        return dateFormat.format(calendar.time)
    }

    fun getMonths(monthAgo: Int): String {
        val dateFormat = SimpleDateFormat("MMM")
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -monthAgo)
        return dateFormat.format(calendar.time)
    }

    fun getYear(fiYear: Int): String {
        val dateFormat = SimpleDateFormat("yyyy")
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.YEAR, fiYear)
        return dateFormat.format(calendar.time)
    }

    fun getMonthName(fiMonth: Int): String {
        val loCal = Calendar.getInstance()
        val loMonth = SimpleDateFormat("MMMM")
        loCal[Calendar.MONTH] = fiMonth
        val loMonthName = loMonth.format(loCal.time)
        return loMonthName
    }

    fun getMonthNumber(fsMonthName: String): String {
        val lsDdate = SimpleDateFormat("MMMM").parse(fsMonthName)
        val lsCal = Calendar.getInstance()
        lsCal.time = lsDdate
        var liMonthNumber = lsCal.get(Calendar.MONTH) + 1

        Log.i("GetMonthNumber", "monthNumber: " + liMonthNumber)
        return liMonthNumber.toString()

    }

    @Throws(IOException::class)
    fun loadFile(foInputStream: InputStream, flLength: Long): ByteArray? {
        if (flLength > Int.MAX_VALUE) {
            // File is too large
        }
        val loBytes = ByteArray(flLength.toInt())
        var liOffset = 0
        var liNumRead = 0
        while (liOffset < loBytes.size
            && foInputStream.read(
                loBytes, liOffset,
                loBytes.size - liOffset
            ).also { liNumRead = it } >= 0
        ) {
            liOffset += liNumRead
        }
        if (liOffset < loBytes.size) {
            throw IOException(
                "Could not completely read file "
                        + flLength
            )
        }
        return loBytes
    }

    fun logLargeString(foString: String): String {
        var lsValue = ""
        val maxLogSize = 4000
        val stringLength = foString.length
        for (i in 0..stringLength / maxLogSize) {
            val start = i * maxLogSize
            var end = (i + 1) * maxLogSize
            end = if (end > foString.length) foString.length else end
            Log.i("EncodedString", foString.substring(start, end))
            lsValue = foString.substring(start, end)
        }
        return lsValue
    }

    fun convertTime(fsTime: String): String? {
        var loFormattedDate: String = ""
        try {
            val loTime = fsTime
            val loSdf = SimpleDateFormat("HH:mm")
            val loDate = loSdf.parse(loTime)
            val loSdfs = SimpleDateFormat("hh:mm a")
            loFormattedDate = loSdfs.format(loDate)
            Log.e("parseTime", loFormattedDate)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return loFormattedDate.toUpperCase()
    }

    @SuppressLint("SimpleDateFormat")
    fun convertTimeSecond(fsTime: String): String? {
        var loFormattedDate: String = ""
        try {
            val loTime = fsTime
            val loSdf = SimpleDateFormat("HH:mm:ss")
            val loDate = loSdf.parse(loTime)
            val loSdfs = SimpleDateFormat("hh:mm:ss a")
            loFormattedDate = loSdfs.format(loDate)
            Log.e("parseTime", loFormattedDate)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return loFormattedDate.toUpperCase()
    }

    fun convertDecimalToString(fsString: String?): String {
        if (fsString != null && fsString.isNotEmpty()) {
            val fdText: Double = fsString.toDouble()
            val fiText: Int = fdText.toInt()
            val lsResult = fiText.toString()
            return lsResult
        } else return ""
    }

    fun getImgFromString(foActivity: Activity, fsProfilePic: String, userImg: ImageView?): Bitmap? {
        var loBmp: Bitmap? = null
        doAsync {
            val url = URL(fsProfilePic)
            loBmp = BitmapFactory.decodeStream(url.openConnection().getInputStream())
            foActivity.runOnUiThread {
                if (loBmp != null) {
                    userImg!!.setImageBitmap(loBmp)
                }
            }
        }
        return loBmp

    }

    fun setImgFromString(
        foActivity: Activity,
        fsProfilePic: String,
        userImg: CircleImageView?
    ): Bitmap? {
        var loBmp: Bitmap? = null
        doAsync {
            val url = URL(fsProfilePic)
            loBmp = BitmapFactory.decodeStream(url.openConnection().getInputStream())
            foActivity.runOnUiThread {
                if (loBmp != null) {
                    userImg!!.setImageBitmap(loBmp)
                }
            }
        }
        return loBmp

    }

    infix fun View.below(view: View) {
        (this.layoutParams as? RelativeLayout.LayoutParams)?.addRule(RelativeLayout.BELOW, view.id)
    }

    infix fun View.above(view: View) {
        (this.layoutParams as? RelativeLayout.LayoutParams)?.addRule(RelativeLayout.ABOVE, view.id)
    }

    fun View.CenterHorizonatally() {
        (this.layoutParams as? RelativeLayout.LayoutParams)?.addRule(RelativeLayout.CENTER_HORIZONTAL)
    }

    fun View.CenterVertically() {
        (this.layoutParams as? RelativeLayout.LayoutParams)?.addRule(RelativeLayout.CENTER_VERTICAL)
    }

    infix fun View.right(view: View) {
        (this.layoutParams as? RelativeLayout.LayoutParams)?.addRule(
            RelativeLayout.RIGHT_OF,
            view.id
        )
    }

    fun String.hasDigits(): Boolean {
        return this.any { it.isDigit() }
    }

    @SuppressLint("SimpleDateFormat")
    fun getTodaysDate(): String {
        val loTodayDate = Calendar.getInstance().time
        val loDateFormatter = SimpleDateFormat("d MMM yyyy")
        return loDateFormatter.format(loTodayDate)
    }

    @SuppressLint("SimpleDateFormat")
    fun getDate(fsDateString: String): String {
        val loDateFormatter = SimpleDateFormat("MM/dd/yyyy HH:mm:ss")
        val loDateFormatter1 = SimpleDateFormat(YEARMONTHFORMAT)
        val loDate = loDateFormatter.parse(fsDateString)
        return loDateFormatter1.format(loDate!!)
    }

    @SuppressLint("SimpleDateFormat")
    fun convertSeperatorDate(foDate: String): String {
        val loParseFormat = SimpleDateFormat("yyyy-MM-dd")
        val parseDate = loParseFormat.parse(foDate)
        val loDateFormatter = SimpleDateFormat("dd/MM/yyyy")
        return loDateFormatter.format(parseDate!!)
    }

    @SuppressLint("SimpleDateFormat")
    fun parseDate(foDate: String): String {
        val loParseFormat = SimpleDateFormat(YEARMONTHFORMAT)
        val loDateFormatter = SimpleDateFormat(DATEFORMAT)
        return loParseFormat.format(loDateFormatter.parse(foDate))
    }

    @SuppressLint("SimpleDateFormat")
    fun parseDateOnlyMonth(foDate: String): String {
        val loParseFormat = SimpleDateFormat(MONTHDATEFORMAT)
        val loDateFormatter = SimpleDateFormat(DATEFORMAT)
        return loParseFormat.format(loDateFormatter.parse(foDate))
    }

    @SuppressLint("SimpleDateFormat")
    fun parseMonthDate(foDate: String): String {
        val loParseFormat = SimpleDateFormat(SIMPLEDATEFORMAT)
        val loDateFormatter = SimpleDateFormat(DATEFORMAT)
        return loParseFormat.format(loDateFormatter.parse(foDate))
    }

    @SuppressLint("SimpleDateFormat")
    fun getCalendarDate(fsDateString: String): String {
        val loFormat = SimpleDateFormat("dd/MM/yyyy")
        return loFormat.format(loFormat.parse(fsDateString)!!)
    }

    @SuppressLint("SimpleDateFormat")
    fun checkIfDateHasPassed(fsDateString: String): Boolean {
        var lbCheck = false
        val loFormat = SimpleDateFormat("dd/MM/yyyy")
        val lsPassedDate = loFormat.parse(fsDateString)
        if (lsPassedDate!!.before(Calendar.getInstance().time)) {
            lbCheck = false
        } else if (lsPassedDate.after(Calendar.getInstance().time)) {
            lbCheck = true
        }
        return lbCheck
    }

    fun getPath(foContext: Context): String? {
        val loStorageDir =
            foContext.getExternalFilesDir(Environment.getDataDirectory().absolutePath)
        return loStorageDir!!.path
    }

    fun deleteFile(fsInputPath: String?) {
        if (!TextUtils.isEmpty(fsInputPath)) {
            try {
                val loFile = File(fsInputPath)
                if (loFile.exists()) {
                    if (loFile.isDirectory) {
                        val loList = loFile.list()
                        for (i in loList.indices) {
                            val loDirectoryFile = File(loFile, loList[i])
                            loDirectoryFile.delete()
                        }
                        loFile.delete()
                    } else {
                        loFile.delete()
                    }
                }
            } catch (e: java.lang.Exception) {
//                Log.logException(app.hazardscout.util.Common.TAG, e)
            }
        }
    }

    @SuppressLint("Recycle")
    fun getFilePath(foContext: Context, foUri: Uri?): String? {
        var fsFilePath = ""
        fsFilePath = try {
            val loProjection = arrayOf(MediaStore.Video.Media.DATA)
            val loCursor: Cursor? =
                foContext.contentResolver.query(foUri!!, loProjection, null, null, null)
            if (loCursor != null) {
                val loColumnIndex = loCursor.getColumnIndex(MediaStore.Video.Media.DATA)
                loCursor.moveToFirst()
                loCursor.getString(loColumnIndex)
            } else {
                ""
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
        return fsFilePath

    }


    @Throws(IOException::class)
    fun copyFiles(foSource: File, foDestination: File): Boolean {
        if (foSource.isDirectory) {
            if (!foDestination.exists()) {
                foDestination.mkdir()
            }
            val children = foSource.list()
            for (i in foSource.listFiles().indices) {
                copyFiles(File(foSource, children[i]), File(foDestination, children[i]))
            }
        } else {
            val `in`: InputStream = FileInputStream(foSource)
            val out: OutputStream = FileOutputStream(foDestination)
            val buf = ByteArray(1024)
            var len: Int
            while (`in`.read(buf).also { len = it } > 0) {
                out.write(buf, 0, len)
            }
            `in`.close()
            out.close()
        }
        return true
    }

    fun getFileType(fsUrl: String): String? {
        return fsUrl.substring(fsUrl.lastIndexOf('.') + 1)
    }

    @SuppressLint("NewApi")
    object RealPath {
        fun getPathFromContentUri(context: Context, uri: Uri): String? {
            val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

            // DocumentProvider
            if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
                // ExternalStorageProvider
                if (isExternalStorageDocument(uri)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":".toRegex()).toTypedArray()
                    val type = split[0]
                    if ("primary".equals(type, ignoreCase = true)) {
                        return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                    }

                    // TODO handle non-primary volumes
                } else if (isDownloadsDocument(uri)) {
                    val id = DocumentsContract.getDocumentId(uri)
//                    val contentUri = ContentUris.withAppendedId(
//                        Uri.parse("content://downloads/public_downloads"),
//                        java.lang.Long.valueOf(id)
//                    )

                    return if (id.startsWith("raw:")) {
                        id.replaceFirst("raw:", "")
                    } else try {
                        val contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"),
                            java.lang.Long.valueOf(id)
                        )
                        getDataColumn(context, contentUri, null, null)
                    } catch (e: NumberFormatException) {
                        null
                    }
//                    return getDataColumn(context, contentUri, null, null)
                } else if (isMediaDocument(uri)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":".toRegex()).toTypedArray()
                    val type = split[0]
                    var contentUri: Uri? = null
                    if ("image" == type) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    } else if ("video" == type) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    } else if ("audio" == type) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }
                    val selection = "_id=?"
                    val selectionArgs = arrayOf(split[1])
                    return getDataColumn(context, contentUri, selection, selectionArgs)
                }
            } else if ("content".equals(uri.scheme, ignoreCase = true)) {
                return getDataColumn(context, uri, null, null)
            } else if ("file".equals(uri.scheme, ignoreCase = true)) {
                return uri.path
            }
            return null
        }

        fun getDataColumn(
            context: Context,
            uri: Uri?,
            selection: String?,
            selectionArgs: Array<String>?
        ): String? {
            var cursor: Cursor? = null
            val column = "_data"
            val projection = arrayOf(column)
            try {
                cursor =
                    context.contentResolver.query(uri!!, projection, selection, selectionArgs, null)
                if (cursor != null && cursor.moveToFirst()) {
                    val column_index = cursor.getColumnIndexOrThrow(column)
                    return cursor.getString(column_index)
                }
            } finally {
                cursor?.close()
            }
            return null
        }

        fun isExternalStorageDocument(uri: Uri): Boolean {
            return "com.android.externalstorage.documents" == uri.authority
        }

        fun isDownloadsDocument(uri: Uri): Boolean {
            return "com.android.providers.downloads.documents" == uri.authority
        }

        fun isMediaDocument(uri: Uri): Boolean {
            return "com.android.providers.media.documents" == uri.authority
        }
    }

    fun getBuildVersion(foContext: Context): String {
        var versionCode = ""
        var versionName = ""

        val pInfo: PackageInfo = foContext.packageManager.getPackageInfo(foContext.packageName, 0)
        versionCode = PackageInfoCompat.getLongVersionCode(pInfo).toString()
        versionName = pInfo.versionName
        Log.i("Version", versionName + "." + versionCode)
        return versionName + "." + versionCode
    }

    fun getImageBitmap(url: String): Bitmap? {
        var bm: Bitmap? = null
        try {
            val aURL = URL(url)
            val conn: URLConnection = aURL.openConnection()
            conn.connect()
            val `is`: InputStream = conn.getInputStream()
            val bis = BufferedInputStream(`is`)
            bm = BitmapFactory.decodeStream(bis)
            bis.close()
            `is`.close()
        } catch (e: IOException) {
            Log.e("IOException", "Error getting bitmap$e")
        }
        return bm
    }

    fun getImageUriFromBitmap(context: Context, bitmap: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path =
            MediaStore.Images.Media.insertImage(
                context.contentResolver,
                bitmap,
                "Title_" + Calendar.getInstance().time,
                null
            )
        return Uri.parse(path.toString())
    }

    fun getImageSizeInMegaBytes(foContext: Context, foUri: Uri): Float {
        var lsFileSize = 0f
        val loCursor: Cursor? = foContext.contentResolver.query(foUri, null, null, null, null)
        if (loCursor != null) {
            val loSizeIndex = loCursor.getColumnIndex(OpenableColumns.SIZE)
            loCursor.moveToFirst()
            val loImageSizeBytes = loCursor.getLong(loSizeIndex).toFloat()
            val loImageSizeKB = loImageSizeBytes / 1024f
            val loImageSizeInMB = loImageSizeKB / 1024f
            lsFileSize = loImageSizeInMB
            loCursor.close()
        }
        return lsFileSize
    }

    @SuppressLint("SimpleDateFormat")
    fun DateTimeDiff(
        foParseDate: Date?
    ): Boolean {
        val calendar1 = Calendar.getInstance()
        calendar1.time = foParseDate!!
        calendar1.add(Calendar.HOUR_OF_DAY, -1)
        val loJobDate = calendar1.time
        val formatter1 = SimpleDateFormat(COMMONDATEFORMATE)
        val lsCurrentdDateTime: String = formatter1.format(Calendar.getInstance().time)
        val loCurrentdDateTime: Date = formatter1.parse(lsCurrentdDateTime)!!
        if (loCurrentdDateTime.before(loJobDate)) {
            return false
        } else {
            if (loCurrentdDateTime.after(loJobDate)) {
                return true // database date is after the current date
            } else if (loCurrentdDateTime.equals(loJobDate)) {
                return true // database date is before the current date
            }
        }
        return false
    }

    fun getCountryName(context: Context?, latitude: Double, longitude: Double): String? {
        var loCountryString = ""
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses: List<Address>?
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1)
            loCountryString = if (addresses != null && addresses.isNotEmpty()) {
                addresses[0].countryName
            } else {
                ""
            }
        } catch (ignored: IOException) {
            d("ignored", ignored.toString())
        }
        return loCountryString
    }

    fun getAddress(foContext: Context, lat: Double, lng: Double): String {
        val geocoder = Geocoder(foContext, Locale.getDefault())
        var add = ""
        try {
            val addresses: List<Address> = geocoder.getFromLocation(lat, lng, 1)
            val obj: Address = addresses[0]
            add = """$add${obj.countryName}""".trimIndent()
            d("Address", "Address$add")
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return add
    }

    fun isDarkModeOn(foContext: Context): Boolean {
        val currentNightMode =
            foContext.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES
    }

    fun openGoogleMap(foContext: Context, address: String) {
        val mapUri: Uri = Uri.parse("geo:0,0?q=" + Uri.encode(address))
        val mapIntent = Intent(Intent.ACTION_VIEW, mapUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        foContext.startActivity(mapIntent)
    }

    fun convertDateToUnix(foDate: String): Long {
        var loMainDate = foDate
        loMainDate += " 23:59:59"
        val loSdf: SimpleDateFormat = SimpleDateFormat(COMMONDATEFORMATE)
        loSdf.isLenient = false
        val loDate: Date = loSdf.parse(loMainDate)
        val loCalendar: Calendar = Calendar.getInstance()
        loCalendar.time = loDate
        loCalendar.timeInMillis
        Log.i("Dates", "" + loCalendar.timeInMillis)
        return loCalendar.timeInMillis
    }

    fun convertDate(fsUnixData: Long): String {
        val loDate: Date = Date(fsUnixData)
        val loSdf: SimpleDateFormat = SimpleDateFormat(COMMONDATEFORMATE)
        loSdf.timeZone = TimeZone.getTimeZone("GMT-6")
        val lsDate: String = loSdf.format(loDate)

        return lsDate
    }

    fun getCurrentDate(): String {
        val formatter1 = SimpleDateFormat(COMMONCALDATEFORMAT)
        val lsCurrentdDateTime: String = formatter1.format(Calendar.getInstance().time)
        return lsCurrentdDateTime
    }

    fun Context.resourceUri(foResourceId: Int): Uri = with(resources) {
        Uri.Builder()
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(getResourcePackageName(foResourceId))
            .appendPath(getResourceTypeName(foResourceId))
            .appendPath(getResourceEntryName(foResourceId))
            .build()
    }

    fun resize(image: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap? {
        var image = image
        return if (maxHeight > 0 && maxWidth > 0) {
            val width = image.width
            val height = image.height
            val ratioBitmap = width.toFloat() / height.toFloat()
            val ratioMax = maxWidth.toFloat() / maxHeight.toFloat()
            var finalWidth = maxWidth
            var finalHeight = maxHeight
            if (ratioMax > ratioBitmap) {
                finalWidth = (maxHeight.toFloat() * ratioBitmap).toInt()
            } else {
                finalHeight = (maxWidth.toFloat() / ratioBitmap).toInt()
            }
            image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, false)
            image
        } else {
            image
        }
    }

    fun getFileDetails(foContext: Context, data: Intent?): String {
        var loFileName = ""
        data!!.data?.let { returnUri ->
            foContext.contentResolver.query(returnUri, null, null, null, null)
        }?.use { cursor ->
            /*
             * Get the column indexes of the data in the Cursor,
             * move to the first row in the Cursor, get the data,
             * and display it.
             */
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            cursor.moveToFirst()
            loFileName = cursor.getString(nameIndex)

        }
        return loFileName
    }

    fun getRealPathFromURI(foContext: Context, contentURI: Uri): String? {
        val result: String?
        val cursor: Cursor = foContext.contentResolver.query(
            contentURI,
            null,
            null,
            null,
            null
        )!!
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.path
        } else {
            cursor.moveToFirst()
            val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            result = cursor.getString(idx)
            cursor.close()
        }
        return result
    }

    fun getTimeZoneOffset(): String {
        val calendar = Calendar.getInstance(
            TimeZone.getTimeZone("GMT"),
            Locale.getDefault()
        )
        val currentLocalTime = calendar.time

        val date: DateFormat = SimpleDateFormat("ZZZZZ", Locale.getDefault())
        val localTime: String = date.format(currentLocalTime)
        return localTime

    }

    fun getCurrentTime(): String {
        val cal = Calendar.getInstance()
        val currentLocalTime = cal.time
        val date: DateFormat = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())
        // you can get seconds by adding  "...:ss" to it
        // you can get seconds by adding  "...:ss" to it
        // date.timeZone = TimeZone.getTimeZone("GMT+1:00")
        val localTime = date.format(currentLocalTime)
        return localTime
    }

    @SuppressLint("InlinedApi")
    fun changeStatusColor(foActivity: Activity) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
                val window: Window = foActivity.window
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                window.navigationBarColor = Color.BLACK
                when (foActivity) {
                    is MainActivity -> {
                        window.statusBarColor = Color.parseColor("#EEF1F4")
                    }
                    else -> {
                        window.statusBarColor = Color.WHITE
                    }
                }
            }
        }
    }

    fun getDisplaySize(activity: Activity): String {
        var x = 0.0
        var y = 0.0
        val mWidthPixels: Int
        val mHeightPixels: Int
        try {
            val windowManager = activity.windowManager
            val display = windowManager.defaultDisplay
            val displayMetrics = DisplayMetrics()
            display.getMetrics(displayMetrics)
            val realSize = Point()
            Display::class.java.getMethod("getRealSize", Point::class.java)
                .invoke(display, realSize)
            mWidthPixels = realSize.x
            mHeightPixels = realSize.y
            val dm = DisplayMetrics()
            activity.windowManager.defaultDisplay.getMetrics(dm)
            x = (mWidthPixels / dm.xdpi).toDouble().pow(2.0)
            y = (mHeightPixels / dm.ydpi).toDouble().pow(2.0)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        d("displaySize", String.format(Locale.US, "%.2f", sqrt(x + y)))
        return String.format(Locale.US, "%.2f", sqrt(x + y))
    }

    fun addZeroIfRequired(fsHour: String): String? {
        d("addZero", fsHour)
        var fsReturnString = ""
        if (!fsHour.isEmpty()) {
            fsReturnString = if (fsHour.length == 1) {
                "0$fsHour"
            } else {
                fsHour
            }
        }
        return fsReturnString
    }

    fun randomNumber(from: Int, to: Int): Int {
        return (from..to).random()
    }

    fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    fun View.showKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInputFromWindow(
            windowToken,
            InputMethodManager.SHOW_FORCED,
            0
        )
    }

    fun setBoldSpannable(custMatl: String, value: String?, color: Int): SpannableString {
        val spannableContent = SpannableString("$custMatl $value")
        spannableContent.setSpan(
            StyleSpan(Typeface.NORMAL),
            0,
            custMatl.length,
            Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )

        spannableContent.setSpan(
            ForegroundColorSpan(color),
            0,
            spannableContent.length,

            Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )

        return spannableContent
    }

    fun getSpannableString(
        fsFromString: String,
        fsToBeSpanned: String,
        fsToString: String,
        fsSpannedAgain: String
    ): SpannedString {
        return buildSpannedString {
            append(fsFromString)
            bold {
                append(fsToBeSpanned)
            }
            append(fsToString)
            bold {
                append(fsSpannedAgain)
            }
        }
    }
}