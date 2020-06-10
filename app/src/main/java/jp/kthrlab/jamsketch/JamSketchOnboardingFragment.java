package jp.kthrlab.jamsketch;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v17.leanback.app.OnboardingSupportFragment;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class JamSketchOnboardingFragment extends OnboardingSupportFragment {
    private static final String TAG = "JamSketchOnboardingFragment";
    static final String SHOW_ONBOARDING_PREF_NAME = "SHOW_ONBOARDING";
    private FragmentActivity activity = null;

    @Override
    protected int getPageCount() {
        return 6;
    }

    @Override
    protected CharSequence getPageTitle(int i) {
        TextView textViewTitle = getView().findViewById(R.id.title);
        switch (i) {
            case 0: textViewTitle.setBackgroundColor(Color.TRANSPARENT);
                    return getResources().getText(R.string.onboard_welcom);
            case 1: textViewTitle.setBackgroundColor(Color.WHITE);
                    int height = textViewTitle.getHeight();
                    textViewTitle.setTop(textViewTitle.getTop() - height);
                    textViewTitle.setBottom(textViewTitle.getBottom() - height);
                    return getResources().getText(R.string.onboard_select_midiout_title);
            case 2: textViewTitle.setBackgroundColor(Color.TRANSPARENT);
                    return getResources().getText(R.string.onboard_start_app);
            case 3: return getResources().getText(R.string.onboard_draw_curve_title);
            case 4: return getResources().getText(R.string.onboard_eval_app);
            case 5: return getResources().getText(R.string.onboard_send_melody_title);
            default: return null;
        }
    }

    @Override
    protected void onPageChanged(int newPage, int previousPage) {
        super.onPageChanged(newPage, previousPage);
        ImageView imageView = new ImageView(getContext());
        ViewGroup viewGroup = getView().findViewById(R.id.background_container);

        switch (newPage) {
            case 0:
            case 5:
                imageView.setImageResource(R.drawable.onboarding_jamsketch_plane);
                    break;
            case 1: imageView.setImageResource(R.drawable.onboarding_select_midi_out_device);
                    break;
            case 2: imageView.setImageResource(R.drawable.onboarding_start_jamsketch);
                    break;
            case 3: imageView.setImageResource(R.drawable.onboarding_draw_curves);
                    break;
            case 4: imageView.setImageResource(R.drawable.onboarding_evaluate);
                    break;
        }
        viewGroup.getChildAt(viewGroup.getChildCount()-1).setForeground(imageView.getDrawable());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setTitleViewTextColor(Color.BLACK);
        setDescriptionViewTextColor(Color.BLACK);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected CharSequence getPageDescription(int i) {
        TextView textViewDescription = getView().findViewById(R.id.description);
        switch (i) {
            case 0:
            case 2:
            case 4:
                    textViewDescription.setBackgroundColor(Color.TRANSPARENT);
                    return null;
            case 1: int height = textViewDescription.getHeight();
                    System.out.println("height " + height);
                    textViewDescription.setTop(textViewDescription.getTop() - height);
                    textViewDescription.setBottom(textViewDescription.getBottom() - height);
                    textViewDescription.setBackgroundColor(Color.WHITE);
                    textViewDescription.setGravity(Gravity.CENTER);
//                    textViewDescription.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                    return getResources().getText(R.string.onboard_select_midiout_description);
            case 3: //textViewDescription.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                    return getResources().getText(R.string.onboard_draw_curve_description);
            case 5: textViewDescription.setBottom(textViewDescription.getBottom() * 2);
//                    textViewDescription.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                    return getResources().getText(R.string.onboard_send_melody_description);
            default: return null;
        }
    }

    @Nullable
    @Override
    protected View onCreateBackgroundView(LayoutInflater layoutInflater, ViewGroup viewGroup) {
        ImageView imageView = new ImageView(getContext());
        imageView.setImageResource(R.drawable.onboarding_jamsketch);
        return imageView;
    }

    @Nullable
    @Override
    protected View onCreateContentView(LayoutInflater layoutInflater, ViewGroup viewGroup) {
            return null;
    }

    @Nullable
    @Override
    protected View onCreateForegroundView(LayoutInflater layoutInflater, ViewGroup viewGroup) {
        return null;
    }

    @Override
    public int onProvideTheme() {
        return R.style.Theme_Leanback_Onboarding;
    }

    @Override
    protected void onFinishFragment() {
        ((JamSketchActivity)activity).putSharedPreferencesBoolean(JamSketchOnboardingFragment.SHOW_ONBOARDING_PREF_NAME, false);
        activity.getSupportFragmentManager()
                .beginTransaction()
                .commit();
        ((JamSketchActivity)activity).startJamSketch();
    }

    public void setView(View view, FragmentActivity activity) {
        this.activity = activity;
        activity.getSupportFragmentManager()
            .beginTransaction()
            .add(view.getId(), this)
            .commit();
    }

}
