package com.example.support.ticket

import CircleCropWithBorder
import android.annotation.SuppressLint
import android.widget.TextView
import android.content.Intent
import com.bumptech.glide.Glide
import androidx.recyclerview.widget.RecyclerView
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.support.R
import com.example.support.new_model.TicketData
import com.example.support.new_model.TicketsModel
import java.text.DecimalFormat
import java.util.ArrayList


class TicketAdapter(
    var context: Context,
    ticketList: List<TicketData>,
    private val isOnline : Boolean
) :

    RecyclerView.Adapter<TicketAdapter.viewholder>() {
    var ticketList: List<TicketData> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewholder {
        val view = LayoutInflater.from(context).inflate(R.layout.itemticket, parent, false)
        return viewholder(view)
    }

    private fun getStatusIcon(_status: String) =  when (_status) {
            "waiting_admin" -> {
                R.drawable.ic_support;
            }
            "waiting_customer" -> {
                R.drawable.ic_customer;
            }
            "done" -> {
                R.drawable.ic_finish;
            }
            "open" -> {
                R.drawable.ic_waiting;
            }
            else -> R.drawable.ic_waiting
    }

    private fun getStatus(_status: String) = when (_status) {
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

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: viewholder, position: Int) {
        val datamodelticket = ticketList[position]

        holder.onlineView.setImageDrawable(null)

        Log.e("tikects",isOnline.toString())


        holder.tv_date.text = datamodelticket.created_at
        holder.tv_title.text = datamodelticket.title
        holder.tv_status.text = getStatus(datamodelticket.status.toString())
        holder.onlineView.setImageDrawable(ContextCompat.getDrawable(
            context,
            if (isOnline) R.drawable.circle_shape else R.drawable.circle_white_shape)
        )


        if (datamodelticket.status.toString() == "waiting_customer") {
            datamodelticket.unread_count?.takeIf { it > 0 }?.let {
                holder.tv_ticketNewMessages.visibility = View.VISIBLE
                holder.tv_ticketNewMessages.text =
                    datamodelticket.unread_count.toString() + " پیام ناخوانده"
            } ?: kotlin.run {
                holder.tv_ticketNewMessages.visibility = View.GONE
            }
        } else {
            holder.tv_ticketNewMessages.visibility = View.GONE
        }

        if (datamodelticket.status.toString() == "waiting_customer") {
            val sharedPreferences = context.getSharedPreferences("Shtoken", 0)
            val profile = sharedPreferences.getString("profile", "")

            Glide.with(context)
                .load(profile)
                .transform(CircleCrop(), CircleCropWithBorder(4, Color.GRAY))
                .error(ContextCompat.getDrawable(holder.itemView.context,R.drawable.baseline_person_24))
                .into(
                    holder.img_ticket
                )

        } else {
            Glide.with(context)
                .load(getStatusIcon(datamodelticket.status.toString()))
                .transform(CircleCrop(), CircleCropWithBorder(4, Color.GRAY))
                .error(ContextCompat.getDrawable(holder.itemView.context,R.drawable.baseline_person_24))
                .into(
                    holder.img_ticket
                )
        }

//
        holder.itemView.setOnClickListener {
            val intent = Intent(context, MainDetailTicketActivity::class.java)
            intent.putExtra("ticketId", datamodelticket.id)
            context.startActivity(intent)
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
        var img_ticket: ImageView
        var tv_title: TextView
        var tv_date: TextView
        var tv_status: TextView
        var tv_ticketNewMessages: TextView
        var onlineView : ImageView

        init {
            img_ticket = itemView.findViewById(R.id.Im_post)
            tv_title = itemView.findViewById(R.id.Tv_TicketTitle)
            tv_date = itemView.findViewById(R.id.Tv_TicketDate)
            tv_status = itemView.findViewById(R.id.Tv_ticketStatus)
            tv_ticketNewMessages = itemView.findViewById(R.id.Tv_ticketNewMessage)
            onlineView = itemView.findViewById(R.id.online_check)
        }
    }

    init {
        this.ticketList = ticketList
    }
}