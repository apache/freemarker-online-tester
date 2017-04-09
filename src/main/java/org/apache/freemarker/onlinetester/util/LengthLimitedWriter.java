/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.freemarker.onlinetester.util;

import java.io.FilterWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

/**
 * A {@link StringWriter} that limits its buffer size, and throws {@link LengthLimitExceededException} when that's
 * exceeded.
 */
public class LengthLimitedWriter extends FilterWriter {
    
    private int lengthLeft;

    public LengthLimitedWriter(Writer writer, int lengthLimit) {
        super(writer);
        this.lengthLeft = lengthLimit;
    }

    @Override
    public void write(int c) throws IOException {
        if (lengthLeft < 1) {
            throw new LengthLimitExceededException();
        }
        
        super.write(c);
        
        lengthLeft--;
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        final boolean lengthExceeded;
        if (lengthLeft < len) {
            len = lengthLeft;
            lengthExceeded = true;
        } else {
            lengthExceeded = false;
        }
        
        super.write(cbuf, off, len);
        lengthLeft -= len;
        
        if (lengthExceeded) {
            throw new LengthLimitExceededException();
        }
    }

    @Override
    public void write(String str, int off, int len) throws IOException {
        final boolean lengthExceeded;
        if (lengthLeft < len) {
            len = lengthLeft;
            lengthExceeded = true;
        } else {
            lengthExceeded = false;
        }
        
        super.write(str, off, len);
        lengthLeft -= len;
        
        if (lengthExceeded) {
            throw new LengthLimitExceededException();
        }
    }

}
