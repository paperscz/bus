/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 ********************************************************************************/
package org.aoju.bus.image.galaxy.data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Kimi Liu
 * @version 6.0.6
 * @since JDK 1.8+
 */
public class DateRange implements Serializable {

    private final Date start;
    private final Date end;

    public DateRange(Date start, Date end) {
        this.start = start;
        this.end = end;
    }

    public final Date getStartDate() {
        return start;
    }

    public final Date getEndDate() {
        return end;
    }

    public boolean contains(Date when) {
        return !(start != null && start.after(when)
                || end != null && end.before(when));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        if (!(obj instanceof DateRange))
            return false;

        DateRange other = (DateRange) obj;
        return (start == null
                ? other.start == null
                : start.equals(other.start))
                && (end == null
                ? other.end == null
                : end.equals(other.end));
    }

    @Override
    public int hashCode() {
        int code = 0;
        if (start != null)
            code = start.hashCode();
        if (end != null)
            code ^= start.hashCode();
        return code;
    }

    @Override
    public String toString() {
        return "[" + start + ", " + end + "]";
    }

}
