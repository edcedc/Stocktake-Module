package com.yyc.stocktake.weight.loadCallBack


import com.yyc.stocktake.R
import com.kingja.loadsir.callback.Callback


class EmptyCallback : Callback() {

    override fun onCreateView(): Int {
        return R.layout.layout_empty
    }

}