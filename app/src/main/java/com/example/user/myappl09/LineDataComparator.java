package com.example.user.myappl09;

import android.util.Log;

import java.io.File;
import java.util.Comparator;

//
// Created by User on 2017/06/21.
//

class LineDataComparator implements Comparator<File> {

    @Override
    public boolean equals(Object obj) {
        return false;
    }

    @Override
    public int compare(File o1, File o2) {

        if ( null == o1 || null == o2 ) {
            Log.d( "LineDataComparator", "Illegal Argument [ o1=" + o1 + " o2=" + o2 + " ]" );
            return( -1 );
        }

        //folder -> file の順にソート
        if ( true == o1.isDirectory() && true != o2.isDirectory() ) {
            return( -1 );
        }
        else if ( true != o1.isDirectory() && true == o2.isDirectory() ) {
            return( 1 );
        }

        // パス文字列でソートする。
        String str1 = o1.getName();                 //比較する文字列取得
        String str2 = o2.getName();                 //比較する文字列取得

        //o1とo2を比較
        //      String.compareTo("A") はUnicode値で大小を判定する。
        //      大文字小文字を区別するとの事で注意が必要。
        //      区別したくない場合は「compareToIgnoreCase()」を使用する。
        //      ＜返り値は差の値＞
        //          負の値  : Stringは"A"より小さい
        //          0       : Stringは"A"と等しい
        //          正の値  : Stringは"A"より大きい
        //
        return( str1.compareTo( str2 ) );
    }
}

