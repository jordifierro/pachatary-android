package com.pachatary.presentation.common.view

import android.annotation.SuppressLint
import android.content.Context
import android.support.design.internal.BottomNavigationItemView
import android.support.design.internal.BottomNavigationMenuView
import android.support.design.widget.BottomNavigationView
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout


class UnlabeledBottomNavigationView(context: Context, attrs: AttributeSet)
                                                            : BottomNavigationView(context, attrs) {

    private val bottomMenuView: BottomNavigationMenuView?
        get() {
            var menuView: Any? = null
            try {
                val field = BottomNavigationView::class.java.getDeclaredField("mMenuView")
                field.isAccessible = true
                menuView = field.get(this)
            }
            catch (e: NoSuchFieldException) { e.printStackTrace() }
            catch (e: IllegalAccessException) { e.printStackTrace() }

            return menuView as BottomNavigationMenuView?
        }

    init {
        removeShiftMode()
        centerMenuIcon()
    }

    @SuppressLint("RestrictedApi")
    private fun centerMenuIcon() {
        val menuView = bottomMenuView

        if (menuView != null) {
            for (i in 0 until menuView.childCount) {
                val menuItemView = menuView.getChildAt(i) as BottomNavigationItemView

                val icon = menuItemView.getChildAt(0) as AppCompatImageView

                val params = icon.layoutParams as FrameLayout.LayoutParams
                params.gravity = Gravity.CENTER

                //menuItemView.setShiftingMode(true)
            }
        }
    }

    @SuppressLint("RestrictedApi")
    private fun removeShiftMode() {
        val menuView = bottomMenuView
        try {
            val shiftingMode = BottomNavigationView::class.java.getDeclaredField("mShiftingMode")
            shiftingMode.setAccessible(true)
            shiftingMode.setBoolean(menuView, false)
            shiftingMode.setAccessible(false)
            for (i in 0 until menuView!!.getChildCount()) {
                val item = menuView.getChildAt(i) as BottomNavigationItemView
                item.setShiftingMode(false)
                // set once again checked value, so view will be updated
                item.setChecked(item.itemData.isChecked)
            }
        }
        catch (e: NoSuchFieldException) { e.printStackTrace() }
        catch (e: IllegalAccessException) { e.printStackTrace() }
    }
}