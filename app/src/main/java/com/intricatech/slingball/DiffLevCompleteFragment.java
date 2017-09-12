package com.intricatech.slingball;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Bolgbolg on 23/08/2017.
 */
public class DiffLevCompleteFragment extends Fragment {

    private static String TAG;

    private TextView messageTextView;
    private TextView hardLevUnlockedTV;
    private String messageString;
    private String messageBegin;
    private String messageEnd;
    private String diffLevString;
    private boolean displayHardUnlockedTV;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TAG = getClass().getSimpleName();
        messageBegin = getResources().getString(R.string.you_have_completed);
        messageEnd = getResources().getString(R.string.difficulty_level);
        diffLevString = getArguments().getString("DIFFICULTY_LEVEL");
        if (diffLevString.equals(DifficultyLevel.NORMAL.toString())) {
            displayHardUnlockedTV = true;
        } else {
            displayHardUnlockedTV = false;
        }
        messageString = messageBegin + " " + diffLevString + " " + messageEnd;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.diff_lev_complete_fragment, container, false);
        messageTextView = (TextView) view.findViewById(R.id.level_specific_textview);
        messageTextView.setText(messageString);
        hardLevUnlockedTV = (TextView) view.findViewById(R.id.hard_level_unlocked_message);
        hardLevUnlockedTV.setVisibility(displayHardUnlockedTV ? View.VISIBLE : View.GONE);
        return view;

    }


}
