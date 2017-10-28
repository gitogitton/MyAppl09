package com.example.user.myappl09;

import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    final String TAG_SD = "MyAppl09";
    final int RESULT_OK = -1;
    final int RESULT_CANCEL = 0;

    // コピー元ファイル（パス含む）
    final int FILE_NUM = 64;
    String[] copiedFile = new String[FILE_NUM];    // 本当は可変長で処理したいけどとりあえず指定
    StringBuilder strBld = new StringBuilder();
    StringBuffer strBuff = new StringBuffer();
    // コピー元ファイルの内容
    BufferedInputStream readData = null;

    CustomAdapter aAdapter;                 // ListView のアダプター
    int resultAlertDialog = RESULT_OK;  // 確認ダイアログのＯＫ／ＣＡＮＣＥＬボタンの結果を格納
    TextView emptyTextView;                //ListViewに表示する内容が０件の場合に表示するTextView
    ArrayList<LineData> savedData = new ArrayList<>();   //選択時（複数可能）のデータを保存するバッファ

    // ============================================================================================================
    //
    // ============================================================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Log.d( TAG_SD, "MainActivity : super.onCreate() fin." );

        setContentView(R.layout.activity_main);
        Log.d( TAG_SD, "MainActivity : setContentView( activity_main ) fin." );

        //ツールバー
        Toolbar toolbar = (Toolbar) findViewById( R.id.menu_main_1 );
        Log.d( TAG_SD, "findViewById( menu_main_1 ) fin." );
        toolbar.setTitle( "ファイル一覧" );
        setSupportActionBar( toolbar );
//
// 今回はMainAvtivityのみを使用してリスト表示している。
// これでHomeに戻ると前の階層にはならず終了してしまうので使用しない事にする。
// サブ画面を使ってサブ画面のリスト表示内容を変えるという作り方に変えれば使えそう。
// 階層を１つ上がる機能を実装する。
// （UP／BACKはActivityに関するものであり、表示内容など状態を戻したりするものではない、ようです。）
//
//        getSupportActionBar().setDisplayHomeAsUpEnabled( true );        //UPナビゲーション有効：１つまえに戻る　「←」が表示されるだけか・・・
//


// Don't delete the following 2 lines!!! (this is sample) --------------
//        //HOMEに戻るための「←」が出たぞ！！！！
//        getSupportActionBar().setDisplayHomeAsUpEnabled( true );    //.setHomeAsUpEnabled( true );
//-----------------------------------------------------------------------

        //
        // ファイルリストを表示するListViewを取得
        //
        ListView fileList = (ListView)findViewById( R.id.fileList01 );
        Log.d( "MyAppl09", "findViewById( fileList01 ) fin." );
        //
        // ファイルリストを表示する
        //      第一引数：       表示対象パス
        //      第二引数：       表示対象のListView
        //
        setDataOnListView( null, fileList );       //初期表示はルートディレクトリー。nullで指定。
        Log.d( "MyAppl09", "setDataOnListView() fin." );

        //
        //ListViewの行にクリック時のlistenerを登録
        //
        // onItemClick() ( onItemlick() )の引数について
        //      第１引数	AdapterView parent	イベント発生のListView
       //       第２引数	View view	選択されたリスト項目
        //      第３引数	int position	選択されたリスト項目の位置（最小値：0）
        //      第４引数	long id	選択されたリスト項目のID（最小値：0）
        //
        //＜＜注意＞＞
        //      new viewのクラスt.OnClickListener(){}の内部では「this」は「View.OnClickListenerを実装した無名の内部クラス」という扱い。
        //      だから、「this」を指定しても大元のViewは伝わらずエラーとなる。
        //      対応として、「MainActivity.this」にするか「getApplicationContext()」にする必要がある。
        //
        fileList.setOnItemClickListener(
        new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick( AdapterView<?> parent, View view, int pos, long id ) {
                Log.d( TAG_SD, "onItemClick() start." );
                Log.d( TAG_SD, "pos : " + pos );
                Log.d( TAG_SD, "id : " + id );
                // 選択アイテムを取得
                ListView listView = (ListView)parent;
                LineData item = (LineData) listView.getItemAtPosition( pos );
                Log.d( TAG_SD, "listView.getItemPosition() fin." );
                //
                //Itemがフォルダの時そのフォルダの中身をList（dir）する。
                //
                if ( item.getFolderOrFile() ) {

// コピー先は違うディレクトリーになるので邪魔。
//
//                    // 選択中データがあれば「選択中はフォルダ移動は出来ない」とユーザに告げる。一括で選択中を解除する事もできる。
//                    if ( !savedData.isEmpty() ) {
//                        showAlertDialog();
//                        return;
//                    }

                    // ルートディレクトリー直下なら「／」をつけない。ついてるんで、２つになっちゃう。最初にしっかり考えないから・・・。今はそういう時期じゃないの、勉強優先。
                    if ( item.getAbsolutePath().equals( System.getProperty( "file.separator" ) ) ) {

                        setDataOnListView( item.getAbsolutePath() + item.getText(), (ListView)parent );
                    }
                    else {
                        setDataOnListView(item.getAbsolutePath() + System.getProperty("file.separator") + item.getText(), (ListView) parent);
                    }

//                    // 選択中データをキャンセル
//                    savedData.clear();
// debug                    Log.d( TAG_SD,"num of saved data after saveData.clear() = " + savedData.size() );

//　→　結局全部書き換えてセットしているからいらなくなくなった・・・あってる？                    aAdapter.notifyDataSetChanged();
                }
                else {      //タップされたのがファイルでもフォルダでもない場合、、、
                    Toast.makeText( MainActivity.this, "フォルダではないです。", Toast.LENGTH_SHORT ).show();
                }
                Log.d( TAG_SD, "onItemClick() end." );

            } //onItemClick()

        } // AdapterView.OnItemClickListener()

        ); // fileList.setOnItemClickListener()

        //
        //ListViewの行にロングクリック（長押し）時のlistenerを登録
        // (Activity が ListActivity からの派生の場合)
        //
        // ListAcitivyとはListViewを持っているActivityらしい。知らなかった。
        // 今でも非推奨にならず使えるのかは知らないが、知ってて損はないかな。。。
        //
        //
        //  onItemLongClick()
        //      ＜戻り値＞       true：   残りのイベントの処理はせず終了
        //                       false：  残りのイベントの処理はして終了
        //      第１引数    AdapterView parent      イベントが起きたListView
        //      第２引数    View view	            選択されたリスト項目
        //      第３引数    int position	        選択されたリスト項目の位置(０〜）
        //      第４引数    long id	                選択されたリスト項目のIDを示す値（０〜）
        //
        // 【注意】
        //      「Activity の継承が ListActivity の場合は onListItemLongClick はサポートされていない」とか
        //      その場合、「setOnItemLongClickListener を登録して、そこで onItemLongClick の中に処理を記述」とかって
        //      とあるサイトに書いてあったが今一つわからないのだ。覚えてたら ListActivity を使った Activity にも挑戦してみる。
        //
        fileList.setOnItemLongClickListener(
                new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick( AdapterView parent,
                                                   View view, int position, long id)  {
                        // ロングタッチ時の処理を記述する。
//debug                         Toast.makeText( MainActivity.this, "長押ししてしまったねぇ～。\n後悔するでぇ～。", Toast.LENGTH_LONG ).show();

                        // 操作された ListView を取得
                        ListView targetListView = (ListView)parent;

                        // ========================================================================================================
                        // ここでは（ロングタッチされた場合）、タッチされた項目を選択状態とする。
                        //　＊ 「選択状態解除」は他の場所をタップした時とする。
                        //　＊「選択状態」とは
                        //      ・アイコンをチェックマーク付きのものにする。
                        //      ・選択されたリストのバックグラウンド色を変える。単色系の落ち着いたのにしよう。アグレッシブな気分で作ってしまうと、ショッキング系かも・・
                        //      ・「選択状態」では、選択された情報を内部で「選択解除」されるまで保持し解除されたら破棄する。
                        //      ・選択できるのはファイルのみとしフォルダは含まない。複数選択は出来ないものとする。
                        //      ・この状態の時は「コピー」と「削除」を実行可能とする。上に上がる、ルートに戻る、ペースト機能は出来ない。
                        // ========================================================================================================

                        // ListView（View情報）からLineData（１行の情報） 取得
                        LineData item = (LineData) targetListView.getItemAtPosition( position );
                        Log.d( TAG_SD, "onItemLongClick().listView.getItemPosition() fin." );

                        // ファイルが選択されたのかを確認。ファイル以外は処理しない。
                        if ( item.getFolderOrFile() ) {
                            Toast.makeText( MainActivity.this, "フォルダは選択できません。", Toast.LENGTH_LONG ).show();
                            Log.d( TAG_SD, "onItemClick() フォルダが選択されたので処理しません。" );
                            return( true );
                        }

                        //選択済みを設定
// 長押しした時に見えているすべての行に対して true が設定されているようだ・・・。おいおい。
//                        if ( targetListView.isSelected() ) {
//                            targetListView.setSelected( false );
//                        }
//                        else {
//                            targetListView.setSelected( true );
//                        }
// 長押しした時に見えているすべての行に対して true が設定されているようだ・・・。おいおい、なぜ？？　　↑↑↑
//で、独自クラスの LineData のフラグを使用することにした。↓↓↓
                        Drawable icon;
                        if ( item.getSelected() ) {
                            item.setSelected( false );
                            //選択された項目のアイコンを変更 → ファイルアイコン
                            icon = ContextCompat.getDrawable( MainActivity.this, R.drawable.ic_file );
                            //選択中データリストから削除
                            savedData.remove( item );
                        }
                        else {
                            item.setSelected( true );
                            icon = ContextCompat.getDrawable( MainActivity.this, R.drawable.ic_assignment_turned_in_black_24dp );
                            //選択された項目を保存
                            savedData.add( item );
                        }
                        Log.d( TAG_SD, "選択中データの数： " + savedData.size() );

                        //選択された項目のアイコンを変更 → チェックアイコン
                        //
                        //ICONの表示位置を設定
                        //      引数：     座標 x, 座標 y, 幅, 高さ
                        //
                        //　＊これ忘れるとIcon表示されないので・・・。
                        icon.setBounds( 0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight() );
                        //TextViewの左にICONを設定
                        item.getTextView().setCompoundDrawables( icon, null, null, null );

                        //選択された項目のバックグランド色を変更
                                    //
                                    // 状態毎の色は listselector.xml で定義。Main_activity.xml のListViewで指定する事で実装完了。
                                    //


                        //lsitview にselectionを設定
                        targetListView.setSelection( position );

                        // ListView が変更されたことを通知
                        aAdapter.notifyDataSetChanged();

                        //=====================================================================================
                        // true でリターンする理由 ＝ false で返すとタップイベントを処理してしまう。発生していること自体が「？」なのだが。
                        // true = 残りの処理を処理して終了 : という事を考えるとタップイベントが発生し長押しも発生しているという事？
                        // わからんのですが、true で返す。タップイベントは長押し時に不要なので。
                        //=====================================================================================
                        return( true );

                    } //onItemLongClick()
                }
        ); // End setOnItemLongClickListener()
        //
        //Toastでメッセージ表示
        //
        Toast.makeText( this, "ファイル一覧を表示しています。", Toast.LENGTH_LONG ).show();
//
// ディレクトリー内のファイルを表示する。（ここまで）

    } //onCreate()

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

        Log.d( TAG_SD, "onKeyDown() ---start." );

// 戻るキーを一時的に無効にする処理　→　コメントアウト（2017/07/11）
//        if ( keyCode == KeyEvent.KEYCODE_BACK ) {
//            // 戻るボタンの処理
//            Toast.makeText( MainActivity.this, "戻るキーは押さないで！！！\n\n押すと不幸が起こるので禁止してます。", Toast.LENGTH_SHORT ).show();
//            return false;
//        }

        return super.onKeyDown( keyCode, event );
    }

    // ============================================================================================================
    //
    //getSupportActionBar().setDisplayHomeAsUpEnabled( true ); を使う際に追加した。
    //
    // ============================================================================================================
    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {

        Log.d( TAG_SD, "onOptionsItemSelected() --- start. [ menu = " + item.toString() + " ]" );

        switch( item.getItemId() ) {

            case android.R.id.home:     // 戻るボタン   // ここの場合、ずっとMainActivityなのでfinish()をCallするとアプリ画面が閉じてしまう・・・のだった・・・。
            {
                //confirmFinishDialog();
                finish();
                return true;
            }

            case R.id.action_copy :     // コピー
            {
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
                        Log.d( TAG_SD, "copiedFile : " + copiedFile[cnt] );

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
                        Log.d( TAG_SD, "strBld : " + copiedFile[cnt] );

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
                        Log.d( TAG_SD, "strBuff : " + copiedFile[cnt] );

                        //
                        // ファイルの読み込みはペースト側に持っていく。（10/18）
                        //

                    } //for ( int cnt=0 )
                } //if (!savedData.isEmpty())

                break;
            }
            case R.id.action_paste :    // ペースト
            {
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
                            // コピー元ファイルを読み込む。
                            srcFile = new FileInputStream(copiedFile[cnt]);         // open file
//                            readData = new BufferedInputStream(srcFile);            // read file

                            // コピー先ディレクトりを編集
                            strBld.setLength( 0 );      // 初期化
                            strBld.append( destPath );    // コピー先となるディレクトリーをセット
                            strBld.append( "/" );       // ファイル名を追加する前にseparatorを追加しておく。
                            int pos = copiedFile[cnt].lastIndexOf( "/" );   // コピー元からファイル名だけを取得するため最後の／を検索
                            String fileMame = copiedFile[cnt].substring( pos+1, copiedFile[cnt].length() );
                            strBld.append( fileMame );      // ファイル名を追加

                            // コピー先へ書き込みを行う。ファイル名は変更なし。
                            destFile = new FileOutputStream( strBld.toString() );   // 出力先ファイル作成
                            int data = -1;
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
                                    Log.d(TAG_SD, "コピー元ファイル [" + srcFile + "] を閉じました。");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } // if ( null!=srcFile)
                            if ( null != destFile ) {   // コピー先ファイル
                                try {
                                    destFile.close();
                                    Log.d(TAG_SD, "コピー先ファイル [" + destFile + "] を閉じました。");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } // if ( null!=destFile)
                        } // finally

                        // 明示的な null セット
                        // ＧＣ任せなメモリー管理に慣れるのは無理・・・なような気がする。
                        srcFile = null;
                        destFile = null;

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
            }
            case R.id.action_delete :   // 削除
                break;

            case R.id.action_upFolder :      // Up Folder

                // ファイルリスト用のTextViewに表示中のパス文字列を取得
                TextView dispPath = (TextView)findViewById( R.id.textView4 );
                String currentPath = String.valueOf( dispPath.getText() );
                Log.d( TAG_SD, "onOptionsItemSelected() [ currentPath = " + currentPath + " ]" );
                // １つ上のフォルダパス文字列を取得
                String upFolderPath = getUpFolderPath( currentPath );
                // １つ上のフォルダパスのファイルリストを表示
                setDataOnListView( upFolderPath, (ListView)findViewById( R.id.fileList01 ) );
                break;

            case R.id.action_upRoot :      // Up Root
                // ルートフォルダのファイルリストを表示
                setDataOnListView( System.getProperty( "file.separator" ), (ListView)findViewById( R.id.fileList01 ) );
                break;

            default:
                //do nothing
                break;
        }
        return super.onOptionsItemSelected( item );
    }

// 2017/07/11
// いつのまにかカスタムダイアログとごっちゃになっていた気がする。
// AlertDialogに戻るので、いったんソース無効。
//
//    // true = push OK / not true = push cancel
//    private void dispDialog() {
//
//        // ダイアログに渡す値を設定（自動生成されるときにも使われる・・みたい・・・）
//        // ここはメモのような気分で書いてます。今回はActivityから渡したいデータは無いので本当はいらないけれど一応書いてみたりして。
//        Bundle savedArgs = new Bundle();
//        savedArgs.putString( "title", "ｘｘｘダイアログ" );
//        savedArgs.putShort( "value", (short)100 );
//        // 目的のダイアログを表示
//        confirmDialogFragment dlg = new confirmDialogFragment();
//        dlg.setArguments( savedArgs );      // 自動的に再生成される時のために値を保存しておく。
//        Dialog dialog = dlg.onCreateDialog( savedArgs );    // 設定する値も渡しておく。
//        dialog.show();
//
//    }

    // ============================================================================================================
    // アプリを終了するか否かを確認する alertDialog を出力する。
    // ============================================================================================================
    private void confirmFinishDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder( MainActivity.this );
        builder.setTitle( "Confirm" );  // ダイアログのタイトル設定
        builder.setMessage( "アプリを終了しますか？" );    // ダイアログに表示するメッセージを設定
        builder.setCancelable( false );      // backキーなどを押して、あるいは、その他操作でキャンセルされないようにする。ユーザの回答入力を待つ。

        // OKボタン押下時のリスナー登録
        builder.setPositiveButton( "いいです。", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
// debug                        Toast.makeText( MainActivity.this, "confirmFinishDialog.onClick() of Positive button", Toast.LENGTH_SHORT ).show();
                        resultAlertDialog = RESULT_OK;  // 確認ダイアログのＯＫを格納
                    } // onClick()
                } // DialogInterface.OnClickListener()
        ); // setPositiveButton()

        // CANCELボタン押下時のリスナー登録
        builder.setNegativeButton( "ダメです。", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText( MainActivity.this, "confirmFinishDialog.onClick() of Negative button", Toast.LENGTH_SHORT ).show();
                        resultAlertDialog = RESULT_CANCEL;  // 確認ダイアログのＣＡＮＣＥＬを格納
                    } // onClick()
                } // DialogInterface.OnClickListener()
        ); // setNegativeButton()

        // cancel()された時のリスナー
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Log.d( "alertDialog", "setOnDismissListener.onCancel() runs." );
            } // onCancel()
        }); // setOnCancelListener()

        // ダイアログが閉じるが実行された時のリスナー
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                Log.d( "alertDialog", "setOnDismissListener.onDismiss() runs." );
            } // onDismiss()
        }); // setOnDismissListener()

        AlertDialog alertDlg = builder.create();
        alertDlg.show();
    }

    // ============================================================================================================
    // 選択状態を解除するのか否かを確認する alertDialog を出力する。
    // ============================================================================================================
    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder( MainActivity.this );
//        builder = new AlertDialog.Builder( MainActivity.this );
        builder.setTitle( "Confirm" );  // ダイアログのタイトル設定
        builder.setMessage( "ファイル選択中の状態でフォルダ移動は出来ません。\n選択解除しますか？" );    // ダイアログに表示するメッセージを設定
        builder.setCancelable( false );      // backキーなどを押して、あるいは、その他操作でキャンセルされないようにする。ユーザの回答入力を待つ。

        // OKボタン押下時のリスナー登録
        builder.setPositiveButton( "いいです。", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
// debug                        Toast.makeText( MainActivity.this, "showAlertDialog.onClick() of Positive button", Toast.LENGTH_SHORT ).show();
                        resultAlertDialog = RESULT_OK;  // 確認ダイアログのＯＫを格納
                        savedData.clear();  // 選択中データをキャンセル状態にする。

                        // ファイルリスト用のTextViewに表示中のパス文字列を取得
                        TextView dispPath = (TextView)findViewById( R.id.textView4 );
                        String currentPath = String.valueOf( dispPath.getText() );
                        Log.d( TAG_SD, "builder.setPositiveButton [ currentPath = " + currentPath + " ]" );
                        // ファイルリストを表示
                        setDataOnListView( currentPath, (ListView)findViewById( R.id.fileList01 ) );

                        Toast.makeText( MainActivity.this, "ファイルの選択状態を解除しました。", Toast.LENGTH_LONG ).show();

                    } // onClick()
                } // DialogInterface.OnClickListener()
        ); // setPositiveButton()

        // CANCELボタン押下時のリスナー登録
        builder.setNegativeButton( "ダメです。", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText( MainActivity.this, "showAlertDialog.onClick() of Negative button", Toast.LENGTH_SHORT ).show();
                        resultAlertDialog = RESULT_CANCEL;  // 確認ダイアログのＣＡＮＣＥＬを格納
//                        dialog.cancel();
                    } // onClick()
                } // DialogInterface.OnClickListener()
        ); // setNegativeButton()

        // cancel()された時のリスナー
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Log.d( "alertDialog", "setOnDismissListener.onCancel() runs." );
            } // onCancel()
        }); // setOnCancelListener()

        // ダイアログが閉じるが実行された時のリスナー
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                Log.d( "alertDialog", "setOnDismissListener.onDismiss() runs." );
            } // onDismiss()
        }); // setOnDismissListener()

        AlertDialog alertDlg = builder.create();
        alertDlg.show();
    }

    // ============================================================================================================
    //
    //指定されたパスの一つ上の階層にあたるパスを編集し返す。
    //
    // ============================================================================================================
    private  String getUpFolderPath( @NonNull String currentPath ) {

        Log.d( TAG_SD, "getUpFolerPath() --- start. [ currentPath = " + currentPath + " ]" );

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
        Log.d( TAG_SD, "File.pathSeparator = " + fileSeparator );
        Log.d( TAG_SD, "tempPath = " + tempPath );
        String[] folderName = tempPath.split( fileSeparator, 0 );
        if ( folderName.length >= 1 ) {
            Log.d(TAG_SD, "folderName.length = " + folderName.length + ", " + folderName[0]);
        }
        // folderName.lenght >= 2 のケース
        // (folderName.lenght-1)個のデータを頭から順にセパレータをはさみながら編集する。
        tempPath = "";          //編集用パスをクリアする。
        tempPath = tempPath.concat( fileSeparator );   //先頭を"/"に設定
        int setNum = folderName.length-1;       // 今のフォルダの上までがほしいので最後のフォルダはカットする。
        for ( short i=0; i<setNum; i++ ) {
            Log.d( TAG_SD, "concat folder name = " + folderName[i] );
            tempPath = tempPath.concat( folderName[ i ] );         // フォルダ文字列セット
            if ( 0 < folderName[i].length() && i != setNum-1 ) {  //フォルダ名が空であったり、最後のフォルダの後には"/"をつけない。
                tempPath = tempPath.concat( fileSeparator );      // separatorセット
            }
        }
// debug
        Log.d( TAG_SD, "Returned Path = " + tempPath  );

        return( tempPath );
    }

    // ============================================================================================================
    //
    //指定されたパスに従って内容を表示する処理
    //
    // ============================================================================================================
    private void setDataOnListView( String selectedPath, ListView targetListView ) {

        Log.d( TAG_SD, "setDataOnListView() --- start. [ Indicated Path = " + selectedPath + " ]" );

//        if ( null == selectedPath || null == targetListView ) {
        if ( null == targetListView ) {
            Toast.makeText( MainActivity.this, "setDataOnListView() : Illegal Argument !!", Toast.LENGTH_LONG ).show();
            return;
        }

        //====================================================================
        //ListViewにデータを表示する手順は以下の通り。
        //  1) データを準備
        //  2) Adapterを作成(いくつか種類がある中で今回はArrayAdapterを使用)
        //  3) ListViewに表示
        //====================================================================

//=======================================================================
// ディレクトリー内のファイルを表示する。（ここから）
//
        //
        //表示対象のパスを表示
        //「http://skys.co.jp/archives/4890」を参考にしてListViewにタイトル行(TextView)をつけた。
        //ListViewに放り込むというやり方をしているページもあったが参照時に行がずれる点に注意がいるので不便そう。
        //特にこれくらいならこちらの方がシンプルで分かりやすいと思う。
        //
        TextView dispPath = (TextView)findViewById( R.id.textView4 );
        Log.d( TAG_SD, "Text of textView4 = " + dispPath.getText() );
        if (null == selectedPath || selectedPath.equals("")) {
            dispPath.setText("/");
        } else {
            dispPath.setText(selectedPath);
        }
        String path = dispPath.getText().toString();
        Log.d( TAG_SD, "textView.setText() fin." );
        //
        //ListViewに表示する情報が０件の場合に表示するTextViewを指定する。
        //
        emptyTextView = (TextView) findViewById( R.id.emptyView );
        targetListView.setEmptyView( emptyTextView );
        //
        //ルートのディレクトリーを検索し、結果をListViewに表示するためにArrayListに追加
        //
        Log.d( TAG_SD, "ディレクトリ [" + path + "]検索" );
        File[] list;
        list = new File( path ).listFiles();    //ディレクトリーのファイル一覧取得
        //listFies()の結果をArrayListへ追加
        Log.d( TAG_SD, "ArrayListへ追加  -- start" );
        ArrayList<LineData> al = new ArrayList<>();
        for ( short i=0; null!=list && i<list.length; i++ ) {       //一行分のLineDataを編集しArrayListへ設定
            LineData line = new LineData( this );
            //
            // 検索対象のパスを保存
            //
            line.setAbsolutePath( path );
            //
            // TextViewにICON設定
            //
            Drawable icon;
            if ( list[i].isDirectory() ) {      //folder
//API17では使えない　↓
//            Drawable icon = this.getResources().getDrawable(R.mipmap.ic_folder_black_24dp, null);
// -> ContextCompat クラスを使用する。（互換性を保つため用意されているみたい）
//                icon = ContextCompat.getDrawable(this, R.mipmap.ic_folder_black_24dp);
                icon = ContextCompat.getDrawable(this, R.drawable.ic_folder_black_24dp);
                line.setFolderOrFile( true );       //true : this is foler.
            }
            else {
//API17では使えない　↓
//            Drawable icon = this.getResources().getDrawable(R.mipmap.ic_folder_black_24dp, null);
// -> ContextCompat クラスを使用する。（互換性を保つため用意されているみたい）
//                icon = ContextCompat.getDrawable(this, R.mipmap.ic_file_black_24dp);
                icon = ContextCompat.getDrawable(this, R.drawable.ic_file);
                line.setFolderOrFile( false );       //not true : this is file.
            }
            //ICONの表示位置を設定
            //
            //  引数：     座標 x, 座標 y, 幅, 高さ
            icon.setBounds( 0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight() );
            //TextViewにアイコンセット（四辺(left, top, right, bottom)に対して別個にアイコンを描画できる）
            TextView v = line.getTextView();
            v.setCompoundDrawables( icon, null, null, null );
            //
            //TextViewにパス文字列を設定
            //
            //line.getTextView().setText( list[i].toString() );
            //=============================================================================================================
            //すいません、今更なのでこんな不細工な感じな事してしまいます。。。。。
            //絶対パスでリスト表示して階層が深くなり複数行で表示しているのが不細工で、、、気づいてしまった・・・。
            //最初から気づかないのがセンスない証拠・・・。
            //で、めんどくさいのでこんな事で逃げてます。今はAndroidに触れ覚える事が目的なので、すいません。
            //ん？誰が見るんだ？夜中枕元に立つ武士が見るかな？草葉の影にひそむあの子がみるのかな？
            //--------------------------------------------------
            String sep = System.getProperty( "file.separator");
            String tmp = list[i].toString();
            String[] str1 = tmp.split( sep );
            String str2 = sep;
            if ( str1.length > 1 ) {    //「－１」するのでもし０なら落ちる。その防止の if 文。ないと思うんだけれど気持ち悪いので。
                //最後の文字列決め打ちでとる。
                str2 = str1[ str1.length - 1 ];
            }
            line.getTextView().setText( str2 );
            //=============================================================================================================

//debug
//            Log.d( TAG_SD, "line.getTextView().setText() = " + list[i].toString() );
            //
            //ArrayListに行データを設定
            //
            al.add( i, line );

        }//for()
        Log.d( TAG_SD, "ArrayListへ追加  -- end" );
/*
        //
        //debug：ArrayListの内容確認
        //
        Log.d( TAG_SD, "list.length / al.size = " + list.length + " / " + al.size() );
        for ( short i=0; i<al.size(); i++ ) {
            Log.d( TAG_SD, "al.get(" + i + ").getStrName() = " + al.get(i).getStrName() );
        }
*/
        //
        //表示内容（ArrayListの中身）を簡単にソート
        //      Comparatorはinterfaceだ！！
        //
        Collections.sort( al, new LineDataComparator() );
//        //debug : al の内容確認
//        for ( short i=0; i<al.size(); i++ ) {
//            Log.d( TAG_SD, "al.get(" + i + ").getTextView().getText() = " + al.get(i).getTextView().getText() );
//        }
        Log.d( TAG_SD, "ArrayList.add() fin." );
        //
        //ArrayAdapter に準備したデータを設定
        //  ・第１引数   ：       コンテキスト
        //  ・第２引数   ：       表示先となるレイアウトを指定（レイアウトファイル名）
        //  ・第３引数   ：       指定したレイアウトの中のView指定
        //  ・第４引数   ：       設定するArrayAdapterを指定
        //
        aAdapter = new CustomAdapter ( this, R.layout.file_list, R.id.textView, al );
        Log.d( TAG_SD, "new CustomAdapter() fin." );
        //
        //ListViewにArrayAdapterをセット
        //
        targetListView.setAdapter(aAdapter);
        Log.d( TAG_SD, "fileList.setAdapter() fin.");

    }

}
