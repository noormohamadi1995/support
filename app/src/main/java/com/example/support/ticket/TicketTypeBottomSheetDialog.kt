package com.example.support.ticket

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.example.support.R
import com.example.support.databinding.BottomSheetTicketTypeBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class TicketTypeBottomSheetDialog : BottomSheetDialogFragment() {
    private var mBinding : BottomSheetTicketTypeBinding? = null

    companion object{
        const val TICKET_TYPE_REQUEST = "ticket_type_request"
        const val TYPE = "ticket_type"
        const val TITLE = "title"
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = BottomSheetTicketTypeBinding.inflate(inflater,container,false).apply {
        mBinding = this
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding?.apply {
            arguments?.getString(TYPE)?.let {
                when(it){
                    TicketType.OPEN.type -> {
                        mNavigation.menu.findItem(R.id.open).isChecked = true
                        mNavigation.menu.findItem(R.id.open).setIcon(R.drawable.check)
                    }
                    TicketType.CLOSE.type -> {
                        mNavigation.menu.findItem(R.id.close).isChecked = true
                        mNavigation.menu.findItem(R.id.close).setIcon(R.drawable.check)
                    }
                    else -> Unit
                }
            }
            mNavigation.setNavigationItemSelectedListener {
                when (it.itemId) {
                    R.id.open -> {
                        setType(TicketType.OPEN.type,it.title?.toString() ?: "")
                    }

                    R.id.close -> {
                        setType(TicketType.CLOSE.type,it.title?.toString() ?: "")
                    }
                }
                true
            }
        }
    }

    private fun setType(type : String,message : String){
        dismiss()
        requireActivity().supportFragmentManager.setFragmentResult(
            TICKET_TYPE_REQUEST,
            bundleOf(
                TYPE to type,
                TITLE to message
            )
        )
    }
}

enum class TicketType(val type : String){
    OPEN("open"),CLOSE("close")
}