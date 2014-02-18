package com.example.oauth;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * OAuth로 인증받은 User 객체
 * 
 * @author jykim
 */
public class OAuthUser implements Parcelable {
    
    /**
     * Json string 을 OAuthUser 객체로 변환하는 API
     * 
     * @author jykim
     */
    public interface Convertable {
        public abstract OAuthUser convertUser(String jsonString);
    }
    
    /**
     * serialVersionUID
     */
    @SuppressWarnings("unused")
    private static final long serialVersionUID = 6974043612215757261L;
    
    private String mID = null;
    private String mName = null;
    private OAuthVendor mAuthType = OAuthVendor.NONE;
    
    /**
     * Constructor 
     * @param id user id
     * @param name user name
     * @param type auth type
     */
    public OAuthUser(String id, String name, OAuthVendor type) {
        this.mID = id;
        this.mName = name;
        this.mAuthType = type;
    }

    /**
     * @return the mID
     */
    public String getID() {
        return mID;
    }

    /**
     * @return the mName
     */
    public String getName() {
        return mName;
    }

    /**
     * @return the mAuthType
     */
    public OAuthVendor getAuthType() {
        return mAuthType;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mID);
        dest.writeString(mName);
        dest.writeValue(mAuthType);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        return sb.append("mID : ").append(mID).append("\nmName : ").append(mName)
                .append("\nmAuthType : ").append(mAuthType).toString();
    }
}
