package com.github.danielzhang130.aceeditor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.SystemClock;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.PopupWindow;
import android.widget.Toast;

public class AceEditor extends WebView
{
    Context context;
    private PopupWindow pw;
    private View popupView;
    private LayoutInflater inflater;

    private ResultReceivedListener received;
    private OnLoadedEditorListener onLoadedEditorListener;
    private OnSelectionActionPerformedListener onSelectionActionPerformedListener;
    private TextWatcher onTextChangeListener;

    private float x;
    private float y;
    private boolean actAfterSelect;
    private int requestedValue;
    private String findString;

    private boolean loadedUI;

    private OnTouchListener scroller;
    private OnTouchListener selector;

    public static int ACTION_SCROLL=1;
    public static int ACTION_SELECT=0;

    @SuppressLint("SetJavaScriptEnabled")
    public AceEditor(Context context)
    {
        super(context);
        loadedUI = false;
        this.context = context;
        initialize();
    }


    @SuppressLint("SetJavaScriptEnabled")
    public AceEditor(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        loadedUI = false;
        this.context = context;
        initialize();
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        return new BaseInputConnection(this, false); //this is needed for #dispatchKeyEvent() to be notified.
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return super.dispatchKeyEvent(event);

    }
    @SuppressLint("SetJavaScriptEnabled")
    private void initialize()
    {
        inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );

        actAfterSelect = true;
        initPopup();

        setResultReceivedListener(new ResultReceivedListener() {
            @Override
            public void onReceived(int FLAG_VALUE, String... results) {

            }
        });

        setOnLoadedEditorListener(new OnLoadedEditorListener() {
            @Override
            public void onCreate() {

            }
        });

        setOnSelectionActionPerformedListener(new OnSelectionActionPerformedListener() {
            @Override
            public void onSelectionFinished(boolean usingSelectAllOption) {

            }

            @Override
            public void onCut() {

            }

            @Override
            public void onCopy() {

            }

            @Override
            public void onPaste() {

            }

            @Override
            public void onUndo() {

            }

            @Override
            public void onRedo() {

            }
        });

        setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                result.confirm();
                onTextChangeListener.onTextChanged(message, 0, 0, 0);
                return true;
            }
        });

        setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                if(!loadedUI)
                {
                    loadedUI = true;
                    onLoadedEditorListener.onCreate();
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }
        });


        selector=new View.OnTouchListener()
        {
            float downTime;
            int xtimes;
            int ytimes;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        downTime = event.getEventTime();
                        x=event.getX();
                        y=event.getY();
                        break;
                    case MotionEvent.ACTION_UP:
                        float tot = SystemClock.uptimeMillis() - downTime;
                        x = event.getX();
                        y = event.getY();
                        if(tot <= 500)
                            v.performClick();
                        else {
                            if (actAfterSelect)
                                pw.showAtLocation(v, Gravity.NO_GRAVITY, (int) x - getResources().getDisplayMetrics().widthPixels / 3, getResources().getDisplayMetrics().heightPixels / 12 + (int) y);
                            onSelectionActionPerformedListener.onSelectionFinished(false);
                        }
                        setOnTouchListener(scroller);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        xtimes = (int) (x - event.getX()) / 25;
                        ytimes = (int) (y - event.getY()) / 60;
                        if (xtimes > 0) {
                            v.dispatchKeyEvent(new KeyEvent(0, 0, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_LEFT, xtimes, KeyEvent.META_SHIFT_ON));
                            x=event.getX();
                        }
                        else if(xtimes < 0)
                        {
                            v.dispatchKeyEvent(new KeyEvent(0, 0, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_RIGHT, -xtimes, KeyEvent.META_SHIFT_ON));
                            x=event.getX();
                        }

                        if (ytimes > 0) {
                            v.dispatchKeyEvent(new KeyEvent(0, 0, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_UP, ytimes, KeyEvent.META_SHIFT_ON));
                            y=event.getY();
                        }
                        else if(ytimes < 0) {
                            v.dispatchKeyEvent(new KeyEvent(0, 0, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_DOWN, -ytimes, KeyEvent.META_SHIFT_ON));
                            y=event.getY();
                        }
                        break;
                }
                return false;
            }
        };

        scroller = new OnTouchListener() {
            float downTime;
            int xtimes;
            int ytimes;
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        downTime = event.getEventTime();
                        x=event.getX();
                        y=event.getY();
                        break;
                    case MotionEvent.ACTION_UP:
                        x = event.getX();
                        y = event.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        xtimes = (int) (x - event.getX());
                        ytimes = (int) (y - event.getY());
                        scrollBy(xtimes,ytimes);
                        break;
                }
                return false;
            }
        };

        setOnTouchListener(scroller);

        setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                setOnTouchListener(selector);
                return true;
            }
        });
        getSettings().setJavaScriptEnabled(true);
        loadUrl("file:///android_asset/index.html");
    }

    @SuppressLint("InflateParams")
    private void initPopup()
    {
        pw = new PopupWindow(context);
        pw.setHeight(getResources().getDisplayMetrics().heightPixels/15);
        pw.setWidth(75*getResources().getDisplayMetrics().widthPixels/100);
        pw.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        pw.setElevation(50.0f);
        pw.setOutsideTouchable(true);
        pw.setTouchable(true);

        popupView = inflater.inflate(R.layout.webview_dialog_set_1,null);

        final View optSet1 = popupView.findViewById(R.id.optSet1);
        final View optSet2 = popupView.findViewById(R.id.optSet2);

        popupView.findViewById(R.id.nextOptSet).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                optSet1.setVisibility(GONE);
                optSet2.setVisibility(VISIBLE);
            }
        });
        popupView.findViewById(R.id.prevOptSet).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                optSet2.setVisibility(GONE);
                optSet1.setVisibility(VISIBLE);
            }
        });

        popupView.findViewById(R.id.cut).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AceEditor.this.dispatchKeyEvent(new KeyEvent(0, 0, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_X, 0, KeyEvent.META_CTRL_ON));
                AceEditor.this.requestFocus();
                pw.dismiss();
                onSelectionActionPerformedListener.onCut();
            }
        });
        popupView.findViewById(R.id.copy).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AceEditor.this.dispatchKeyEvent(new KeyEvent(0, 0, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_C, 0, KeyEvent.META_CTRL_ON));
                AceEditor.this.requestFocus();
                pw.dismiss();
                onSelectionActionPerformedListener.onCopy();
            }
        });
        popupView.findViewById(R.id.paste).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AceEditor.this.dispatchKeyEvent(new KeyEvent(0, 0, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_V, 0, KeyEvent.META_CTRL_ON));
                AceEditor.this.requestFocus();
                pw.dismiss();
                onSelectionActionPerformedListener.onPaste();
            }
        });
        popupView.findViewById(R.id.selectall).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AceEditor.this.dispatchKeyEvent(new KeyEvent(0, 0, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_A, 0, KeyEvent.META_CTRL_ON));
                popupView.findViewById(R.id.prevOptSet).performClick();
                onSelectionActionPerformedListener.onSelectionFinished(true);
            }
        });
        popupView.findViewById(R.id.undo).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AceEditor.this.dispatchKeyEvent(new KeyEvent(0, 0, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_Z, 0, KeyEvent.META_CTRL_ON));
                onSelectionActionPerformedListener.onUndo();
            }
        });
        popupView.findViewById(R.id.redo).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AceEditor.this.dispatchKeyEvent(new KeyEvent(0, 0, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_Z, 0, KeyEvent.META_CTRL_ON|KeyEvent.META_SHIFT_ON));
                onSelectionActionPerformedListener.onRedo();
            }
        });
        pw.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                optSet2.setVisibility(GONE);
                optSet1.setVisibility(VISIBLE);
            }
        });
        pw.setContentView(popupView);
    }

    public void setResultReceivedListener(ResultReceivedListener listener)
    {
        this.received = listener;
    }

    public void setOnLoadedEditorListener(OnLoadedEditorListener listener)
    {
        this.onLoadedEditorListener = listener;
    }

    public void setOnSelectionActionPerformedListener(OnSelectionActionPerformedListener listener)
    {
        this.onSelectionActionPerformedListener = listener;
    }

    public void setOnTextChangeListener(TextWatcher listener) {
        this.onTextChangeListener = listener;
    }

    public void showOptionsAfterSelection(boolean show)
    {
        actAfterSelect = show;
    }

    public void setText(String text)
    {
        loadUrl("javascript:editor.session.setValue(\"" + text +"\");");
    }

    public void setFontSize(int fontSizeInpx)
    {
        loadUrl("javascript:editor.setFontSize(" + fontSizeInpx + ");");
    }

    public void insertTextAtCursor(String text)
    {
        loadUrl("javascript:editor.insert(\"" + text +"\");");
    }

    public void startFind(String toFind, boolean backwards, boolean wrap, boolean caseSensitive, boolean wholeWord)
    {
        findString = toFind;
        loadUrl("javascript:editor.find('" + toFind + "', backwards: "+ String.valueOf(backwards) +
                ", wrap: "+ String.valueOf(wrap) +
                ",caseSensitive: "+ String.valueOf(caseSensitive) +
                ",wholeWord: "+ String.valueOf(wholeWord) +",regExp: false});");
    }

    public void findNext()
    {
        if(findString == null) {
            return;
        }
        loadUrl("javascript:editor.findNext();");
    }

    public void findNext(String errorToastMessage, int showFor)
    {
        if(findString == null) {
            Toast.makeText(context,errorToastMessage,showFor).show();
            return;
        }
        loadUrl("javascript:editor.findNext();");
    }

    public void findPrevious()
    {
        if(findString == null) {
            return;
        }
        loadUrl("javascript:editor.findPrevious();");
    }

    public void findPrevious(String toastMessage, int showFor)
    {
        if(findString == null) {
            Toast.makeText(context,toastMessage,showFor).show();
            return;
        }
        loadUrl("javascript:editor.findPrevious();");
    }

    public void replace(String replaceText, boolean replaceAll)
    {
        if(replaceAll)
            loadUrl("javascript:editor.replaceAll('" + replaceText + "');");
        else
            loadUrl("javascript:editor.replace('" + replaceText + "');");
    }

    public void endFind()
    {
        findString = null;
    }

    public void setSoftWrap(boolean enabled)
    {
        if(enabled)
            loadUrl("javascript:editor.getSession().setUseWrapMode(true);");
        else
            loadUrl("javascript:editor.getSession().setUseWrapMode(false);");
    }

    public void setTheme(Theme theme)
    {
        loadUrl("javascript:editor.setTheme(\"ace/theme/" + theme.name().toLowerCase() + "\");");
    }

    public void setMode(Mode mode)
    {
        loadUrl("javascript:editor.session.setMode(\"ace/mode/" + mode.name().toLowerCase() + "\");");
    }

    public enum Theme
    {
        SOLARIZED_DARK, SOLARIZED_LIGHT
    }

    public enum Mode
    {
        Python
    }

}
