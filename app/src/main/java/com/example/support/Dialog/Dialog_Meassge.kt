package com.example.support.Dialog

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import com.example.support.LoginActivity
import com.example.support.R
import com.example.support.RegisterUserActivity


class Dialog_Meassge(var mcontext: Context) : Dialog(mcontext) {

    var Btn_login: Button? = null
    var Btn_Register: Button? = null
//
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog)

        Btn_login = findViewById(R.id.Btn_login)
        Btn_Register = findViewById(R.id.Btn_Register)
        Btn_login!!.setOnClickListener {
            val intent = Intent(context, LoginActivity::class.java)
            context.startActivity(intent)
            dismiss()
        }

        Btn_Register!!.setOnClickListener {
            val intent = Intent(context, RegisterUserActivity::class.java)
            context.startActivity(intent)
            dismiss()

        }
    }
}