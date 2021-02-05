package com.example.monotre

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.fragment.app.DialogFragment


class SingleDialogFragment: DialogFragment() {
    private lateinit var listener: NoticeDialogListener
    interface NoticeDialogListener {
        fun onDialogPositiveClick(dialog: DialogFragment)
        fun onDialogNegativeClick(dialog: DialogFragment)
        fun toastDisplay(str: String)
    }
    
    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as NoticeDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException((context.toString() +
                    " must implement NoticeDialogListener"))
        }
        
    }
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            builder.setView(inflater.inflate(R.layout.dialog_registration, null))
                    .setPositiveButton(R.string.ok) { _, _ ->
                        Log.d("normally", "setPositiveButton")
                        this.listener.onDialogPositiveClick(this)
                    }
                    .setNegativeButton(R.string.cancel) { _, _ ->
                        Log.d("normally", "setNegativeButton")
                        dialog?.cancel()
                        this.listener.onDialogNegativeClick(this)
                    }

            
            val dialog = builder.create()
            
            dialog.setOnShowListener { // Dialog表示後じゃないとボタンがnullでsetListenerできない
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnTouchListener {
                    view: View, motionEvent: MotionEvent ->
                            checkDialog(dialog) // trueを返すとDialogの "Closeを" キャンセルする
                }
            }

            return dialog
        } ?: throw IllegalStateException("Activity cannot be null")
        
    }

    private fun checkDialog(target: Dialog?): Boolean {
        val dialog = target ?: return true
        val emptyEditText =
            listOf(R.id.itemName, R.id.UUID, R.id.major, R.id.minor)
                    .map {id -> dialog.findViewById<EditText>(id)}
                    .map {editText -> editText.hint to editText.text }
                    .find { (_, text) -> text.isEmpty() }
        emptyEditText?.let { (hint, _) ->
            Log.d("error", "${hint}が空白")
            listener.toastDisplay("${hint}が未入力です")
        }
        
        return emptyEditText != null
    }
    
}
