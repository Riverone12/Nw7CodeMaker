package biz.riverone.nw7codemaker

import android.content.Context
import android.preference.PreferenceManager

/**
 * AppPreference.kt: このアプリの設定項目
 * Created by kawahara on 2018/02/06.
 */
object AppPreference {

    private const val PREFERENCE_VERSION = 1

    // スタート・ストップキャラクタ
    var lastStartChar: String = ""
    var lastStopChar: String = ""

    // 数値
    var lastNumberString: String = ""

    // 履歴
    var historyJson: String = ""

    private const val PREF_KEY_VERSION = "pref_version"
    private const val PREF_KEY_LAST_START_CHAR = "pref_last_start_char"
    private const val PREF_KEY_LAST_STOP_CHAR = "pref_last_stop_char"
    private const val PREF_KEY_LAST_NUMBER = "pref_last_number"
    private const val PREF_KEY_HISTORY_JSON = "pref_history_json"

    fun initialize(applicationContext: Context) {
        val pref = PreferenceManager.getDefaultSharedPreferences(applicationContext)

        lastStartChar = pref.getString(PREF_KEY_LAST_START_CHAR, "")
        lastStopChar = pref.getString(PREF_KEY_LAST_STOP_CHAR, "")
        lastNumberString = pref.getString(PREF_KEY_LAST_NUMBER, "")
        historyJson = pref.getString(PREF_KEY_HISTORY_JSON, "")

        val version = pref.getInt(PREF_KEY_VERSION, 0)
        if (version < PREFERENCE_VERSION) {
            saveAll(applicationContext)
        }
    }

    fun saveAll(applicationContext: Context) {
        val pref = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val editor = pref.edit()

        editor.putInt(PREF_KEY_VERSION, PREFERENCE_VERSION)
        editor.putString(PREF_KEY_LAST_START_CHAR, lastStartChar)
        editor.putString(PREF_KEY_LAST_STOP_CHAR, lastStopChar)
        editor.putString(PREF_KEY_LAST_NUMBER, lastNumberString)
        editor.putString(PREF_KEY_HISTORY_JSON, historyJson)

        editor.apply()
    }
}