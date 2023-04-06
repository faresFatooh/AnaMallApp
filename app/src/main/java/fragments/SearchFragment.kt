package fragments

import adapters.SearchListAdapter
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.mall.anamall.R
import com.mall.anamall.databinding.FragmentSearchBinding
import models.SearchItemDetails

class SearchFragment : Fragment() {
    private val firebaseFirestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var searchList: List<SearchItemDetails> = ArrayList()
    private val searchListAdapter = SearchListAdapter(searchList)
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private var mRecyclerView: RecyclerView? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        val view = binding.root
        val adapter: RecyclerView.Adapter<RecyclerAdapter.ViewHolder>
        val layoutManager =
            LinearLayoutManager(activity?.applicationContext, LinearLayoutManager.HORIZONTAL, false)
        mRecyclerView = view.findViewById<View>(R.id.searchItemRecyclerViewd) as RecyclerView
        mRecyclerView!!.setLayoutManager(layoutManager)
        adapter = RecyclerAdapter()
        mRecyclerView!!.setAdapter(adapter)
        return view

    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        binding.searchItemRecyclerView.hasFixedSize()
        binding.searchItemRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.searchItemRecyclerView.adapter = searchListAdapter

        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                val searchText: String = binding.searchEditText.text.toString()
                val sharedPreference =
                    activity!!.getSharedPreferences("PREFERENCE_NAMEasa", Context.MODE_PRIVATE)
                var store = sharedPreference.getString("store", "Mall")
                searchInFirestore(searchText.toLowerCase(), store)

            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })

    }

    @SuppressLint("LogNotTimber")
    private fun searchInFirestore(searchText: String, store: String?) {
        firebaseFirestore.collection(store.toString()).orderBy("search_restaurant")
            .startAt(searchText)
            .endAt("$searchText\uf8ff").get().addOnCompleteListener {
                if (it.isSuccessful) {
                    searchList = it.result!!.toObjects(SearchItemDetails::class.java)
                    searchListAdapter.searchList = searchList
                    searchListAdapter.notifyDataSetChanged()
                }
            }

    }

    private class RecyclerAdapter : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {
        var list = arrayListOf<String>("Mall", "Restaurant")
        var sharedPreference: SharedPreferences? = null
        var defaultColor: Drawable? = null
        var defaultColor2: Drawable? = null
        var selectedPosition = -1

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): ViewHolder {
            val v =
                LayoutInflater.from(parent.context).inflate(R.layout.row_item, parent, false)
            sharedPreference =
                parent.context.getSharedPreferences("PREFERENCE_NAMEasa", Context.MODE_PRIVATE)
            defaultColor = ContextCompat.getDrawable(parent.context, R.drawable.shapee)
            defaultColor2 = ContextCompat.getDrawable(parent.context, R.drawable.shape)

            return ViewHolder(v)
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
            viewHolder.itemTitle.setText(list.get(i))
            if (i == selectedPosition) {
                // Set the background color of the root view to the selected color
                viewHolder.itemView.setBackgroundDrawable(defaultColor!!)
            } else {
                // Set the background color of the root view to the default color
                viewHolder.itemView.setBackgroundDrawable(defaultColor2!!)
            }

        }

        override fun getItemCount(): Int {
            return list.size
        }


        internal inner class ViewHolder(itemView: View) :
            RecyclerView.ViewHolder(itemView) {
            var itemTitle: TextView

            init {
                itemTitle = itemView.findViewById(R.id.tv_row)

                itemView.setOnClickListener { v ->
                    val position = adapterPosition
//                    store = list.get(position)
                    var editor = sharedPreference!!.edit()
                    editor.putString("store", "[" + list.get(position) + "]")
                    editor.commit()
                    selectedPosition = position
                    notifyDataSetChanged()

                }
            }
        }
    }


}