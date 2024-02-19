package com.yyc.stocktake.ui.frg

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.TimeUtils
import com.kingja.loadsir.core.LoadService
import com.yyc.stocktake.R
import com.yyc.stocktake.api.UIHelper
import com.yyc.stocktake.base.BaseFragment
import com.yyc.stocktake.bean.AppRoomDataBase
import com.yyc.stocktake.bean.RfidStateBean
import com.yyc.stocktake.bean.dao.AssetDao
import com.yyc.stocktake.bean.db.AssetBean
import com.yyc.stocktake.databinding.FAssetBinding
import com.yyc.stocktake.ext.ASSET_TYPE
import com.yyc.stocktake.ext.INVENTORY_FAIL
import com.yyc.stocktake.ext.INVENTORY_STOCK
import com.yyc.stocktake.ext.SCAN_STATUS_QRCODE
import com.yyc.stocktake.ext.bindViewPager2
import com.yyc.stocktake.ext.init
import com.yyc.stocktake.ext.initClose
import com.yyc.stocktake.ext.loadServiceInit
import com.yyc.stocktake.mar.eventViewModel
import com.yyc.stocktake.util.CacheUtil
import com.yyc.stocktake.util.MusicUtils
import com.yyc.stocktake.viewmodel.AssetModel
import com.yyc.stocktake.viewmodel.RfidModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import me.hgj.jetpackmvvm.ext.nav
import java.util.Locale

/**
 * @Author nike
 * @Date 2023/7/27 11:43
 * @Description RFID 列表
 */
class AssetFrg: BaseFragment<AssetModel, FAssetBinding>() {

    private val assetModel: AssetModel by activityViewModels()

    private val rfidModel: RfidModel by activityViewModels()

    var orderId: String? = null

    var isSta = false

    lateinit var loadsir: LoadService<Any>

    val assetDao = AppRoomDataBase.get().getAssetDao()
    val roNo = CacheUtil.getUser()!!.RoNo
    val companyID = CacheUtil.getCompanyID()

    //fragment集合
    var fragments: ArrayList<Fragment> = arrayListOf()

    override fun initView(savedInstanceState: Bundle?) {
        arguments?.let {
            orderId = it.getString("orderId")
        }
        mDatabind.includeToolbar.toolbar.initClose(getString(R.string.full_list) + "(0)") {nav().navigateUp()}
        mDatabind.viewmodel = mViewModel
        mDatabind.click = ProxyClick()
        mViewModel.state.set(getString(R.string.start))
        mViewModel.save.set(getString(R.string.save))

        //状态页配置
        loadsir = loadServiceInit(mDatabind.layout) {

        }


        //初始化viewpager2
        var bundle = Bundle()
        bundle.putString("orderId", orderId)


        val assetAllFrg = AssetAllFrg()
        assetAllFrg.arguments = bundle
        fragments.add(assetAllFrg)

        val assetInStockFrg = AssetInStockFrg()
        assetInStockFrg.arguments = bundle
        fragments.add(assetInStockFrg)

        val assetNoStockFrg = AssetNoStockFrg()
        assetNoStockFrg.arguments = bundle
        fragments.add(assetNoStockFrg)

        val assetFailFrg = AssetFailFrg()
        assetFailFrg.arguments = bundle
        fragments.add(assetFailFrg)

        mDatabind.includeViewpager.viewPager.init(this, fragments)
        var mTitles =
            arrayListOf(
            getString(R.string.full_list),
            getString(R.string.found),
            getString(R.string.missing),
            getString(R.string.abnormal)
        )

        mDatabind.includeViewpager.magicIndicator.bindViewPager2(mDatabind.includeViewpager.viewPager, mTitles)
        mDatabind.includeViewpager.viewPager.offscreenPageLimit = mTitles.size

        lifecycle.addObserver(object : DefaultLifecycleObserver {

            override fun onDestroy(owner: LifecycleOwner) {
                super.onDestroy(owner)
                rfidModel.isOpen.value = false
                assetModel.epcData.value = null
                assetModel.epcUploadData.value = null
                mViewModel.state.equals(getString(R.string.start))
                MusicUtils.clear()
            }

            override fun onPause(owner: LifecycleOwner) {
                super.onPause(owner)
                rfidModel.isOpen.value = false
                mViewModel.state.equals(getString(R.string.start))
                isSta = false
            }
        })

        mDatabind.includeSearch.ivQr.setOnClickListener {
            UIHelper.startZxingAct(ASSET_TYPE)
        }

        mDatabind.includeSearch.etText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                assetModel.assetSerch.value = p0.toString()
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })

    }

    override fun lazyLoadData() {
        //设置界面 加载中
//        loadsir.showLoading()
    }

    override fun createObserver() {
        super.createObserver()
        assetModel.assetTitle.observe(viewLifecycleOwner, {
            mDatabind.includeToolbar.toolbar.initClose(it) {nav().navigateUp()}
        })

        assetModel.epcData.observe(viewLifecycleOwner, {
            if (it == null)return@observe
            val tagId = it.tagId
            mViewModel.viewModelScope.launch(Dispatchers.IO) {
                //先查找epc
                var assetBean = assetDao.findLabelTagId(tagId, orderId, roNo, companyID)
                if (assetBean != null){
                    setAssetBean(assetBean, assetDao, it)
                }else{
                    //再查找 assetNo
                    val assetBean = assetDao.findAssetId(tagId, orderId, roNo, companyID)
                    if (assetBean != null){
                        setAssetBean(assetBean, assetDao, it)
                    }else{
                        //全部没有归异常
                        setAssetFailBean(assetDao, it, roNo, companyID)
                    }
                }
            }
        })

        //扫码
        eventViewModel.zkingType.observeInFragment(this, Observer {
            if (it.type == ASSET_TYPE){
                assetModel.epcData.value = RfidStateBean(
                    tagId = it.text,
                    scanStatus = SCAN_STATUS_QRCODE,
                    rssi = "0"
                )
            }
        })

        mViewModel.isShowDialog.observe(viewLifecycleOwner, {
            if (it){
                showLoading(getString(R.string.text1))
            }else{
                dismissLoading()
            }
        })

    }


    private suspend fun setAssetFailBean(assetDao: AssetDao, it: RfidStateBean, roNo: String, companyID: String) {
        var bean = assetDao.findFailLabelTagId(it.tagId, orderId, roNo, companyID)
        if (bean != null)return
        bean = AssetBean()
        bean.OrderRoNo = orderId!!
        bean.InventoryStatus = INVENTORY_FAIL
        bean.scanTime = TimeUtils.getNowString()
        bean.scanStatus = it.scanStatus
        bean.LabelTag = it.tagId.toString()
        bean.RoNo = roNo
        bean.companyId = companyID
        assetDao.add(bean)
        MusicUtils.play()
        withContext(Dispatchers.Main) {
            assetModel.epcUploadData.value = bean
        }
    }

    private suspend fun setAssetBean(bean: AssetBean, assetDao: AssetDao, it: RfidStateBean){
        if (bean.InventoryStatus == INVENTORY_STOCK)return
        bean.scanTime = TimeUtils.getNowString()
        bean.scanStatus = it.scanStatus
        if (!bean.AssetNo.equals(it.tagId, ignoreCase = true)){
            bean.LabelTag = it.tagId.toString()
        }
        bean.InventoryStatus = INVENTORY_STOCK
        assetDao.update(bean)
        MusicUtils.play()
        withContext(Dispatchers.Main) {
            assetModel.epcUploadData.value = bean
        }
    }

    inner class ProxyClick() {

         fun state(){
            if (isSta == false){
                rfidModel.isOpen.value = true
                MusicUtils.init(activity)
            }else{
                rfidModel.isOpen.value = false
                MusicUtils.clear()
            }
            mViewModel.state.set(if (isSta == true) getString(R.string.start) else getString(R.string.stop))
            isSta = !isSta
        }

        fun save(){
            if (isSta){
                rfidModel.isOpen.value = false
                MusicUtils.clear()
                mViewModel.state.set(getString(R.string.start))
                isSta = !isSta
            }

            mViewModel.onUpload(orderId, this@AssetFrg)
        }
    }

}