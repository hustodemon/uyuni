/**
 * Copyright (c) 2009--2017 Red Hat, Inc.
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.redhat.rhn.common.finder.test;

import java.util.List;

import com.redhat.rhn.common.finder.Finder;
import com.redhat.rhn.common.finder.FinderFactory;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

public class JarFinderTest extends Assert {

    // NOTE: Test is dependent on knowing things like "How many classes are in jarfile X"
    // When "X" changes, the test FAILS.
    // Sigh.
    // At least make it clear what we're looking for...

    // Currently used jarfile: postgresql-jdbc-42.2.10.jar
    // (previously redstone.xmlrpc could find either redstone-xmlrpc.jar or
    //  redstone-xmlrpc-client.jar, making test-results indeterminate)
    private static final String TESTJAR = "org.postgresql";
    private static final int NUM_CLASSES_IN_TESTJAR = 343;
    private static final int NUM_SUBDIRS_IN_TESTJAR = 343;

    @Test
    public void testGetFinder() throws Exception {
        Finder f = FinderFactory.getFinder(TESTJAR);
        assertNotNull(f);
    }

    @Test
    public void testFindFiles() throws Exception {
        Finder f = FinderFactory.getFinder(TESTJAR);
        assertNotNull(f);

        List<String> result = f.find(".class");
        assertEquals(NUM_CLASSES_IN_TESTJAR, result.size());
    }

    @Test
    public void testFindFilesSubDir() throws Exception {
        Finder f = FinderFactory.getFinder(TESTJAR);
        assertNotNull(f);

        List<String> result = f.find("");
        assertEquals(NUM_SUBDIRS_IN_TESTJAR, result.size());
    }

    @Test
    public void testFindFilesExcluding() throws Exception {
        Finder f = FinderFactory.getFinder(TESTJAR);
        assertNotNull(f);

        String[] sarr = {"End"};

        List<String> result = f.findExcluding(sarr, "class");
        assertEquals(NUM_CLASSES_IN_TESTJAR, result.size());
    }
}


