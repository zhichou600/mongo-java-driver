/*
 * Copyright (c) 2008-2014 MongoDB, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mongodb.operation;

import org.mongodb.Document;

public class RemoveRequest extends WriteRequest {
    private final Document filter;
    private boolean isMulti = true;

    public RemoveRequest(final Document filter) {
        super();
        this.filter = filter;
    }

    public Document getFilter() {
        return filter;
    }

    //CHECKSTYLE:OFF
    public RemoveRequest multi(final boolean isMulti) {
        this.isMulti = isMulti;
        return this;
    }
    //CHECKSTYLE:ON

    public boolean isMulti() {
        return isMulti;
    }

    @Override
    public Type getType() {
        return Type.REMOVE;
    }
}