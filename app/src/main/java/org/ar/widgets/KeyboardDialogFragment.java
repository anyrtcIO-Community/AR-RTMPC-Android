package org.ar.widgets;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.ar.rtmpc.R;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by KathLine on 2016/12/30.
 */

public class KeyboardDialogFragment extends AppBaseDialogFragment {

    TextView close;
    EditText editSendMessage;
    Button btnSend;
    private static InputMethodManager imm;
    public KeyboardDialogFragment() {
        // Required empty public constructor
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }




    public interface EdittextListener {
        void setTextStr(String text);

        void dismiss(DialogFragment dialogFragment);
    }

    private EdittextListener edittextListener;

    public void setEdittextListener(EdittextListener listener) {
        edittextListener = listener;
    }

    @Override
    protected void setLayout() {
        Window window = getDialog().getWindow();
        window.getDecorView().setPadding(0, 0, 0, 0);
        window.setBackgroundDrawable(new ColorDrawable(0));//背景透明
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        lp.height =ViewGroup.LayoutParams.MATCH_PARENT;
        lp.dimAmount = 0;
        window.setAttributes(lp);
    }

    @Override
    protected int getContentViewID() {
        return R.layout.dialogfragment_keyboard;
    }

    @Override
    protected void initData(View view) {
        close=view.findViewById(R.id.close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (edittextListener != null) {
                    hideKeyboard();
                   dismiss();
                }
            }
        });
        editSendMessage=view.findViewById(R.id.edit_send_message);
        btnSend=view.findViewById(R.id.btn_send);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = editSendMessage.getText().toString();
                if (!TextUtils.isEmpty(text)) {
                    if (edittextListener != null) {
                        edittextListener.setTextStr(text);
                        editSendMessage.setText("");
                        hideKeyboard();
                        dismiss();
                    }
                }
            }
        });
        Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                               public void run() {
                                  showKeyboard(editSendMessage.getContext(), editSendMessage);
                               }
                           },
                    150);

    }


    private void hideKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager) editSendMessage.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm!=null&&getDialog()!=null) {
                imm.hideSoftInputFromWindow(getDialog().getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }catch (Exception e){
        }

    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (edittextListener != null) {
            edittextListener.dismiss(this);
        }
    }

    public  void hideKeyboard(Context context, View view) {
        try {
            view.requestFocus();
            imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm!=null) {
                imm.hideSoftInputFromWindow(view.getApplicationWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }catch (Exception e){

        }

    }

    public  void hideKeyboard(Activity activity) {
        try {
            imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm!=null) {
                imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }catch (Exception e){

        }

    }

    public  void showKeyboard(Context context, View view) {
        try {
            imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
//        imm.showSoftInput(view, 0);
            if (imm!=null) {
                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
            }
        }catch (Exception e){

        }

    }




}
