package com.example.user.myappl09;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;

public class MyDialogFragment extends DialogFragment {

    private static int mType;

    // Fragmentの再生成の時に呼ばれるので、引数なしのpublicなコンストラクタが必要、、、なの？
    public MyDialogFragment() {
        Log.d( "MyDialogFragment", "------------> constructor starts !!" );
    }

    public static MyDialogFragment newInstance(int title, int message, int type) {
        Log.d("", "MyDialogFragment:newInstance() type="+type);
        MyDialogFragment alertDlg = new MyDialogFragment();
        Bundle args = new Bundle();
        args.putInt("title", title);
        args.putInt("message", message);
        args.putInt("type", type);
        mType =type;
        alertDlg.setArguments(args);
        return alertDlg;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog( Bundle savedInstanceState ) {

        Log.d( "MyDialogFragment", "onCreateDialog() -------------> starts !!" );

        int title = getArguments().getInt("title");
        int message = getArguments().getInt("message");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //builder.setIcon();
        builder.setTitle(title);
        builder.setMessage(message);

        //ＯＫボタンだけは必ず表示する。
        //ＯＫボタンだけ有効な場合はダイアログ以外を操作されてもダイアログをキャンセル出来るようにする。
        builder.setCancelable(true);   //背面等をタッチされたら消える設定
        builder.setPositiveButton( R.string.dialog_ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // OK 押下時
                        Log.d( "MyDialogFragment", "push OK !!" );
                        dismiss();
                        ((MainActivity)getActivity()).doPositiveClick(mType);
                    }// onClick()
                }// OnClickListener()
        );

        //Ｃａｎｃｅｌボタンを表示する。なんだか嫌な作りだが・・・
        //この場合、ダイアログボタン以外をタッチされた時にダイアログを終了しないように設定する。
        if (mType==1 || mType>=2) {

            builder.setCancelable(false);   //背面等をタッチされてもきえないように設定
            // Cancelボタンのキャプション設定とリスナー登録
            builder.setNegativeButton(R.string.dialog_cancel,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // CANCEL 押下時
                            Log.d("MyDialogFragment", "push Cancel !!");
                            dismiss();
                            ((MainActivity) getActivity()).doNegativeClick();
                        }// onClick()
                    }// OnClickListener()
            );

        }

        //保留ボタンを表示する。
        //この場合、ダイアログボタン以外をタッチされた時にダイアログを終了しないように設定する。
        if (mType==2) {

            builder.setCancelable(false);   //背面等をタッチされてもきえないように設定
            // Cancelボタンのキャプション設定とリスナー登録
            builder.setNeutralButton(R.string.dialog_delay,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // CANCEL 押下時
                            Log.d("MyDialogFragment", "push Cancel !!");
                            dismiss();
                            ((MainActivity) getActivity()).doNegativeClick();
                        }// onClick()
                    }// OnClickListener()
            );

        }

        // Create the AlertDialog object and return it
        return builder.create();

    }// onCreateDialog()

    public int getType() {
        return mType;
    }
    public void setType(int type) {
        mType = type;
        return;
    }
}
