package com.yyc.stocktake.ui.frg

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.ConvertUtils
import com.kingja.loadsir.core.LoadService
import com.yyc.stocktake.R
import com.yyc.stocktake.adapter.DisposalAdapter
import com.yyc.stocktake.api.UIHelper
import com.yyc.stocktake.base.BaseFragment
import com.yyc.stocktake.bean.DataBean
import com.yyc.stocktake.databinding.FDisposalBinding
import com.yyc.stocktake.ext.DISPOSAL_TYPE
import com.yyc.stocktake.ext.init
import com.yyc.stocktake.ext.initClose
import com.yyc.stocktake.ext.loadListData
import com.yyc.stocktake.ext.loadServiceInit
import com.yyc.stocktake.ext.setNbOnItemClickListener
import com.yyc.stocktake.ext.showLoading
import com.yyc.stocktake.mar.eventViewModel
import com.yyc.stocktake.viewmodel.DisposalModel
import com.yyc.stocktake.weight.recyclerview.SpaceItemDecoration
import me.hgj.jetpackmvvm.ext.nav

/**
 * @Author nike
 * @Date 2023/8/8 14:46
 * @Description 外部借出
 */
class DisposalFrg: BaseFragment<DisposalModel, FDisposalBinding>() {

    val adapter: DisposalAdapter by lazy { DisposalAdapter(arrayListOf()) }

    //界面状态管理者
    lateinit var loadsir: LoadService<Any>

    override fun initView(savedInstanceState: Bundle?) {
        mDatabind.includeToolbar.toolbar.initClose(getString(R.string.disposal)) {nav().navigateUp()}

        //初始化recyclerView
        mDatabind.recyclerView.init(LinearLayoutManager(context), adapter).let {
            it.addItemDecoration(SpaceItemDecoration(ConvertUtils.dp2px(10f), ConvertUtils.dp2px(10f), true))
        }

        adapter.run {
            setNbOnItemClickListener{adapter, view, position ->
                val bean = adapter.data[position] as DataBean

                UIHelper.starDisposalFrg2(nav(), bean.OrderNo, bean.Title!!)
            }
        }

        //状态页配置
        loadsir = loadServiceInit(mDatabind.swipeRefresh) {
            //点击重试时触发的操作
            loadsir.showLoading()
            mViewModel.onRequest()
        }

        //初始化 SwipeRefreshLayout  刷新
        mDatabind.swipeRefresh.init {
            mViewModel.onRequest()
        }

        mDatabind.includeSearch.etText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                adapter!!.filter.filter(p0.toString())
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })

        mDatabind.includeSearch.ivQr.setOnClickListener {
            UIHelper.startZxingAct(DISPOSAL_TYPE)
        }
    }

    override fun createObserver() {
        super.createObserver()
        mViewModel.listData.observe(viewLifecycleOwner, {
            loadListData(it, adapter, loadsir, mDatabind.recyclerView, mDatabind.swipeRefresh, it.pageSize)
            adapter.appendList(it.listData)
        })
        //扫码
        eventViewModel.zkingType.observeInFragment(this, Observer {
            if (it.type == DISPOSAL_TYPE){
                val filteredList = adapter.data.filterIndexed()  { index, bean ->
                    it.text.equals(bean.OrderNo)
                }
                if (filteredList.size != 0){
                    UIHelper.starDisposalFrg2(nav(), filteredList.get(0).OrderNo, filteredList.get(0).Title!!)
                }
            }
        })
    }

    override fun lazyLoadData() {
        //设置界面 加载中
        loadsir.showLoading()
        mViewModel.onRequest()
    }

}