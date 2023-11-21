package com.example.support.Dialog

import android.os.Bundle
import android.text.InputFilter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.Toast
import androidx.core.view.children
import androidx.core.widget.doAfterTextChanged
import com.example.support.databinding.DialogWalletBinding
import com.example.support.new_api.ApiPanelController
import com.example.support.new_api.PanelRoutes
import com.example.support.new_model.StoreResponseModel
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class WalletDialog : BottomSheetDialogFragment(){
    private var mBinding : DialogWalletBinding? = null
    private var current = ""
    private var progressDialog : android.app.ProgressDialog? = null

    companion object {
        private val nonDigits = Regex("[^\\d]")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = DialogWalletBinding.inflate(inflater,container,false).apply {
        mBinding = this
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (containsEditText(view)) {
            dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
            (dialog as BottomSheetDialog).behavior.state = STATE_EXPANDED
        }

        val sharedPreferences = requireActivity().getSharedPreferences("Shtoken",0)
        val wallet = sharedPreferences.getLong("walletAmount",0)
        val token = sharedPreferences.getString("token","")
        progressDialog = android.app.ProgressDialog(requireContext())
        progressDialog?.setMessage("لطفا منتظر بمانبد...")
        progressDialog?.setCancelable(false)

        mBinding?.apply {
            btnPay.isEnabled = wallet > 0
            btnPay.setOnClickListener {
                val cardNumber = etCardNumber.text.toString().trim().replace(" ","")
                val cardName = etName.text.toString().trim()
                if (cardNumber.length != 16) {
                    etCardLayout.error = "شماره کارت نامعتبر است"
                    return@setOnClickListener
                }

                if (cardName.isNotEmpty() && cardName.length < 3){
                    etNameLayout.error = "نام صاحب حساب نامعتبر است"
                    return@setOnClickListener
                }

                pay(cardNumber,cardName,wallet,token ?: "")
            }

            etCardNumber.doAfterTextChanged {text->
                if (text.toString() != current) {
                    val userInput = text.toString().replace(nonDigits,"")
                    if (userInput.length <= 16) {
                        current = userInput.chunked(4).joinToString(" ")
                        text?.filters = arrayOfNulls<InputFilter>(0)
                    }
                    text?.replace(0, text.length, current, 0, current.length)
                }
            }
        }
    }

    private fun pay(cardNumber : String,cardName : String,wallet : Long,token : String){
        progressDialog?.show()
        val route = ApiPanelController.client.create(PanelRoutes::class.java)
        route.store(
            token = token,
            cardNumber = cardNumber,
            cardName = cardName,
            wallet = wallet.toString()
        ).enqueue(object : Callback<StoreResponseModel> {
            override fun onResponse(
                call: Call<StoreResponseModel>,
                response: Response<StoreResponseModel>
            ) {
                progressDialog?.dismiss()
               if (response.isSuccessful){
                   if (response.code() == 200 && response.body()?.status == "ok")
                           Toast.makeText(requireContext(),response.body()?.message ?: "",Toast.LENGTH_SHORT).show()
               }else if(response.code() == 404) {
                   response.errorBody()?.string()?.let {
                       Log.e("error body",it)
                       val jObjError = JSONObject(it)

                       Log.e("model",jObjError.getString("message"))
                       Log.e("model",jObjError.getString("error"))

                       Toast.makeText(requireContext(), jObjError.getString("message") ?: "" ,Toast.LENGTH_SHORT).show()
                   }
               }else{
                   Toast.makeText(requireContext(),"خطا در دریافت اطلاعات",Toast.LENGTH_SHORT).show()
               }
            }

            override fun onFailure(call: Call<StoreResponseModel>, t: Throwable) {
                progressDialog?.dismiss()
                Log.e("store Error","error",t)
                Toast.makeText(requireContext(),"خطا در دریافت اطلاعات",Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun  containsEditText(view: View): Boolean {
        var result = false
        if (view is ViewGroup) {
            for (v in view.children) {
                if (v is ViewGroup) {
                    if (containsEditText(v)) {
                        result = true
                    }
                } else {
                    if (v is EditText) {
                        result = true
                        break
                    }
                }
            }
        }


        return result
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        mBinding = null
    }
}