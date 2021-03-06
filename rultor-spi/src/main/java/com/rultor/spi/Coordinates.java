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
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.Tv;
import com.jcabi.urn.URN;
import com.rultor.tools.Time;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.Validate;

/**
 * Coordinates to do.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@SuppressWarnings("PMD.TooManyMethods")
public interface Coordinates extends Comparable<Coordinates> {

    /**
     * When scheduled, in milliseconds.
     * @return Milliseconds
     */
    @NotNull(message = "time is never NULL")
    Time scheduled();

    /**
     * Owner of this work.
     * @return The owner
     */
    @NotNull(message = "URN of owner is never NULL")
    URN owner();

    /**
     * Name of the work (unique for the user).
     * @return The rule
     */
    @NotNull(message = "rule name is never NULL")
    String rule();

    /**
     * No work at all.
     */
    @Immutable
    @ToString
    @EqualsAndHashCode
    final class None implements Coordinates {
        /**
         * {@inheritDoc}
         */
        @NotNull
        @Override
        public Time scheduled() {
            throw new UnsupportedOperationException();
        }
        /**
         * {@inheritDoc}
         */
        @NotNull
        @Override
        public URN owner() {
            throw new UnsupportedOperationException();
        }
        /**
         * {@inheritDoc}
         */
        @NotNull
        @Override
        public String rule() {
            throw new UnsupportedOperationException();
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public int compareTo(final Coordinates coords) {
            return 1;
        }
    }

    /**
     * Simple implementation.
     */
    @Loggable(Loggable.DEBUG)
    @EqualsAndHashCode(of = { "time", "urn", "label" })
    @Immutable
    final class Simple implements Coordinates {
        /**
         * When scheduled.
         */
        private final transient Time time;
        /**
         * Owner of it.
         */
        private final transient URN urn;
        /**
         * Name of it.
         */
        private final transient String label;
        /**
         * Public ctor.
         */
        public Simple() {
            this(URN.create("urn:facebook:1"), "test-rule");
        }
        /**
         * Public ctor.
         * @param coords Other coordinates
         */
        public Simple(final Coordinates coords) {
            this(coords.owner(), coords.rule(), coords.scheduled());
        }
        /**
         * Public ctor.
         * @param owner Owner
         * @param name Name
         */
        public Simple(final URN owner, final String name) {
            this(owner, name, new Time());
        }
        /**
         * Public ctor.
         * @param owner Owner
         * @param name Name
         * @param when When it should start
         */
        public Simple(@NotNull(message = "owner can't be NULL") final URN owner,
            @NotNull(message = "rule name can't be NULL")
            @Pattern(
                message = "invalid name of rule",
                regexp = "[a-z0-9\\-]+"
            )
            final String name,
            @NotNull(message = "time can't be NULL") final Time when) {
            this.urn = owner;
            this.label = name;
            this.time = when;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return String.format(
                "%s %s %s",
                this.time,
                this.label,
                this.urn
            );
        }
        /**
         * Create from a string.
         * @param text Text form
         * @return Coordinates found in text
         */
        public static Coordinates valueOf(final String text) {
            final String[] parts = text.split(" ");
            Validate.isTrue(
                parts.length == Tv.THREE,
                "invalid coordinates `%s`", text
            );
            return new Coordinates.Simple(
                URN.create(parts[2]), parts[1], new Time(parts[0])
            );
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public int compareTo(final Coordinates coords) {
            return this.time.compareTo(coords.scheduled());
        }
        /**
         * {@inheritDoc}
         */
        @Override
        @NotNull(message = "time of work is never NULL")
        public Time scheduled() {
            return this.time;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        @NotNull(message = "URN of owner of work is never NULL")
        public URN owner() {
            return this.urn;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        @NotNull(message = "rule name of work is never NULL")
        public String rule() {
            return this.label;
        }
    }

}
