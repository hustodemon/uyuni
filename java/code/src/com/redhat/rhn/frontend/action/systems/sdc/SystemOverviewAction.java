/**
 * Copyright (c) 2009--2015 Red Hat, Inc.
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
package com.redhat.rhn.frontend.action.systems.sdc; import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.entitlement.Entitlement;
import com.redhat.rhn.domain.server.MinionServer;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserServerPreferenceId;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnAction;
import com.redhat.rhn.frontend.struts.RhnHelper;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.rhnpackage.PackageManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.manager.user.UserManager;

/**
 * SystemOverviewAction
 * @version $Rev$
 */
public class SystemOverviewAction extends RhnAction {

    public static final String[] SERVER_PREFERENCES = {UserServerPreferenceId
                                                       .INCLUDE_IN_DAILY_SUMMARY,
                                                       UserServerPreferenceId
                                                       .RECEIVE_NOTIFICATIONS};

    /** {@inheritDoc} */
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response) {
        RequestContext rctx = new RequestContext(request);
        Long sid = rctx.getRequiredParam("sid");
        User user = rctx.getCurrentUser();
        Server s  = SystemManager.lookupByIdAndUser(sid, user);

        /* Here we htmlify the description stored in the database such that end line's
         * are represented correctly
         */
        String description = null;

        if (s.getDescription() != null) {
            description = StringEscapeUtils.escapeHtml4(s.getDescription())
                .replaceAll("\\n", "<br/>");
        }

        // System Channels
        Map baseChannel = new HashMap();
        List childChannels = new ArrayList();
        DataResult channelList = SystemManager.systemChannelSubscriptions(sid);

        for (Iterator i = channelList.iterator(); i.hasNext();) {
            Map ch = (HashMap) i.next();

            if (s.getBaseChannel() != null &&
                ch.get("id").equals(s.getBaseChannel().getId())) {
                baseChannel.put("id", ch.get("id"));
                baseChannel.put("name", ch.get("name"));
            }
            else {
                childChannels.add(ch);
            }
        }

        // Errata Counts
        int criticalErrataCount = SystemManager.countCriticalErrataForSystem(user, sid);
        int nonCriticalErrataCount =
            SystemManager.countNoncriticalErrataForSystem(user, sid);

        // Upgradable Packages
        int upgradablePackagesCount = PackageManager.countUpgradable(sid);

        boolean hasUpdates =
            criticalErrataCount + nonCriticalErrataCount + upgradablePackagesCount > 0;

        // Reboot needed after certain types of updates
        boolean rebootRequired = SystemManager.requiresReboot(user, sid);

        if (!processLock(user, s, rctx)) {
            request.setAttribute("serverLock", s.getLock());
        }

        processPing(user, s, rctx);
        proccessSatApplet(user, s, rctx);

        SdcHelper.ssmCheck(request, sid, user);

        //Order entitlements
        List<Entitlement> entitlements = s.getEntitlements().stream().sorted(
                (e1, e2) -> (e1.isBase() && e2.isBase()) || (!e1.isBase() && !e2.isBase()) ?
                        e1.getHumanReadableLabel().compareTo(e2.getHumanReadableLabel()) :
                        (e1.isBase() ? -1 : 1))
                .collect(Collectors.toList());

        if (s.getChannels().stream().anyMatch(Channel::isModular)) {
            createErrorMessage(request, "packagelist.jsp.modulespresent", null);
        }

        request.setAttribute("rebootRequired", rebootRequired);
        request.setAttribute("unentitled", s.getEntitlements().isEmpty());
        request.setAttribute("entitlements", entitlements);
        request.setAttribute("systemInactive", s.isInactive());
        request.setAttribute("criticalErrataCount", criticalErrataCount);
        request.setAttribute("nonCriticalErrataCount", nonCriticalErrataCount);
        request.setAttribute("upgradablePackagesCount", upgradablePackagesCount);
        request.setAttribute("hasUpdates", hasUpdates);
        request.setAttribute("baseChannel", baseChannel);
        request.setAttribute("childChannels", childChannels);
        request.setAttribute("maintenanceSchedule", s.getMaintenanceScheduleOpt().orElse(null));
        request.setAttribute("description", description);
        request.setAttribute("installedProducts", s.getInstalledProductSet().orElse(null));
        request.setAttribute("prefs", findUserServerPreferences(user, s));
        request.setAttribute("system", s);
        request.setAttribute("minionId", s.getMinionId());
        request.setAttribute("hasLocation",
                !(s.getLocation() == null || s.getLocation().isEmpty()));
        request.setAttribute("activationKey", SystemManager.getActivationKeys(s));
        request.setAttribute("kernelLiveVersion",
                s.asMinionServer().map(MinionServer::getKernelLiveVersion).orElse(null));

        return mapping.findForward(RhnHelper.DEFAULT_FORWARD);
    }

    protected List findUserServerPreferences(User user, Server s) {
        List serverPreferenceList = new ArrayList();

        if (user.getEmailNotify() == 0) {
            serverPreferenceList.add("sdc.details.overview.notifications.disabled");

            return serverPreferenceList;
        }

        for (int j = 0; j < SERVER_PREFERENCES.length; ++j) {
            if (UserManager.lookupUserServerPreferenceValue(user,
                                                            s,
                                                            SERVER_PREFERENCES[j])) {
                serverPreferenceList.add(SERVER_PREFERENCES[j]);
            }
        }

        return serverPreferenceList;
    }

    /**
     * @param user The user
     * @param s The server
     * @param rctx The request context
     * @return true if the server was unlocked, false otherwise
     */
    protected boolean processLock(User user, Server s, RequestContext rctx) {
        Long lockValue = rctx.getParamAsLong("lock");
        LocalizationService ls = LocalizationService.getInstance();

        if (lockValue != null) {

            if (lockValue.longValue() == 1) {
                if (s.getLock() == null) {
                    SystemManager.lockServer(user,
                                             s,
                                             ls.getMessage
                                             ("sdc.details.overview.lock.reason"));

                    createSuccessMessage(rctx.getRequest(),
                                         "sdc.details.overview.locked.alert",
                                         s.getName());
                }
            }
            else if (lockValue.longValue() == 0) {
                if (s.getLock() != null) {

                    SystemManager.unlockServer(user, s);
                    createSuccessMessage(rctx.getRequest(),
                            "sdc.details.overview.unlocked.alert",
                            s.getName());

                    return true;
                }
            }
        }

        return false;
    }

    protected void processPing(User user, Server s, RequestContext rctx) {
        Long pingValue = rctx.getParamAsLong("ping");

        if (pingValue != null && pingValue.longValue() == 1) {
            s.getPushClient().setLastPingTime(new Date(System.currentTimeMillis()));
            s.getPushClient().setNextActionTime(null);
            SystemManager.storeServer(s);
            createSuccessMessage(rctx.getRequest(),
                    "sdc.details.overview.osa.status.pinged",
                    s.getName());
        }

    }

    protected void proccessSatApplet(User user, Server s, RequestContext rctx) {
        Long appValue = rctx.getParamAsLong("applet");

        if (appValue != null && appValue.longValue() == 1) {
            Action a = ActionManager.createBaseAction(user,
                                                      s,
                                                      ActionFactory
                                                      .TYPE_RHN_APPLET_USE_SATELLITE);
            ActionManager.storeAction(a);
            createSuccessMessage(rctx.getRequest(),
                                 "sdc.details.overview.applet.scheduled",
                                 "/rhn/systems/details/history/Event.do?sid=" +
                                 s.getId() +
                                 "&aid=" +
                                 a.getId());
        }
    }
}
