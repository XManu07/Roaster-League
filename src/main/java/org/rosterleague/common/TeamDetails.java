/*
 * Copyright (c), Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v1.0, which is available at
 * https://www.eclipse.org/org/documents/edl-v10.php
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package org.rosterleague.common;

import java.io.Serial;
import java.io.Serializable;


public class TeamDetails implements Serializable {
    @Serial
    private static final long serialVersionUID = -1618941013515364318L;
    private final String id;
    private final String name;
    private final String city;
    private final String color;

    public TeamDetails(String id, String name, String city, String color) {
        this.id = id;
        this.name = name;
        this.city = city;
        this.color = color;
    }


    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCity() {
        return city;
    }

    public String getColor() {
        return color;
    }

    @Override
    public String toString() {
        return id + " " + name + " " + city + " " + color;
    }

}
