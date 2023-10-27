package com.lazarus.run_track1

import android.content.Context
import android.util.Log
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.io.File
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Security
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.SecretKeySpec

fun generateAESKey(): ByteArray {
    val keyGen = KeyGenerator.getInstance("AES")
    keyGen.init(256) // You can choose the key size you need (e.g., 128, 256)
    val secretKey = keyGen.generateKey()
    return secretKey.encoded
}

fun generateKeyPair():KeyPair {

    // Create a KeyPairGenerator instance for RSA
    val keyPairGenerator = KeyPairGenerator.getInstance("RSA", "BC")

    // Initialize the KeyPairGenerator with the desired key size (e.g., 2048 bits)
    keyPairGenerator.initialize(2048)

    // Generate the key pair (public and private keys)
    return keyPairGenerator.generateKeyPair()
}

fun encryptFile(file:File, publicKey:PublicKey):Array<ByteArray>{
    val fileBytes = file.readBytes();
    val aesKey = generateAESKey();
    // Encrypt file
    val cipher = Cipher.getInstance("AES")
    val secretKey = SecretKeySpec(aesKey, "AES")
    cipher.init(Cipher.ENCRYPT_MODE, secretKey)
    val byteArray = cipher.doFinal(fileBytes)
    // Encrypt AES key
    val cipherRSA = Cipher.getInstance("RSA/ECB/PKCS1Padding", "BC")
    cipherRSA.init(Cipher.ENCRYPT_MODE, publicKey)
    return arrayOf(byteArray, cipher.doFinal(byteArray));
}

fun decryptFile(file:File, privateKey: PrivateKey):ByteArray{
    val fileBytes = file.readBytes();
    val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", "BC")
    cipher.init(Cipher.DECRYPT_MODE, privateKey)
    return cipher.doFinal(fileBytes);
}

fun getPublicKey(context: Context?):PublicKey{
    val publicFilePath = context?.filesDir.toString() + "/keys/rsa_public_key.x509";
    val publicKeyBytes: ByteArray = File(publicFilePath).readBytes()
    val keyFactory = KeyFactory.getInstance("RSA")
    val publicKeySpec = X509EncodedKeySpec(publicKeyBytes)
    val publicKey: PublicKey = keyFactory.generatePublic(publicKeySpec);
    return publicKey;
}

fun getPrivateKey(context: Context?):PrivateKey{
    val privateFilePath = context?.filesDir.toString() + "/keys/rsa_private_key.pkcs8";
    val privateKeyBytes: ByteArray = File(privateFilePath).readBytes()
    val keyFactory = KeyFactory.getInstance("RSA")
    val privateKeySpec = PKCS8EncodedKeySpec(privateKeyBytes)
    val privateKey: PrivateKey = keyFactory.generatePrivate(privateKeySpec)
    return privateKey;
}

fun initialiseEncryption(context:Context?){

    val privatefilePath = context?.filesDir.toString() + "/keys/rsa_private_key.pkcs8";
    val privatefile = File(privatefilePath);
    val keyPath =  context?.filesDir.toString() + "/keys";
    val keyDir = File(keyPath);
    if (keyDir.exists()) return;
    Log.d("ekeys", "generating");
    keyDir.mkdir();
    privatefile.createNewFile()

    val publicFilePath = context?.filesDir.toString() + "/keys/rsa_public_key.x509";
    val publicFile = File(publicFilePath);
    publicFile.createNewFile()

    val keyPair = generateKeyPair()
    val privateKey = keyPair.private;
    val publicKey = keyPair.public;

    val privatekeyBytes = privateKey.encoded
    val pkcs8KeySpec = PKCS8EncodedKeySpec(privatekeyBytes)
    val privatekeyFactory = KeyFactory.getInstance("RSA")

    val privateKeyInfo = privatekeyFactory.generatePrivate(pkcs8KeySpec)
    privatefile.writeBytes(privateKeyInfo.encoded)

    val publickeyBytes = publicKey.encoded
    val X509keySpec = X509EncodedKeySpec(publickeyBytes)

    val publickeyFactory = KeyFactory.getInstance("RSA")
    val reconstitutedKey = publickeyFactory.generatePublic(X509keySpec)
    publicFile.writeBytes(reconstitutedKey.encoded)

}

fun initialiseProvider(){
    Security.addProvider(BouncyCastleProvider())
}