package com.example.happy_mothers_day.nfc

import android.app.Activity
import android.nfc.NfcAdapter
import android.nfc.NfcManager
import android.content.Context
import android.util.Log

class NfcHelper(private val activity: Activity) {

    private var nfcAdapter: NfcAdapter? = null
    private var onTagDiscovered: (() -> Unit)? = null

    fun isNfcAvailable(): Boolean {
        val manager = activity.getSystemService(Context.NFC_SERVICE) as? NfcManager
        nfcAdapter = manager?.defaultAdapter
        return nfcAdapter != null && nfcAdapter!!.isEnabled
    }

    fun isNfcSupported(): Boolean {
        val manager = activity.getSystemService(Context.NFC_SERVICE) as? NfcManager
        return manager?.defaultAdapter != null
    }

    fun startNfcReader(onTagDetected: () -> Unit) {
        onTagDiscovered = onTagDetected
        val manager = activity.getSystemService(Context.NFC_SERVICE) as? NfcManager
        nfcAdapter = manager?.defaultAdapter

        nfcAdapter?.let { adapter ->
            if (adapter.isEnabled) {
                adapter.enableReaderMode(
                    activity,
                    { tag ->
                        Log.d("NfcHelper", "NFC tag discovered: ${tag.id.toHexString()}")
                        activity.runOnUiThread {
                            onTagDiscovered?.invoke()
                        }
                    },
                    NfcAdapter.FLAG_READER_NFC_A or
                            NfcAdapter.FLAG_READER_NFC_B or
                            NfcAdapter.FLAG_READER_NFC_F or
                            NfcAdapter.FLAG_READER_NFC_V or
                            NfcAdapter.FLAG_READER_NFC_BARCODE,
                    null
                )
                Log.d("NfcHelper", "NFC reader mode enabled")
            }
        }
    }

    fun stopNfcReader() {
        nfcAdapter?.disableReaderMode(activity)
    }

    private fun ByteArray.toHexString(): String {
        return joinToString("") { "%02x".format(it) }
    }
}
