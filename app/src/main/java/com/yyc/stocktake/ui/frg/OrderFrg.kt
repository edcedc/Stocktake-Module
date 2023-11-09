package com.yyc.stocktake.ui.frg

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import androidx.core.view.GravityCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.ConvertUtils
import com.yyc.stocktake.R
import com.yyc.stocktake.adapter.OrderAdapter
import com.yyc.stocktake.ext.init
import com.yyc.stocktake.ext.loadServiceInit
import com.yyc.stocktake.ext.showLoading
import com.yyc.stocktake.viewmodel.OrderModel
import com.yyc.stocktake.weight.recyclerview.SpaceItemDecoration
import com.google.android.material.navigation.NavigationView
import com.kingja.loadsir.core.LoadService
import com.yyc.stocktake.api.UIHelper
import com.yyc.stocktake.base.BaseFragment
import com.yyc.stocktake.bean.DataBean
import com.yyc.stocktake.bean.db.OrderBean
import com.yyc.stocktake.databinding.FOrderBinding
import com.yyc.stocktake.ext.ORDER_TYPE
import com.yyc.stocktake.ext.setNbOnItemClickListener
import com.yyc.stocktake.mar.eventViewModel
import com.yyc.stocktake.util.CacheUtil
import com.yyc.stocktake.viewmodel.RfidModel
import me.hgj.jetpackmvvm.ext.nav

/**
 * @Author nike
 * @Date 2023/7/7 11:59
 * @Description
 */
class OrderFrg : BaseFragment<OrderModel, FOrderBinding>(), NavigationView.OnNavigationItemSelectedListener {

    val adapter: OrderAdapter by lazy { OrderAdapter(arrayListOf()) }

    private val rfidModel: RfidModel by activityViewModels()

    lateinit var loadsir: LoadService<Any>

    override fun initView(savedInstanceState: Bundle?) {
        mDatabind.viewmodel = mViewModel
        mDatabind.navView.setNavigationItemSelectedListener(this)
//        hideSoftKeyboard(activity)

        mDatabind.includeToolbar.toolbar.run {
            init(getString(R.string.stock_take))
            inflateMenu(R.menu.home_menu)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.home_set -> {
                        onOpenDrawer()
                    }
                }
                true
            }
        }

        //状态页配置
        loadsir = loadServiceInit(mDatabind.swipeRefresh) {

        }

        //初始化recyclerView
        mDatabind.recyclerView.init(LinearLayoutManager(context), adapter).let {
            it.addItemDecoration(SpaceItemDecoration(ConvertUtils.dp2px(10f), ConvertUtils.dp2px(10f), true))
        }
        adapter.run {
            setNbOnItemClickListener{adapter, view, position ->
                val bean = adapter.data[position] as OrderBean
                UIHelper.startAssetFrg(nav(), bean.stocktakeno)
            }
        }

        mDatabind.swipeRefresh.isEnabled = false
        mDatabind.includeSearch.etText.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                adapter!!.filter.filter(p0.toString())
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })

        mDatabind.includeSearch.ivQr.setOnClickListener {
            UIHelper.startZxingAct(ORDER_TYPE)
        }

        /*lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                super.onResume(owner)
                rfidModel.isShowToolbarData.value = false
            }

            override fun onPause(owner: LifecycleOwner) {
                super.onPause(owner)
                rfidModel.isShowToolbarData.value = true
            }
        })*/
    }

    override fun createObserver() {
        super.createObserver()
        mViewModel.listBean.observe(viewLifecycleOwner, Observer {
            loadsir.showSuccess()
            adapter.setList(it)
            adapter!!.appendList(it)
        })
        eventViewModel.mainListEvent.observeInFragment(this, Observer {
            mViewModel.onRequest()
        })
        eventViewModel.zkingType.observeInFragment(this, Observer {
            if (it.type == ORDER_TYPE){
                 val filteredList = adapter.data.filterIndexed()  { index, bean ->
                     it.text.equals(bean.stocktakeno)
                 }
                if (filteredList.size != 0){
                    UIHelper.startAssetFrg(nav(), filteredList.get(0).stocktakeno)
                }
            }
        })
    }

    override fun lazyLoadData() {
        super.lazyLoadData()
        //设置界面 加载中
        loadsir.showLoading()
        mViewModel.onRequest()
    }

    override fun onDestroy() {
        super.onDestroy()
        mActivity.setSupportActionBar(null)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        mDatabind.drawerLayout.postDelayed({
            when (item.itemId) {
                R.id.nav_load -> {
                    UIHelper.startDownloadFrg(nav())
                }

                R.id.nav_upload -> {
                    UIHelper.starUploadFrg(nav())
                }

               /* R.id.nav_external_borrow -> {
                    UIHelper.starExternalBaorrowFrg(nav())
                }

                R.id.nav_internal_borrow -> {
                    UIHelper.starInternalBaorrowFrg(nav())
                }

                R.id.nav_disposal -> {
                    UIHelper.starDisposalFrg(nav())
                }
*/
                R.id.nav_login -> {
                    val user = CacheUtil.getUser()
                    user?.Password = null
                    CacheUtil.setUser(user)
                    UIHelper.startLoginAct()
                    ActivityUtils.finishAllActivities()
                }
            }
            mDatabind.drawerLayout.closeDrawer(GravityCompat.END)
        }, 300)
        return true
    }

    //region  抽屉布局
    fun onOpenDrawer() {
        if (!mDatabind.drawerLayout.isDrawerOpen(GravityCompat.END)) {
            mDatabind.drawerLayout.openDrawer(GravityCompat.END)
        }
    }

}