import java.util.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;


/*
 SPECIFICATIONS:
 	key			= normal string
 	account		= base64
 	username	= base64
 	password	= base64
 	salt		= base64
 	pepper		= base64
 */
public class Passwords{
	private ArrayList<Password> password;
	int key_counter;
	
	public Passwords(String xmldata) throws Exception{
		this.password = new ArrayList<Password>(0);
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		
		StringBuilder xmlStringBuilder = new StringBuilder();
		xmlStringBuilder.append(xmldata);
		ByteArrayInputStream input = new ByteArrayInputStream(xmlStringBuilder.toString().getBytes("UTF-8"));
		Document doc = builder.parse(input);
		
		Element root = doc.getDocumentElement();
		this.key_counter = Integer.parseInt(root.getAttribute("key_counter").toString());
		
		if(this.key_counter == 0){
			return;
		}
		
		NodeList nList = root.getElementsByTagName("password");
		for(int i = 0; i < nList.getLength(); i++){
			Node nNode = nList.item(i);
			if(nNode.getNodeType() == Node.ELEMENT_NODE){
				Element eElement	= (Element) nNode;
				int 	iKey		= Integer.parseInt(eElement.getAttribute("key").toString());
				String	sAccount	= eElement.getAttribute("account").toString();
				String	sUsername	= eElement.getAttribute("username").toString();
				String	sPassword	= eElement.getAttribute("password").toString();
				String	sSalt		= eElement.getAttribute("salt").toString();
				String	sPepper		= eElement.getAttribute("pepper").toString();
				
				sAccount	= b64dstr(sAccount);
				sUsername	= b64dstr(sUsername);
				
				
				
				byte[] bPepper = Base64.getDecoder().decode(sPepper);
				add_password(iKey, sAccount, sUsername, sPassword, sSalt, bPepper);
			}
		}
		
		/*
		Element passwords = doc.getDocumentElement();
		NodeList password = doc.getElementsByTagName("password");
		
		Element testElement = (Element) password.item(0);
		testElement.getElementsByTagName("key");
		
		*/
		
		
	}
	
	
	private String b64dstr(String base64){
		byte[] a = Base64.getDecoder().decode(base64);
		String ret = "";
		for(int i = 0; i < a.length; i++){
			ret += String.valueOf((char)a[i]);
		}
		return ret;
	}
	
	private void add_password(int key, String account, String username, String password, String salt, byte[] pepper){
		Password obj = new Password();
		
		obj.key = key;
		obj.account = account;
		obj.username = username;
		obj.setPassword(password, true);
		obj.setSalt(salt);
		obj.setPepper(pepper);
		
		this.password.add(obj);
	}
	
	public void create(String account, String username, String password) throws Exception{
		Password obj = new Password();
		
		obj.key = key_counter;
		obj.account = account;
		obj.username = username;
		obj.setPassword(password);
		
		this.password.add(obj);
		this.key_counter++;
	}
	
	public void showAccounts(){
		if(this.password.size() == 0){
			System.out.println("You have no account stored in this system");
			return;
		}
		for(int i = 0; i < this.password.size(); i++){
			System.out.println(
				"Key: " + password.get(i).key + "\n" +
				"Account: " + password.get(i).account + "\n" +
				"Username: " + password.get(i).username + "\n"
			);
		}
	}
	
	public void showDebug() throws Exception{
		for(int i = 0; i < this.password.size(); i++){
			password.get(i).debug();
		}
	}
	
	public boolean change_password(int key, String password) throws Exception{
		boolean ret = false;
		for(int i = 0; i < this.password.size(); i++){
			if(this.password.get(i).key == key){
				ret = true;
				this.password.get(i).changePassword(password);
			}
		}
		return ret;
	}
	
	public String delete_item(int key) throws Exception{
		boolean exists = false;
		for(int i = 0; i < this.password.size(); i++){
			if(password.get(i).key == key){
				exists = true;
				password.remove(i);
			}
		}
		if(exists){
			return "Account #" + key + " has been deleted successfully.";
		}else{
			return "Account #" + key + " doesn't exist";
		}
	}
	
	public String showPassword(int key) throws Exception{
		for(int i = 0; i < this.password.size(); i++){
			if(password.get(i).key == key){
				return this.password.get(i).getPassword();
			}
		}
		return "";
	}
	
	public void save_to_file(String sKey, byte[] ivKey) throws Exception{
		String xmldata = 
			"<?xml version=\"1.0\" encoding=\"utf-8\"?>" + 
			"<passwords key_counter=\"" + String.valueOf(key_counter) + "\">"
		;
		
		for(int i = 0; i < this.password.size(); i++){
			xmldata += this.password.get(i).get_xml();
		}
		
		xmldata += "</passwords>";
		
		AESCrypto aes = new AESCrypto(sKey, ivKey);
		xmldata = aes.encrypt(xmldata);
		
		File dtFile = new File("dt.b64");
		FileWriter dtFileFw = new FileWriter(dtFile);
		
		dtFileFw.write(xmldata);
		dtFileFw.flush();
		dtFileFw.close();
	}
	
}