package biz.riverone.nw7codemaker

import android.content.pm.ActivityInfo
import android.graphics.Point
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds

/**
 * Nw7CodeMaker: NW7コードメーカー
 * Copyright (C) 2018 J.Kawahara
 * 2018.2.6 J.Kawahara JanCodeMaker からフォーク
 * 2018.2.6 J.Kawahara ver.1.01 初版公開
 */
class MainActivity : AppCompatActivity(), ClearComfirmDialog.DialogListener {

    companion object {
        private const val BARCODE_HEIGHT = 200
    }

    private val spinnerStartChar by lazy { findViewById<Spinner>(R.id.spinnerStartChar) }
    private val spinnerStopChar by lazy { findViewById<Spinner>(R.id.spinnerStopChar) }
    private val editNumber by lazy { findViewById<EditText>(R.id.editTextNumber) }

    private val barcodeListView by lazy { findViewById<RecyclerView>(R.id.barcodeListView) }
    private lateinit var barcodeListAdapter: Nw7ListAdapter

    private fun getDisplayWidth(): Int {
        val display = windowManager.defaultDisplay
        val point = Point()
        display.getSize(point)
        return point.x
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 画面をポートレートに固定する
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        initializeControls()

        // バーコード表示コントロールの準備
        val barcodeWidth = (getDisplayWidth() * 0.75).toInt()
        val barcodeHeight = BARCODE_HEIGHT

        barcodeListView.layoutManager = LinearLayoutManager(this)
        barcodeListAdapter = object : Nw7ListAdapter() {
            override fun onClicked(barcodeString: String) {
                BarcodeLabelFragment.show(
                        supportFragmentManager,
                        barcodeString,
                        barcodeWidth,
                        barcodeHeight)
            }
        }
        barcodeListAdapter.barcodeWidth = barcodeWidth
        barcodeListAdapter.barcodeHeight = barcodeHeight
        barcodeListView.adapter = barcodeListAdapter

        // スワイプで要素を削除する準備
        val touchHelper = ItemTouchHelper(swipeListCallback)
        touchHelper.attachToRecyclerView(barcodeListView)

        // AdMob
        MobileAds.initialize(applicationContext, "ca-app-pub-1882812461462801~5801633399")
        val adView = findViewById<AdView>(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

    private fun charToIndex(c: String): Int {
        return when (c) {
            "B" -> 1
            "C" -> 2
            "D" -> 3
            else -> 0
        }
    }

    override fun onResume() {
        super.onResume()

        // 前回終了時の情報を取得する
        AppPreference.initialize(applicationContext)

        spinnerStartChar.setSelection(charToIndex(AppPreference.lastStartChar))
        spinnerStopChar.setSelection(charToIndex(AppPreference.lastStopChar))
        editNumber.setText(AppPreference.lastNumberString)

        barcodeListAdapter.fromJson(AppPreference.historyJson)
    }

    override fun onPause() {
        super.onPause()

        // 終了時の情報を保存する
        AppPreference.lastStartChar = spinnerStartChar.selectedItem.toString()
        AppPreference.lastStopChar = spinnerStopChar.selectedItem.toString()
        AppPreference.lastNumberString = editNumber.text.toString()

        AppPreference.historyJson = barcodeListAdapter.toJson()

        AppPreference.saveAll(applicationContext)
    }

    private fun zeroPadding(value: Long, columns: Int): String {
        var str = value.toString()
        if (str.length > columns) {
            str = "0"
        }
        while (str.length < columns) {
            str = "0" + str
        }
        return str
    }

    private fun initializeControls() {

        // クリアボタンの準備
        val buttonClear = findViewById<Button>(R.id.buttonClear)
        buttonClear.setOnClickListener {
            editNumber.text.clear()
            editNumber.requestFocus()
        }

        // インクリメントボタンの準備
        val buttonIncrement = findViewById<Button>(R.id.buttonIncrement)
        buttonIncrement.setOnClickListener {
            // 現在の値の桁数を取得する
            val oldValue = editNumber.text.toString()
            val nextNumber = txtToNum(oldValue) + 1
            val nextValue = zeroPadding(nextNumber, oldValue.length)

            editNumber.setText(nextValue)
            editNumber.requestFocus()
        }

        // 作成ボタンの準備
        val buttonGenerate = findViewById<Button>(R.id.buttonGenerate)
        buttonGenerate.setOnClickListener {
            val strNumber = editNumber.text.toString()
            if (strNumber.isNotEmpty()) {
                val startChar = spinnerStartChar.selectedItem.toString()
                val stopChar = spinnerStopChar.selectedItem.toString()
                val strBarcode = Nw7Digit.toNw7String(startChar, stopChar, strNumber)

                addBarcode(strBarcode)
            }
        }
    }

    private fun txtToNum(text: String): Long {
        try {
            if (text.isEmpty()) {
                return 0
            }
            return text.toLong()
        }
        catch (e: NumberFormatException) {
            return 0
        }
    }

    // リストにバーコードを追加する
    private fun addBarcode(jan: String) {
        barcodeListAdapter.addItem(0, jan)
        barcodeListView.scrollToPosition(0)
    }

    // スワイプでリサイクラービューの要素を削除する
    private val swipeListCallback = object: ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
        override fun onMove(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?, target: RecyclerView.ViewHolder?): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder?, direction: Int) {
            // 横にスワイプされたら要素を削除する
            if (viewHolder != null) {
                val swipedPosition = viewHolder.adapterPosition
                barcodeListAdapter.remove(swipedPosition)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_all_clear -> {
                val confirmDialog = ClearComfirmDialog.create()
                confirmDialog.setDialogListener(this)
                confirmDialog.show(supportFragmentManager, "Dialog")
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun doPositiveClick() {
        barcodeListAdapter.removeAll()
    }

    override fun doNegativeClick() { }
}
