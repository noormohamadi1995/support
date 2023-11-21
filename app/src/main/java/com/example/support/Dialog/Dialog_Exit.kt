package com.example.support.Dialog

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Button
import com.example.support.R


class Dialog_Exit(var mcontext: Context, var activity: Activity) : Dialog(mcontext) {


    var Btn_exit: Button? = null
    var Btn_cancel: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layoutexit)

        Btn_exit = findViewById(R.id.Btn_exit)
        Btn_cancel = findViewById(R.id.Btn_cancel)

        Btn_cancel!!.setOnClickListener{
            dismiss()
        }

        Btn_exit?.setOnClickListener {
            val sharedPreferences = context.getSharedPreferences("Shtoken", 0)
            val editor = sharedPreferences.edit()
            editor.clear()
            editor.commit()
            activity.finish()
        }
    }
}
