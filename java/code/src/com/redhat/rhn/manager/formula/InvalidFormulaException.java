/**
 * Copyright (c) 2018 SUSE LLC
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
package com.redhat.rhn.manager.formula; import static org.junit.jupiter.api.Assertions.*;

/**
 * Exception to represent when content of formula mismatch with definition
 */
public class InvalidFormulaException extends Exception  {

    /**
     * Message constructor.
     * @param message the message
     */
    public InvalidFormulaException(String message) {
        super(message);
    }
}
