package com.intricatech.slingball;

/**
 * Created by Bolgbolg on 26/04/2017.
 */
public interface OnGameOverDialogFinishedListener {

    enum ActionToTake {
        RESUME_GAME_AT_CURRENT_POINT,
        END_GAME_AND_GO_TO_MAIN_MENU
    }

    void onGameOverDialogFinished(ActionToTake actionToTake, int deltaLives);

}
