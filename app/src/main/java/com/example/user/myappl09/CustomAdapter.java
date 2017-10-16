package com.example.user.myappl09;

import android.content.Context;
import android.graphics.Color;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;

import static android.util.TypedValue.COMPLEX_UNIT_SP;

// package-private class
class CustomAdapter extends ArrayAdapter<LineData> {

    private int _textViewResourceId;
    private int _layoutResourceId;
    private List<LineData> _objects;
    private LayoutInflater _inflater;
//    private ArrayList<String> list;

    private final String CLASS_NAME = "CustomAdapter";

    CustomAdapter( Context context, int resource, int textViewResourceId, List<LineData> objects ) {

        super(context, resource, textViewResourceId, objects);

        this._textViewResourceId = textViewResourceId;
        this._layoutResourceId = resource;
        this._objects = objects;
        this._inflater = LayoutInflater.from(context);

        Log.d( CLASS_NAME, "constructor end !" );
    }

    @Override
    public LineData getItem(int position) {
        return( this._objects.get( position ) );
    }//getItem()

    @Override
    public long getItemId(int position) {
        return( position );
    }//getItemId()

    @Override
    public int getCount() {
        return( _objects.size() );
    }//getCount()

    @Override
    @NonNull
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View     view;
        TextView text;

        //表示先のViewを設定
        if ( null != convertView ) {
            view = convertView;
        }
        else {
            // inflate() --- [引数] 挿入する子レイアウト、true／false で返り値を以下のどちらかで指定する。-> 親レイアウト／子レイアウト（第一引数）
            view = _inflater.inflate( _layoutResourceId, null );
        }
        //表示するデータを取得
        try {
            if ( 0 == _textViewResourceId ) {
                //  If no custom field is assigned, assume the whole resource is a TextView
                text = (TextView) view;
            } else {
                //  Otherwise, find the TextView field within the layout
                text = (TextView) view.findViewById( _textViewResourceId );
            }
            //テキストのサイズを指定
            text.setTextSize( COMPLEX_UNIT_SP , 32 );        //デフォルトでは単位が「sp」だそうだ。
        } catch (ClassCastException e) {
            Log.e("ArrayAdapter", "You must supply a resource ID for a TextView");
            throw new IllegalStateException(
                    "ArrayAdapter requires the resource ID to be a TextView", e);
        }

        LineData item = getItem( position );
        if ( null == item ) {
            text.setText( "" );
            Log.d( CLASS_NAME, "item = null" );
        }
        else if ( item instanceof CharSequence ) {
            text.setText( ( CharSequence )item );
//            Log.d( CLASS_NAME, "item instanceof CharSeqeunce [ " + item.toString() + " ]" );
        } else {
            text.setText( item.getText() );
//            Log.d( CLASS_NAME, "item other case [ " + item.getText() + " ]" );
        }
        //TextViewのTextの左にアイコンを表示
        //      setCompoundDrawables()  < 引数  ：   位置　=　[0]左、[1]上、[2]右、[3]下 >
        if ( null == item ) {
            text.setCompoundDrawables( null, null, null, null );
        }
        else {
            text.setCompoundDrawables( item.getIcon()[0].getCurrent(), null, null, null );
        }

// debug
//        Log.d( CLASS_NAME, Log.getStackTraceString( new Throwable() ) );

        if ( null != convertView ) {
// １行しか選択してないのに見えているすべての行が true で入ってくる・・・。わからんのでLineDataのフラグを使用・・・。    if (convertView.isSelected()) {
            if ( item.getSelected() ) {
// debug                Log.d( CLASS_NAME, "item.getSelected() == true  [ position = " + position + " ] ( " +  item.getText() + " )" );
//                convertView.setBackgroundColor( Color.GREEN );
            }
            else {
// debug                Log.d(CLASS_NAME, "item.getSelected() == false  [ position = " + position + " ] ( " + item.getText() + " )");
//                convertView.setBackgroundColor( Color.WHITE );
            }
        }

        return( view );

    }//getView()

}//class
