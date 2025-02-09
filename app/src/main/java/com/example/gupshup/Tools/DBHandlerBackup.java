package com.example.gupshup.Tools;

import static android.content.Context.MODE_PRIVATE;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DBHandlerBackup extends SQLiteOpenHelper {

    private SQLiteDatabase myDataBase;
    private Context context = null;
    private static String TABLE_NAME = "messageTable";
    public static final String BACKUP_DATABASE_NAME = "GupshupBackup.db";
    public final static String DATABASE_PATH = "/data/data/com.example.gupshup/databases/";
    public static final int DATABASE_VERSION = 1;
    public static final String colMessageId = "messageId";
    public static final String colDecryptedMessage = "decryptedMessage";
    String strAESKey; 

    public DBHandlerBackup(Context context, String strAESKey) {
        super(context, BACKUP_DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        this.strAESKey = strAESKey;
        try {
            createDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        myDataBase = sqLiteDatabase;

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    //Check database already exists or not

    private boolean checkDatabaseExists() {
        boolean checkDB = false;
        try {
            String PATH = DATABASE_PATH + BACKUP_DATABASE_NAME;
            File dbFile = new File(PATH);
            checkDB = dbFile.exists();

        } catch (SQLiteException e) {

        }
        return checkDB;
    }


    //Create a empty database on the system
    public void createDatabase() throws IOException {
        boolean dbExist = checkDatabaseExists();

        if (dbExist) {
            Log.i("DB Exists", "db exists");
        }

        boolean dbExist1 = checkDatabaseExists();
        if (!dbExist1) {
            this.getWritableDatabase();
//            try {
//                this.close();
//                copyDataBase();
//            } catch (IOException e) {
//                throw new Error("Error copying database");
//            }
        }
    }

    public void createTable(){
        String query = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
                + colMessageId + " TEXT PRIMARY KEY, "
                + colDecryptedMessage + " TEXT)";
        myDataBase.execSQL(query);

    }

    //Copies your database from your local assets-folder to the just created empty database in the system folder
    private void copyDataBase() throws IOException {
        String outFileName = DATABASE_PATH + BACKUP_DATABASE_NAME;
        OutputStream myOutput = new FileOutputStream(outFileName);
        InputStream myInput = context.getAssets().open(BACKUP_DATABASE_NAME);

        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }
        myInput.close();
        myOutput.flush();
        myOutput.close();
    }


    //Open Database
    public void openDatabase() throws SQLException {
        String PATH = DATABASE_PATH + BACKUP_DATABASE_NAME;
        myDataBase = SQLiteDatabase.openDatabase(PATH, null, SQLiteDatabase.OPEN_READWRITE);
    }

    //for insert data into database


    public void insertMessage(String messageId, String decryptedMessage) {
        try {
            openDatabase();
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put("messageId", messageId);
            contentValues.put("decryptedMessage", decryptedMessage);
            db.insert(TABLE_NAME, null, contentValues);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public String getDecrypted(String messageId) {

        String decryptedMessage = "";
        //boolean flag = false;
        String selectQuery = "SELECT decryptedMessage FROM " + TABLE_NAME + " WHERE messageId='" + messageId + "'";

        try {
            openDatabase();
            Cursor cursor = myDataBase.rawQuery(selectQuery, null);
            //cursor.moveToFirst();


            if (cursor.getCount() > 0) {
                if (cursor.moveToFirst()) {
                    do {
                        decryptedMessage = cursor.getString(cursor.getColumnIndex(colDecryptedMessage));
                    }
                    while (cursor.moveToNext());
                }
            }

            cursor.close();
            myDataBase.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }


        return decryptedMessage;
    }



//    public void updateCustomer(String id, String email_id, String description, int balance_amount) {
//
//        try {
//            openDatabase();
//            SQLiteDatabase db = this.getWritableDatabase();
//            ContentValues contentValues = new ContentValues();
//            contentValues.put("email_id", email_id);
//            contentValues.put("description", description);
//            contentValues.put("balance_amount", balance_amount);
//
//            db.update(TABLE_NAME, contentValues, "id=" + id, null);
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//
//    }
}


