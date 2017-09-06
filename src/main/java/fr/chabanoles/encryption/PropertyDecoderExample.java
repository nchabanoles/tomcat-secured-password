package fr.chabanoles.encryption;

import bitronix.tm.utils.CryptoEngine;

/**
 * This class creates an indirection to the real implementation of the crypto algo.
 * In this example we use Bitronix implementation.
 *
 * @author Nicolas Chabanoles
 */
public class PropertyDecoderExample implements PropertyDecoder {

    @Override
    public String decrypt(String property) throws Exception {

        return CryptoEngine.decrypt("DES",property);
    }
}
