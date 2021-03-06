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
package ro.kuberam.libs.java.crypto.keyManagement;

import java.util.Map;

import org.junit.Test;

import ro.kuberam.tests.junit.BaseTest;

public class TranslateKeyTest extends BaseTest {

	@Test
	public void rsaKeyPair() throws Exception {
		String algorithm = "DSA";
		String keyGenerationProvider = "SUN";
		Map<String, String> keys = GenerateKeyPair.run(algorithm, keyGenerationProvider);

		System.out.println("Private key:\n" + keys.get("private-key"));
		System.out.println("Public key:\n" + keys.get("public-key"));
	}
}
