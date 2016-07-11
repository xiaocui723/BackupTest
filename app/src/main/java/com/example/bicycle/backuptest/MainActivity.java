package com.example.bicycle.backuptest;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.bicycle.backuptest.storage.SimpleStorage;
import com.example.bicycle.backuptest.storage.Storage;
import com.example.bicycle.backuptest.storage.StorageException;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private MyDatabaseHelper dbHelper;

    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sp = this.getSharedPreferences("mysp", MODE_PRIVATE);
        dbHelper = new MyDatabaseHelper(this, "BookStore.db", null, 1);
        Button createDatabase = (Button) findViewById(R.id.btn_create_database);
        assert createDatabase != null;
        createDatabase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbHelper.getReadableDatabase();
            }
        });

        Button addData = (Button) findViewById(R.id.btn_add_data);
        assert addData != null;
        addData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                ContentValues values = new ContentValues();

                values.put("name", "The Da Vinvi Code");
                values.put("author", "Dan Brown");
                values.put("pages", 454);
                values.put("price", 16.96);
                db.insert("Book", null, values);
                values.clear();
            }
        });

        Button getDatabasePath = (Button) findViewById(R.id.btn_get_database_path);
        assert getDatabasePath != null;
        getDatabasePath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String databasePath = MainActivity.this.getDatabasePath("BookStore.db").getAbsolutePath();
                Toast.makeText(MainActivity.this, databasePath, Toast.LENGTH_SHORT).show();
            }
        });

        Button addSPData = (Button) findViewById(R.id.btn_add_sp_data);
        assert addSPData != null;
        addSPData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("name", "Bicycle");
                editor.commit();
            }
        });

        Button cpDB2Sdcard = (Button) findViewById(R.id.btn_cp_db);
        assert cpDB2Sdcard != null;
        cpDB2Sdcard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File dbFile = new File(MainActivity.this.getDatabasePath("BookStore.db").getAbsolutePath());
                Storage storage = SimpleStorage.getExternalStorage();
                storage.createDirectory("MyDirName", true);
                storage.copy(dbFile, "MyDirName", "newFileName.db");
            }
        });

        Button cpDB2Game = (Button) findViewById(R.id.btn_cp_db_to_game);
        assert cpDB2Game != null;
        cpDB2Game.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Storage storage = SimpleStorage.getExternalStorage();
                File dbFile = storage.getFile("MyDirName", "newFileName.db");
                String path = MainActivity.this.getDatabasePath("BookStore.db").getAbsolutePath();
                String databaseDir = MainActivity.this.getDatabasePath("BookStore.db").getParent();

                if (!dbFile.isFile()) {
                    return;
                }

                if (!dbFile.exists()) {
                    return;
                }

                File databasefiles = new File(databaseDir);
                if (!databasefiles.exists()) {
                    databasefiles.mkdir();
                }

                FileInputStream inStream = null;
                FileOutputStream outStream = null;
                try {
                    inStream = new FileInputStream(dbFile);
                    outStream = new FileOutputStream(path);
                    FileChannel inChannel = inStream.getChannel();
                    FileChannel outChannel = outStream.getChannel();
                    inChannel.transferTo(0, inChannel.size(), outChannel);


                    try {
                        String command = "chmod 660 " + path;
                        Log.i("", "command = " + command);
                        Runtime runtime = Runtime.getRuntime();
                        Process proc = runtime.exec(command);
                    } catch (IOException e) {
                        Log.i("", "chmod fail!!!!");
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    throw new StorageException(e);
                } finally {
                    closeQuietly(inStream);
                    closeQuietly(outStream);
                }
            }
        });
    }

    /**
     * Get all files under the directory
     *
     * @param directory
     * @param out
     * @return
     */
    private void getDirectoryFilesImpl(File directory, List<File> out) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files == null) {
                return;
            } else {
                for (int i = 0; i < files.length; i++) {
                    if (files[i].isDirectory()) {
                        getDirectoryFilesImpl(files[i], out);
                    } else {
                        out.add(files[i]);
                    }
                }
            }
        }
    }

    private void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
            }
        }
    }
}
