package es.uvigo.ei.sing.jarvest.web.dao;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

public class HibernateUtils {
	/**
	 * Get the singleton hibernate Session Factory.
	 */
	@SuppressWarnings("deprecation")
	public static SessionFactory getSessionFactory() {
		return org.zkoss.zkplus.hibernate.HibernateUtil.getSessionFactory();
	}
	
	/**
	 * Wrapping HibernateUtil.getSessionFactory().getCurrentSession() into a simple API.
	 */
	@SuppressWarnings("deprecation")
	public static Session currentSession() throws HibernateException {
		return org.zkoss.zkplus.hibernate.HibernateUtil.getSessionFactory().getCurrentSession();
	}
	
	/**
	 * Wrapping HibernateUtil.getSessionFactory().getCurrentSession().close() into a simple API.
	 */
	@SuppressWarnings("deprecation")
	public static void closeSession() throws HibernateException {
		org.zkoss.zkplus.hibernate.HibernateUtil.currentSession().close();
	}
}
