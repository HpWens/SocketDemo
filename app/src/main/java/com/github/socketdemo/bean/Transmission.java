package com.github.socketdemo.bean;

/**
 * Created by boby on 2017/2/21.
 */

import android.os.Parcel;
import android.os.Parcelable;

import com.github.library.entity.MultiItemEntity;
import com.github.socketdemo.socket.Constants;

/**
 * 传输
 */
public class Transmission implements Parcelable, MultiItemEntity {

    //文件名称
    public String fileName;

    //文件长度
    public long fileLength;

    //传输类型
    public int transmissionType;

    //传输内容
    public String content;

    //传输的长度
    public long transLength;

    //发送还是接受类型
    public int itemType = Constants.CHAT_SEND;

    //0 文本  1  图片
    public int showType;

    public Transmission() {
    }

    @Override
    public int getItemType() {
        return itemType;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.fileName);
        dest.writeLong(this.fileLength);
        dest.writeInt(this.transmissionType);
        dest.writeString(this.content);
        dest.writeLong(this.transLength);
        dest.writeInt(this.itemType);
        dest.writeInt(this.showType);
    }

    protected Transmission(Parcel in) {
        this.fileName = in.readString();
        this.fileLength = in.readLong();
        this.transmissionType = in.readInt();
        this.content = in.readString();
        this.transLength = in.readLong();
        this.itemType = in.readInt();
        this.showType = in.readInt();
    }

    public static final Creator<Transmission> CREATOR = new Creator<Transmission>() {
        @Override
        public Transmission createFromParcel(Parcel source) {
            return new Transmission(source);
        }

        @Override
        public Transmission[] newArray(int size) {
            return new Transmission[size];
        }
    };
}
