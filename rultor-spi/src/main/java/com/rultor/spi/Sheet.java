/**
 * Copyright (c) 2009-2013, rultor.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the rultor.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.rultor.spi;

import com.jcabi.aspects.Immutable;
import com.rultor.tools.Time;
import java.util.List;
import javax.validation.constraints.NotNull;

/**
 * Sheet of transactions.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
public interface Sheet extends Pageable<List<Object>, Integer> {

    /**
     * Column names/titles.
     * @return Titles
     */
    @NotNull(message = "list of titles is never NULL")
    List<Column> columns();

    /**
     * Order by.
     * @param column Column to order by
     * @param asc Ascending order
     * @return New sheet
     */
    @NotNull(message = "new sheet is never NULL")
    Sheet orderBy(String column, boolean asc);

    /**
     * Group by.
     * @param column Column to group by
     * @return New sheet
     */
    @NotNull(message = "new sheet is never NULL")
    Sheet groupBy(String column);

    /**
     * Between these dates.
     * @param left Left time
     * @param right Right time
     * @return New sheet
     */
    @NotNull(message = "new sheet is never NULL")
    Sheet between(Time left, Time right);

    /**
     * Construct condition and return.
     * @return Condition
     */
    @NotNull(message = "condition can never be NULL")
    Sheet.Condition where();

    /**
     * Condition.
     */
    interface Condition {
        /**
         * Back to sheet.
         * @return Sheet
         */
        Sheet sheet();
        /**
         * Equals to.
         * @param column Column name
         * @param value Value
         * @return Condition
         */
        Sheet.Condition equalTo(String column, String value);
    }

}
