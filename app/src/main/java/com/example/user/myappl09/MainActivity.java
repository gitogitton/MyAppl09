package com.example.user.myappl09;

import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

//import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    final String PACKAGE_NAME = "ファイル一覧";
    final String FILE_SEPARATOR=System.getProperty("file.separator");
    final String ROOT_DIR=FILE_SEPARATOR;

    // コピー元ファイル（パス含む）
    final int FILE_NUM = 64;
    String[] copiedFile = new String[FILE_NUM];    // 本当は可変長で処理したいけどとりあえず指定
    StringBuilder strBld = new StringBuilder();
    StringBuffer strBuff = new StringBuffer();

    ArrayList<LineData> savedData = new ArrayList<>();   //選択時（複数可能）のデータを保存するバッファ

    // ============================================================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d( PACKAGE_NAME, "MainActivity : super.onCreate() start." );

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
        ListView listView = (ListView)findViewById(R.id.fileList01);
        listView.setOnItemClickListener(
            new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick( AdapterView<?> parent, View view, int pos, long id ) {
                    Log.d( PACKAGE_NAME, "onItemClick() start." );
                    Log.d( PACKAGE_NAME, "pos : " + pos );
                    Log.d( PACKAGE_NAME, "id : " + id );
                    Log.d( PACKAGE_NAME, "onItemClick() fin." );
                } //onItemClick()
            } // AdapterView.OnItemClickListener()
        ); // listView.setOnItemClickListener()

        //ListViewの行にロングクリック（長押し）時のlistenerを登録
        // (Activity が ListActivity からの派生の場合)
        listView.setOnItemLongClickListener(
            new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick( AdapterView parent,
                                               View view, int position, long id)  {
                    Log.d( PACKAGE_NAME, "onItemLongClick() start." );

                    // 操作された ListView を取得
                    ListView targetListView = (ListView)parent;
                    // ListView（View情報）からLineData（１行の情報） 取得
                    LineData item = (LineData) targetListView.getItemAtPosition( position );

                    Log.d( PACKAGE_NAME, "onItemLongClick() fin." );
                    return( true );
                } //onItemLongClick()
            }
        ); // End setOnItemLongClickListener()

        //Toastでメッセージ表示
        Toast.makeText( this, "ファイル一覧を表示しています。", Toast.LENGTH_LONG ).show();

    } //onCreate()

    // ============================================================================================================
    //パスを表示
    private void setPath(int id,String str) {
        TextView tv = (TextView)findViewById(id);
        tv.setText(str);
        return;
    }
    // ============================================================================================================
    //指定されたパスに従って内容を表示する処理
    // ============================================================================================================
    private void setList( String selectedPath, int listViewId ) {

        Log.d( PACKAGE_NAME, "setList() --- start. [ Indicated Path = " + selectedPath + " ]" );
        if(selectedPath.isEmpty()) { //指定が無ければrootとする。
            selectedPath=System.getProperty("file.separator");
        }
        //指定されたパスの ファイル一覧を 取得
        File[] list=getFileListFromDir(selectedPath);
        CustomAdapter arrayAdapter=new CustomAdapter(getApplicationContext());   //adapterの生成
        //directoryの内容をlistviewへ
        ListView fileList = (ListView)findViewById(listViewId); //listview取得
        fileList.setAdapter(arrayAdapter);    //listviewにadapterを指定
        for( int i=0; i<arrayAdapter.getCount(); i++) {
            Log.d(PACKAGE_NAME,"arrayAdapter list ["+i+"]"+arrayAdapter.getItem(i).toString());
        }
        arrayAdapter.notifyDataSetChanged();
        Log.d( PACKAGE_NAME, "setList() --- fin.");

    }
    // ============================================================================================================
    private File[] getFileListFromDir(String path) {

        Log.d( PACKAGE_NAME, "getFileListFromDir() start. [path="+path+"]");
        //指定したdirectory内のファイル名を取得する。
        File[] dirList = new File(path).listFiles();
        Log.d(PACKAGE_NAME,"dirList.length = "+dirList.length);
        for( File file:dirList) {
            Log.d(PACKAGE_NAME,"file = "+file.toString());
        }//for(File)
        Log.d( PACKAGE_NAME, "getFileListFromDir() fin.");

        return dirList;
    }
    // ============================================================================================================
    //
    //どこから呼ばれるかは自分で確認してみよう！！！親クラスから呼ばれてるんだろうけれど。
    //menu_main.xmlを定義し、本メソッドのオーバーライドでツールバーの端に縦点３つのやつが出てきた。
    //
    //menu選択時のアクションは「onOptionsItemSelected()」で実装
    //
    // ============================================================================================================
    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {
        getMenuInflater().inflate( R.menu.menu_main, menu );
        return true;
    }

    // ============================================================================================================
    //
    // ============================================================================================================
    @Override
    public boolean onKeyDown( int keyCode, KeyEvent event ) {

        Log.d( PACKAGE_NAME, "onKeyDown() ---start." );

        if ( KeyEvent.KEYCODE_BACK==keyCode ) {
            // ダイアログで終了を確認する。
            showAlertDialog(R.string.alertDlg_message_fin);
            return true;
        }
        Log.d( PACKAGE_NAME, "onKeyDown() fin." );
        return super.onKeyDown( keyCode, event );
    }
    // ============================================================================================================
    //getSupportActionBar().setDisplayHomeAsUpEnabled( true ); を使う際に追加した。
    // ============================================================================================================
    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        Log.d( PACKAGE_NAME, "onOptionsItemSelected() --- start. [ menu = " + item.toString() + " ]" );

        switch( item.getItemId() ) {

            case android.R.id.home:     // 戻るボタン   // ここの場合、ずっとMainActivityなのでfinish()をCallするとアプリ画面が閉じてしまう・・・のだった・・・。
                finish();
                return true;

            case R.id.action_copy :     // コピー
                if (!savedData.isEmpty()) {
                    int maxNum = savedData.size();
                    if ( maxNum > FILE_NUM ) {
                        // copied file は配列の数を指定しているのでそれを超えている場合は処理を中断する。
                        Toast.makeText(MainActivity.this, "指定できるファイルの数を超えているので処理を中断します。\nごめんなさい。", Toast.LENGTH_LONG).show();
                        break;
                    }
                    // コピー処理を行う。
                    for ( int cnt=0; cnt<maxNum; cnt++ ) {
// String型で処理した場合
                        copiedFile[cnt] = savedData.get( cnt ).getAbsolutePath();
                        if ( 0<copiedFile[cnt].length() &&
                                !copiedFile[cnt].substring(copiedFile[cnt].length()-1).equals("/") ) {     // パスが「/」で終わっていない場合はつける
                            copiedFile[cnt] += "/";
                        }
                        copiedFile[cnt] += savedData.get( cnt ).getText();

                        // 内容確認
                        Log.d( PACKAGE_NAME, "copiedFile : " + copiedFile[cnt] );

// StringBuilder型で文字列連結処理した場合
// 初期化　例１                        copiedFileBld.delete( 0, copiedFileBld.length()-1 );
                        strBld.setLength( 0 );   // 初期化　例２
                        strBld.append( savedData.get( cnt ).getAbsolutePath() );
                        if ( 0<strBld.toString().length() &&
                                !strBld.toString().substring(strBld.toString().length()-1).equals("/") ) {     // パスが「/」で終わっていない場合はつける
                            strBld.append( "/" );
                        }
                        strBld.append( savedData.get( cnt ).getText() );
                        copiedFile[cnt] = strBld.toString();

                        // 内容確認
                        Log.d( PACKAGE_NAME, "strBld : " + copiedFile[cnt] );

// StringBuffer型で文字列連結処理した場合
// 初期化　例１                        copiedFileBuff.setLength( 0 );
                        if ( 0 < strBuff.length() ) {
                            strBuff.delete(0, strBuff.length()-1);    // 初期化　例２
                        }
                        strBuff.append( savedData.get( cnt ).getAbsolutePath() );
                        if ( 0<strBuff.toString().length() &&
                                !strBuff.toString().substring(strBuff.toString().length()-1).equals("/") ) {     // パスが「/」で終わっていない場合はつける
                            strBuff.append( "/" );
                        }
                        strBuff.append( savedData.get( cnt ).getText() );
                        copiedFile[cnt] = strBuff.toString();

                        // 内容確認
                        Log.d( PACKAGE_NAME, "strBuff : " + copiedFile[cnt] );

                        //
                        // ファイルの読み込みはペースト側に持っていく。（10/18）
                        //

                    } //for ( int cnt=0 )
                } //if (!savedData.isEmpty())

                break;

            case R.id.action_paste :    // ペースト
                if (!savedData.isEmpty()) {

                    int maxNum = savedData.size();
                    for (int cnt = 0; cnt < maxNum; cnt++) {

                        // コピー元ファイルが同じディレクトリーならば次を処理する。
                        TextView dispPath = (TextView)findViewById( R.id.textView4 );
                        String destPath = dispPath.getText().toString();
                        String srcPath = savedData.get(cnt).getAbsolutePath();
                        if ( srcPath.equals( destPath )  ) {
                            Toast.makeText(MainActivity.this, "ファイル [" + savedData.get(cnt).getText() + "] は同じディレクトリーなのでコピーしません。", Toast.LENGTH_LONG).show();
                            continue;
                        }

                        //=================================================================================
                        // ファイルの内容を読み込んで保存し、その内容を指定のディレクトリに書き込む。
                        // (貼り付け)
                        //=================================================================================
                        FileInputStream srcFile = null;
                        FileOutputStream destFile = null;
                        try {
                            // コピー先ディレクトりを編集
                            strBld.setLength( 0 );      // 初期化
                            strBld.append( destPath );    // コピー先となるディレクトリーをセット
                            strBld.append( "/" );       // ファイル名を追加する前にseparatorを追加しておく。
                            int pos = copiedFile[cnt].lastIndexOf( "/" );   // コピー元からファイル名だけを取得するため最後の／を検索
                            String fileMame = copiedFile[cnt].substring( pos+1, copiedFile[cnt].length() );
                            strBld.append( fileMame );      // ファイル名を追加

                            // コピー元ファイルを読み込む。
                            srcFile = new FileInputStream(copiedFile[cnt]);         // open file
//                            readData = new BufferedInputStream(srcFile);            // read file

                            // コピー先へ書き込みを行う。ファイル名は変更なし。
                            destFile = new FileOutputStream( strBld.toString() );   // 出力先ファイル作成
                            int data;
                            while( -1 != (data=srcFile.read()) ){
                                destFile.write( data );
                            }

                        } catch (IOException e) { // FileNoException はサブクラスになってるようなのでこれだけ指定する
                            e.printStackTrace();
                        } finally {
                            // ファイルが開いているなら必ず閉じる！！
                            if (null != srcFile) {  // コピー元ファイル
                                try {
                                    srcFile.close();
                                    Log.d(PACKAGE_NAME, "コピー元ファイル [" + srcFile + "] を閉じました。");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } // if ( null!=srcFile)
                            if ( null != destFile ) {   // コピー先ファイル
                                try {
                                    destFile.close();
                                    Log.d(PACKAGE_NAME, "コピー先ファイル [" + destFile + "] を閉じました。");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } // if ( null!=destFile)
                        } // finally
                    } // for (int cnt = 0)

//
// 直近のファイルを覚えておく処理を追加するのを忘れない。
//

                    Toast.makeText(MainActivity.this, "ファイル貼り付けを終了しました。", Toast.LENGTH_LONG).show();
                } // if (!savedData.isEmpty())
                else {
                    Toast.makeText(MainActivity.this, "ファイルが選択されていません。", Toast.LENGTH_LONG).show();
                }
                break;

            case R.id.action_delete :   // 削除
                // 選択データが無い場合は即時終了する。
                if ( savedData.isEmpty() ) {
                    Toast.makeText(MainActivity.this, "削除するデータがありません。", Toast.LENGTH_LONG).show();
                    break;
                }
                // savedData の数だけ削除を繰り返す。
                StringBuilder strBld = null;
                for ( LineData data : savedData )
                {
                    // なかったら作る。
                    if ( null==strBld ) {
                        strBld = new StringBuilder();
                    }
                    // 削除対象のファイル名を絶対パスで指定する。
                    strBld.setLength(0);
                    strBld.append( data.getAbsolutePath() );
                    strBld.append("/");
                    strBld.append( data.getText() );
                    Log.d( PACKAGE_NAME, "Target file name is [ "+ strBld.toString() + " ]." );

                    // 削除を実行する。
                    File file;
                    try {
                        // deleteFile( file name ) は /data/data/パッケージ名/files/配下のファイルが削除対象・・・らしい。（illegal contain file separator が出た）
                        // 任意にパスを切る場合は File#delete() を使用する・・・・らしい。
                        //this.deleteFile( strBld.toString() );

                        file = new File( strBld.toString() );
                        if ( !file.delete() ) {
                            Log.e( PACKAGE_NAME, "ファイル削除でエラーが発生（ファイル名：" + strBld.toString() + "）" );
                        }
                    }
                    catch ( IllegalArgumentException e ) {
                        e.printStackTrace();
                    }
                } // for ( LineData data : savedData )
                Toast.makeText(MainActivity.this, "削除を終了しました。", Toast.LENGTH_LONG).show();
                break;

            case R.id.action_upFolder :      // Up Folder

                // ファイルリスト用のTextViewに表示中のパス文字列を取得
                TextView dispPath = (TextView)findViewById( R.id.textView4 );
                String currentPath = String.valueOf( dispPath.getText() );
                Log.d( PACKAGE_NAME, "onOptionsItemSelected() [ currentPath = " + currentPath + " ]" );
                // １つ上のフォルダパス文字列を取得
                String upFolderPath = getUpFolderPath( currentPath );
                // １つ上のフォルダパスのファイルリストを表示
                setList( upFolderPath, R.id.fileList01 );
                break;

            case R.id.action_upRoot :      // Up Root
                // ルートフォルダのファイルリストを表示
                setList( System.getProperty( "file.separator" ), R.id.fileList01 );
                break;

            default:
                //do nothing
                break;
        }
        return super.onOptionsItemSelected( item );
    }

    // ============================================================================================================
    //
    //指定されたパスの一つ上の階層にあたるパスを編集し返す。
    //
    // ============================================================================================================
    private  String getUpFolderPath( @NonNull String currentPath ) {

        Log.d( PACKAGE_NAME, "getUpFolerPath() --- start. [ currentPath = " + currentPath + " ]" );

        //引数を確認する。おかしな場合はルートディレクトリーを返却しておく。
        if ( currentPath.isEmpty() ) {  // android studio v3 に更新した時エラーになった。gradle のバージョンが合わないとか。メッセージに従って更新すると直った。
            Toast.makeText( MainActivity.this, "指定されたパスが異常につき、\nルートを指定する。", Toast.LENGTH_LONG ).show();
            return ( "/" );      //よくわからんがルートを返しておこうか。
        }
        //既にルートディレクトリーの場合は何もしないで終了
        if ( currentPath.equals( "/" ) ) {
            Toast.makeText( MainActivity.this, "現在ルートディレクトリーです。", Toast.LENGTH_LONG ).show();
            return ( "/" );
        }

        //
        // 一つ上のディレクトリーのパスを編集して返す。
        //
        //      最後が”/”で終わっていれば2つ目の”/”、”/”でなければ1つ目の”/”を検索し、末尾までの文字列を削除する。
        //      ルートが指定されている場合のチェックは上で実施済みなので考慮しない処理とする。
        //
        String tempPath = currentPath;      //編集用にパスをコピー
        // パス区切り文字でパス文字列を分割する。
        String fileSeparator = System.getProperty( "file.separator" );
        Log.d( PACKAGE_NAME, "File.pathSeparator = " + fileSeparator );
        Log.d( PACKAGE_NAME, "tempPath = " + tempPath );
        String[] folderName = tempPath.split( fileSeparator, 0 );
        if ( folderName.length >= 1 ) {
            Log.d(PACKAGE_NAME, "folderName.length = " + folderName.length + ", " + folderName[0]);
        }
        // folderName.lenght >= 2 のケース
        // (folderName.lenght-1)個のデータを頭から順にセパレータをはさみながら編集する。
        tempPath = "";          //編集用パスをクリアする。
        tempPath = tempPath.concat( fileSeparator );   //先頭を"/"に設定
        int setNum = folderName.length-1;       // 今のフォルダの上までがほしいので最後のフォルダはカットする。
        for ( short i=0; i<setNum; i++ ) {
            Log.d( PACKAGE_NAME, "concat folder name = " + folderName[i] );
            tempPath = tempPath.concat( folderName[ i ] );         // フォルダ文字列セット
            if ( 0 < folderName[i].length() && i != setNum-1 ) {  //フォルダ名が空であったり、最後のフォルダの後には"/"をつけない。
                tempPath = tempPath.concat( fileSeparator );      // separatorセット
            }
        }
// debug
        Log.d( PACKAGE_NAME, "Returned Path = " + tempPath  );

        return( tempPath );
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
        Log.i(PACKAGE_NAME, "alertDialog : Positive click!");
        finish();
    }
    public void doNegativeClick() {
        // Do stuff here.
        Log.i(PACKAGE_NAME, "alertDialog : Negative click!");
    }
// ここまで（DialogFragment のため追加）

}
