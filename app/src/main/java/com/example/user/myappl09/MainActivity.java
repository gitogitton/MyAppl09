package com.example.user.myappl09;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

//import java.io.BufferedInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    final String APPL_MAME = "ファイル一覧";
    final String FILE_SEPARATOR=System.getProperty("file.separator");
    final String ROOT_DIR=FILE_SEPARATOR;

    // ============================================================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d( APPL_MAME, "MainActivity : super.onCreate() start." );

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //ツールバー -> API21 (android v5.0) からの機能であるためAPI17からサポートしようと思ったら使えないので ActionBar を使用。
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_01);
        toolbar.setTitle( "ファイル一覧" );
        setSupportActionBar( toolbar );

        //パスを表示
        setPath(R.id.textView4,FILE_SEPARATOR);
        // ファイルリストを表示する
        setList(ROOT_DIR,R.id.fileList01);       //初期表示はルートディレクトリー。

        //ListViewの行にクリック時のlistenerを登録
        final ListView listView = (ListView)findViewById(R.id.fileList01);
        listView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Log.d(APPL_MAME,"onItemClick() start. [pos="+position+"/id="+id+"]");
//
// ListView.getChildAt(int position)で取れます。
//
                        LineData item = (LineData) parent.getItemAtPosition(position);

                        Log.d(APPL_MAME,"onItemClick() end.");
                    }
                }
        );
        //Toastでメッセージ表示
        Toast.makeText( this, "ファイル一覧を表示しています。", Toast.LENGTH_LONG ).show();

    } //onCreate()

    // ============================================================================================================
    //長押し時の処理
    private void selectItem(ListView listView, View view, int position, long id) {
        Log.d(APPL_MAME,"selectItem() start.");

        //一度でもチェックされたアイテムを取得
        SparseBooleanArray checked = listView.getCheckedItemPositions();

        for (int i = 0; i < checked.size(); i++) {
            // チェックされているアイテムの key の取得
            int key = checked.keyAt(i);
            Log.d(APPL_MAME,"i="+i+"("+checked.toString()+")");
        }

        Log.d(APPL_MAME,"selectItem() fin.");
    }
    // ============================================================================================================
    //パスを表示
    private void setPath(int id,String str) {
        TextView tv = (TextView)findViewById(id);
        tv.setText(str);
    }
    // ============================================================================================================
    //指定されたパスに従って内容を表示する処理
    private void setList( String selectedPath, int listViewId ) {

        Log.d( APPL_MAME, "setList() --- start. [ Indicated Path = " + selectedPath + " ]" );
        if(selectedPath.isEmpty()) { //指定が無ければrootとする。
            selectedPath=System.getProperty("file.separator");
        }

        //指定されたパスの ファイル一覧を 取得
        File[] list = new File(selectedPath).listFiles();
//        ArrayList<File> arrayList=new ArrayList<>(Arrays.asList(list)); //ArrayListを生成して初期化
        ArrayList<LineData> arrayList=new ArrayList<>(); //ArrayListを生成
        //arrayListへファイルの一覧情報をセットする。
        for (File file : list) {
            LineData lineData = new LineData(getApplicationContext());
            lineData.add(file);
            arrayList.add(lineData);
        }

        // listViewへのadapter生成
//        CustomAdapter arrayAdapter=new CustomAdapter(getApplicationContext(),arrayList);
        CustomAdapter arrayAdapter = new CustomAdapter(getApplicationContext(), arrayList);

        //directoryの内容をlistviewへ
        ListView listView = (ListView)findViewById(listViewId); //listview取得
        listView.setAdapter(arrayAdapter);    //listviewにadapterを指定

//        for( int i=0; i<arrayAdapter.getCount(); i++) {  //debug
//            Log.d(APPL_MAME, "arrayAdapter list [" + i + "]" + arrayAdapter.getItem(i).toString());
//        }

        Log.d( APPL_MAME, "setList() --- end.");
    }
    // ============================================================================================================
    //オーバーフローメニューを表示
    //
    //どこから呼ばれるかは自分で確認してみよう！！！親クラスから呼ばれてるんだろうけれど。
    //menu_main.xmlを定義し、本メソッドのオーバーライドでツールバーの端に縦点３つのやつが出てきた。
    //
    //menu選択時のアクションは「onOptionsItemSelected()」で実装
    //
    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {
        getMenuInflater().inflate( R.menu.menu_main, menu );
        return true;
    }
    // ============================================================================================================
    //ナビゲーションボタンのイベントを受ける。
    @Override
    public boolean onKeyDown( int keyCode, KeyEvent event ) {

        Log.d( APPL_MAME, "onKeyDown() ---start." );

        if ( KeyEvent.KEYCODE_BACK==keyCode ) {
            // ダイアログで終了を確認する。
            showAlertDialog(R.string.alertDlg_message_fin);
            return true;
        }
        Log.d( APPL_MAME, "onKeyDown() fin." );
        return super.onKeyDown( keyCode, event );
    }
    // ============================================================================================================
    //ナビゲーションボタンのイベントを処理
    // ============================================================================================================
    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        Log.d( APPL_MAME, "onOptionsItemSelected() --- start. [ menu = " + item.toString() + " ]" );
        switch( item.getItemId() ) {
            case android.R.id.home:     // 戻るボタン   // ここの場合、ずっとMainActivityなのでfinish()をCallするとアプリ画面が閉じてしまう・・・のだった・・・。
                finish();
                return true;
            default:
                //do nothing
                break;
        }
        return super.onOptionsItemSelected( item );
    }
// 以下はDialogFragment のため追加
    // ============================================================================================================
    // alertDialog を表示する。
    // ============================================================================================================
    public void showAlertDialog(int msg) {
        MyAlertDialogFragment alertDlg = MyAlertDialogFragment.newInstance(R.string.alertDlg_title,msg);
        // APIのサポート下限が１５なので getFragmentManager() は使用できない。
        // かわりに getSupportFragmentManager() を使用する。
        alertDlg.show(getSupportFragmentManager(),"dialog");
    }
    public void doPositiveClick() {
        // Do stuff here.
        Log.i(APPL_MAME, "alertDialog : Positive click!");
        finish();
    }
    public void doNegativeClick() {
        // Do stuff here.
        Log.i(APPL_MAME, "alertDialog : Negative click!");
    }
// ここまで（DialogFragment のため追加）

}
