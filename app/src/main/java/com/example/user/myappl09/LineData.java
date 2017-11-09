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

    public String getName() {
        return file.getName();
    }

    public boolean isDirectory() {
        return file.isDirectory();
    }

    public void add(File f) {
        file = f;
    }


////////////////// ここから下は古いやつ

    private TextView textView = null;     // ファイル名、フォルダ名設定用(表示データ用)
    private String absolutePath = "";       // (絶対)パス保存・・ファイル名まで含める
    private boolean folder = true;      //フォルダ(true)、ファイル(true以外)の切り分けフラグ
    private boolean selected = false;   //選択されたのか：　true = yse / false = no

    // ==================================================================
    // set() data method
    // ==================================================================
    //引数    true = Folder / true以外 = File
    void setFolderOrFile( boolean type ) {
        this.folder = type;
    }
    //表示部の TextView データを保存
    void setTextView( TextView txV ) {
        this.textView = txV;
    }
    //パス保存
    void setAbsolutePath( String setPath ) {
        this.absolutePath = setPath;
    }
    //選択状態セット
/*    void setSelected( boolean flag ) {
        this.selected = flag;
    }
*/
    // ==================================================================
    // get() data method
    // ==================================================================
/*    String getText() {
        return( this.textView.getText().toString() );
    }
*/    Drawable[] getIcon() {
        return( this.textView.getCompoundDrawables() );
    }
    //表示部の TextView データ取得
    TextView getTextView() {
        return( this.textView );
    }
    //戻り値   true = Folder / true以外 = File
    boolean getFolderOrFile() {
        return( this.folder );
    }
    //パス取得
    String getAbsolutePath() {
        return( this.absolutePath );
    }
    //選択状態取得
    boolean getSelected() {
        return( this.selected );
    }

    // ==================================================================
    // 他
    // ==================================================================
    //データ初期化
    void initData() {
        this.textView = null;
        this.folder = true;
        this.selected = false;
        absolutePath = "";
    }
}
