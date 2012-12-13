package cz.cvut.fel.via.client;

import android.app.Dialog;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

public class SettingsDialog extends Dialog
{
    private ClientMainActivity mMainActivity;
    private EditText mNicknameEdit;
    private EditText mSyncDelayEdit;

    public SettingsDialog(final ClientMainActivity mainActivity)
    {
        super(mainActivity);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.settings_dialog_layout);

        mMainActivity = mainActivity;

        mNicknameEdit = (EditText) findViewById(R.id.nicknameEditText);
        mSyncDelayEdit = (EditText) findViewById(R.id.syncDelayEditText);

        ((Button) findViewById(R.id.saveSettingsButton)).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                mainActivity.setNickname(mNicknameEdit.getText().toString());
                mainActivity.setSyncDelay(Integer.valueOf(mSyncDelayEdit.getText().toString()));
                dismiss();
            }
        });

        ((Button) findViewById(R.id.discardSettingsButton)).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                dismiss();
            }
        });

        setCanceledOnTouchOutside(false);
    }

    @Override
    public void show()
    {
        mNicknameEdit.setText(mMainActivity.getNickname());
        mSyncDelayEdit.setText(String.valueOf(mMainActivity.getSyncDelay()));

        super.show();
    }
}