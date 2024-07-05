package jp.kthrlab.jamsketch

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.leanback.app.OnboardingSupportFragment

class JamSketchOnboardingFragment : OnboardingSupportFragment() {
//    private var myActivity: FragmentActivity? = null
    override fun getPageCount(): Int {
        return 6
    }

    override fun getPageTitle(i: Int): CharSequence? {
        val textViewTitle = requireView().findViewById<TextView>(androidx.leanback.R.id.title)
        return when (i) {
            0 -> {

                textViewTitle.setBackgroundColor(Color.TRANSPARENT)
                resources.getText(R.string.onboard_welcom)
            }
            1 -> {
                textViewTitle.setBackgroundColor(Color.WHITE)
                val height = textViewTitle.height
                textViewTitle.top = textViewTitle.top - height
                textViewTitle.bottom = textViewTitle.bottom - height
                resources.getText(R.string.onboard_select_midiout_title)
            }
            2 -> {
                textViewTitle.setBackgroundColor(Color.TRANSPARENT)
                resources.getText(R.string.onboard_start_app)
            }
            3 -> resources.getText(R.string.onboard_draw_curve_title)
            4 -> resources.getText(R.string.onboard_eval_app)
            5 -> resources.getText(R.string.onboard_send_melody_title)
            else -> null
        }
    }

    override fun onPageChanged(newPage: Int, previousPage: Int) {
        super.onPageChanged(newPage, previousPage)
        val imageView = ImageView(context)
        val viewGroup = requireView().findViewById<ViewGroup>(androidx.leanback.R.id.background_container)
        when (newPage) {
            0, 5 -> imageView.setImageResource(R.drawable.onboarding_jamsketch_plane)
            1 -> imageView.setImageResource(R.drawable.onboarding_select_midi_out_device)
            2 -> imageView.setImageResource(R.drawable.onboarding_start_jamsketch)
            3 -> imageView.setImageResource(R.drawable.onboarding_draw_curves)
            4 -> imageView.setImageResource(R.drawable.onboarding_evaluate)
        }
        viewGroup.getChildAt(viewGroup.childCount - 1).foreground = imageView.drawable
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        titleViewTextColor = Color.BLACK
        descriptionViewTextColor = Color.BLACK
    }

//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        val view = super.onCreateView(inflater, container, savedInstanceState)
//
//        titleViewTextColor = Color.BLACK
//        descriptionViewTextColor = Color.BLACK
//        return view
//    }

    override fun getPageDescription(i: Int): CharSequence? {
        val textViewDescription = requireView().findViewById<TextView>(androidx.leanback.R.id.description)
        return when (i) {
            0, 2, 4 -> {
                textViewDescription.setBackgroundColor(Color.TRANSPARENT)
                null
            }
            1 -> {
                val height = textViewDescription.height
                println("height $height")
                textViewDescription.top = textViewDescription.top - height
                textViewDescription.bottom = textViewDescription.bottom - height
                textViewDescription.setBackgroundColor(Color.WHITE)
                textViewDescription.gravity = Gravity.CENTER
                //                    textViewDescription.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                resources.getText(R.string.onboard_select_midiout_description)
            }
            3 -> resources.getText(R.string.onboard_draw_curve_description)
            5 -> {
                textViewDescription.bottom = textViewDescription.bottom * 2
                //                    textViewDescription.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                resources.getText(R.string.onboard_send_melody_description)
            }
            else -> null
        }
    }

    override fun onCreateBackgroundView(
        layoutInflater: LayoutInflater,
        viewGroup: ViewGroup
    ): View? {
        val imageView = ImageView(context)
        imageView.setImageResource(R.drawable.onboarding_jamsketch)
        return imageView
    }

    override fun onCreateContentView(layoutInflater: LayoutInflater, viewGroup: ViewGroup): View? {
        return null
    }

    override fun onCreateForegroundView(
        layoutInflater: LayoutInflater,
        viewGroup: ViewGroup
    ): View? {
        return null
    }

    override fun onProvideTheme(): Int {
        return androidx.leanback.R.style.Theme_Leanback_Onboarding
    }

    override fun onFinishFragment() {
        (requireActivity() as JamSketchActivity?)!!.putSharedPreferencesBoolean(
            SHOW_ONBOARDING_PREF_NAME,
            false
        )
//        requireActivity().supportFragmentManager
//            .beginTransaction()
//            .commit()
        (requireActivity() as JamSketchActivity?)!!.startJamSketch()
    }

//    fun setView(view: View, activity: FragmentActivity) {
//        this.myActivity = activity
//        activity.supportFragmentManager
//            .beginTransaction()
//            .add(view.id, this)
//            .commit()
//    }

    companion object {
        private const val TAG = "JamSketchOnboardingFragment"
        const val SHOW_ONBOARDING_PREF_NAME = "SHOW_ONBOARDING"
    }
}