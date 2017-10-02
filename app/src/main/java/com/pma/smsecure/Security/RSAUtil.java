package com.pma.smsecure.Security;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

import android.util.Log;

public class RSAUtil {
	
	private static final String TAG = "RSA Util";
	
	public static byte[] hex2byte(byte[] b){
		if ((b.length % 2) != 0)
			throw new IllegalArgumentException("hex2byte");
    
		byte[] b2 = new byte[b.length / 2];
    
		for (int n = 0; n < b.length; n += 2){
			String item = new String(b, n, 2);
			b2[n/2] = (byte)Integer.parseInt(item, 16);
		}
		return b2;
    }
	
	public static String byte2hex(byte[] b){
		String hs = "";
		String stmp = "";
		for (int n = 0; n < b.length; n ++){
			stmp = Integer.toHexString(b[n] & 0xFF);
			if (stmp.length() == 1)
				hs += ("0" + stmp);
			else
				hs += stmp;
		}
		return hs.toUpperCase();
    }
	
	/**
	 *  Encode the original data with RSA private key
	 */
	public static String encrypt(String data, Key key){
		
		byte[] encodedBytes = null;
		try {
			Cipher c = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			c.init(Cipher.ENCRYPT_MODE, key);
			encodedBytes = c.doFinal(data.getBytes());
			
		} catch (Exception e) {
			Log.e(TAG, "RSA encryption error");
		}
		
		return Base64.encodeBytes(encodedBytes);

	}
	
	/**
	 *  Decode the encoded data with RSA public key
	 */
	public static String decrypt(String data, Key key){
		Log.d("Message ENC: ", data);
		byte[] decodedBytes = null;
		try {
		
			Cipher c = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			c.init(Cipher.DECRYPT_MODE, key);
			decodedBytes = c.doFinal(Base64.decode(data.getBytes()));
			
		} catch (Exception e) {
			Log.e(TAG, "RSA decryption error");
			Log.e(TAG, "Cause = " + e.getCause() + "/n message = " + e.getMessage());
		}
		Log.d("Number of bytes per array = ", ""+decodedBytes.length);
		return new String(decodedBytes);

	}


	/**
	 * Generate key pair for 1024-bit RSA
	 * @return new KeyPair or null
	 */
	public static KeyPair generateNewKeys(){
		
		Key publicKey = null;
		Key privateKey = null;
		KeyPair kp = null;
		try {
			KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
			kpg.initialize(1024);
			kp = kpg.genKeyPair();
			publicKey = kp.getPublic();
			privateKey = kp.getPrivate();
			
		} catch (Exception e) {
			Log.e(TAG, "RSA key pair error");
			Log.e(TAG, e.getMessage());
		}

		return kp;
	}
	
	/**
	 * Convert string to PublicKey
	 * @param publicK
	 * @return PublicKey or null
	 */
	public static PublicKey stringToPublicKey(String publicK){
		
		PublicKey key = null;
		byte[] keyBytes;
		
		try {
			keyBytes = Base64.decode(publicK.getBytes("UTF-8"));
			X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");

			key = keyFactory.generatePublic(spec);
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return key;
	}
	
	/**
	 * Convert string to PrivateKey
	 * @param publicK
	 * @return PrivateKey or null
	 */
	public static PrivateKey stringToPrivateKey(String privateK){
		
		PrivateKey key = null;
		byte[] keyBytes;
		
		try {
			keyBytes = Base64.decode(privateK.getBytes("UTF-8"));
			// PKCS8 treba prebaciti u PKCS1 ?
			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
			KeyFactory fact = KeyFactory.getInstance("RSA");
			key = fact.generatePrivate(keySpec);
			
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return key;
	}

}
