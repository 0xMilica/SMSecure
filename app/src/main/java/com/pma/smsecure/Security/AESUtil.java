package com.pma.smsecure.Security;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class AESUtil {

	public static byte[] encryptAES(String secretKeyString, String msgContentString) {

		try {
			byte[] returnArray;

			// generate AES secret key from user input
			Key key = new SecretKeySpec(secretKeyString.getBytes(), "AES");

			// specify the cipher algorithm using AES
			Cipher c = Cipher.getInstance("AES");

			// specify the encryption mode
			c.init(Cipher.ENCRYPT_MODE, key);

			// encrypt
			returnArray = c.doFinal(msgContentString.getBytes());
			return returnArray;

		} catch (Exception e) {
			e.printStackTrace();
			byte[] returnArray = null;
			return returnArray;
		}
	}

	public static byte[] decryptAES(String secretKeyString, byte[] encryptedMsg) throws Exception {

		// generate AES secret key from the user input secret key
		Key key = new SecretKeySpec(secretKeyString.getBytes(), "AES");

		// get the cipher algorithm for AES
		Cipher c = Cipher.getInstance("AES");

		// specify the decryption mode
		c.init(Cipher.DECRYPT_MODE, key);

		// decrypt the message
		byte[] decValue = c.doFinal(encryptedMsg);
		return decValue;
		
	}

}
