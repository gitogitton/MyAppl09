package com.example.user.myappl09;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
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
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static android.os.Environment.DIRECTORY_DOCUMENTS;
import static android.support.v4.content.PermissionChecker.PERMISSION_GRANTED;

public class MainActivity extends AppCompatActivity {

    private final String APPL_MAME = "ファイル一覧";
    private final String FILE_SEPARATOR = System.getProperty("file.separator");
    private final String ROOT_DIR = FILE_SEPARATOR;
    private final String INIT_DIR = "/storage";     //機種変更でアンドロイドのバージョンが上がった。で、権限強化で（？）ルートにアクセスできないので初期表示を変える。
    private final String INTERNAL_STORAGE = "/sdcard";  //なぜsdcardなる名前なのかわからない・・・

    //メニューの状態定義
    private final int MENU_NORMAL = 0;          //通常メニュー
    private final int MENU_COPY = 1;            //［コピー］実行中のメニュー
    private final int MENU_CUT = 2;             //［切り取り］実行中のメニュー
    private final int MENU_DELETE = 3;          //［削除］実行中のメニュー
    private int mCurrentMenu = MENU_NORMAL;    //実行中のメニュー

    //Request code
    private final int MY_PERMISSIONS_REQUEST_READWRITE_EXTERNAL_STORAGE = 1; //WRITE_EXTERNAL_STORAGE パーミッション要求

    //ダイアログでＯＫされた時の処理
    private boolean mAppEnd = false;            //フラグ＝アプリ終了が指定された
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

        //パスを表示
        TextView textView = (TextView)findViewById(R.id.textView4);
        textView.setText( INIT_DIR );

        //ListView取得
        ListView listView = (ListView)findViewById(R.id.fileList01);
        //ListViewが空の場合のＶｉｅｗを指定しておく
        View emptyView = findViewById(R.id.emptyView);
        listView.setEmptyView(emptyView);

        //機種変更に伴う処理追加（android4.4からandroid7.1.1へ変わった）
        //パーミッションチェックを追加（api23から変わったらしい）
        if ( Build.VERSION.SDK_INT >= 23 ) {
            checkPermissions();
        }
        else {
            //ルートの内容をリストに表示
            showListOfDirectory( INIT_DIR );
            //Toastでメッセージ表示
            Toast.makeText( getApplicationContext(), "ファイル一覧を表示しています。", Toast.LENGTH_LONG ).show();
        }

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
                            //パス情報取得
                            TextView textView = (TextView)findViewById( R.id.textView4 );
                            String currentPath = textView.getText().toString();
                            //フルパスファイル名編集
                            String filePath = currentPath + "/" + item.getName();
                            //暗黙的Intent
                            Intent intent = new Intent( Intent.ACTION_VIEW );
                            File file = new File( filePath );
                            Uri uri = FileProvider.getUriForFile( MainActivity.this,
                                    getApplicationContext().getPackageName() + ".provider",
                                    file );
                            String contentType = getContentResolver().getType( uri );
                            intent.setDataAndType( uri, contentType );
                            intent.addFlags( Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                                    | Intent.FLAG_GRANT_READ_URI_PERMISSION );
                            PackageManager packageManager = getPackageManager();
                            List activities = packageManager.queryIntentActivities( intent,
                                    PackageManager.MATCH_DEFAULT_ONLY );
                            boolean isIntentSafe = activities.size() > 0;
                            if( isIntentSafe )
                            {
                                startActivity( intent );
                            }
                        }
                    }
                }
        );
        //長押し
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(APPL_MAME,"onItemLongClick() start. [pos="+position+"/id="+id+"]");

                //クリックされたのがフォルダの場合は何もしない。ファイルなら選択状態をトグルする。
                LineData item = (LineData)parent.getItemAtPosition(position);
                if (item.isDirectory()) {
//                    showListOfDirectory(item.getAbsolutePath());
                } else {
                    //チェック状態を反転する。
                    item.toggle();
                    refreshList((ListView)findViewById(R.id.fileList01));
                }

                return true; //onItemClick()に飛ばさなくていいのでtrueで返す。
            }
        });
    } //onCreate()

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
        Log.d( APPL_MAME, "onActivityResult() start." );
        if ( requestCode == 1 ) {
            Log.d( APPL_MAME, "onActivityResult() requestCode="+requestCode );
        } else {
            Log.d( APPL_MAME, "onActivityResult() requestCode is invalid ("+requestCode+")" );
        }
    }

    private void checkPermissions() {
        //Apiバージョン２３以上なら！！
        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {
            int permissionRead = checkSelfPermission( Manifest.permission.READ_EXTERNAL_STORAGE );
            int permissionWrite = checkSelfPermission( Manifest.permission.READ_EXTERNAL_STORAGE );
            if ( permissionRead == PERMISSION_GRANTED && permissionWrite == PERMISSION_GRANTED ) {
                showListOfDirectory( INIT_DIR );
            }
            else {
                reqPermissions();
//                Toast.makeText( getApplicationContext(), "パーミッションエラー", Toast.LENGTH_LONG ).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if ( requestCode == MY_PERMISSIONS_REQUEST_READWRITE_EXTERNAL_STORAGE) {
            if ( ( grantResults.length >= 2 ) &&
                    ( grantResults[0] == PermissionChecker.PERMISSION_GRANTED &&
                            grantResults[1] == PermissionChecker.PERMISSION_GRANTED ) ) { //許可された場合
                //ルートの内容をリストに表示
                showListOfDirectory( INIT_DIR );
            }
        }
    }

    private void reqPermissions() {
        boolean should = ActivityCompat.shouldShowRequestPermissionRationale(  this,
                Manifest.permission.READ_EXTERNAL_STORAGE  );
        // パーミッションが必要である理由をユーザに説明すべきときはこちら！！
        if ( should ) {

            // Show an expanation to the user *asynchronously* -- don't block
            // this thread waiting for the user's response! After the user
            // sees the explanation, try again to request the permission.

            //説明のためのUIを自分で実装しなきゃならない！！
            Log.d( APPL_MAME, "shouldShowRequestPermissionRationale() == true" );

// 権限チェックした結果、持っていない場合はダイアログを出す
            new AlertDialog.Builder(this)
                    .setTitle("パーミッションの追加理由")
                    .setMessage("このアプリではディクス読み込み／書き込みの権限が必要です。")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                                    MY_PERMISSIONS_REQUEST_READWRITE_EXTERNAL_STORAGE);
                        }
                    })
                    .create()
                    .show();

        } else {    //ユーザに説明が必要ない場合はこちら

            ActivityCompat.requestPermissions( this,
                    new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE },
                    MY_PERMISSIONS_REQUEST_READWRITE_EXTERNAL_STORAGE);

            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.
        }
    }

    // ============================================================================================================
    //メニュー表示
    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {
        Log.d(APPL_MAME, "onCreateOptionsMenu() start.");
        getMenuInflater().inflate( R.menu.menu_main, menu );
        return super.onCreateOptionsMenu(menu);
    }
    //メニューの動的な切り替え
    @Override
    public boolean onPrepareOptionsMenu( Menu menu ) {
        Log.d(APPL_MAME, "onPrepareOptionsMenu() start.");
        //実行中のメニューにより使えるメニューを変更する
        if (mCurrentMenu==MENU_COPY) {
            //［貼り付け］［キャンセル］有効
            menu.findItem(R.id.action_paste).setVisible(true);
            menu.findItem(R.id.action_cancel).setVisible(true);
            menu.findItem(R.id.action_copy).setVisible(false);
            menu.findItem(R.id.action_cut).setVisible(false);
            menu.findItem(R.id.action_delete).setVisible(false);
        } else if (mCurrentMenu==MENU_CUT) {
            //［貼り付け］［キャンセル］有効
            menu.findItem(R.id.action_paste).setVisible(true);
            menu.findItem(R.id.action_cancel).setVisible(true);
            menu.findItem(R.id.action_copy).setVisible(false);
            menu.findItem(R.id.action_cut).setVisible(false);
            menu.findItem(R.id.action_delete).setVisible(false);
        } else if (mCurrentMenu==MENU_NORMAL) {
            //［コピー］［切り取り］［削除］有効
            menu.findItem(R.id.action_paste).setVisible(false);
            menu.findItem(R.id.action_cancel).setVisible(false);
            menu.findItem(R.id.action_copy).setVisible(true);
            menu.findItem(R.id.action_cut).setVisible(false);
            menu.findItem(R.id.action_delete).setVisible(true);
        } else if ( mCurrentMenu==MENU_DELETE) {
            menu.findItem(R.id.action_paste).setVisible(false);
            menu.findItem(R.id.action_cancel).setVisible(true);
            menu.findItem(R.id.action_copy).setVisible(false);
            menu.findItem(R.id.action_cut).setVisible(false);
            menu.findItem(R.id.action_delete).setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }
    // ============================================================================================================
    //メニュー選択時のイベント処理
    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        Log.d( APPL_MAME, "onOptionsItemSelected() --- start. [ menu = " + item.toString() + " ]" );
        switch( item.getItemId() ) {
            case R.id.action_appEnd : //アプリ終了
                showAlertDialog(R.string.alertDlg_q_appEnd, MyDialogFragment.ALERT_DIALOG_OK_NG);
                mAppEnd = true;
                break;
            case R.id.action_upFolder : //上のディレクトリへ
                String path = getParentDirectoryName();
                if(path.length()>0) {
                    if ( checkPremissionDenied( path ) ) { //機種変更でルートへアクセスできなくなった。ディレクトリアクセスが厳しくなってるので処理追加。
//                        Toast.makeText( getApplicationContext(), "Permission Denied !!", Toast.LENGTH_LONG ).show();
                        // カレントディレクトリが /storage , /sdcard であれば /sdcard , /storage へ移動する。（全体的に見直すのが面倒なので・・）
                        TextView textView = (TextView)findViewById(R.id.textView4);
                        String currentPath = textView.getText().toString();
                        if ( currentPath.equals( INIT_DIR ) ) {
                            showListOfDirectory( INTERNAL_STORAGE ); //内部ストレージトップを表示
                        } else if ( currentPath.equals( INTERNAL_STORAGE ) ) {
                            showListOfDirectory( INIT_DIR ); //外部ストレージトップを表示
                        }
                    }
                    else {
                        showListOfDirectory( path );
                    }
                }
                break;
//機種変更でルートアクセスが出来なくった。ディレクトリへのアクセスのPermissionが厳しくなって意味をなさない。
//            case R.id.action_upRoot : //ルートへ
//                showListOfDirectory(ROOT_DIR);
//                break;
            case R.id.action_copy : //コピー
                //コピー対象のファイルの情報を保存（実際のコピー処理はペーストされたタイミングで行う）
                if ( setSelectedItemList() ) {
                    mCurrentMenu = MENU_COPY;
                    invalidateOptionsMenu();
                }
                break;
            case R.id.action_paste : //貼り付け
                pasteSelectedFiles();
                refreshList((ListView)findViewById(R.id.fileList01));
                mCurrentMenu = MENU_NORMAL;
                invalidateOptionsMenu();
                break;
            case R.id.action_cut : //切り取り
                if ( cutSelectedFiles() ) {
                    refreshList((ListView)findViewById(R.id.fileList01));
                    mCurrentMenu = MENU_CUT;
                    invalidateOptionsMenu();
                }
                break;
            case R.id.action_cancel : //実行取り消し
                returnFileFromCacheToOriginalDirectory();
                showCurrentList();
                refreshList( (ListView)findViewById(R.id.fileList01) );
                mSelectedFileList.clear();
                mCurrentMenu = MENU_NORMAL;
                invalidateOptionsMenu();
                break;
            case R.id.action_delete : //削除
                //listViewからcheckされている項目を取得し、mSelectedFileList へ保存する。
                if ( !setSelectedItemList() ) {
                    break;
                }
                confirmDeletion();
                mCurrentMenu = MENU_DELETE;
                invalidateOptionsMenu();
                break;
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
            //コピー先パス編集
            String dstFilePath = getApplicationContext().getCacheDir().toString();
            Log.d(APPL_MAME, "Cache Directory : " + dstFilePath);
            if (dstFilePath.length()>0) {
                char c = dstFilePath.charAt( dstFilePath.length()-1 );
                String lastChar =String.valueOf(c);
                if (!lastChar.equals(FILE_SEPARATOR)) {
                    //末尾に区切り文字を入れる。
                    dstFilePath = dstFilePath+FILE_SEPARATOR;
                }
                //コピー先パス編集
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
    private boolean cutSelectedFiles() {

        //listViewからcheckされている項目を取得し、mSelectedFileList へ保存する。
        if ( !setSelectedItemList() ) {
            return false;
        }

        //アプリのキャッシュ領域にファイルを退避する。
        copySelectedFilesToCache();  //切り取りがキャンセルされたら戻せ！！！

        //コピー元ファイルを削除する確認ダイアログを表示
        confirmDeletion();

//debug : キャッシュの中身を見たい！！！(日本語のファイルがadbで見えない・・・)
        File[] listDirectory = new File( getApplicationContext().getCacheDir().getAbsolutePath() ).listFiles();
        for (File item : listDirectory) {
            Log.d(APPL_MAME, "Directory Cntents : "+item.getName());
        }
        return true;
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
    private void confirmDeletion() {
        //ListView から選択されたファイルを取得
        ArrayList<LineData> selectedItems = getCheckedItemsFromView();
        if (selectedItems.isEmpty()) {
            showAlertDialog(R.string.alertDlg_notSpecifidFile, MyDialogFragment.INFORMATION_DIALOG_OK);
            return;
        }
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
                //
                //※）2018.08.17
                // 機種変更でOSバージョンアップしたためルートにアクセスできない。ディレクトリ変更時にRead出来るかチェックするので完成後はここに来ない（はず）。
                //

                //ダイアログで終了を確認する。
                showAlertDialog(R.string.alertDlg_q_appEnd, MyDialogFragment.ALERT_DIALOG_OK_NG);
                mAppEnd = true;
            } else {
                //直上のディレクトリーのパス取得
                String parentPath = getParentDirectoryName();
                if ( checkPremissionDenied( parentPath ) ) {
//                    Toast.makeText( getApplicationContext(), "Permission Denied !!", Toast.LENGTH_LONG ).show();
                    // カレントディレクトリが /storage , /sdcard であれば /sdcard , /storage へ移動する。（全体的に見直すのが面倒なので・・）
                    if ( path.equals( INIT_DIR ) ) {
                        showListOfDirectory( INTERNAL_STORAGE ); //内部ストレージトップを表示
                    } else if ( path.equals( INTERNAL_STORAGE ) ) {
                        showListOfDirectory( INIT_DIR ); //外部ストレージトップを表示
                    }
                }
                else {
                    showListOfDirectory(parentPath);
                }
            }
            return true;
        }
        Log.d( APPL_MAME, "onKeyDown() fin." );
        return super.onKeyDown( keyCode, event );
    }

    private boolean checkPremissionDenied( String path ) {
        File checkedDirectory = new File( path );
        if ( checkedDirectory == null ) {
            //new 失敗だからpermission deniedにする
            return true;
        }
        if ( !checkedDirectory.canRead() ) {
            //リード不可のディレクトリだからpermission deniedにする
            return true;
        }
        return false;
    }

    // ============================================================================================================
    //指定されたディレクトリの内容をリストに設定
    private void showListOfDirectory(String directoryName) {
        Log.d(APPL_MAME,"showListOfDirectory start. [args="+directoryName+"]");

        //パスを表示
        TextView textView = (TextView)findViewById(R.id.textView4);
        textView.setText(directoryName);
        //リストを表示
        ListView listView = (ListView)findViewById(R.id.fileList01);
        setList(directoryName, listView);

        Log.d(APPL_MAME,"showListOfDirectory end.");
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
    private boolean setSelectedItemList() {
        //ListView から check されたitemを取得
        ArrayList<LineData> items = getCheckedItemsFromView();
        //mSelectedFileList へコピー
        mSelectedFileList.clear();
        for (LineData everyItem : items) {
            mSelectedFileList.add(everyItem);
            Log.d(APPL_MAME, "add mSelectedFileList --> "+everyItem.getAbsolutePath());
        }
//        if (!mSelectedFileList.isEmpty()) {
//            Toast.makeText(getApplicationContext(), "内部で記憶！！", Toast.LENGTH_LONG).show();
//        }
        return mSelectedFileList.isEmpty() ? false:true;
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
            if (mCurrentMenu==MENU_COPY) {  //[コピー] ＆ [貼り付け]
                sourcePath = copiedFile.getAbsolutePath();
            } else if (mCurrentMenu==MENU_CUT) {        //[切り取り] ＆ [貼り付け]  等
                sourcePath = getApplicationContext().getCacheDir().getAbsolutePath();
                sourcePath = sourcePath + FILE_SEPARATOR + copiedFile.getName();
            } else {
                showAlertDialog(R.string.alertDlg_IllegalParameter, MyDialogFragment.INFORMATION_DIALOG_OK);
                Log.d(APPL_MAME, "pasteSelectedFiles() : mMode = "+mCurrentMenu);
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

        if (mCurrentMenu==MENU_CUT || mCurrentMenu==MENU_DELETE) { //ファイル削除、又は、切り取りの場合
            executeDeleteFiles();
            refreshList( (ListView)findViewById(R.id.fileList01) );
            mCurrentMenu = MENU_NORMAL;
            invalidateOptionsMenu();
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
        } else if (mCurrentMenu==MENU_CUT || mCurrentMenu==MENU_DELETE ) { //ファイル削除、又は、切り取りの場合
            cancelAction(mCurrentMenu);
            returnFileFromCacheToOriginalDirectory();
            refreshList( (ListView)findViewById(R.id.fileList01) );
            mCurrentMenu = MENU_NORMAL;
            invalidateOptionsMenu();
        }
    }
// ここまで（DialogFragment のため追加）
    //-------------------------------------------------------------------------------------
    //編集中のリストをもとに戻す。
    private void cancelAction(int action) {
        ListView listView = (ListView)findViewById(R.id.fileList01);
        CustomAdapter adapter = (CustomAdapter)listView.getAdapter();
        for (LineData item : mSelectedFileList) {
            for (int i=0; i<adapter.getCount(); i++) {
                //ファイル名で検索
                String path =  adapter.getItem(i).getAbsolutePath();
                if (item.getAbsolutePath().equals(path)) {
                    adapter.getItem(i).setChecked(false);
                    break;
                }
            }
        }
    }
    //-------------------------------------------------------------------------------------
    //表示中のディレクトリーの内容を取得
    private void showCurrentList() {
        ListView listView = (ListView)findViewById(R.id.fileList01);
        TextView textView = (TextView)findViewById(R.id.textView4);
        setList(textView.getText().toString(), listView);
        CustomAdapter adapter = (CustomAdapter) listView.getAdapter();
        adapter.notifyDataSetChanged();
    }

    //引用）From：https://tech.nosuz.jp/2016/12/android-fast-file-copy/
    private boolean copy(File src, File dst) {
        boolean result = false;

        FileInputStream inStream = null;
        FileOutputStream outStream = null;

        try {
            inStream = new FileInputStream(src);
            outStream = new FileOutputStream(dst);
            FileChannel inChannel = inStream.getChannel();
            FileChannel outChannel = outStream.getChannel();
            long pos = 0;
            while (pos < inChannel.size()) {
                pos += inChannel.transferTo(pos, inChannel.size(), outChannel);
            }
            result = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inStream != null) inStream.close();
                if (outStream != null) outStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if ((! result) && dst.exists()) dst.delete();

        return result;
    }
}

