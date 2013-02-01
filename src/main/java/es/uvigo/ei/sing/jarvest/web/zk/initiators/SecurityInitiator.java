package es.uvigo.ei.sing.jarvest.web.zk.initiators;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.util.Initiator;

import es.uvigo.ei.sing.jarvest.web.zk.vm.UserViewModel;

public class SecurityInitiator implements Initiator {
	private static final String INDEX_PAGE = "/index.zul";
	private static final String LOGOUT_PAGE = "/logout.zul";
	private final static Set<String> IGNORE_PAGES = new HashSet<String>();
	
	static {
		IGNORE_PAGES.add(INDEX_PAGE);
		IGNORE_PAGES.add("/loginConfirmation.zul");
		IGNORE_PAGES.add("/passwordRecovery.zul");
	}
	
	@Override
	public void doInit(Page page, Map<String, Object> args) throws Exception {
		final String requestPath = page.getRequestPath();
		
		if (requestPath.equals(LOGOUT_PAGE)) {
			final Session session = Sessions.getCurrent(false);
			if (session != null) session.invalidate();
			
			Executions.sendRedirect(INDEX_PAGE);
		} else 	if (!IGNORE_PAGES.contains(requestPath)) {
			final Session session = Sessions.getCurrent(false);
			if (session == null || !session.hasAttribute(UserViewModel.USER_SESSION_KEY)) {
				Executions.sendRedirect(INDEX_PAGE);
			}
		}
	}
}
