package ai.hnu.kr.viewpager

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

private const val ARG_BG = "arg_bg"
private const val ARG_TEXT = "arg_text"

class PageFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_page, container, false)
        val text = view.findViewById<TextView>(R.id.page_text)
        val bg = arguments?.getString(ARG_BG) ?: "#333333"
        val txt = arguments?.getString(ARG_TEXT) ?: "Page"
        try {
            view.setBackgroundColor(Color.parseColor(bg))
        } catch (e: Exception) {
            view.setBackgroundColor(Color.DKGRAY)
        }
        text.text = txt
        return view
    }

    companion object {
        fun newInstance(bgColor: String, text: String): PageFragment {
            val f = PageFragment()
            f.arguments = Bundle().apply {
                putString(ARG_BG, bgColor)
                putString(ARG_TEXT, text)
            }
            return f
        }
    }
}