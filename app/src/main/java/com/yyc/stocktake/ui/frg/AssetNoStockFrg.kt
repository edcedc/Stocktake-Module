package com.yyc.stocktake.ui.frg

import android.os.Bundle
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.StringUtils
import com.yyc.stocktake.R
import com.yyc.stocktake.adapter.AssetAdapter
import com.yyc.stocktake.api.UIHelper
import com.yyc.stocktake.base.BaseFragment
import com.yyc.stocktake.bean.db.AssetBean
import com.yyc.stocktake.databinding.BNotTitleRecyclerBinding
import com.yyc.stocktake.ext.INVENTORY_FAIL
import com.yyc.stocktake.ext.INVENTORY_NOT
import com.yyc.stocktake.ext.init
import com.yyc.stocktake.ext.setNbOnItemClickListener
import com.yyc.stocktake.viewmodel.AssetModel
import com.yyc.stocktake.weight.recyclerview.SpaceItemDecoration
import me.hgj.jetpackmvvm.ext.nav

/**
 * @Author nike
 * @Date 2023/7/27 16:18
 * @Description 不在库
 */
class AssetNoStockFrg: BaseFragment<AssetModel, BNotTitleRecyclerBinding>(){

    private val assetModel: AssetModel by activityViewModels()

    val adapter: AssetAdapter by lazy { AssetAdapter(arrayListOf()) }

    var orderId: String? = null

    var searchText: String? = null

    var fmIsVisible = false

    override fun initView(savedInstanceState: Bundle?) {
        arguments?.let {
            orderId = it.getString("orderId")
        }
        mDatabind.swipeRefresh.isEnabled = false

        //初始化recyclerView
        mDatabind.recyclerView.init(LinearLayoutManager(context), adapter).let {
            it.addItemDecoration(SpaceItemDecoration(ConvertUtils.dp2px(10f), ConvertUtils.dp2px(10f), true))
        }
        adapter.run {
            setNbOnItemClickListener{adapter, view, position ->
                val bean = mFilterList[position]
                UIHelper.startAssetDetailsFrg(nav(), bean)
            }
            setSearchCallback(object :AssetAdapter.SearchCallback{
                override fun onSearchResults(filteredData: ArrayList<AssetBean>) {
//                    assetModel.assetTitle.value = getString(R.string.missing) + "(" + filteredData.size + ")"
                }
            })
        }

        mViewModel.onRequest(orderId, INVENTORY_NOT)

        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                super.onResume(owner)
                fmIsVisible = true
                assetModel.assetTitle.value = getString(R.string.missing) + "(" + adapter.data.size + ")"
            }

            override fun onPause(owner: LifecycleOwner) {
                super.onPause(owner)
                fmIsVisible = false
            }
        })
    }


    override fun createObserver() {
        super.createObserver()
        mViewModel.listBean.observe(viewLifecycleOwner, Observer {
            adapter.setList(it)
            adapter!!.appendList(it)
        })
        //搜索
        assetModel.assetSerch.observe(viewLifecycleOwner, {
            searchText = it
            adapter!!.filter.filter(searchText)
        })
        //识别RFID
        assetModel.epcUploadData.observe(viewLifecycleOwner, {
            if (it == null || it.InventoryStatus == INVENTORY_FAIL)return@observe
            val index = adapter.data.indexOfFirst {bean ->
                (!StringUtils.isEmpty(bean.LabelTag) && !bean.LabelTag.equals("null", ignoreCase = true) && bean.LabelTag.equals(it.LabelTag, ignoreCase = true))
                        || (!StringUtils.isEmpty(bean.AssetNo) && !bean.AssetNo.equals("null", ignoreCase = true) && bean.AssetNo.equals(it.AssetNo, ignoreCase = true))
            }
            if (index != -1){
                adapter.removeAt(index)
            }
            /*if (it.InventoryStatus == INVENTORY_STOCK){
                adapter.removeAt(index)
            }else{
                //添加不在库
                adapter.addData(0, it)
            }*/
            //更新搜索页面item状态
            if (!StringUtils.isEmpty(searchText)){
                adapter!!.filter.filter(searchText)
            }
            if (fmIsVisible)assetModel.assetTitle.value = getString(R.string.missing) + "(" + adapter.data.size + ")"
        })
    }

}