package pl.nkg.notifier.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import pl.nkg.notifier.R;

public class MainFragment extends Fragment {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    private OnFragmentInteractionListener listener;
    private Unbinder unbinder;

    @BindView(R.id.degreeIDateTextView) TextView degreeIDateTextView;
    @BindView(R.id.degreeIIDateTextView) TextView degreeIIDateTextView;
    private TextView[] degreeDateTextView;

    @BindView(R.id.goIButton) Button goIButton;
    @BindView(R.id.goIIButton) Button goIIButton;
    private Button[] goButton;

    @BindView(R.id.lastCheckedTextView) TextView lastCheckedTextView;

    @BindView(R.id.lastCheckedErrorDateTextView) TextView lastCheckedErrorDateTextView;
    @BindView(R.id.lastCheckedErrorTextView) TextView lastCheckedErrorTextView;

    @BindView(R.id.lastCheckedErrorLinearLayout) LinearLayout lastCheckedErrorFrameLayout;

    @BindView(R.id.checkNowButton) Button checkNowButton;
    @BindView(R.id.progressBar) ProgressBar progressBar;

    public static MainFragment newInstance() {
        return new MainFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        unbinder = ButterKnife.bind(this, view);
        degreeDateTextView = new TextView[] {degreeIDateTextView, degreeIIDateTextView};
        goButton = new Button[] {goIButton, goIIButton};
        return view;
    }

    @OnClick(R.id.goIButton)
    public void onClickGoIButton(View view) {
        hangClickTimetableURL(1);
    }

    @OnClick(R.id.goIIButton)
    public void onClickGoIIButton(View view) {
        hangClickTimetableURL(2);
    }

    @OnClick(R.id.checkNowButton)
    public void onClickCheckNowButton(View view) {
        hangClickCheckNow();
    }

    public void hangClickCheckNow() {
        if (listener != null) {
            listener.onClickCheckNow();
        }
    }

    private void hangClickTimetableURL(int stage) {
        if (listener != null) {
            listener.onClickTimetableURL(stage);
        }
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        try {
            listener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        goButton = null;
        degreeDateTextView = null;
    }

    public void setStageTimetableChanged(int stage, Date date, URL url) {
        int index = stage - 1;
        goButton[index].setVisibility(url != null ? View.VISIBLE : View.GONE);
        if (date == null) {
            degreeDateTextView[index].setText(R.string.label_empty_degree_date);
        } else {
            degreeDateTextView[index].setText(DATE_FORMAT.format(date));
        }
    }

    public void setLastCheckedDate(Date date) {
        if (date == null) {
            lastCheckedTextView.setText(R.string.label_empty_checked_date);
        } else {
            lastCheckedTextView.setText(DATE_TIME_FORMAT.format(date));
        }
    }

    public void setLastCheckedError(Date date, int type, String error) {
        if (date != null && type != 0) {
            lastCheckedErrorDateTextView.setText(DATE_TIME_FORMAT.format(date));
            lastCheckedErrorTextView.setText(getResources().getStringArray(R.array.error_type_array)[type - 1] + "\n\n" + error);
        }
        lastCheckedErrorFrameLayout.setVisibility(date == null ? View.GONE : View.VISIBLE);
    }

    public void setPendingStatus(boolean pending) {
        checkNowButton.setVisibility(pending ? View.GONE : View.VISIBLE);
        progressBar.setVisibility(pending ? View.VISIBLE : View.GONE);
    }

    public interface OnFragmentInteractionListener {
        void onClickCheckNow();

        void onClickTimetableURL(int stage);
    }
}
