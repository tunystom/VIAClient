package cz.cvut.fel.via.client;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class MessagesDataSource
{
    private SQLiteDatabase database;

    private SQLiteHelper dbHelper;

    private String[] columnNames = { SQLiteHelper.COLUMN_ID, SQLiteHelper.COLUMN_AUTHOR, SQLiteHelper.COLUMN_CONTENT, SQLiteHelper.COLUMN_TIMESTAMP };

    private SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

    private static final String GET_MAXIMUM_MESSAGE_ID_SQL = "SELECT MAX(" + SQLiteHelper.COLUMN_ID + ") FROM " + SQLiteHelper.TABLE_MESSAGES;

    private boolean openned = false;

    public MessagesDataSource(Context context)
    {
        dbHelper = new SQLiteHelper(context);
    }

    public void open() throws SQLException
    {
        if (!openned)
        {
            openned = true;
            database = dbHelper.getWritableDatabase();
        }

    }

    public void close()
    {
        if (openned)
        {
            openned = false;
            dbHelper.close();
        }
    }

    public boolean persistMessage(Message message)
    {
        ContentValues values = new ContentValues();

        values.put(SQLiteHelper.COLUMN_ID, message.getId());
        values.put(SQLiteHelper.COLUMN_AUTHOR, message.getAuthor());
        values.put(SQLiteHelper.COLUMN_CONTENT, message.getContent());
        values.put(SQLiteHelper.COLUMN_TIMESTAMP, iso8601Format.format(message.getTimestamp()));

        return (database.insert(SQLiteHelper.TABLE_MESSAGES, null, values) != -1);
    }

    public int getLastReceivedMessageId()
    {
        Cursor cursor = database.rawQuery(GET_MAXIMUM_MESSAGE_ID_SQL, null);

        int id = (cursor.moveToFirst() ? cursor.getInt(0) : 0);

        cursor.close();

        return id;
    }

    public List<Message> getMessages()
    {
        List<Message> messages = new ArrayList<Message>();

        Cursor cursor = database.query(SQLiteHelper.TABLE_MESSAGES, columnNames, null, null, null, null, SQLiteHelper.COLUMN_TIMESTAMP + " DESC");

        for (cursor.moveToFirst() ; !cursor.isAfterLast() ; cursor.moveToNext())
        {
            try
            {
                messages.add(new Message(cursor.getInt(0), cursor.getString(1), cursor.getString(2), iso8601Format.parse(cursor.getString(3))));
            }
            catch (ParseException e)
            {
                Log.e(MessagesDataSource.class.getName(), "The message timestamp is in a wrong format.", e);
            }
        }

        cursor.close();

        return messages;
    }

    public boolean deleteMessage(int messageId)
    {
        return (database.delete(SQLiteHelper.TABLE_MESSAGES, SQLiteHelper.COLUMN_ID + " = " + messageId, null) == 1);
    }
}