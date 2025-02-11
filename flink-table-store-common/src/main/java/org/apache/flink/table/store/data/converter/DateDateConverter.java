/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.table.store.data.converter;

import org.apache.flink.annotation.Internal;
import org.apache.flink.table.store.utils.DateTimeUtils;
import org.apache.flink.table.types.logical.DateType;

/** Converter for {@link DateType} of {@link java.sql.Date} external type. */
@Internal
public class DateDateConverter implements DataStructureConverter<Integer, java.sql.Date> {

    private static final long serialVersionUID = 1L;

    @Override
    public Integer toInternal(java.sql.Date external) {
        return DateTimeUtils.toInternal(external);
    }

    @Override
    public java.sql.Date toExternal(Integer internal) {
        return DateTimeUtils.toSQLDate(internal);
    }
}
