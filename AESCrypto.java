/**
 * @(#)AES256Crypto.java
 *
 *
 * @author : Ghabx
 * @version 1.00 2015/6/17
 */
 
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.*;
import javax.crypto.spec.*;
import java.util.Base64;


public class AESCrypto {

	//Constructor-------------------------------
    public AESCrypto(String sKey, byte[] ivKey) throws Exception {
    	MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
    	byte[] bTemp = sha256.digest(sKey.getBytes("UTF-8"));
    	bKey = new byte[16];
    	for(int i = 0; i < bKey.length; i++){
    		bKey[i] = bTemp[i];
    	}
    	
    	bTemp = sha256.digest(bKey);
    	iv = ivKey;
    	
    }
    //------------END OF CONSTRUCTOR------------
    
    
    private byte[] bKey;
    private byte[] iv;
    
    
    
    //Public Methods-------------------------------
	public String decrypt(String encData) throws Exception{
		Key kKey = new SecretKeySpec(this.bKey, "AES");
		Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
    	IvParameterSpec ivspec = new IvParameterSpec(iv);
    	
		c.init(Cipher.DECRYPT_MODE, kKey, ivspec);
		byte[] bEncData = Base64.getDecoder().decode(encData);
		byte[] bDecrypted = c.doFinal(bEncData);
    	return new String(bDecrypted);
    }
    
    public String encrypt(String strData) throws Exception{
    	Key kKey = new SecretKeySpec(this.bKey, "AES");
    	Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
    	IvParameterSpec ivspec = new IvParameterSpec(iv);
    	
    	c.init(Cipher.ENCRYPT_MODE, kKey, ivspec);
    	byte[] enc = c.doFinal(strData.getBytes());
    	return Base64.getEncoder().encodeToString(enc);
    }
    //------------END OF PUBLIC METHODS------------




    //Private Methods-------------------------------
    
    	
 	//------------END OF PRIVATE METHODS------------
    
}