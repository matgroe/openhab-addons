/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.giraone.internal.communication.webservice;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class offers functionality for handling the authentication for using the
 * Gira One Webservice interface.
 *
 * @author Matthias Gröger - Initial contribution
 */
@NonNullByDefault
class GiraOneWebserviceAuthentication {
    private static final Logger logger = LoggerFactory.getLogger(GiraOneWebserviceAuthentication.class);
    private final MessageDigest digest;

    GiraOneWebserviceAuthentication() {
        try {
            this.digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    String saltAndHashPassword(GiraOneWebserviceSession session, final String password) {
        if ("GDS_1".equals(session.getVersion())) {
            return createHashSaltedPasswordGDS1(session, password);
        }
        throw new IllegalArgumentException("Unsupported version: " + session.getVersion());
    }

    /**
     * Adopted from javascript code as provided by GiraOne Server
     *
     * <pre>
     * authMethodGDS1(e,t){
     *    const r=_e.sha256.create();
     *    r.update(_e.util.encodeUtf8(e)+t.salt);
     *    const n=r.digest().getBytes();
     *    return _e.util.encode64(n).substring(0,43)
     *  }
     * </pre>
     *
     * @param session
     * @param password
     * @return
     */

    private String createHashSaltedPasswordGDS1(GiraOneWebserviceSession session, final String password) {
        logger.trace("Salting given password with {}", session.getSalt());
        String text = password + session.getSalt();
        byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash).substring(0, 43);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes)
            result.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        return result.toString();
    }

    /**
     * computes the authentication token for webservice command doAuthenticateSession
     * 
     * @param session
     * @param password
     * @return
     */
    String computeAuthToken(GiraOneWebserviceSession session, String password) {
        String saltedPasswd = saltAndHashPassword(session, password);
        logger.trace("Salting salted and hashed password with {}", session.getSessionSalt());
        String text = String.format("%s+%s", saltedPasswd, session.getSessionSalt());
        byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(hash).toUpperCase();
    }
}
