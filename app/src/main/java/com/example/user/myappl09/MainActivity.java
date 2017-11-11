package com.example.user.myappl09;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

//import java.io.BufferedInputStream;
import org.w3c.dom.Text;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    final String APPL_MAME = "ファイル一覧";
    final String FILE_SEPARATOR = System.getProperty("file.separator");
    final String ROOT_DIR = FILE_SEPARATOR;
    ArrayList<String> copiedFileList = new ArrayList<String>();

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
        //ルートの内容をリストに表示
        showListOfDirectory(FILE_SEPARATOR);

//        //パスを表示
//        TextView textView = (TextView)findViewById(R.id.textView4);
//        setPath(textView,FILE_SEPARATOR);
//        // ファイルリストを表示する
//        ListView listView = (ListView)findViewById(R.id.fileList01);
//        setList(ROOT_DIR, listView);       //初期表示はルートディレクトリー。

        //ListViewの行にクリック時のlistenerを登録
        ListView listView = (ListView)findViewById(R.id.fileList01);
        listView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Log.d(APPL_MAME,"onItemClick() start. [pos="+position+"/id="+id+"]");

                        //クリックされたのがフォルダの場合は移動する。ファイルなら選択状態をトグルする。
                        LineData item = (LineData)parent.getItemAtPosition(position);
                        if (item.isDirectory()) {
                            showListOfDirectory(item.getAbsolutePath());
                        } else {
                            selectItem((ListView) parent, view, position, id);
                        }
                        Log.d(APPL_MAME,"onItemClick() end.");
                    }
                }
        );

        //Toastでメッセージ表示
        Toast.makeText( this, "ファイル一覧を表示しています。", Toast.LENGTH_LONG ).show();

    } //onCreate()

    // ============================================================================================================
    //指定されたディレクトリの内容をリストに設定
    private void showListOfDirectory(String directoryName) {
        Log.d(APPL_MAME,"showListOfDirectory start. [args="+directoryName+"]");

        //パスを表示
        TextView textView = (TextView)findViewById(R.id.textView4);
        setPath(textView,directoryName);
        //リストを表示
        ListView listView = (ListView)findViewById(R.id.fileList01);
        setList(directoryName, listView);

        Log.d(APPL_MAME,"showListOfDirectory end.");
    }

    // ============================================================================================================
    //チェック状態にする処理
    private void selectItem(ListView listView, View view, int position, long id) {
        Log.d(APPL_MAME,"selectItem() start.");
        //チェック状態を反転する。
        LineData item = (LineData) listView.getItemAtPosition(position);
        if (item.isChecked()) {
            Log.d(APPL_MAME,"isChecked() = true->false");
//            item.setChecked(false);
        } else {
            Log.d(APPL_MAME,"isChecked() = false->true");
//            item.setChecked(true);
        }
        item.toggle();
        //リストにセット
        CustomAdapter adapter = (CustomAdapter) listView.getAdapter();
        adapter.add(item);
//call automatically when add(),remove(),clear() are called.        adapter.notifyDataSetChanged();
        Log.d(APPL_MAME,"selectItem() fin.");
    }
    //---------------------------------------------------------------------------
    //checkされている項目を取得
    private ArrayList<LineData> getCheckedItems() {
        ArrayList<LineData> items = new ArrayList<>();
        for (LineData everyItem : items) {
            if (!everyItem.isChecked()) {
                continue;
            }
            items.add(everyItem);
        }
        Log.d(APPL_MAME,"getCheckedItems() size="+items.size());
        return items;
    }
    // ============================================================================================================
    //パスを設定
    private void setPath(TextView textView,String str) {
        textView.setText(str);
    }
    // ============================================================================================================
    //指定されたパスに従って内容をリストに設定
    //(パスは絶対パスで！！)
    private void setList( String selectedPath, ListView listView ) {

        Log.d( APPL_MAME, "setList() --- start. [ Indicated Path = " + selectedPath + " ]" );
        if(selectedPath.isEmpty()) { //指定が無ければrootとする。
            selectedPath=System.getProperty("file.separator");
        }
        //指定されたパスの ファイル一覧を 取得
        File[] list = new File(selectedPath).listFiles();
        //表示内容（ArrayListの中身）を簡単にソート
        //  Array.asList -> 配列を List （ような物）に変換する。ただし、ArrayList とは違う。
        ArrayList<LineData> arrayList=new ArrayList<>(); //ArrayListを生成
        if (list!=null) {
            Collections.sort(Arrays.asList(list), new LineDataComparator());
            //arrayListへファイルの一覧情報をセットする。
            for (File file : list) {
                LineData lineData = new LineData(getApplicationContext());
                lineData.add(file);
                arrayList.add(lineData);
            }
        }
        // listViewへのadapter生成
        CustomAdapter arrayAdapter = new CustomAdapter(getApplicationContext(), arrayList);
        //directoryの内容をlistviewへ
        listView.setAdapter(arrayAdapter);    //listviewにadapterを指定

        Log.d( APPL_MAME, "setList() --- end.");
    }
    // ============================================================================================================
    //overflowmenを表示
    //  どこから呼ばれるかは自分で確認してみよう！！！親クラスから呼ばれてるんだろうけれど。
    //  menu_main.xmlを定義し、本メソッドのオーバーライドでツールバーの端に縦点３つのやつが出てきた。
    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {
        getMenuInflater().inflate( R.menu.menu_main, menu );
        return true;
    }
    // ============================================================================================================
    //overflowmenuのイベントを処理
    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        Log.d( APPL_MAME, "onOptionsItemSelected() --- start. [ menu = " + item.toString() + " ]" );
        switch( item.getItemId() ) {
            case R.id.action_upFolder :
                String path = getParentDirectoryName();
                if(path.length()>0) {
                    showListOfDirectory(path);
                }
                break;
            case R.id.action_appEnd :
                showAlertDialog(R.string.alertDlg_message_fin);
                break;
            case R.id.action_upRoot :
                showListOfDirectory(FILE_SEPARATOR);
                break;
            case R.id.action_copy :
                //コピー対象のファイルの情報を保存（実際のコピー処理はペーストされたタイミングで行う）
                copyCheckedFileInfo();
                break;
            default:
                //do nothing
                break;
        }
        return super.onOptionsItemSelected( item );
    }

    //--------------------------------------------
    //ファイルコピー
    private void copyCheckedFileInfo() {
        //checkされたitemを取得
        ArrayList<LineData> items = getCheckedItems();
        for (LineData everyItem : items) {
            String absolutePath = everyItem.getAbsolutePath();
            StringBuilder stringBuilder = new StringBuilder(absolutePath);
            if (!absolutePath.equals(FILE_SEPARATOR)) { //絶対パスがルートでなければ末尾に区切り文字を入れる。
                stringBuilder.setLength(0);
                stringBuilder.append(FILE_SEPARATOR);
            }
            stringBuilder.append(everyItem.getName());  //ファイル名をセット
            absolutePath = stringBuilder.toString();    //絶対パスでのファイル名作成完了
            copiedFileList.add(absolutePath);
            Log.d(APPL_MAME, "add copiedFileList --> "+absolutePath);
        }
        return;
    }

    //--------------------------------------------
    //直上のパスを取得
    private String getParentDirectoryName() {
        //現在表示中のパス文字列を取得
        TextView textView = (TextView)findViewById(R.id.textView4);
        String path = textView.getText().toString();
        Log.d(APPL_MAME,"getParentDirectoryName() specified path="+path);
        if (path.equals(FILE_SEPARATOR)) {
//あとで作成(???)            showInfomationDialog("現在、ルートディレクトリーです。")
            showAlertDialog(R.string.informationDlg_message_01);
            return "";
        }
        //直上のディレクトリーを絶対パスで取得
        int index = path.lastIndexOf(FILE_SEPARATOR);
        String editedPath="";
        if (index<=0) {  //現在ルート、もしくは異常値の場合
            editedPath = FILE_SEPARATOR;
        } else {
            editedPath = path.substring(0, index);
        }
        Log.d(APPL_MAME,"getParentDirectoryName() edited path="+editedPath);
        return editedPath;
    }
    // ============================================================================================================
    //ナビゲーションボタンのイベントを受ける。
    @Override
    public boolean onKeyDown( int keyCode, KeyEvent event ) {

        Log.d( APPL_MAME, "onKeyDown() ---start." );

        if ( KeyEvent.KEYCODE_BACK==keyCode ) {
            //現在表示中のパス文字列を取得
            TextView textView = (TextView)findViewById(R.id.textView4);
            String path = textView.getText().toString();
            if (path.equals(FILE_SEPARATOR)) {
                //ダイアログで終了を確認する。
                showAlertDialog(R.string.alertDlg_message_fin);
            } else {
                //直上のディレクトリーのパス取得
                String parentPath = getParentDirectoryName();
                showListOfDirectory(parentPath);
            }
            return true;
        }
        Log.d( APPL_MAME, "onKeyDown() fin." );
        return super.onKeyDown( keyCode, event );
    }
// 以下はDialogFragment のため追加
    // ============================================================================================================
    // alertDialog を表示する。
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
