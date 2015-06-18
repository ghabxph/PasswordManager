import java.util.Base64;
import java.security.SecureRandom;

public class Password{
	public int key;
	public String account;
	public String username;
	private String password;
	private String salt;	// random passcode
	private byte[] pepper;	// random IV
	
	public String getPassword() throws Exception{
		AESCrypto aes = new AESCrypto(this.salt, this.pepper);
		String ret = aes.decrypt(this.password);
		
		byte[] bSalt = new byte[24];
		new SecureRandom().nextBytes(bSalt);
		new SecureRandom().nextBytes(this.pepper);
		
		this.salt = Base64.getEncoder().encodeToString(bSalt);
		
		aes = new AESCrypto(this.salt, this.pepper);
		this.password = aes.encrypt(ret);
		return ret;
	}
	
	public void changePassword(String sPassword) throws Exception{
		byte[] bSalt = new byte[24];
		new SecureRandom().nextBytes(bSalt);
		new SecureRandom().nextBytes(this.pepper);
		this.salt = Base64.getEncoder().encodeToString(bSalt);
		
		AESCrypto aes = new AESCrypto(this.salt, this.pepper);
		this.password = aes.encrypt(sPassword);
	}
	
	public boolean setPassword(String sPassword) throws Exception{
		byte[] bSalt = new byte[24];
		this.pepper = new byte[16];
		new SecureRandom().nextBytes(bSalt);
		new SecureRandom().nextBytes(this.pepper);
		this.salt = Base64.getEncoder().encodeToString(bSalt);
		
		AESCrypto aes = new AESCrypto(this.salt, this.pepper);
		this.password = aes.encrypt(sPassword);
		return true;
	}
	
	public void setPassword(String sPassword, boolean bool){
		this.password = sPassword;
	}
	
	public void setSalt(String sSalt){
		this.salt = sSalt;
	}
	
	public void setPepper(byte[] sPepper){
		this.pepper = sPepper;
	}
	
	//deprecated -- intended for development purposes
	public void debug() throws Exception{
		AESCrypto aes = new AESCrypto(this.salt, this.pepper);
		String plainpwd = aes.decrypt(this.password);
		String base64pepper = Base64.getEncoder().encodeToString(this.pepper);
		
		System.out.println("Key: " + this.key);
		System.out.println("Account: " + this.account);
		System.out.println("Username: " + this.username);
		System.out.println("Password: " + this.password);
		System.out.println("Plaintext Password: " + plainpwd);
		System.out.println("Salt: " + this.salt);
		System.out.println("Pepper: " + base64pepper);
		System.out.println("------------------------------------\n");
	}
	
	private String b64estr(String plaintext)throws Exception{
		return Base64.getEncoder().encodeToString(plaintext.getBytes("UTF-8"));
	}
	
	public String get_xml() throws Exception{
		String key = String.valueOf(this.key);
		String act = b64estr(this.account);
		String usr = b64estr(this.username);
		String pwd = this.password;
		String slt = this.salt;
		String pep = Base64.getEncoder().encodeToString(this.pepper);
		return "<password key=\"" + key + "\" account=\"" + act + "\" username=\"" + usr + "\" password=\"" + pwd +"\" salt=\"" + slt + "\" pepper=\"" + pep + "\"></password>";
	}
}