package com.pma.smsecure.Helper;

import android.content.Context;

import com.pma.smsecure.Dao.Contact;

public class ContactPma {

    private String phoneNumber;
    private String name;
    private String publicKey;
    private Integer phone_id_contact;
    private Long id;

    public ContactPma(Contact contact, Context context){
        this.id = contact.getId();
        this.phoneNumber = contact.getPhoneNumber();
        this.publicKey = contact.getPublicKey();
        this.phone_id_contact = contact.getPhone_id_contact();

        String tempName = SMSHelper.getContactName(context, contact.getPhoneNumber());
        if(!tempName.equals(phoneNumber))
            this.name = tempName;
        else
            this.name = "";
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getName() {
        return name;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public Integer getPhone_id_contact() {
        return phone_id_contact;
    }

    public Long getId() {
        return id;
    }

}
