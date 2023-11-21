package com.example.support.Dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import com.example.support.databinding.BottomSheetGetPhoneBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class GetCodeDialog : BottomSheetDialogFragment() {
    private var mBinding : BottomSheetGetPhoneBinding? = null

    companion object{
        const val REQUEST = "request"
        const val PHONE = "phone"
        const val TYPE = "type"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = BottomSheetGetPhoneBinding.inflate(inflater,container,false).apply {
        mBinding = this
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding?.apply {
            etPhone.doAfterTextChanged {
                btnLogin.isEnabled = it.toString().trim().isNotEmpty() && it.toString().trim().length == 11
            }
            btnLogin.setOnClickListener {
                dismiss()
                requireActivity().supportFragmentManager.setFragmentResult(
                    REQUEST,
                    bundleOf(
                        PHONE to etPhone.text.toString().trim(),
                        TYPE to arguments?.getString("type")
                    )
                )
            }
        }
    }
}