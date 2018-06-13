package ui

import android.app.Dialog
import android.app.DialogFragment
import android.os.Bundle

class PhotoOptionDialogFragment: DialogFragment() {
    interface PhotoOptionDialogListener {
        fun onCaptureClick()
        fun onPickClick()
    }

    private lateinit var listener: PhotoOptionDialogListener

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        
    }
}