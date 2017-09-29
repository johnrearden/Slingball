package com.intricatech.slingball;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class InstructionActivity extends AppCompatActivity {

    private boolean proceedToGame;
    private boolean resumeLastGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instruction_2);

        Intent intent = getIntent();
        proceedToGame = intent.getBooleanExtra("PROCEED_TO_GAME", false);
        resumeLastGame = intent.getBooleanExtra("RESUME_LAST_GAME", false);
    }

    @Override
    public void onBackPressed() {
        if (proceedToGame) {
            startGameDirectFromThisActivity();
        } else {
            super.onBackPressed();
        }
    }

    private void startGameDirectFromThisActivity() {
        Intent intent = new Intent(this, GameActivity.class);

        intent.putExtra("RESUME_LAST_GAME", resumeLastGame);
        startActivity(intent);
    }

}
