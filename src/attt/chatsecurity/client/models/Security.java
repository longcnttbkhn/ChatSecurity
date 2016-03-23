package attt.chatsecurity.client.models;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;
import java.util.StringTokenizer;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SealedObject;

public class Security {

	private String fileSecurityName = "Client_security.txt";
	private ChatKey security_key;
	private ChatKey section_key;

	public Security(ObjectInputStream input, ObjectOutputStream output) throws Exception{
		File file = new File(fileSecurityName);
		if (file.exists()) {
			try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
				security_key = new ChatKey(reader.readLine());
				SecureRandom secureRandom = new SecureRandom();
				Long random1 = (Long) decryption((SealedObject) input.readObject(), security_key);
				random1 -= 1;
				Long random2 = secureRandom.nextLong();
				String send = random1 + "|" + random2;
				output.writeObject(encryption(send, security_key));
				output.flush();

				String receive = (String) decryption((SealedObject) input.readObject(), security_key);
				StringTokenizer strToken = new StringTokenizer(receive, "|");
				random2 -= 1;
				Long random2_ = new Long(strToken.nextToken());
				if (random2.equals(random2_)) {
					BigInteger key = new BigInteger(strToken.nextToken());
					section_key = new ChatKey(key.toByteArray());
				} else {
					throw new Exception("Kết nối không an toàn");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public SealedObject encryption(String plaintext) throws Exception{
		return encryption(plaintext, section_key);
	}

	public String decryption(Object ciphertext) throws Exception{
		return (String) decryption((SealedObject) ciphertext, section_key);
	}

	public SealedObject encryption(Serializable plaintext, ChatKey key) throws Exception {
		SealedObject ciphertext = null;
		Cipher cipher;
		cipher = Cipher.getInstance("DES");
		cipher.init(Cipher.ENCRYPT_MODE, key);
		ciphertext = new SealedObject(plaintext, cipher);
		return ciphertext;
	}

	public Object decryption(SealedObject ciphertext, ChatKey key) throws Exception{
		Object plaintext = null;
		Cipher cipher = Cipher.getInstance("DES");
		cipher.init(Cipher.DECRYPT_MODE, key);
		plaintext = ciphertext.getObject(cipher);
		return plaintext;

	}

}

class ChatKey implements Key {
	private byte[] encoded;

	public ChatKey(byte[] encoded) {
		// TODO Auto-generated constructor stub
		this.encoded = encoded;
	}

	public ChatKey(String encoded) {
		// TODO Auto-generated constructor stub
		BigInteger key = new BigInteger(encoded);
		this.encoded = key.toByteArray();
	}

	@Override
	public String getAlgorithm() {
		// TODO Auto-generated method stub
		return "DES";
	}

	@Override
	public String getFormat() {
		// TODO Auto-generated method stub
		return "RAW";
	}

	@Override
	public byte[] getEncoded() {
		// TODO Auto-generated method stub
		return encoded;
	}

}