package com.rudoy.utils;

import org.hashids.Hashids;

public class CryptoTool {
    private final Hashids hashids;

    public CryptoTool(String salt) {
        int minHashLength = 10;
        this.hashids = new Hashids(salt, minHashLength);
    }

    public String hashOf(Long value) {
        return hashids.encode(value);
    }

    public Long idOf(String value) {
        long[] decodeRes = hashids.decode(value);
        if (decodeRes != null && decodeRes.length > 0) {
            return decodeRes[0];
        }
        return null;
    }
}
