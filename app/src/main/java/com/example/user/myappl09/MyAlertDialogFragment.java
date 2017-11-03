package com.example.user.myappl09;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import static com.example.user.myappl09.R.string.dialog_message;

public class MyAlertDialogFragment extends DialogFragment {

    //=======================================================================================
    // コンストラクター
    //=======================================================================================
    // Fragmentの再生成の時に呼ばれるので、引数なしのpublicなコンストラクタが必要、、、なの？
    public MyAlertDialogFragment() {
        Log.d( "confirmDialogFrament", "------------> constructor starts !!" );
    }

    //=======================================================================================
    // メソッド
    //=======================================================================================
    public static MyAlertDialogFragment newInstance(int title) {
        MyAlertDialogFragment alertDlg = new MyAlertDialogFragment();
        Bundle args = new Bundle();
        args.putInt("title",title);
        alertDlg.setArguments(args);

        return alertDlg;
    }
    @NonNull
    @Override
    public Dialog onCreateDialog( Bundle savedInstanceState ) {

        Log.d( "MyAlertDialogFragment", "onCreateDialog() -------------> starts !!" );

        int title = getArguments().getInt("title");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //alertDlg.setIcon();
        builder.setTitle(title);

        // OKボタンのキャプション設定とリスナー登録
        builder.setPositiveButton( "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // OK 押下時
                        Log.d( "confirmDialog", "push OK !!" );
                        dismiss();
                    }// onClick()
                }// OnClickListener()
        );

        // Cancelボタンのキャプション設定とリスナー登録
        builder.setNegativeButton( "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // CANCEL 押下時
                        Log.d( "confirmDialog", "push Cancel !!" );
                        dismiss();
                    }// onClick()
                }// OnClickListener()
        );

        // Create the AlertDialog object and return it
        return builder.create();

    }// onCreateDialog()
}
