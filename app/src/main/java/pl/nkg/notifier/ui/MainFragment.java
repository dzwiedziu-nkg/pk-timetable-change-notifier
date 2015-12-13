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

import pl.nkg.notifier.R;

public class MainFragment extends Fragment {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    private OnFragmentInteractionListener listener;

    private TextView[] degreeDateTextView = new TextView[2];
    private Button[] goButton = new Button[2];

    private TextView lastCheckedTextView;

    private TextView lastCheckedErrorDateTextView;
    private TextView lastCheckedErrorTextView;

    private LinearLayout lastCheckedErrorFrameLayout;

    private Button checkNowButton;
    private ProgressBar progressBar;

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

        degreeDateTextView[0] = (TextView) view.findViewById(R.id.degreeIDateTextView);
        goButton[0] = (Button) view.findViewById(R.id.goIButton);

        degreeDateTextView[1] = (TextView) view.findViewById(R.id.degreeIIDateTextView);
        goButton[1] = (Button) view.findViewById(R.id.goIIButton);

        lastCheckedTextView = (TextView) view.findViewById(R.id.lastCheckedTextView);

        lastCheckedErrorDateTextView = (TextView) view.findViewById(R.id.lastCheckedErrorDateTextView);

        lastCheckedErrorFrameLayout = (LinearLayout) view.findViewById(R.id.lastCheckedErrorLinearLayout);
        lastCheckedErrorDateTextView = (TextView) view.findViewById(R.id.lastCheckedErrorDateTextView);
        lastCheckedErrorTextView = (TextView) view.findViewById(R.id.lastCheckedErrorTextView);

        checkNowButton = (Button) view.findViewById(R.id.checkNowButton);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        goButton[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hangClickTimetableURL(1);
            }
        });

        goButton[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hangClickTimetableURL(2);
            }
        });

        checkNowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hangClickCheckNow();
            }
        });

        return view;
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

    public void setStageTimetableChanged(int stage, Date date, URL url) {
        int index = stage - 1;
        goButton[index].setVisibility(url != null ? View.VISIBLE : View.GONE);
        if (date == null) {
            degreeDateTextView[index].setText("please clicks \"Check now\"");
        } else {
            degreeDateTextView[index].setText(DATE_FORMAT.format(date));
        }
    }

    public void setLastCheckedDate(Date date) {
        if (date == null) {
            lastCheckedTextView.setText("never, please clicks \"Check now\"");
        } else {
            lastCheckedTextView.setText(DATE_TIME_FORMAT.format(date));
        }
    }

    public void setLastCheckedError(Date date, int type, String error) {
        if (date != null) {
            lastCheckedErrorDateTextView.setText(DATE_TIME_FORMAT.format(date));

            lastCheckedErrorTextView.setText(error);
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
