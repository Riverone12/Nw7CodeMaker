package biz.riverone.nw7codemaker

import java.text.DecimalFormat

/**
 * Nw7Digit.kt: JAN数値
 * Created by kawahara on 2018/02/05.
 */
object Nw7Digit {

    fun toNw7String(startChar: String, stopChar: String, number: String): String {
        return startChar + number + stopChar
    }
}