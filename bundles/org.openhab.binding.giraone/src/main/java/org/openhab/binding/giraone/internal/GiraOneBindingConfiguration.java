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
package org.openhab.binding.giraone.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link GiraOneBindingConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Matthias Groeger - Initial contribution
 */
@NonNullByDefault
public class GiraOneBindingConfiguration {

    public String hostname = "";
    public String username = "";
    public String password = "";

    public int refreshInterval = 60;
    public int defaultTimeoutSeconds = 10;
    public int maxTextMessageSize = 102400; //
}
