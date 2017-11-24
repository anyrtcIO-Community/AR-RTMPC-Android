package org.anyrtc.widgets;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gyf.barlibrary.ImmersionBar;

import org.anyrtc.live_line.R;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by KathLine on 2016/12/30.
 */

public class KeyboardDialogFragment extends AppBaseDialogFragment {

    @BindView(R.id.close)
    TextView close;
    @BindView(R.id.edit_send_message)
    EditText editSendMessage;
    @BindView(R.id.btn_send)
    Button btnSend;
    @BindView(R.id.ll_input_h)
    LinearLayout llInputH;
    private static InputMethodManager imm;
    private ImmersionBar mImmersionBar;
    public KeyboardDialogFragment() {
        // Required empty public constructor
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @OnClick({R.id.close, R.id.btn_send})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.close:
                if (edittextListener != null) {
                    hideKeyboard();
                    this.dismiss();
                }
                break;
            case R.id.btn_send:
                String text = editSendMessage.getText().toString();
                if (!TextUtils.isEmpty(text)) {
                    if (edittextListener != null) {
                        edittextListener.setTextStr(text);
                        editSendMessage.setText("");
                        hideKeyboard();
                        this.dismiss();
                    }
                }
                break;
        }
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
    protected void initData() {
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
