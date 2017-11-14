package com.example.user.myappl09;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.CheckedTextView;
import android.widget.TextView;

import java.io.File;

/*
 * Created by User on 2017/06/20.
 *
 * ListView の行データの定義
 *
 */

//package-private
class LineData extends android.support.v7.widget.AppCompatCheckedTextView {

    private File file;

    public LineData(Context context) {
        super(context);
    }

    //===get
    public File getFile() { return file; } //ファイルディスクリプタ
    public String getName() { return file.getName(); } //ファイル名のみ
    public String getAbsolutePath() { return  file.getAbsolutePath(); } //ファイル名を含んだフルパス

    //===is
    public boolean isDirectory() {
        return file.isDirectory();
    }

    //===add, remove, clear etc.
    public void add(File f) {
        file = f;
    }

}
