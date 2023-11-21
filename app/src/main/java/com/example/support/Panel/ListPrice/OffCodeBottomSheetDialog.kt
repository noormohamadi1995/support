package com.example.support.Panel.ListPrice

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import com.example.support.databinding.DialogOffCodeBinding
import com.example.support.new_api.ApiController
import com.example.support.new_api.Routes
import com.example.support.new_model.CheckReferralCodeResponse
import com.example.support.new_model.OffCodeModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OffCodeBottomSheetDialog : BottomSheetDialogFragment() {
    private var mBinding: DialogOffCodeBinding? = null
    private var offCodePrice : Long? = null
    private var morefCodePrice  : Long? = null
    private var progressDialog: ProgressDialog? = null
    private var offCodeFinal: String? = null
    private var morefCodeFinal: String? = null
    private var offCodePercent : String? = null
    private var morefCodePercent : String? = null

    companion object {
        const val OFF_CODE_REQUEST = "off_code_request"
        const val OFF_CODE = "off_code"
        const val MOREF_CODE = "moref_code"
        const val TOKEN = "token"
        const val ID = "id"
        const val PRICE = "price"
        const val BEFORE_PRICE = "before_price"
        const val TYPE = "type"
        const val PERCENT = "percent"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        progressDialog = ProgressDialog(requireContext())
        progressDialog?.setMessage("لطفا منتظر بمانبد...")
        progressDialog?.setCancelable(false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = DialogOffCodeBinding.inflate(inflater, container, false).apply {
        mBinding = this
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding?.apply {
            arguments?.getString(TYPE)?.let {
                offCodeLay.visibility = if (it == "off") View.VISIBLE else View.GONE
                referralCodeLay.visibility = if (it == "referral") View.VISIBLE else View.GONE
            }
            btnEffectOff.setOnClickListener {
                offCodeTextInputLayout.error = ""
                edtOffCode.text?.toString()?.trim()?.takeIf { it.isNotEmpty() || it.isNotBlank() }
                    ?.let {
                        effectOffCode(it)
                    } ?: kotlin.run {
                    offCodeTextInputLayout.error = "وارد کردن کد تخفیف الزامی است"
                }
            }

            btnAddMorefCode.setOnClickListener {
                morefCodeTextInputLayout.error = ""
                edtMorefCode.text?.toString()?.trim()?.takeIf { it.isNotEmpty() || it.isNotBlank() }
                    ?.let {
                        checkReferralCode(it)
                    } ?: kotlin.run {
                    morefCodeTextInputLayout.error = "کد معرف نمی تواند خالی باشد"
                }
            }

            btnPay.setOnClickListener {
                val type = arguments?.getString(TYPE)
                Log.e("offPrice",offCodePrice?.toString() + "")
                Log.e("morefPrice",morefCodePrice?.toString() + "")
                dismiss()
                requireActivity().supportFragmentManager.setFragmentResult(
                    OFF_CODE_REQUEST,
                    bundleOf(
                        OFF_CODE to offCodeFinal,
                        MOREF_CODE to morefCodeFinal,
                        PRICE to if (type == "off") offCodePrice?.toString() else morefCodePrice?.toString(),
                        PERCENT to if (type == "off") offCodePercent else morefCodePercent
                    )
                )
            }
        }
    }

    private fun effectOffCode(offCode: String) {
        offCodePrice = null
        offCodeFinal = null
        offCodePercent = null
        progressDialog?.show()
        val token = arguments?.getString(TOKEN, "")
        val id = arguments?.getString(ID, "")
        val routes = ApiController.client.create(Routes::class.java)
        val call = routes.checkOffCode(token, id, offCode)
        call.enqueue(object : Callback<OffCodeModel> {
            override fun onResponse(
                call: Call<OffCodeModel>,
                response: Response<OffCodeModel>
            ) {
                Log.e("mmmm",response.body()?.toString() + "")
                progressDialog?.dismiss()
                val offCodeModel = response.body()

                if (offCodeModel?.status == "ok") {
                    if (response.body() != null) {
                        offCodePrice = response.body()?.priceAfterOff?.toLong()
                        offCodeFinal = offCode
                        offCodePercent = response.body()?.offCOde
                        Toast.makeText(
                            requireContext(),
                            response.body()?.message ?: "",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        offCodeModel?.error ?: offCodeModel?.message ?: "",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<OffCodeModel?>, t: Throwable) {
                Toast.makeText(
                    requireContext(),
                    "خطا در ارسال اطلاعات....",
                    Toast.LENGTH_SHORT
                ).show()
                progressDialog?.dismiss()
            }
        })
    }

    private fun checkReferralCode(referralCode: String) {
        morefCodePrice = null
        morefCodeFinal = null
        morefCodePercent = null
        progressDialog?.show()
        val token = arguments?.getString(TOKEN, "")
        val id = arguments?.getString(ID, "")
        val route = ApiController.client.create(Routes::class.java)
        route.checkReferralCode(
            token = token,
            referralCode = referralCode,
            idVip = id

        ).enqueue(object : Callback<CheckReferralCodeResponse> {
            override fun onResponse(
                call: Call<CheckReferralCodeResponse>,
                response: Response<CheckReferralCodeResponse>
            ) {
                progressDialog?.dismiss()
                if (response.isSuccessful) {
                    if (response.body()?.status == "ok") {
                        val referralModel = response.body()
                        morefCodePrice = referralModel?.amount
                        morefCodeFinal = referralCode
                        morefCodePercent = referralModel?.referralCode
                        Toast.makeText(
                            requireContext(),
                            response.body()?.message ?: "",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else if (response.body()?.status == "error") {
                        Toast.makeText(
                            requireContext(),
                            response.body()?.error ?: "",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            override fun onFailure(call: Call<CheckReferralCodeResponse>, t: Throwable) {
                Toast.makeText(
                    requireContext(),
                    "خطا در ارسال اطلاعات....",
                    Toast.LENGTH_SHORT
                ).show()
                progressDialog?.dismiss()
            }
        })
    }
}