package com.github.socketdemo;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.flyco.dialog.entity.DialogMenuItem;
import com.flyco.dialog.listener.OnOperItemClickL;
import com.flyco.dialog.widget.NormalListDialog;
import com.github.socketdemo.bean.Transmission;
import com.github.socketdemo.socket.ClientThread;
import com.github.socketdemo.socket.Constants;
import com.google.gson.Gson;
import com.vincent.filepicker.Constant;
import com.vincent.filepicker.activity.ImagePickActivity;
import com.vincent.filepicker.filter.entity.ImageFile;

import java.util.ArrayList;
import java.util.List;

import static com.vincent.filepicker.activity.ImagePickActivity.IS_NEED_CAMERA;

public class MainActivity extends PermissionActivity {

    private RecyclerView mRecyclerView;
    private EditText mEtContent;
    private ChatAdapter mAdapter;
    private ClientThread mClientThread;
    private Gson mGson;

    private RelativeLayout mRelativeProgress;
    private TextView mTvProgress;
    private String mFilePath;

    protected String[] mFilePermissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    private ArrayList<DialogMenuItem> mMenuItems = new ArrayList<>();

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == Constants.RECEIVE_MSG) {
                String content = msg.obj.toString();
                Log.e("MainActivity", "handleMessage--------" + content);
                Transmission trans = mGson.fromJson(content, Transmission.class);
                if (trans.transmissionType == Constants.TRANSFER_STR) {
                    mAdapter.addData(trans);
                    mRecyclerView.smoothScrollToPosition(mAdapter.getItemCount());
                }
            } else if (msg.what == Constants.PROGRESS) {
                mRelativeProgress.setVisibility(View.VISIBLE);

                if (msg.obj == null) {
                    return;
                }

                mTvProgress.setText(msg.obj.toString() + "%");

                if (msg.obj.toString().equals("100")) {
                    mRelativeProgress.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "上传成功", Toast.LENGTH_SHORT).show();

                    Transmission trans = new Transmission();
                    trans.itemType = Constants.CHAT_SEND;
                    trans.content = mFilePath;
                    trans.showType = 1;

                    mAdapter.addData(trans);
                    mRecyclerView.smoothScrollToPosition(mAdapter.getItemCount());
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        initData();
    }

    /**
     * init data
     */
    private void initData() {
        mGson = new Gson();

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter = new ChatAdapter(getDefaultData()));

        mClientThread = new ClientThread(mHandler);
        mClientThread.start();
    }

    /**
     * init  view id
     */
    private void initViews() {
        mEtContent = (EditText) findViewById(R.id.et_content);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler);
        mRelativeProgress = (RelativeLayout) findViewById(R.id.relative);
        mTvProgress = (TextView) findViewById(R.id.progress);
    }

    /**
     * send  message
     *
     * @param v
     */
    public void send(View v) {
        if (mEtContent.getText().toString().equals("")) {
            Toast.makeText(this, "输入的内容不能为空!", Toast.LENGTH_SHORT).show();
            return;
        }

        Transmission trans = new Transmission();
        trans.itemType = Constants.CHAT_SEND;
        trans.content = mEtContent.getText().toString();
        trans.transmissionType = Constants.TRANSFER_STR;

        Message message = new Message();
        message.what = Constants.SEND_MSG;
        message.obj = mGson.toJson(trans);

        if (mClientThread.getWriteHandler() != null) {
            mClientThread.getWriteHandler().sendMessage(message);
            mEtContent.setText("");
        }

        mRecyclerView.smoothScrollToPosition(mAdapter.getItemCount());
    }

    /**
     * send file
     *
     * @param view
     */
    public void send_file(View view) {
        if (!mayRequestPermission(mFilePermissions)) {
            return;
        }
        getImage();
    }

    /**
     * get default adapter data
     *
     * @return
     */
    public List<Transmission> getDefaultData() {
        List<Transmission> datas = new ArrayList<>();

        Transmission trans = new Transmission();
        trans.itemType = Constants.CHAT_FROM;
        trans.transmissionType = Constants.TRANSFER_STR;
        trans.content = "昆仑";
        datas.add(trans);

        trans = new Transmission();
        trans.itemType = Constants.CHAT_SEND;
        trans.transmissionType = Constants.TRANSFER_STR;
        trans.content = "英雄志";
        datas.add(trans);

        return datas;
    }


    @Override
    public void requestPermissionResult(boolean allowPermission) {
        if (allowPermission) {
            getImage();
        }
    }

    public void getImage() {

        Intent intent1 = new Intent(this, ImagePickActivity.class);
        intent1.putExtra(IS_NEED_CAMERA, true);
        intent1.putExtra(Constant.MAX_NUMBER, 1);
        startActivityForResult(intent1, Constant.REQUEST_CODE_PICK_IMAGE);

//        String[] projection = {MediaStore.Images.Media._ID,
//                MediaStore.Images.Media.DISPLAY_NAME,
//                MediaStore.Images.Media.DATA};
//        String orderBy = MediaStore.Images.Media.DISPLAY_NAME;
//        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
//        getContentProvider(uri, projection, orderBy);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constant.REQUEST_CODE_PICK_IMAGE:
                if (resultCode == RESULT_OK) {
                    ArrayList<ImageFile> list = data.getParcelableArrayListExtra(Constant.RESULT_PICK_IMAGE);
                    for (ImageFile file : list) {
                        String path = file.getPath();
                        Message message = new Message();
                        message.what = Constants.SEND_FILE;
                        //注意图片地址换成你自己可以使用的图片地址
                        message.obj = path;
                        mFilePath = path;

                        if (mClientThread.getWriteHandler() != null) {
                            mClientThread.getWriteHandler().sendMessage(message);
                        }
                    }
                }
                break;
            default:
        }
    }

    /**
     * @param uri
     * @param projection
     * @param orderBy
     */
    private void getContentProvider(Uri uri, String[] projection, String orderBy) {
        Cursor cursor = getContentResolver().query(uri, projection, null, null, orderBy);
        if (cursor == null) {
            return;
        }
        while (cursor.moveToNext()) {
            //Log.e("MainActivity", "getContentProvider--------" + cursor.getString(cursor.getColumnIndex("_data")));
            //Log.e("MainActivity", "getContentProvider--------" + cursor.getString(cursor.getColumnIndex("_display_name")));

            DialogMenuItem item = new DialogMenuItem(cursor.getString(cursor.getColumnIndex("_data")), R.mipmap.ic_launcher);
            mMenuItems.add(item);
        }

        chooseTransmissionFile();

    }


    private void chooseTransmissionFile() {
        final NormalListDialog dialog = new NormalListDialog(this, mMenuItems);
        dialog.title("请选择")//
                .isTitleShow(false)//
                .itemPressColor(Color.parseColor("#85D3EF"))//
                .itemTextColor(Color.parseColor("#303030"))//
                .itemTextSize(15)//
                .cornerRadius(2)//
                .widthScale(0.75f)//
                .show();

        dialog.setOnOperItemClickL(new OnOperItemClickL() {
            @Override
            public void onOperItemClick(AdapterView<?> parent, View view, int position, long id) {
                Message message = new Message();
                message.what = Constants.SEND_FILE;
                //注意图片地址换成你自己可以使用的图片地址
                message.obj = mMenuItems.get(position).mOperName;

                if (mClientThread.getWriteHandler() != null) {
                    mClientThread.getWriteHandler().sendMessage(message);
                }
                dialog.dismiss();
            }
        });
    }
}
