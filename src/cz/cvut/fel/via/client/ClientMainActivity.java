package cz.cvut.fel.via.client;

import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.droidgesture.sensors.AccelerometerManager;
import com.google.gson.Gson;

public class ClientMainActivity extends Activity
{
    public static final String SETTINGS_FILE = "viaclient-settings";
    public static final String SETTING_NICKNAME = "nickname";
    public static final String SETTING_SYNC_INTERVAL = "sync-interval";
    public static final String SETTING_LAST_MESSAGE_ID = "last-message-id";
    
    /* FIXME: This should definitely point someplace with a running VIAServer. */
    public static final String MESSAGE_SERVER_LOCATION = "http://46.255.228.228:8080";

    private static final int MESSAGE_LIST_ITEM_DELETE = 0;

    private MessageListAdapter mMessageListAdapter;

    private TextView mEmptyMessageListLabel;

    private EditText mEditText;

    private Handler mHandler;

    /* The synchronization interval in seconds. */
    private int mSyncDelay;

    /* The id of the last received message. */
    private int mLastReceivedMessageId;

    /* The user's nickname. */
    private String mNickname;

    private boolean mIsDispatchingMessage;

    private boolean mIsSynchronizing;

    private MessagesDataSource messageDataSource;
    private AccelerometerManager mManager;

    private Dialog mSettingsDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_client_main);

        SharedPreferences settings = getSharedPreferences(SETTINGS_FILE, Activity.MODE_PRIVATE);
        mNickname = settings.getString(SETTING_NICKNAME, "Anonymous");
        mSyncDelay = settings.getInt(SETTING_SYNC_INTERVAL, 60);
        mLastReceivedMessageId = settings.getInt(SETTING_LAST_MESSAGE_ID, 0);

        mEditText = (EditText) findViewById(R.id.messageEditView);

        mEditText.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                if (hasFocus)
                {
                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });

        ((Button) findViewById(R.id.sendMessageButton)).setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendMessage();
            }
        });

        mEmptyMessageListLabel = ((TextView) findViewById(R.id.emptyList));

        messageDataSource = new MessagesDataSource(getApplicationContext());
        messageDataSource.open();

        mMessageListAdapter = new MessageListAdapter(this, R.layout.message_listitem_layout, messageDataSource.getMessages());

        mLastReceivedMessageId = messageDataSource.getLastReceivedMessageId();

        ListView messageListView = (ListView) findViewById(R.id.messageListView);

        messageListView.setAdapter(mMessageListAdapter);

        registerForContextMenu(messageListView);

        mHandler = new Handler();

        mManager = AccelerometerManager.getInstance(this);

        mManager.addAccelerometerListener(new ShakeDetector(this));

        mSettingsDialog = new SettingsDialog(this);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        messageDataSource.open();

        startMessageSynchronization();

        mManager.startListening(SensorManager.SENSOR_DELAY_NORMAL, false);
    }

    @Override
    protected void onPause()
    {
        stopMessageSynchronization();

        messageDataSource.close();

        mManager.stopListening();

        super.onPause();
    }

    @Override
    protected void onStop()
    {
        SharedPreferences settings = getSharedPreferences(SETTINGS_FILE, Activity.MODE_PRIVATE);

        /* Opens the settings for editing... */
        SharedPreferences.Editor editor = settings.edit();

        editor.putString(SETTING_NICKNAME, mNickname);
        editor.putInt(SETTING_SYNC_INTERVAL, mSyncDelay);
        editor.putInt(SETTING_LAST_MESSAGE_ID, mLastReceivedMessageId);

        // Commits the changes.
        editor.commit();
        
        super.onStop();
    }

    public void startMessageSynchronization()
    {
        /* Initializes the message synchronization on the background. */
        mMessageSynchronizer.run();
    }

    public void stopMessageSynchronization()
    {
        mHandler.removeCallbacks(mMessageSynchronizer);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_client_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
        case R.id.menu_settings:
            mSettingsDialog.show();
            return true;
            
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo)
    {
        if (view.getId() == R.id.messageListView)
        {
            // AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo ;

            menu.setHeaderTitle("Message");
            menu.add(Menu.NONE, MESSAGE_LIST_ITEM_DELETE, MESSAGE_LIST_ITEM_DELETE, "Delete");
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        switch (item.getItemId())
        {
        case MESSAGE_LIST_ITEM_DELETE:
            deleteMessage(mMessageListAdapter.getItem(info.position));
            break;
        }

        return true;
    }

    Runnable mMessageSynchronizer = new Runnable()
    {
        @Override
        public synchronized void run()
        {
            if (!mIsSynchronizing)
            {
                mIsSynchronizing = true;
                (new MessageSyncWorker(ClientMainActivity.this)).execute();
            }
        }
    };

    public void addMessage(Message message)
    {
        if (messageDataSource.persistMessage(message))
        {
            mMessageListAdapter.addMessage(message);
        }
    }

    public void deleteMessage(Message message)
    {
        if (messageDataSource.deleteMessage(message.getId()))
        {
            mMessageListAdapter.removeMessage(message);
        }
    }

    public void sendMessage()
    {
        if (!mIsDispatchingMessage)
        {
            mIsDispatchingMessage = true;

            String content = mEditText.getText().toString();

            if (content != null && content.length() != 0)
            {
                Gson gson = new Gson();

                (new SendMessageWorker()).execute(gson.toJson(new Message(mNickname, content)));
            }

            mEditText.setText("");

            mIsDispatchingMessage = false;
        }
    }

    public int getLastReceivedMessageId()
    {
        return mLastReceivedMessageId;
    }

    public void scheduleNextSynchronization(int lastReceivedMessageId)
    {
        if (lastReceivedMessageId >= 0)
        {
            mLastReceivedMessageId = lastReceivedMessageId;
        }

        mIsSynchronizing = false;

        mHandler.postDelayed(mMessageSynchronizer, mSyncDelay * 1000);
    }

    private class MessageListAdapter extends BaseAdapter
    {
        private LinkedList<Message> mMessageList;

        public MessageListAdapter(Context context, int textViewResourceId, List<Message> messageList)
        {
            mMessageList = new LinkedList<Message>(messageList);
        }

        @Override
        public synchronized View getView(int position, View convertView, ViewGroup parent)
        {
            View view = convertView;

            if (view == null)
            {
                view = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.message_listitem_layout, null);
            }

            Message message = mMessageList.get(position);

            if (message != null)
            {
                ((TextView) view.findViewById(R.id.messageAuthor)).setText(message.getAuthor());
                ((TextView) view.findViewById(R.id.messageContent)).setText(message.getContent());
            }

            return view;
        }

        public synchronized void addMessage(Message message)
        {
            if (mMessageList.isEmpty())
            {
                mEmptyMessageListLabel.setVisibility(View.GONE);
            }

            mMessageList.addFirst(message);

            notifyDataSetChanged();
        }

        public synchronized void removeMessage(Message message)
        {
            if (mMessageList.remove(message))
            {
                if (mMessageList.isEmpty())
                {
                    mEmptyMessageListLabel.setVisibility(View.VISIBLE);
                }

                notifyDataSetChanged();
            }
        }

        @Override
        public int getCount()
        {
            return mMessageList.size();
        }

        @Override
        public Message getItem(int index)
        {
            return mMessageList.get(index);
        }

        @Override
        public long getItemId(int index)
        {
            return getItem(index).hashCode();
        }
    }

    public String getNickname()
    {
        return mNickname;
    }

    public void setNickname(String nickname)
    {
        mNickname = nickname;
    }

    public int getSyncDelay()
    {
        return mSyncDelay;
    }

    public void setSyncDelay(int delay)
    {
        mSyncDelay = delay;
    }

    public void syncMessagesImediately()
    {
        stopMessageSynchronization();
        startMessageSynchronization();
    }
}
