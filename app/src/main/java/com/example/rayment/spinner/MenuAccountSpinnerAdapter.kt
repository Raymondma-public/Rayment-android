package com.example.rayment.spinner

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import com.example.rayment.Application.Companion.ctx
import com.example.rayment.R
import com.example.rayment.model.Account
import kotlinx.android.synthetic.main.account_spinner_item.view.*
import kotlinx.android.synthetic.main.nav_header_main2.view.*

class MenuAccountSpinnerAdapter (ctx: Context, accts: List<Account>): ArrayAdapter<Account>(ctx, 0, accts) {
    override fun getView(position: Int, recycledView: View?, parent: ViewGroup): View {
        return this.createView(position, recycledView, parent)
    }
    override fun getDropDownView(position: Int, recycledView: View?, parent: ViewGroup): View {
        return this.createView(position, recycledView, parent)
    }
    private fun createView(position: Int, recycledView: View?, parent: ViewGroup): View {
        val acct = getItem(position)
        val view = recycledView ?: LayoutInflater.from(context).inflate(
            R.layout.account_spinner_item,
            parent,
            false
        )
        view.emailTV.text =acct?.email
        view.phoneTV.text = acct?.phone
        return view
    }
}