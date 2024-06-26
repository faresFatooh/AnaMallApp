package adapters

import models.SearchItemDetails
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.mall.anamall.R

class SearchListAdapter(var searchList: List<SearchItemDetails>) : RecyclerView.Adapter<SearchListAdapter.SearchListViewHolder>(){

    class SearchListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        fun bind(searchModel: SearchItemDetails){
            itemView.findViewById<TextView>(R.id.searchResName).text = searchModel.restaurant_name
            Glide.with(itemView).load(searchModel.restaurant_spotimage).apply(RequestOptions().override(152,152)).into(itemView.findViewById(R.id.searchImageRes))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchListAdapter.SearchListViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.custom_search_item_layout, parent, false)
        return SearchListViewHolder(view)

    }

    override fun onBindViewHolder(holder: SearchListAdapter.SearchListViewHolder, position: Int) {
        holder.bind(searchList[position])
    }

    override fun getItemCount(): Int {
        return searchList.size
    }

}