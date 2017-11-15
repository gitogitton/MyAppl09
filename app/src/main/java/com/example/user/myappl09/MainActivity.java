package com.example.user.myappl09;

import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    final String APPL_MAME = "ファイル一覧";
    final String FILE_SEPARATOR = System.getProperty("file.separator");
    final String ROOT_DIR = FILE_SEPARATOR;
    //ダイアログでＯＫされた時の処理
    private boolean mAppEnd = false;            //フラグ＝アプリ終了が指定された
    private boolean mDeleteFiles = false;       //フラグ＝削除を実行中
    //[貼り付け]処理の実行モード
    private char mMode = 0;     //貼り付け実行時のモード。下記の２つ。
    private final char PASTE_BY_COPY = 0;       //コピーからの貼り付け時
    private final char PASTE_BY_CUT = 1;        //切り取りからの貼り付け時
    //チェックされた項目を保存する領域
    private  ArrayList<LineData> mSelectedFileList = new ArrayList<LineData>();

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
        //ListView取得
        ListView listView = (ListView)findViewById(R.id.fileList01);
        //ListViewが空の場合のＶｉｅｗを指定しておく
        View emptyView = findViewById(R.id.emptyView);
        listView.setEmptyView(emptyView);

        //ListViewの行にクリック時のlistenerを登録
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
                            setChecked((ListView) parent, view, position, id);
                            refreshList((ListView)findViewById(R.id.fileList01));
                        }
                        Log.d(APPL_MAME,"onItemClick() end.");
                    }
                }
        );

        //Toastでメッセージ表示
        Toast.makeText( getApplicationContext(), "ファイル一覧を表示しています。", Toast.LENGTH_LONG ).show();

    } //onCreate()

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
            case R.id.action_upFolder : //上のディレクトリへ
                String path = getParentDirectoryName();
                if(path.length()>0) {
                    showListOfDirectory(path);
                }
                break;
            case R.id.action_appEnd : //アプリ終了
                showAlertDialog(R.string.alertDlg_q_appEnd, MyDialogFragment.ALERT_DIALOG_OK_NG);
                mAppEnd = true;
                break;
            case R.id.action_upRoot : //ルートへ
                showListOfDirectory(FILE_SEPARATOR);
                break;
            case R.id.action_copy : //コピー
                //コピー対象のファイルの情報を保存（実際のコピー処理はペーストされたタイミングで行う）
                mMode = PASTE_BY_COPY;
                setSelecteItemList();
                break;
            case R.id.action_paste : //貼り付け
                pasteSelectedFiles();
                refreshList((ListView)findViewById(R.id.fileList01));
                break;
            case R.id.action_cut : //切り取り
                mMode = PASTE_BY_CUT;
                cutSelectedFiles();
                refreshList((ListView)findViewById(R.id.fileList01));
                break;
//            case R.id.action_delete : //削除
//                deleteSelectedFiles();
//                refreshList((ListView)findViewById(R.id.fileList01));
//                break;
            default:
                //do nothing
                break;
        }
        return super.onOptionsItemSelected( item );
    }

    //---------------------------------------------------------------------
    //指定されたファイルをキャッシュ領域に移動する。
    private void copySelectedFilesToCache() {

        for (LineData item : mSelectedFileList) {
            //コピー先ファイルを開く
            String dstFilePath = getApplicationContext().getCacheDir().toString();
            Log.d(APPL_MAME, "Cache Directory : " + dstFilePath);
            if (dstFilePath.length()>0) {
                char c = dstFilePath.charAt( dstFilePath.length()-1 );
                String lastChar =String.valueOf(c);
                if (!lastChar.equals(FILE_SEPARATOR)) {
                    //末尾に区切り文字を入れる。
//                        StringBuilder stringBuilder = new StringBuilder(dstFilePath);
//                        stringBuilder.append(FILE_SEPARATOR);
//                        dstFilePath = stringBuilder.toString();
                    dstFilePath = dstFilePath+FILE_SEPARATOR;
                }
                dstFilePath = dstFilePath+item.getName();   //ファイル名セット
            }
            //コピー実行
            executeCopyFile(item.getAbsolutePath(), dstFilePath);
        }//for (LineData item : mSelectedFileList)

    }

    private void executeCopyFile(String source, String destination) {

        DataInputStream dataInputStream = null;
        DataOutputStream dataOutputStream = null;
        byte[] buff = new byte[4096];

        try {
            dataInputStream = new DataInputStream(
                    new BufferedInputStream(
                            new FileInputStream(source)
                    )
            );
            dataOutputStream = new DataOutputStream(
                    new BufferedOutputStream(
                            new FileOutputStream(destination)
                    )
            );
            while (-1 != dataInputStream.read(buff)) {
                dataOutputStream.write(buff);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            //両ファイルを閉じる
            if (dataInputStream != null) {
                dataInputStream.close();
            }
            if (dataOutputStream != null) {
                dataOutputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //---------------------------------------------------------------------
    // キャッシュから元に戻す処理
    private void returnFileFromCacheToOriginalDirectory() {

        for (LineData itemFromCache : mSelectedFileList) {

            //cacheのファイル名をパス付きで編集
            String sourceFile = getApplicationContext().getCacheDir().toString();
            String lastChar = String.valueOf(sourceFile.charAt(sourceFile.length() - 1));
            if (!lastChar.equals(FILE_SEPARATOR)) {
                sourceFile = sourceFile + FILE_SEPARATOR;
            }
            sourceFile = sourceFile + itemFromCache.getName();

            //元のファイル名をパス付きで編集
            String destinationFile = itemFromCache.getAbsolutePath();
            Log.d(APPL_MAME, "FromCacheTo : "+sourceFile+" to "+destinationFile);

            //コピー実行
            executeCopyFile(sourceFile, destinationFile);

        }
    }

    //---------------------------------------------------------------------
    //指定されたファイルを切り取る。
    private void cutSelectedFiles() {

        //listViewからcheckされている項目を取得し、mSelectedFileList へ保存する。（コピー）
        setSelecteItemList();
        //アプリのキャッシュ領域にファイルを退避する。
        copySelectedFilesToCache();  //切り取りがキャンセルされたら戻せ！！！
        //コピー元ファイルを削除する。
        deleteSelectedFiles();

//debug : キャッシュの中身を見たい！！！(日本語のファイルがadbで見えない・・・)
        File[] listDirectory = new File( getApplicationContext().getCacheDir().getAbsolutePath() ).listFiles();
        for (File item : listDirectory) {
            Log.d(APPL_MAME, "Directory Cntents : "+item.getName());
        }

    }

    //---------------------------------------------------------------------
    //ファイル削除実行！！　処理
    private void executeDeleteFiles() {
        ListView listView = (ListView)findViewById(R.id.fileList01);
        CustomAdapter adapter = (CustomAdapter)listView.getAdapter();
        for (LineData item : mSelectedFileList) {
            Log.d(APPL_MAME, "deleted fie = "+item.getAbsolutePath());
            File file = item.getFile();
            file.delete();
            //adapterから削除
            int pos = adapter.getPosition(item);
            adapter.remove(item);
        }//for (LineData item : mSelectedFileList)
    }

    //---------------------------------------------------------------------
    //削除して良いか？のメッセージ表示
    private void deleteSelectedFiles() {
        //ListView から選択されたファイルを取得
        ArrayList<LineData> selectedItems = getCheckedItemsFromView();
        if (selectedItems.isEmpty()) {
            showAlertDialog(R.string.alertDlg_notSpecifidFile, MyDialogFragment.INFORMATION_DIALOG_OK);
            return;
        }
        mDeleteFiles = true;
        showAlertDialog(R.string.alertDlg_q_deleteFile, MyDialogFragment.ALERT_DIALOG_OK_NG);
    }

    //---------------------------------------------------------------------
    //ListViewの再描画処理
    private void refreshList(ListView listView) {
        CustomAdapter adapter = (CustomAdapter)listView.getAdapter();
        listView.getEmptyView().setEnabled(adapter.isEmpty()); //空であればemptyViewの内容を表示
        adapter.notifyDataSetChanged();
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
                showAlertDialog(R.string.alertDlg_q_appEnd, MyDialogFragment.ALERT_DIALOG_OK_NG);
                mAppEnd = true;
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
    private void setChecked(ListView listView, View view, int position, long id) {
        Log.d(APPL_MAME,"setChecked() start.");
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
        Log.d(APPL_MAME,"setChecked() fin.");
    }
    //---------------------------------------------------------------------------
    //checkされている項目を取得
    private ArrayList<LineData> getCheckedItemsFromView() {
        ArrayList<LineData> items = new ArrayList<>();
        ListView listView = (ListView)findViewById(R.id.fileList01);
        CustomAdapter listAdapter = (CustomAdapter) listView.getAdapter();
        for ( int i=0; i<listAdapter.getCount(); i++ ) {
            LineData item = (LineData)listAdapter.getItem(i);
            if (item.isChecked()) {
                items.add(item);
            }
        }
        Log.d(APPL_MAME,"getCheckedItemsFromView() size="+items.size());
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

    //--------------------------------------------
    //ファイルコピー
    private void setSelecteItemList() {
        //ListView から check されたitemを取得
        ArrayList<LineData> items = getCheckedItemsFromView();
        //mSelectedFileList へコピー
        mSelectedFileList.clear();
        for (LineData everyItem : items) {
            mSelectedFileList.add(everyItem);
            Log.d(APPL_MAME, "add mSelectedFileList --> "+everyItem.getAbsolutePath());
        }
        String msg = items.isEmpty() ? "コピーするファイルは有りません。" : "コピーしました。";
        Toast.makeText( getApplicationContext(), msg, Toast.LENGTH_LONG ).show();
        return;
    }

    //---------------------------------------------------------------------
    //同じディレクトリーかを確認
    private boolean isIncludedSameDirectory() {
        boolean isSame = false;

        String dstPath = ((TextView)findViewById(R.id.textView4)).getText().toString();
        //選択されたファイルが貼り付け先と同じディレクトリーを含んでいないかちぇっくする。含んでいる場合trueを返して即時終了。
        for (LineData item : mSelectedFileList) {
            int pos = item.getAbsolutePath().lastIndexOf(FILE_SEPARATOR);
            String srcPath = ( pos>0 ? item.getAbsolutePath().substring(0, pos) : FILE_SEPARATOR ); //ファイル名を除いたパスの部分だけ取得
            if (srcPath.equals(dstPath)) {
                return true;
            }
        }

        return isSame;
    }
    //---------------------------------------------------------------------
    //指定のファイルが既に存在するか否かのチェック
    private boolean isExistSameFileName() {

        ListView listView = (ListView)findViewById(R.id.fileList01);
        CustomAdapter adapter = (CustomAdapter) listView.getAdapter();
        int listCount = adapter.getCount();

        //選択されているファイルと同名のファイルの有無を確認
        for (LineData copiedItem : mSelectedFileList) {
            String fileName = copiedItem.getName();   //file name
            for (int i=0; i<listCount; i++) {
                LineData list = (LineData)adapter.getItem(i);
                if (list.getName().equals(fileName)) {
                    return true;    //あり
                }
            }
        }
        return false;   //無し
    }

    //---------------------------------------------------------------------
    //ペースト処理
    private void pasteSelectedFiles() {
        //コピーされたファイルがない場合は終了
        if (mSelectedFileList.isEmpty()) {
            showAlertDialog(R.string.alertDlg_notSpecifidFile, MyDialogFragment.INFORMATION_DIALOG_OK);  //「コピーするファイルが無いです。」
            return;
        }
        //コピー元と同じディレクトリーのファイルを含む場合はコピーしない。
        if (isIncludedSameDirectory()) {
            showAlertDialog(R.string.alertDlg_canNotCopytoSameDirectory, MyDialogFragment.INFORMATION_DIALOG_OK);  //「同じディレクトリーには貼り付け出来ません。」
            return;
        }
        //同名のファイルがある場合はコピーしない。
        if ( isExistSameFileName()) {
            showAlertDialog(R.string.alertDlg_fileAlreadyExist, MyDialogFragment.INFORMATION_DIALOG_OK);  //「既に存在します。\n確認して下さい。 」
            return;
        }
        ListView listView = (ListView)findViewById(R.id.fileList01);
        CustomAdapter adapter = (CustomAdapter)listView.getAdapter();
        //ファイルコピーを行う。
        for (LineData copiedFile : mSelectedFileList) {

            //コピー元ファイル名編集
            String sourcePath;
            if (mMode==PASTE_BY_COPY) {  //[コピー] ＆ [貼り付け]
                sourcePath = copiedFile.getAbsolutePath();
            } else if (mMode==PASTE_BY_CUT) {        //[切り取り] ＆ [貼り付け]  等
                sourcePath = getApplicationContext().getCacheDir().getAbsolutePath();
                sourcePath = sourcePath + FILE_SEPARATOR + copiedFile.getName();
            } else {
                showAlertDialog(R.string.alertDlg_IllegalParameter, MyDialogFragment.INFORMATION_DIALOG_OK);
                Log.d(APPL_MAME, "pasteSelectedFiles() : mMode = "+mMode);
                return;
            }
            //コピー先ファイル絶対パス編集
            String dstDirectory = ((TextView) findViewById(R.id.textView4)).getText().toString();
            StringBuilder stringBuilder = new StringBuilder(dstDirectory);
            if (!dstDirectory.equals(FILE_SEPARATOR)) { //ルートでなければ "/" を末尾に付加
                stringBuilder.append(FILE_SEPARATOR);
            }
            String dstPath = stringBuilder.append(copiedFile.getName()).toString(); //ファイル名を付加（同名）

            //コピー実行
            executeCopyFile(sourcePath, dstPath);

            //adapterへ追加
            adapter.add(copiedFile);
            int pos = adapter.getPosition(copiedFile);
            adapter.getItem(pos).toggle();  //check を off する。

        }//for (String copiedFile : copiedFileList)

        //貼り付けが完了したのでコピー情報を消去
        mSelectedFileList.clear();

        String msg = "貼り付けを完了しました。";
        Toast.makeText( getApplicationContext(), msg, Toast.LENGTH_LONG ).show();
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
            showAlertDialog(R.string.alertDlg_cuuerntIsRoot, MyDialogFragment.INFORMATION_DIALOG_OK);
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

// 以下はDialogFragment のため追加
    // ============================================================================================================
    // alertDialog を表示する。
    public void showAlertDialog(int msg, int type) {
        MyDialogFragment alertDlg = MyDialogFragment.newInstance(R.string.alertDlg_title,msg,type);
        // APIのサポート下限が１５なので getFragmentManager() は使用できない。
        // かわりに getSupportFragmentManager() を使用する。
        alertDlg.show(getSupportFragmentManager(),"dialog");
    }
    public void doPositiveClick(int type) {
        // Do stuff here.
        Log.i(APPL_MAME, "alertDialog : Positive click!");

        if (mDeleteFiles) { //ファイル削除の場合
            mDeleteFiles = false;
            executeDeleteFiles();
            refreshList( (ListView)findViewById(R.id.fileList01) );
        } else if (mAppEnd) { //アプリ終了フラグがＯＮの場合終了する。
            mAppEnd = false;
            finish();
        }
    }
    public void doNegativeClick() {
        // Do stuff here.
        Log.i(APPL_MAME, "alertDialog : Negative click!");
        if (mAppEnd==true) { //アプリ終了フラグがＯＮならＯＦＦする。
            mAppEnd = false;
        }
    }
// ここまで（DialogFragment のため追加）
}
