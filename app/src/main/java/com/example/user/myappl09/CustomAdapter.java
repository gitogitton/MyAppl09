package com.example.user.myappl09;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

// package-private class
class CustomAdapter extends ArrayAdapter<LineData> {

    private final String CLASS_NAME = "CustomAdapter";
    private LayoutInflater mInflater;

    CustomAdapter(Context context, List<LineData> objects) {
        super(context, 0, objects);
        mInflater = LayoutInflater.from(context);
        Log.d( CLASS_NAME, "constructor run." );
    }

    @Override
    @NonNull
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        //再利用可能なViewがあったらそれを使う。なかったら新しく作成する。
        if (convertView == null) {
//            Log.d(CLASS_NAME,"getView(): convertView == null ");
            convertView = mInflater.inflate(R.layout.file_list, parent, false);
        }

        //Log.d(CLASS_NAME,"getView() start.");

        //表示するデータを取得
        LineData item = getItem(position);
        TextView textView = (TextView) convertView.findViewById(R.id.textView);
        if(textView==null) { Log.d(CLASS_NAME,"tv is null."); }   //debug
        String msg = (item!=null?item.getName():"null");    //debug
//        Log.d(CLASS_NAME,"item.getName()="+msg);    //debug
//        assert textView != null;
        if (textView!=null) {
            textView.setText(item != null ? item.getName() : "null");  //ファイル名
            AtomicReference<Drawable> icon = new AtomicReference<Drawable>();  //アイコン
            if (item != null) {
                if (item.isDirectory()) {
                    icon.set(ContextCompat.getDrawable(getContext(), R.drawable.ic_folder_black_24dp));
                } else {
                    if (item.isChecked()) {
                        icon.set(ContextCompat.getDrawable(getContext(), R.drawable.ic_check_black_24dp));
                    } else {
                        icon.set(ContextCompat.getDrawable(getContext(), R.drawable.ic_file));
                    }
                }
            }
            icon.get().setBounds(0, 0, icon.get().getIntrinsicWidth(), icon.get().getIntrinsicHeight()); //ICONの表示位置を設定 (引数：座標 x, 座標 y, 幅, 高さ)
            textView.setCompoundDrawables(icon.get(), null, null, null); //TextViewにアイコンセット（四辺(left, top, right, bottom)に対して別個にアイコンを描画できる）
        }//if(textView!=null)
//        Log.d(CLASS_NAME,"getView() end.");
        return convertView;
    }//getView()

}//class
