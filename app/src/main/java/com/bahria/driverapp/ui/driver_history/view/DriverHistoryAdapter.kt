package com.bahria.driverapp.ui.driver_history.view

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bahria.driverapp.databinding.DriverHistoryListItemBinding
import com.bahria.driverapp.ui.driver_history.model.DriverHistoryData

class DriverHistoryAdapter(
    private val context: Context,
    private val driverHistoryList: List<DriverHistoryData>
) :
    RecyclerView.Adapter<DriverHistoryAdapter.ViewHolder>() {

    //this method is returning the view for each item in the list
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DriverHistoryListItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    //this method is binding the data on the list
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val driverInfo = driverHistoryList[position]
        holder.bind(driverInfo)
    }

    //this method is giving the size of the list
    override fun getItemCount(): Int {
        return driverHistoryList.size
    }

    //the class is holding the list view
    class ViewHolder(private val binding: DriverHistoryListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            driverHistoryData: DriverHistoryData
        ) {
//            binding.route.text = driverInfo.route
//            binding.busNo.text = driverInfo.busNo
        }
    }
}