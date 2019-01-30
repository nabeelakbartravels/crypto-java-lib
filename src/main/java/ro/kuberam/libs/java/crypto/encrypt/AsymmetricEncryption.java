/**
 * EXPath Cryptographic Module
 * Java Library providing an EXPath Cryptographic Module
 * Copyright (C) 2015 Kuberam
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package ro.kuberam.libs.java.crypto.encrypt;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Optional;
import java.util.StringTokenizer;

import javax.annotation.Nullable;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import ro.kuberam.libs.java.crypto.CryptoException;
import ro.kuberam.libs.java.crypto.utils.Buffer;

/**
 * @author <a href="mailto:claudius.teodorescu@gmail.com">Claudius
 *         Teodorescu</a>
 */
public class AsymmetricEncryption {

	public static String encryptString(String input, String publicKey, String transformationName)
			throws CryptoException, IOException {
		try (InputStream bais = new ByteArrayInputStream(input.getBytes(UTF_8))) {
			return encrypt(bais, publicKey, transformationName);
		}
	}

	public static String encrypt(InputStream input, String privateKey, String transformationName)
			throws CryptoException, IOException {
		String algorithm = transformationName.split("/")[0];
		String provider = "SUN";

		Cipher cipher;
		try {
			cipher = Cipher.getInstance(transformationName);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			throw new CryptoException(e);
		}

		try {
			PrivateKey privateKey1 = loadPrivateKey(privateKey, algorithm, provider);
			cipher.init(Cipher.ENCRYPT_MODE, privateKey1);
		} catch (InvalidKeyException e) {
			throw new CryptoException(e);
		} catch (Exception e) {
			e.printStackTrace();
		}

		byte[] resultBytes;
		try {
			byte[] buf = new byte[Buffer.TRANSFER_SIZE];
			int read = -1;
			while ((read = input.read(buf)) > -1) {
				cipher.update(buf, 0, read);
			}
			resultBytes = cipher.doFinal();
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			throw new CryptoException(e);
		}

		return getString(resultBytes);
	}

	public static String decryptString(String encryptedInput, String plainKey, String transformationName, String iv,
			@Nullable String provider) throws CryptoException, IOException {
		try (InputStream bais = new ByteArrayInputStream(getBytes(encryptedInput))) {
			return decrypt(bais, plainKey, transformationName, iv, provider);
		}
	}

	public static String decrypt(InputStream encryptedInput, String plainKey, String transformationName, String iv,
			@Nullable String provider) throws CryptoException, IOException {
		String algorithm = transformationName.split("/")[0];

		String actualProvider = Optional.ofNullable(provider).filter(str -> !str.isEmpty()).orElse("SunJCE");

		Cipher cipher;
		try {
			cipher = Cipher.getInstance(transformationName, actualProvider);
		} catch (NoSuchProviderException | NoSuchAlgorithmException | NoSuchPaddingException e) {
			throw new CryptoException(e);
		}

		SecretKeySpec skeySpec = new SecretKeySpec(plainKey.getBytes(UTF_8), algorithm);
		if (transformationName.contains("/")) {
			IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes(UTF_8), 0, 16);
			try {
				cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivSpec);
			} catch (InvalidAlgorithmParameterException | InvalidKeyException e) {
				throw new CryptoException(e);
			}
		} else {
			try {
				cipher.init(Cipher.DECRYPT_MODE, skeySpec);
			} catch (InvalidKeyException e) {
				throw new CryptoException(e);
			}
		}

		try {
			byte[] buf = new byte[Buffer.TRANSFER_SIZE];
			int read = -1;
			while ((read = encryptedInput.read(buf)) > -1) {
				cipher.update(buf, 0, read);
			}

			byte[] resultBytes = cipher.doFinal();
			return new String(resultBytes, UTF_8);
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			throw new CryptoException(e);
		}
	}

	public static String getString(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			byte b = bytes[i];
			sb.append((int) (0x00FF & b));
			if (i + 1 < bytes.length) {
				sb.append("-");
			}
		}
		return sb.toString();
	}

	public static byte[] getBytes(String str) throws IOException {
		StringTokenizer st = new StringTokenizer(str, "-", false);
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
			while (st.hasMoreTokens()) {
				int i = Integer.parseInt(st.nextToken());
				bos.write((byte) i);
			}
			return bos.toByteArray();
		}
	}

	private static PublicKey loadPublicKey(String keyString, String algorithm, String provider)
			throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException {
		byte[] keyBytes = keyString.getBytes(UTF_8);
		// provider = Optional.ofNullable(provider).filter(str ->
		// !str.isEmpty()).orElse("SunRsaSign");
		provider = "SunRsaSign";

		X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
		KeyFactory kf = KeyFactory.getInstance(algorithm, provider);

		return kf.generatePublic(spec);
	}

	private static PrivateKey loadPrivateKey(String keyString, String algorithm, String provider)
			throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException {
		byte[] keyBytes = keyString.getBytes(UTF_8);
		// provider = Optional.ofNullable(provider).filter(str ->
		// !str.isEmpty()).orElse("SunRsaSign");
		provider = "SunRsaSign";

		PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);

		KeyFactory kf = KeyFactory.getInstance(algorithm, provider);

		return kf.generatePrivate(spec);
	}
}
