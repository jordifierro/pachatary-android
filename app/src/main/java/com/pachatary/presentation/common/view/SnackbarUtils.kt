package com.pachatary.presentation.common.view

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.design.widget.Snackbar
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import com.pachatary.R
import android.view.ViewGroup



class SnackbarUtils {
    companion object {
        fun show(view: View, message: String, duration: Int = Snackbar.LENGTH_SHORT) {
            Snackbar.make(view, message, duration).show()
        }

        fun showError(view: View, context: Context) {
            val snackbar = Snackbar.make(view,
                                         context.getString(R.string.error_message).toString(),
                                         Snackbar.LENGTH_SHORT)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                snackbar.view.background = context.resources.getDrawable(R.drawable.snackbar_shape)
            }
            val textView = snackbar.view
                    .findViewById(android.support.design.R.id.snackbar_text) as TextView

            val d = context.resources.displayMetrics.density
            textView.setPadding((25*d).toInt(), (-20*d).toInt(), (25*d).toInt(), (-20*d).toInt())
            snackbar.view.setPadding(0, (-20*d).toInt(), 0, (-20*d).toInt())
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14.0f)
            textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_cross, 0, 0, 0);
            textView.compoundDrawablePadding = context.resources
                    .getDimensionPixelOffset(R.dimen.snackbar_icon_padding)
            setMargins(context, snackbar.view, 20, 20, 20, 20)
            snackbar.show()
        }

        private fun setMargins(c: Context, v: View, l: Int, t: Int, r: Int, b: Int) {
            if (v.layoutParams is ViewGroup.MarginLayoutParams) {
                val p = v.layoutParams as ViewGroup.MarginLayoutParams
                val d = c.resources.displayMetrics.density
                p.setMargins((l*d).toInt(), (t*d).toInt(), (r*d).toInt(), (b*d).toInt())
                v.requestLayout()
            }
        }
    }
}