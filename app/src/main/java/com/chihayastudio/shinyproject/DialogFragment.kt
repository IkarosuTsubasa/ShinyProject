package com.chihayastudio.shinyproject

import android.app.Dialog
import android.app.DialogFragment
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.TextView


class DialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(activity)
        dialog.setContentView(R.layout.custom_dialog)
        dialog.setCancelable(true)
        dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val choicesTextView = dialog.findViewById(R.id.ChoicesTextView) as TextView
        val choicesTextView2 = dialog.findViewById(R.id.ChoicesTextView2) as TextView
        val choicesTextView3 = dialog.findViewById(R.id.ChoicesTextView3) as TextView

        choicesTextView.text = "植物に詳しいんだって？"
        choicesTextView2.text = "お出かけが好きなんだって？"
        choicesTextView3.text = "どんな公園が好きなんだ？"

        return dialog
    }

    override fun onStop() {
        super.onStop()
        activity.finish()
    }
}