package com.newmedia.erxeslibrary.ui.message;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Intent;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;

import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.newmedia.erxeslibrary.configuration.Config;
import com.newmedia.erxeslibrary.configuration.Helper;
import com.newmedia.erxeslibrary.configuration.ReturnType;
import com.newmedia.erxeslibrary.configuration.ErxesRequest;
import com.newmedia.erxeslibrary.configuration.SoftKeyboard;
import com.newmedia.erxeslibrary.ErxesObserver;
import com.newmedia.erxeslibrary.model.User;
import com.newmedia.erxeslibrary.R;


import java.util.ArrayList;


public class MessageActivity extends AppCompatActivity implements ErxesObserver {

    private EditText edittext_chatbox;
    private RecyclerView mMessageRecycler;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ImageView profile1, profile2, backImageView, logoutImageView, sendImageView, attachmentImageView;
    private TextView names, isMessenOnlineImage;
    private ViewGroup container, upload_group;
    private ProgressBar progressBar;
    private Config config;
    private ErxesRequest erxesRequest;
    private Point size;
    private GFilePart gFilePart;

    private final String TAG = "MESSAGEACTIVITY";

    @Override
    public void notify(final int returnType, String conversationId, String message) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MessageListAdapter adapter = (MessageListAdapter) mMessageRecycler.getAdapter();
                switch (returnType) {
                    case ReturnType.ComingNewMessage:
                        if (adapter != null) {
                            adapter.notifyDataSetChanged();
                            mMessageRecycler.smoothScrollToPosition(adapter.getItemCount() - 1);
                        }
                        break;
                    case ReturnType.Getmessages:
                        if (adapter != null) {
                            adapter.notifyDataSetChanged();
                            if (adapter.getItemCount() > 2 && adapter.refresh_data())
                                mMessageRecycler.smoothScrollToPosition(adapter.getItemCount() - 1);
                            swipeRefreshLayout.setRefreshing(false);
                        }
                        break;
                    case ReturnType.Mutation:
                        if (adapter != null) {
                            if (adapter.getItemCount() > 2 && adapter.refresh_data())
                                mMessageRecycler.smoothScrollToPosition(adapter.getItemCount() - 1);
                            swipeRefreshLayout.setRefreshing(false);

                            gFilePart.end_of();
                        }
                        break;
                    case ReturnType.Mutation_new:
                        subscribe_conversation();
                        gFilePart.end_of();
                        break;
                    case ReturnType.IsMessengerOnline:
                        header_profile_change();
                        break;
                    case ReturnType.SERVERERROR:
                        Snackbar.make(container, R.string.serverror, Snackbar.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);
                        break;
                    case ReturnType.CONNECTIONFAILED:
                        Snackbar.make(container, R.string.cantconnect, Snackbar.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);
                        break;
                    case ReturnType.GetSupporters:
                        header_profile_change();
                        break;
                }
            }
        });


    }

    private void subscription() {
        MessageListAdapter adapter = (MessageListAdapter) mMessageRecycler.getAdapter();
        header_profile_change();
        isMessenOnlineImage.setText(R.string.Online);
        if (adapter.getItemCount() > 2 && adapter.refresh_data())
            mMessageRecycler.smoothScrollToPosition(adapter.getItemCount() - 1);
        swipeRefreshLayout.setRefreshing(false);
    }

    private void bind(User user, ImageView por) {
        if (user.avatar != null) {
            try {
                Glide.with(this.getApplicationContext())
                        .load(user.avatar)
                        .placeholder(R.drawable.avatar)
                        .error(R.drawable.avatar)
                        .optionalCircleCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(por);
            } catch (Exception ignored) {
            }
            por.setVisibility(View.VISIBLE);
        }


        String t = user.fullName;
        String upperString = t.substring(0, 1).toUpperCase() + t.substring(1);
        String previous = names.getText().toString();
        names.setText(previous.length() == 0 ? upperString : previous + "," + upperString);
        names.setVisibility(View.VISIBLE);
    }

    private void header_profile_change() {
        if (config.supporters.size() > 0)
            isMessenOnlineImage.setVisibility(View.VISIBLE);
        else
            names.setVisibility(View.INVISIBLE);

        names.setText("");

        if (config.supporters.size() > 0) bind(config.supporters.get(0), profile1);
        else profile1.setVisibility(View.INVISIBLE);
        if (config.supporters.size() > 1) bind(config.supporters.get(1), profile2);
        else profile2.setVisibility(View.INVISIBLE);

        isMessenOnlineImage.setText(config.messenger_status_check() ? R.string.Online : R.string.Offline);

//        isMessenOnlineImage.setVisibility(
//                (Config.isNetworkConnected()&&Config.IsMessengerOnline) ?View.VISIBLE:View.INVISIBLE);

    }

    void load_findViewByid() {
        container = this.findViewById(R.id.container);

        size = Helper.display_configure(this, container, "#00000000");
        InputMethodManager im = (InputMethodManager) getSystemService(Service.INPUT_METHOD_SERVICE);

        SoftKeyboard softKeyboard;
        softKeyboard = new SoftKeyboard((ViewGroup) this.findViewById(R.id.linearlayout), im);
        softKeyboard.setSoftKeyboardCallback(new SoftKeyboard.SoftKeyboardChanged() {
            @Override
            public void onSoftKeyboardHide() {
                MessageActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        container.getLayoutParams().height = size.y * 8 / 10;
                        container.requestLayout();
                    }
                });
            }

            @Override
            public void onSoftKeyboardShow() {
                MessageActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        container.getLayoutParams().height = WindowManager.LayoutParams.MATCH_PARENT;
                        container.requestLayout();
                    }
                });
            }
        });
        upload_group = this.findViewById(R.id.upload_group);
        swipeRefreshLayout = this.findViewById(R.id.swipeRefreshLayout);
        profile1 = this.findViewById(R.id.profile1);
        profile2 = this.findViewById(R.id.profile2);
        isMessenOnlineImage = this.findViewById(R.id.isOnline);
        names = this.findViewById(R.id.names);
        edittext_chatbox = this.findViewById(R.id.edittext_chatbox);
        mMessageRecycler = this.findViewById(R.id.reyclerview_message_list);
        backImageView = this.findViewById(R.id.backImageView);
        logoutImageView = this.findViewById(R.id.logoutImageView);
        sendImageView = this.findViewById(R.id.sendImageView);
        attachmentImageView = this.findViewById(R.id.attachmentImageView);
        initIcon();

        this.findViewById(R.id.info_header).setBackgroundColor(config.colorCode);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshItems();
            }

        });

        logoutImageView.setOnClickListener(v -> logout());
        backImageView.setOnClickListener(v -> finish());

        int index = Integer.getInteger(config.wallpaper, -1);
        if (index > -1 && index < 5)
            mMessageRecycler.setBackgroundResource(Helper.backgrounds[index]);
    }

    private void initIcon() {
        Glide.with(this).load(config.getBackIcon(this, config.getInColor(config.colorCode))).into(backImageView);
        Glide.with(this).load(config.getLogoutIcon(this, config.getInColor(config.colorCode))).into(logoutImageView);
        Glide.with(this).load(config.getsendIcon(this, 0)).into(sendImageView);
        Glide.with(this).load(config.getAttachmentIcon(this, 0)).into(attachmentImageView);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        config = Config.getInstance(this);
        config.setActivityConfig(this);
        erxesRequest = ErxesRequest.getInstance(config);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_messege);
        gFilePart = new GFilePart(config, this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        load_findViewByid();
        mMessageRecycler.setLayoutManager(linearLayoutManager);

        if (config.conversationId != null) {
            linearLayoutManager.setStackFromEnd(true);
            for (int i = 0; i < config.conversations.size(); i++) {
                if (config.conversations.get(i)._id.equals(config.conversationId)) {
                    config.conversations.get(i).isread = true;
                    break;
                }
            }
            subscribe_conversation();
        } else {
            mMessageRecycler.setAdapter(new MessageListAdapter(this, new ArrayList<>()));
        }
        header_profile_change();

        if (shouldAskPermissions()) {
            askPermissions();
        }
    }

    private void subscribe_conversation() {
        mMessageRecycler.setAdapter(new MessageListAdapter(this, config.conversationMessages));
        erxesRequest.getMessages(config.conversationId);
        subscription();
    }

    public void logout() {
        config.Logout(this);
    }

    public void send_message(View view) {
        if (!config.isNetworkConnected()) {
            Snackbar.make(container, R.string.cantconnect, Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (!edittext_chatbox.getText().toString().equalsIgnoreCase("") ||
                gFilePart.get() != null && gFilePart.get().size() > 0) {
            if (config.conversationId != null) {
                erxesRequest.InsertMessage(edittext_chatbox.getText().toString(), config.conversationId, gFilePart.get());
            } else {
                erxesRequest.InsertNewMessage(edittext_chatbox.getText().toString(), gFilePart.get());
            }
            edittext_chatbox.setText("");
        }
    }

    ;

    public void refreshItems() {
        if (config.conversationId != null)
            erxesRequest.getMessages(config.conversationId);
        else
            swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        erxesRequest.remove(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        erxesRequest.add(this);
        erxesRequest.getSupporters();
    }

    //Android 4.4 (API level 19)
    public void onBrowse(View view) {
        Intent chooseFile;
        Intent intent;
        chooseFile = new Intent(Intent.ACTION_PICK);
//        chooseFile.addCategory(Intent.CATEGORY_OPENABLE);
        chooseFile.setType("*/*");
        chooseFile.setAction(Intent.ACTION_GET_CONTENT);
        intent = Intent.createChooser(chooseFile, "Choose a file");
        startActivityForResult(intent, 444);
        upload_group.setClickable(false);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        gFilePart.ActivityResult(requestCode, resultCode, resultData);
        upload_group.setClickable(true);
    }

    protected boolean shouldAskPermissions() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    @TargetApi(23)
    protected void askPermissions() {
        String[] permissions = {
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE"
        };
        int requestCode = 200;
        requestPermissions(permissions, requestCode);
    }
}
