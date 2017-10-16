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

public class confirmDialogFragment extends DialogFragment {

    //=======================================================================================
    // コンストラクター
    //=======================================================================================
    // Fragmentの再生成の時に呼ばれるので、引数なしのpublicなコンストラクタが必要
    public confirmDialogFragment() {
        Log.d( "confirmDialogFrament", "------------> constructor starts !!" );
    }

    //=======================================================================================
    // メソッド
    //=======================================================================================
    @NonNull
    public Dialog onCreateDialog( Bundle savedInstanceState ) {

        Log.d( "confirmDialogFragment", "onCreateDialog() -------------> starts !!" );

        // Use the Builder class for convenient dialog construction
        //      https://teratail.com/questions/1600
        //          ↓
        //      FragmentはContextの子クラスではないのでContextを利用することが出来ません。
        //      ですが、getActivity()を使うことでそれが可能になります。
        //          http://developer.android.com/reference/android/app/Fragment.html#getActivity%28%29
        //      getActivity()はFragmentを呼び出しているActivityをRetrunします。
        //      Activity自身はContextをextendsしているためActivityを使うことでFragment内でContextを利用することが出来ます。
        //
        Activity activity = getActivity();
        Context context = activity.getApplicationContext();
        AlertDialog.Builder builder = new AlertDialog.Builder( context, R.style.AlertDialog_Theme );
//        AlertDialog.Builder builder = new AlertDialog.Builder( getContext() );

        Log.d( "confirmDialogFragment", "new AlertDialog.Builder() fin." );

        builder.setTitle( R.string.dialog_title );
        builder.setMessage( R.string.dialog_message );

        Log.d( "confirmDialogFragment", "builder.setTitle() / setMessage() fin." );

        // 記録されている値をキーをもとにして取得。おっこちて自動再生された場合でも値が残ってくれるのでとっておく。
        // 落ちるのが前提な仕組みに思える・・・・。Android 恐るべし・・・。MS Officeか！！
        String strTtitle = savedInstanceState.getString( "title" );
        short value = savedInstanceState.getShort( "value" );
        Log.d( "dialog Argments", "titile / value = [ " + strTtitle + value + " ]" );

        // OKボタンのキャプション設定とリスナー登録
        builder.setPositiveButton( R.string.dialog_ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // OK 押下時
                        Log.d( "confirmDialog", "push OK !!" );
                    }// onClick()
                }// OnClickListener()
        );

        // Cancelボタンのキャプション設定とリスナー登録
        builder.setNegativeButton( R.string.dialog_cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // CANCEL 押下時
                        Log.d( "confirmDialog", "push Cancel !!" );
                    }// onClick()
                }// OnClickListener()
        );

        // Create the AlertDialog object and return it
        return builder.create();

    }// onCreateDialog()
}
