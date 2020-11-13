/**
 * Copyright (c) 2019 SUSE LLC
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
package com.suse.manager.webui.controllers.contentmanagement.response; import static org.junit.jupiter.api.Assertions.*;


/**
 * JSON response wrapper for the history of a content project.
 */
public class ProjectHistoryEntryResponse {
    private String message;
    private Long version;

    public void setMessage(String messageIn) {
        this.message = messageIn;
    }

    public void setVersion(Long versionIn) {
        this.version = versionIn;
    }
}
