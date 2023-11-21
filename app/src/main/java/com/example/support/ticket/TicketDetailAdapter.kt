package com.example.support.ticket

import CircleCropWithBorder
import android.annotation.SuppressLint
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.opengl.Visibility
import android.util.Log
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.support.FullscreenActivity
import com.example.support.R
import com.example.support.new_model.TicketMessageData
import java.text.DecimalFormat
import java.util.ArrayList


class TicketDetailAdapter(
    var context: Context,
    ticketList: List<TicketMessageData>
) : RecyclerView.Adapter<TicketDetailAdapter.viewholder>() {
    var ticketList: List<TicketMessageData> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewholder {
        return if (viewType == 1) {
            val view = LayoutInflater.from(context)
                .inflate(R.layout.itemticket_detail_admin, parent, false)
             viewholder(view)
        } else {
            val view = LayoutInflater.from(context)
                .inflate(R.layout.itemticket_detail_customer, parent, false)
             viewholder(view)
        }
    }

    fun getStatus(_status: String): String {
        return when (_status) {
            "waiting_admin" -> {
                "در انتظار پاسخ ادمین";
            }
            "waiting_customer" -> {
                "در انتظار پاسخ مشتری";
            }
            "done" -> {
                "بسته";
            }
            "open" -> {
                "باز";
            }
            else -> ""
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (ticketList[position].is_admin == 1) {
            1 // View Type 1
        } else {
            2 // View Type 2
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: viewholder, position: Int) {
        val datamodelticket = ticketList[position]
        if (datamodelticket.is_admin == 1) {
            holder.tv_title.text = "از طرف: ادمین"

            Glide.with(context).load(R.drawable.ic_support).error(R.drawable.ic_support)
                .placeholder(R.drawable.ic_support)
                .transform(CircleCrop(), CircleCropWithBorder(4, Color.GRAY)).into(
                    holder.img_profile
                )

        } else {
            holder.tv_title.text = "از طرف: مشتری"

            val sharedPreferences = context.getSharedPreferences("Shtoken", 0)
            val profile = sharedPreferences.getString("profile", "")

            Glide.with(context).load(profile).error(R.drawable.ic_customer)
                .placeholder(R.drawable.ic_customer)
                .transform(CircleCrop(), CircleCropWithBorder(4, Color.GRAY)).into(
                    holder.img_profile
                )
        }

        holder.imgStatus.setImageDrawable(
            ContextCompat.getDrawable(
                context,
                if (datamodelticket.isVisited == 1)
                    R.drawable.baseline_done_all_24
                else
                    R.drawable.baseline_done_24
            )
        )


        holder.tv_date.text = datamodelticket.created_at.toString()
        holder.tv_comment.text = datamodelticket.comment

        datamodelticket.image?.takeIf { it.isNotEmpty() }?.let {
            holder.image_TicketDetail.visibility = View.VISIBLE
            Glide
                .with(holder.itemView.context)
                .load(datamodelticket.image)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(holder.image_TicketDetail)
        } ?: kotlin.run {
            holder.image_TicketDetail.visibility = View.GONE
        }

        holder.image_TicketDetail.setOnClickListener {
            val intent = Intent(context, FullscreenActivity::class.java)
            intent.putExtra("image_url", datamodelticket.image)
            context.startActivity(intent)
        }

        holder.tv_comment.setOnClickListener {
            datamodelticket.comment?.takeIf { it.isNotEmpty()}?.let {
                val clipboard =
                    context.getSystemService(Context.CLIPBOARD_SERVICE) as? (android.content.ClipboardManager)
                val clip = android.content.ClipData.newPlainText("Copied Text",it)
                clipboard?.apply {
                    setPrimaryClip(clip)
                    Toast.makeText(context,"کپی شد",Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun formatPrice(price: Double): String {
        val formatter = DecimalFormat("#,###.##")
        return formatter.format(price)
    }

    override fun getItemCount(): Int {
        return ticketList.size
    }

    class viewholder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var tv_title: TextView
        var tv_comment: TextView

        //        var image_TicketDetail:ImageView
        var tv_date: TextView
        var img_profile: ImageView
        var image_TicketDetail: ImageView
        var imgStatus: ImageView

        init {
            tv_title = itemView.findViewById(R.id.Tv_TicketDetail_title)
            tv_comment = itemView.findViewById(R.id.Tv_TicketDetailComment)
            image_TicketDetail = itemView.findViewById(R.id.image_TicketDetailComment)
            tv_date = itemView.findViewById(R.id.Tv_DetailDate)
            img_profile = itemView.findViewById(R.id.img_profile)
            imgStatus = itemView.findViewById(R.id.img_status)
        }
    }

    init {
        this.ticketList = ticketList
    }
}