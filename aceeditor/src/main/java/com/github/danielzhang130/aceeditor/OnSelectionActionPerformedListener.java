package com.github.danielzhang130.aceeditor;

/**
 * Created by susmit on 26/1/18.
 */

public interface OnSelectionActionPerformedListener {

    void onSelectionFinished(boolean usingSelectAllOption);
    void onCut();
    void onCopy();
    void onPaste();
    void onUndo();
    void onRedo();

}
