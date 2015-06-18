import java.security.*;
import java.util.*;
import java.io.*;
import java.awt.datatransfer.*;
import java.awt.*;

public class PasswordManager{
	private static String ivKeyString;
	private static String xmldata;
	private static String passphrase;
	private static Console cnsl;
	
	private static final String INITIAL_XMLDATA = 
		"<?xml version=\"1.0\" encoding=\"utf-8\"?>" + 
		"<passwords key_counter=\"0\">" +
		"</passwords>"
	;
	
	private static Passwords pwdObject;
	
	public static void main(String[] args)throws Exception{
		cnsl = System.console();
		ivKeyString = "";
		xmldata = "";
		initialize_files();
		decrypt_data();
		pwdObject = new Passwords(xmldata);
		
		System.out.println("\n\n\n");
		System.out.println("Welcome to Enigma");
		System.out.println("--------------------------------");
		
		pwdObject.showAccounts();
		
		System.out.println("Type \"help\" to show list of commands.");
		
		System.out.print("Command: ");
		String cmd;
		do{
			Scanner sc = new Scanner(System.in);
			cmd = sc.nextLine();
			if(cmd.equals("add")){
				show_add();
			}else if(cmd.equals("capwd")){
				show_change_account_password();
			}else if(cmd.equals("chpp")){
				show_change_passphrase();
			}else if(cmd.equals("cls")){
				clear_screen();
			}else if(cmd.equals("delete")){
				show_delete();
			}else if(cmd.equals("help")){
				show_help();
			}else if(cmd.equals("cbpwd")){
				show_pwd_to_clipboard();
			}else if(cmd.equals("show")){
				pwdObject.showAccounts();
			}else if(cmd.equals("exit")){
				clear_screen();
				break;
			}else{
				System.out.println("Invalid command. Please type \"help\" to show the list of commands.");
			}
			
			System.out.print("Command: ");
		}while(cmd.equals("exit") == false);
		
		save();
	}
	
	private static void show_change_passphrase() throws Exception{
		String abc, def;
		abc = String.valueOf(cnsl.readPassword("Enter current passphrase: "));
		if(!abc.equals(passphrase)){
			clear_screen();
			System.out.println("Invalid passphrase. You cannot change the passphrase.");
			return;
		}
		clear_screen();
		abc = String.valueOf(cnsl.readPassword("Enter new passphrase: "));
		def = String.valueOf(cnsl.readPassword("Confirm passphrase: "));
		if(abc.equals(def) == false){
			clear_screen();
			System.out.println("Passphrase do not match. Please try again later.");
			return;
		}
		passphrase = abc;
		save();
		clear_screen();
		System.out.println("You have changed the passphrase successfully! Please make sure you remember it, or you will not be able to access your passwords again.");
	}
	
	private static void show_delete() throws Exception{
		int n;
		try{
			n = Integer.parseInt(String.valueOf(cnsl.readLine("Key: ")));
		}catch(Exception e){
			System.out.println("Please enter number only! Please try again.");
			return;
		}
		System.out.println(pwdObject.delete_item(n));
	}
	
	private static void show_change_account_password() throws Exception{
		int n;
		try{
			n = Integer.parseInt(String.valueOf(cnsl.readLine("Key: ")));
		}catch(Exception e){
			System.out.println("Please enter number only! Please try again.");
			return;
		}
		String pwd = String.valueOf(cnsl.readPassword("Change password (no confirmation): "));
		if(pwdObject.change_password(n, pwd)){
			System.out.println("Password has been changed successfully");
		}else{
			System.out.println("Account #" + n + " doesn't exist. Cannot change password");
		}
	}
	
	private static void show_pwd_to_clipboard() throws Exception{
		int n;
		try{
			n = Integer.parseInt(String.valueOf(cnsl.readLine("Key: ")));
		}catch(Exception e){
			System.out.println("Please enter number only! Please try again.");
			return;
		}
		String str = pwdObject.showPassword(n);
		clear_screen();
		if(str.isEmpty()){
			System.out.println("Account #" + n + " doesn't exist");
		}else{
			set_clipboard(str);
			System.out.println("The password of account #" + n + " has been copied to the clipboard.");
		}
	}
	
	private static void show_help(){
		clear_screen();
		System.out.println(
			"\n\n\n" +
			"How to use:\n" +
			"add\n - Add an account\n" +
			"capwd\n - Change account password\n" +
			"cbpwd\n - Copy account password to the clippboard\n" +
			"chpp\n - Change passphrase\n" +
			"cls\n - Clear screen\n" +
			"delete\n - Delete an account\n" +
			"help\n - Show available list of commands\n" +
			"show\n - Show the list of accounts\n" +
			"exit\n - Exits the system\n"
		);
	}
	
	private static void show_add() throws Exception{
		 
		if(cnsl == null){
			System.out.println("Please run this application in a console window. Program will now exit.");
			System.exit(-1);
			return;
		}
		String account = cnsl.readLine("Account: ");
		String username = cnsl.readLine("Username: ");
		String password = String.valueOf(cnsl.readPassword("Password: "));
		
		pwdObject.create(account, username, password);
		System.out.println("\n\n\n");
		pwdObject.showAccounts();
		System.out.println("New account has been created!");
	}
	
	private static void decrypt_data() throws Exception{
		 
		if(cnsl == null){
			System.out.println("Please run this application in a console window. Program will now exit.");
			System.exit(-1);
			return;
		}
		boolean invalid_passphrase = false;
		do{
			clear_screen();
			if(invalid_passphrase){
				System.out.println("\nInvalid passphrase. Please try again.");
			}
			passphrase = String.valueOf(cnsl.readPassword("Enter passphrase: "));
			byte[] ivKey = Base64.getDecoder().decode(ivKeyString);
			AESCrypto aes = new AESCrypto(passphrase, ivKey);
			try{
				xmldata = aes.decrypt(xmldata);
				invalid_passphrase = false;
			}catch(javax.crypto.BadPaddingException bpe){
				invalid_passphrase = true;
			}
		}while(invalid_passphrase);
		clear_screen();
	}

	private static void initialize_files() throws Exception{
		byte[] ivKey;
		File ivFile = new File("iv.b64");
		File dtFile = new File("dt.b64");
		if(!(ivFile.exists() && dtFile.exists())){
			ivKey = new byte[16];
			 
			if(cnsl == null){
				System.out.println("Please run this application in a console window. Program will now exit.");
				System.exit(-1);
				return;
			}
			do{
				System.out.println("Setting up the password manager for the first run.");
				System.out.println("Enter a passphrase");
				passphrase = String.valueOf(cnsl.readPassword("Passphrase: "));
				if(passphrase.isEmpty()){
					System.out.println("Please enter a passphrase!\n");
				}
			}while(passphrase.isEmpty());
			
			new SecureRandom().nextBytes(ivKey);
			xmldata = INITIAL_XMLDATA;
			AESCrypto aes = new AESCrypto(passphrase, ivKey);
			xmldata = aes.encrypt(xmldata);
			ivKeyString = Base64.getEncoder().encodeToString(ivKey);
			
			
			if(!(ivFile.createNewFile() && dtFile.createNewFile())){
				System.out.println("Cannot create iv.b64 and dt.b64 files. The system cannot continue.");
				System.exit(-1);
				return;
			}
			
			FileWriter ivFileFw = new FileWriter(ivFile);
			FileWriter dtFileFw = new FileWriter(dtFile);
			
			ivFileFw.write(ivKeyString);
			ivFileFw.flush();
			ivFileFw.close();
			
			dtFileFw.write(xmldata);
			dtFileFw.flush();
			dtFileFw.close();
			return;
		}
		
		FileReader ivFileFr = new FileReader(ivFile);
		FileReader dtFileFr = new FileReader(dtFile);
		//ivKey should be in base64 format!
		int i;
		
		do{
			i = ivFileFr.read();
			if(i != -1){
				ivKeyString += String.valueOf((char)i);
			}
		}while(i != -1);
		ivFileFr.close();
		
		do{
			i = dtFileFr.read();
			if(i != -1){
				xmldata += String.valueOf((char)i);
			}
		}while(i != -1);
		dtFileFr.close();
	}
	
	private static void clear_screen(){
		for(int i = 0; i < 25; i++){
			System.out.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
			System.out.flush();
		}
	}
	
	private static void save() throws Exception{
		byte[] ivKey = new byte[16];
		new SecureRandom().nextBytes(ivKey); // New Random IVKey
		ivKeyString = Base64.getEncoder().encodeToString(ivKey); // new IVKey String in Base64 Format
		pwdObject.save_to_file(passphrase, ivKey);
		
		FileWriter ivFileFw = new FileWriter("iv.b64");
		ivFileFw.write(ivKeyString); // Saves the new IV Key
		ivFileFw.flush();
		ivFileFw.close();
	}
	
	private static void set_clipboard(String str){
		StringSelection ss = new StringSelection(str);
		Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
		cb.setContents(ss, null);
	}
}

/*
 *
 * 
 * Account    | Username                      | Password
 * Google     | ghabxph@gmail.com             | somepassword
 * Google     | ghabxph.official@gmail.com    | somepassword99
 *
 * decrypt -> xml -> memory
 * memory -> xml -> encrypt
 *
 *
 *
 * initialize
 * Enter the passphrase: do not kill dummy.
 *
 *
 * Files that I need:
 * iv.b64
 * dt.b64
 * 
 */