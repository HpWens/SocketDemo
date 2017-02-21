package com.github.socketdemo;

import android.graphics.BitmapFactory;

import com.github.library.BaseMultiItemQuickAdapter;
import com.github.library.BaseViewHolder;
import com.github.socketdemo.bean.Transmission;
import com.github.socketdemo.socket.Constants;

import java.util.List;

/**
 * Created by boby on 2017/2/18.
 */

public class ChatAdapter extends BaseMultiItemQuickAdapter<Transmission, BaseViewHolder> {

    /**
     * Same as QuickAdapter#QuickAdapter(Context,int) but with
     * some initialization data.
     *
     * @param data A new list is created out of this one to avoid mutable list
     */
    public ChatAdapter(List<Transmission> data) {
        super(data);

        addItemType(Constants.CHAT_FROM, R.layout.chat_from_msg);
        addItemType(Constants.CHAT_SEND, R.layout.chat_send_msg);
    }

    @Override
    protected void convert(BaseViewHolder helper, Transmission item) {

        switch (item.itemType) {
            case Constants.CHAT_FROM:
                helper.setText(R.id.chat_from_content, item.content);
                break;
            case Constants.CHAT_SEND:
                if (item.showType == 1) {
                    helper.setVisible(R.id.chat_send_image, true);
                    helper.setVisible(R.id.chat_send_content, false);
                    helper.setImageBitmap(R.id.chat_send_image, BitmapFactory.decodeFile(item.content));
                } else {
                    helper.setVisible(R.id.chat_send_image, false);
                    helper.setVisible(R.id.chat_send_content, true);
                    helper.setText(R.id.chat_send_content, item.content);
                }
                break;
            default:
        }

    }
}
