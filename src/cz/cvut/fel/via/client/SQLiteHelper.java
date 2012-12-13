package cz.cvut.fel.via.client;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLiteHelper extends SQLiteOpenHelper
{
  public static final String TABLE_MESSAGES = "messages";
  
  public static final String COLUMN_ID        = "_id";
  public static final String COLUMN_AUTHOR    = "author";
  public static final String COLUMN_CONTENT   = "content";
  public static final String COLUMN_TIMESTAMP = "ts";

  private static final String DATABASE_NAME    = "messages.db";
  private static final int    DATABASE_VERSION = 1;

  private static final String CREATE_DATABASE_SQL = "CREATE TABLE " + TABLE_MESSAGES + "(" 
      + COLUMN_ID        + " INTEGER PRIMARY KEY, "
      + COLUMN_AUTHOR    + " TEXT NOT NULL, " 
      + COLUMN_CONTENT   + " TEXT NOT NULL, "
      + COLUMN_TIMESTAMP + " DATETIME NOT NULL);" ;

  public SQLiteHelper(Context context) 
  {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  @Override public void onCreate(SQLiteDatabase database) 
  {
    database.execSQL(CREATE_DATABASE_SQL);
  }

  @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
  {
    db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
    onCreate(db);
  }
} 