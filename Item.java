package com.example.webview;

import java.io.File;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

public class Item implements Parcelable {
	public String url;
	public String realName;
	public File downFile;
	public String localPath;
	public List<File> listDownFfile;
	boolean isChekced;

	public Item(String url, String realName) {
		this.url = url;
		this.realName = realName;
	}

	public Item(String url, String realName, File dirList) {
		this.url = url;
		this.realName = realName;
		this.downFile = dirList;
	}
	@Override
	public String toString() {
		return "url : " + url + ", realName : " + realName;
	}

	public Item(Parcel in) {
	        readFromParcel(in);
	    }



	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getRealName() {
		return realName;
	}

	public void setRealName(String realName) {
		this.realName = realName;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeString(url);
		dest.writeString(realName);
	}
	
	private void readFromParcel(Parcel in) {
		// TODO Auto-generated method stub
		url = in.readString();
		realName = in.readString();
	
	}

	@SuppressWarnings("rawtypes")
    public static final Parcelable.Creator<Item> CREATOR = new Parcelable.Creator<Item>() {
 
        @Override
        public Item createFromParcel(Parcel in) {
            return new Item(in);
        }
 
        @Override
        public Item[] newArray(int size) {
            // TODO Auto-generated method stub
            return new Item[size];
        }
 
    };
}