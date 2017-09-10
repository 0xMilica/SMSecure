package com.pma.smsecure.Properties;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

import com.pma.smsecure.Dao.User;
import com.pma.smsecure.Dao.UserDao;
import com.pma.smsecure.Helper.DaoFactory;
import com.pma.smsecure.R;
import com.pma.smsecure.Security.SHA1;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by dell on 21-Jan-17.
 */

public class ChangePasswordPreference extends DialogPreference{

    private Context context;
    private DaoFactory daoFactory = new DaoFactory();
    private EditText newPasswordEt;
    private EditText oldPasswordEt;

    public ChangePasswordPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        setPersistent(false);
        setTitle("Change Password"); // This will override ListPreference Title
        setDialogLayoutResource(R.layout.password_preference);

    }

    @Override
    protected void onBindDialogView(View view) {

        newPasswordEt = (EditText) view.findViewById(R.id.newPassword);
        oldPasswordEt = (EditText) view.findViewById(R.id.oldPassword);

        super.onBindDialogView(view);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        //super.onDialogClosed(positiveResult);
        if(positiveResult){

            String newPassword = newPasswordEt.getText().toString();
            String oldPassword = oldPasswordEt.getText().toString();
            changePassword(newPassword, oldPassword);

        }
    }

    public void changePassword(String newPassword, String oldPassword){

        UserDao userDao = (UserDao)daoFactory.getDaoObject("UserDao", context);
        User user = userDao.load(1L);
        try {
            // TODO add salt ..
            //check old password
            if(!user.getAppPassword().equals(SHA1.toSHA1(oldPassword))){
                return;
            }
            newPassword = SHA1.toSHA1(newPassword);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        user.setAppPassword(newPassword);
        userDao.update(user);

    }
}
