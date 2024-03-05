package com.hapataka.questwalk.ui.record

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import com.hapataka.questwalk.databinding.FragmentRecordItemBinding
import com.hapataka.questwalk.ui.record.adapter.HEADER_TYPE
import com.hapataka.questwalk.ui.record.adapter.RecordDetailAdapter
import com.hapataka.questwalk.ui.record.model.RecordItem
import kotlinx.coroutines.launch

const val TAG = "item_test"

class RecordItemFragment(val items: List<RecordItem>) : Fragment() {
    private val binding by lazy { FragmentRecordItemBinding.inflate(layoutInflater) }
    private val recordDetailAdapter by lazy { RecordDetailAdapter(requireContext()) }
    private val gridLayoutManager by lazy { GridLayoutManager(requireContext(), 3) }
    var successAchieveId = mutableListOf<Int>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        initItemClick()
    }


    private fun initRecyclerView() {
        with(binding.rvRecordDetail) {
            adapter = recordDetailAdapter
            setSpanSizeLookup()
            layoutManager = gridLayoutManager
        }
        recordDetailAdapter.submitList(items)
    }

    private fun setSpanSizeLookup() {
        gridLayoutManager.spanSizeLookup = object : SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val viewType = binding.rvRecordDetail.adapter?.getItemViewType(position)

                return when (viewType) {
                    HEADER_TYPE -> gridLayoutManager.spanCount
                    else -> 1
                }
            }
        }
    }

    private fun initItemClick() {
        object : RecordDetailAdapter.ItemClick {
            override fun onClick(item: RecordItem) {
                if (item is RecordItem.AchieveItem) {
                    val dialog = AchieveDialog(item, successAchieveId)

                    dialog.show(parentFragmentManager, "AchieveDialog")
                }
            }
        }.also { recordDetailAdapter.itemClick = it }
    }
}