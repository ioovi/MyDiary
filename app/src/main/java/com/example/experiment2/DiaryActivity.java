package com.example.experiment2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DiaryActivity extends AppCompatActivity {
    private CreateSQL dbHelper;
    private List<diary> diaryList=new ArrayList<>();
    private TextView title;
    private diary diary;

    @Override
    protected void onStart() {
        super.onStart();
        dbHelper = new CreateSQL(this, "DiaryBook.db", null, 2);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        diaryList.clear();
        //dbHelper.onUpgrade(db,2,2);
        String name = getIntent().getStringExtra("username");
        String authorName =null;
        if(name.equals("admin")){
            authorName = "admin";
        }else if(name.equals("default")){
            authorName = "default";
        }

        Cursor cursor = db.query("diary", null, "author=?",  new String[]{authorName}, null, null, null);
        if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndex("id"));
                    String title = cursor.getString(cursor.getColumnIndex("title"));
                    long time = cursor.getLong(cursor.getColumnIndex("time"));
                    SimpleDateFormat time2 = new SimpleDateFormat("yyyy???MM???dd??? HH:mm:ss");
                    String date = time2.format(new Date(time));
                    diary = new diary(id, title, date);
                    diaryList.add(diary);
                } while (cursor.moveToNext());
            }
            diaryAdapter adapter = new diaryAdapter(DiaryActivity.this, R.layout.diary_every, diaryList);
            ListView listView = findViewById(R.id.list_view);
            listView.setAdapter(adapter);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary);
        title = findViewById(R.id.title);
        title.setText(getIntent().getStringExtra("username")+"????????????");
        Button back =  findViewById(R.id.btn_back);
        Button commit =  findViewById(R.id.commit);
        final ListView listView= findViewById(R.id.list_view);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //???ListView??????????????????????????????????????????
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                diary diary=diaryList.get(position);
                //System.out.println("position"+position);
                Intent intent=new Intent(DiaryActivity.this,editActivity.class);//???????????????????????????
                intent.putExtra("id",diary.getId());        //??????listView??????
                intent.putExtra("title", diary.getTitle());  //??????????????????
                Log.d("title---",diary.getTitle());
                Log.d("id---",String.valueOf(diary.getId()));
                startActivity(intent);
            }
        });

        commit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DiaryActivity.this,AddActivity.class);
                String name = getIntent().getStringExtra("username");
                intent.putExtra("username",name);
                DiaryActivity.this.startActivity(intent);
            }
        });

        //????????????
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,final int position, long id) {
                //?????????????????????????????????
                new AlertDialog.Builder(DiaryActivity.this)
                        .setTitle("????????????") //?????????
                        .setMessage("??????????????????") //??????
                        .setNegativeButton("??????",null) //????????????
                        .setPositiveButton("??????", new DialogInterface.OnClickListener() {
                            //?????????
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                diary diary=diaryList.get(position);
                                dbHelper =new CreateSQL(DiaryActivity.this,"DiaryBook.db",null,2);
                                SQLiteDatabase db=dbHelper.getWritableDatabase();
                                String[] strings = {String.valueOf(diary.getId())}; //??????????????????????????????
                                int flag = db.delete("diary", "id=?",strings);    //?????????????????????
                                Cursor cursor=db.query("diary",null,"id>?",strings,null,null,null);
                                int id1 = 0;
                                while (cursor.moveToNext()) {
                                    id1 = cursor.getInt(cursor.getColumnIndex("id"));
                                    //????????????????????????????????????????????????????????????????????????
                                    ContentValues contentValues = new ContentValues();
                                    //?????????????????????????????????????????????contentValues
                                    contentValues.put("id", id1 - 1);
                                    //??????     ?????????????????????????????????    ?????????????????????????????????id=ID??????????????????
                                    long dd = db.update("diary", contentValues, "id=?", new String[]{String.valueOf(id1)});
                                }

                                String name = getIntent().getStringExtra("username");
                                String authorName =null;
                                if(name.equals("admin")){
                                    authorName = "admin";
                                }else if(name.equals("default")){
                                    authorName = "default";
                                }

                                Cursor cursor1 = db.query("diary", null, "author=?",  new String[]{authorName}, null, null, null);
                                diaryList.clear();  //????????????ListView????????????
                                if(cursor1.moveToFirst()){
                                    do{
                                        int id = cursor1.getInt(cursor1.getColumnIndex("id"));
                                        String title=cursor1.getString(cursor1.getColumnIndex("title"));
                                        long   time1=cursor1.getLong(cursor1.getColumnIndex("time"));
                                        SimpleDateFormat time2=new SimpleDateFormat("yyyy-MM--dd HH:mm:ss");
                                        String time=time2.format(new Date(time1));
                                        diary=new diary(id,title,time);
                                        diaryList.add(diary);
                                    }while (cursor1.moveToNext());
                                }
                                diaryAdapter adapter=new diaryAdapter(DiaryActivity.this,R.layout.diary_every,diaryList);
                                ListView listView= findViewById(R.id.list_view);

                                listView.setAdapter(adapter);
                            }
                        }).show();
                return false;
            }
        });

    }
}
class diaryAdapter extends ArrayAdapter<diary> {
    private int resourceId;
    public diaryAdapter(@NonNull Context context, int resource, List<diary> objects) {
        super(context, resource,objects);
        resourceId=resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        diary diary=getItem(position);
        View view= LayoutInflater.from(getContext()).inflate(resourceId,parent,false);
        TextView item_title=view.findViewById(R.id.item_title);
        TextView item_time=view.findViewById(R.id.item_time);
        //TextView item_id= view.findViewById(R.id.item_id);
       //item_id.setText(String.valueOf(diary.getId()));
        item_title.setText(diary.getTitle());
        item_time.setText(diary.getDate());
        return view;
    }
}