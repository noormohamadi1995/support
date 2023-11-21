package com.example.support.Adapter_0

import android.annotation.SuppressLint
import android.widget.TextView
import android.content.Intent
import com.bumptech.glide.Glide
import androidx.recyclerview.widget.RecyclerView
import android.content.Context
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.support.Panel.ListPrice.ContentPriceActivity
import com.example.support.R
import com.example.support.new_model.PriceListModel
import java.text.DecimalFormat
import java.util.ArrayList


class Adpater_Recyclerview_Price(var context: Context, listprice: List<PriceListModel.ListPrice?>?) :
    RecyclerView.Adapter<Adpater_Recyclerview_Price.viewholder>() {
    var listprice: List<PriceListModel.ListPrice?>? = ArrayList()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewholder {
        val view = LayoutInflater.from(context).inflate(R.layout.itemprice, parent, false)
        return viewholder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: viewholder, position: Int) {
        val datamodelprice = listprice!![position]

        Glide
            .with(context)
            .load(datamodelprice!!.Imageurl)
            .centerCrop()
            .transform( RoundedCorners(13))
            .placeholder(R.drawable.ima)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .into(holder.imageViewo)


        holder.Tv_name.text = "اشتراک: " + datamodelprice.name
        holder.Tv_price.text = " قیمت محصول:" + formatPrice(datamodelprice.price!!.trim().toDouble())+" تومان"
        holder.Tv_days.text = "مدت اعتبار: " + datamodelprice.days

        holder.itemView.setOnClickListener { view: View? ->
            val intent = Intent(context, ContentPriceActivity::class.java)
            intent.putExtra("IdPost", datamodelprice.idvip)
            intent.putExtra("NamePost", datamodelprice.name)
            intent.putExtra("Content_Post", "")
            intent.putExtra("price_Post", datamodelprice.price)
            intent.putExtra("days_Post", datamodelprice.days)
            intent.putExtra("Imageurl", datamodelprice.Imageurl)
            context.startActivity(intent)

        }
    }

    fun formatPrice(price: Double): String {
        val formatter = DecimalFormat("#,###.##")
        return formatter.format(price)
    }

    override fun getItemCount(): Int {
        return listprice!!.size

    }

    class viewholder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageViewo: ImageView
        var Tv_name: TextView
        var Tv_price: TextView
        var Tv_days: TextView

        init {
            imageViewo = itemView.findViewById(R.id.Ig_price)
            Tv_name = itemView.findViewById(R.id.Tv_name)
            Tv_price = itemView.findViewById(R.id.Tv_price)
            Tv_days = itemView.findViewById(R.id.Tv_days)
        }
    }

    init {
        this.listprice = listprice
    }
}