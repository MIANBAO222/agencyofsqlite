package com.example.a75636.agencyofsqlite;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity
{
    private TextView txt;
    private EditText edit;
    private Button btn;
    private EditText edit_server;
    private EditText edit_ip;
    private Button server_OK;
    private LinearLayout mLinearLayout;
    private LinearLayout mLinearLayout_client;

    /**启动服务端端口
     * 服务端IP为手机IP
     * */
    private int pite;
    private SocketServer server;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate ( savedInstanceState );
        setContentView ( R.layout.activity_main );
        txt = (TextView) findViewById ( R.id.textView );
        edit = (EditText) findViewById ( R.id.edit );
        btn = (Button) findViewById ( R.id.btn );
        edit_server=(EditText)findViewById ( R.id.editText_server );
        edit_ip=(EditText)findViewById ( R.id.client_ip );
        server_OK=(Button)findViewById ( R.id.server_OK );
        mLinearLayout=(LinearLayout)findViewById ( R.id.lin_1 ) ;
        mLinearLayout_client=(LinearLayout)findViewById ( R.id.lin_ip ) ;

        mLinearLayout_client.setVisibility ( View.GONE );

        server_OK.setOnClickListener ( new View.OnClickListener ( )
        {
            @Override
            public void onClick(View v)
            {
                mLinearLayout.setVisibility ( View.GONE );
                try {
                    pite= Integer.parseInt(edit_server.getText ().toString ());

                    server=new SocketServer ( pite );
                    /**socket服务端开始监听*/
                    server.beginListen ( );

                }catch (Exception e){
                    Toast.makeText ( MainActivity.this,"请输入数字", Toast.LENGTH_SHORT ).show ();
                    mLinearLayout.setVisibility ( View.VISIBLE );
                    e.printStackTrace ();
                }

            }
        } );


        btn.setOnClickListener ( new View.OnClickListener ( )
        {
            @Override
            public void onClick(View v)
            {
                /**socket发送数据*/
                int value=server.sendMessage ( edit.getText ().toString () );
                if(value==-1)Toast.makeText ( MainActivity.this,"未连接", Toast.LENGTH_SHORT ).show ();
            }
        } );

        /**socket收到消息线程*/
        SocketServer.ServerHandler=new Handler(  ){
            @Override
            public void handleMessage(Message msg)
            {
                SQLiteDatabase db = openOrCreateDatabase("test.db", Context.MODE_PRIVATE, null);
                try {
                    if(msg.obj.toString().startsWith("select")){
                        Cursor cursor=db.rawQuery(msg.obj.toString(),null);
                        StringBuffer resql=new StringBuffer();
                        while (cursor.moveToNext()){
                            int i=0;
                            try{
                                while (cursor.getString(i)!=null)i++;
                            }
                            catch (Exception a){

                            }
                            for(int k=0;k<i;k++){
                                resql.append(cursor.getString(k)+" ");
                            }
                            resql.append("\n");
                            server.sendMessage(resql.toString());
                        }
                    }
                    else {
                        db.execSQL(msg.obj.toString());
                    }

                }
                catch(Exception e)
                {
                txt.append("\nERROR->");
                txt.append ( msg.obj.toString ());
                    server.sendMessage("ERROR->"+msg.obj.toString ()+"\n");
                    db.close();
                    return;
                }
                txt.append("\nSUCCESS->");
                txt.append ( msg.obj.toString ());
                server.sendMessage("SUCCESS->"+msg.obj.toString ()+"\n");
                db.close();
            }
        };
    }

}
