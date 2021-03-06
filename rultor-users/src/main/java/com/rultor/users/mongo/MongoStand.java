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
package com.rultor.users.mongo;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.urn.URN;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;
import com.rexsl.test.SimpleXml;
import com.rexsl.test.XmlDocument;
import com.rultor.snapshot.Snapshot;
import com.rultor.spi.Coordinates;
import com.rultor.spi.Pageable;
import com.rultor.spi.Pulse;
import com.rultor.spi.Spec;
import com.rultor.spi.Stand;
import com.rultor.tools.Time;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.logging.Level;
import javax.xml.transform.dom.DOMSource;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.w3c.dom.Document;
import org.xembly.ImpossibleModificationException;
import org.xembly.XemblySyntaxException;

/**
 * Stand in Mongo.
 *
 * <pre>
 * pulses {
 *   pulse: String,
 *   stand: String,
 *   updated: Time,
 *   xembly: String,
 *   tags: String[]
 * };
 * </pre>
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 * @checkstyle ClassFanOutComplexity (500 lines)
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "mongo", "origin" })
@Loggable(Loggable.DEBUG)
@SuppressWarnings({ "PMD.TooManyMethods", "PMD.ExcessiveImports" })
final class MongoStand implements Stand {

    /**
     * MongoDB table name.
     */
    public static final String TABLE = "stands";

    /**
     * MongoDB table column.
     */
    public static final String ATTR_STAND = "stand";

    /**
     * MongoDB table column.
     */
    public static final String ATTR_COORDS = "coordinates";

    /**
     * MongoDB table column.
     */
    public static final String ATTR_UPDATED = "updated";

    /**
     * MongoDB table column.
     */
    public static final String ATTR_XEMBLY = "xembly";

    /**
     * MongoDB table column.
     */
    public static final String ATTR_TAGS = "tags";

    /**
     * Mongo container.
     */
    private final transient Mongo mongo;

    /**
     * Original stand.
     */
    private final transient Stand origin;

    /**
     * Public ctor.
     * @param mng Mongo container
     * @param stand Original
     */
    protected MongoStand(final Mongo mng, final Stand stand) {
        this.mongo = mng;
        this.origin = stand;
    }

    /**
     * {@inheritDoc}
     * @checkstyle RedundantThrows (5 lines)
     */
    @Override
    public void post(final Coordinates pulse, final long nano,
        final String xembly) {
        while (true) {
            if (this.save(pulse, nano, xembly)) {
                break;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Pageable<Pulse, Coordinates> pulses() {
        return new MongoPulses(this.mongo, this.origin);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String name() {
        return this.origin.name();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URN owner() {
        return this.origin.owner();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void acl(final Spec spec) {
        this.origin.acl(spec);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Spec acl() {
        return this.origin.acl();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Spec widgets() {
        return this.origin.widgets();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void widgets(final Spec spec) {
        this.origin.widgets(spec);
    }

    /**
     * Attempt to save.
     * @param pulse The pulse name
     * @param nano Nano ID
     * @param xembly Xembly script to append
     * @return TRUE if success
     */
    @SuppressWarnings("unchecked")
    private boolean save(final Coordinates pulse, final long nano,
        final String xembly) {
        final DBObject object = this.collection().findAndModify(
            new BasicDBObject()
                .append(MongoStand.ATTR_STAND, this.name())
                .append(
                    MongoStand.ATTR_COORDS,
                    new MongoCoords(pulse).asObject()
                ),
            new BasicDBObject()
                .append(MongoStand.ATTR_COORDS, 1)
                .append(MongoStand.ATTR_STAND, 1)
                .append(MongoStand.ATTR_TAGS, 1)
                .append(MongoStand.ATTR_XEMBLY, 1),
            new BasicDBObject(),
            false,
            new BasicDBObject().append(
                "$setOnInsert",
                new BasicDBObject().append(MongoStand.ATTR_XEMBLY, "")
            ),
            true,
            true
        );
        final String after = new StringBuilder()
            .append(object.get(MongoStand.ATTR_XEMBLY))
            .append(nano).append(' ')
            .append(xembly).append('\n').toString();
        final WriteResult result = this.collection().update(
            object,
            new BasicDBObject()
                .append(MongoStand.ATTR_STAND, this.name())
                .append(MongoStand.ATTR_UPDATED, new Time().toString())
                .append(MongoStand.ATTR_XEMBLY, after)
                .append(
                    MongoStand.ATTR_COORDS,
                    new MongoCoords(pulse).asObject()
                )
                .append(
                    MongoStand.ATTR_TAGS,
                    this.tags(MongoStand.decode(after))
                )
        );
        Validate.isTrue(
            result.getLastError().ok(),
            "failed to update pulse `%s`: %s",
            pulse, result.getLastError().getErrorMessage()
        );
        return result.getN() == 1;
    }

    /**
     * Xembly to DOM.
     *
     * <p>All exceptions are swallowed here since we can't be sure
     * that at this moment Xembly script is fully complete. It may contain
     * broken parts, which will be completed later. That's why we're
     * returning our best guess here.
     *
     * @param xembly Xembly script
     * @return DOM document
     * @throws Stand.BrokenXemblyException If fails
     * @checkstyle RedundantThrows (5 lines)
     */
    private Document dom(final String xembly)
        throws Stand.BrokenXemblyException {
        try {
            return new Snapshot(xembly).dom();
        } catch (XemblySyntaxException ex) {
            throw new BrokenXemblyException(ex);
        } catch (ImpossibleModificationException ex) {
            throw new BrokenXemblyException(ex);
        }
    }

    /**
     * Fetch all visible tags.
     * @param after Xembly after changes
     * @return Array of tags
     */
    private Collection<DBObject> tags(final String after) {
        final Collection<DBObject> tags = new HashSet<DBObject>(0);
        final List<XmlDocument> nodes = new LinkedList<XmlDocument>();
        try {
            nodes.addAll(
                new SimpleXml(new DOMSource(this.dom(after))).nodes(
                    "/snapshot/tags/tag[label and level]"
                )
            );
        } catch (BrokenXemblyException ex) {
            assert ex != null;
        }
        for (XmlDocument node : nodes) {
            tags.add(this.tag(node).asObject());
        }
        return tags;
    }

    /**
     * Decode the text into clean xembly.
     * @param script Script with prefixes
     * @return Clean xembly
     */
    public static String decode(final String script) {
        final ConcurrentMap<Long, String> lines =
            new ConcurrentSkipListMap<Long, String>();
        for (String line : script.split("\n+")) {
            final String[] parts = line.split(" ", 2);
            lines.put(Long.parseLong(parts[0]), parts[1]);
        }
        return StringUtils.join(lines.values(), "\n");
    }

    /**
     * Make Mongo Tag from XML node.
     * @param node XML node
     * @return Mongo tag
     */
    private MongoTag tag(final XmlDocument node) {
        final String data;
        if (node.nodes("data").isEmpty()) {
            data = "{}";
        } else {
            data = node.xpath("data/text()").get(0);
        }
        final String markdown;
        if (node.nodes("markdown").isEmpty()) {
            markdown = "";
        } else {
            markdown = node.xpath("markdown/text()").get(0);
        }
        return new MongoTag(
            node.xpath("label/text()").get(0),
            Level.parse(node.xpath("level/text()").get(0)),
            data, markdown
        );
    }

    /**
     * Collection.
     * @return Mongo collection
     */
    private DBCollection collection() {
        try {
            return this.mongo.get().getCollection(MongoStand.TABLE);
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

}
