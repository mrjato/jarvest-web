package es.uvigo.ei.sing.jarvest.web.dao;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;

import org.hibernate.Session;

@SuppressWarnings("unchecked")
public abstract class HibernateDAO<Key extends Serializable, Entity> {
	private Class<Entity> entityClass;
	private Session session;
	
	public HibernateDAO() {
		this(null);
	}
	
	public HibernateDAO(Session session) {
		this.entityClass = (Class<Entity>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[1];
		this.setSession(session);
	}
	
	public void setSession(Session sesion) {
		this.session = sesion;
	}
	
	protected Session getSession() throws DAOException {
		if (this.session != null && this.session.isOpen()) {
			return this.session;
		} else {
			throw new DAOException("Null or closed session");
		}
	}
	
	public void create(Entity entity) throws DAOException {
		this.getSession().persist(entity);
	}
	
	public void delete(Entity entity) throws DAOException {
		this.getSession().delete(entity);
	}
	
	public void delete(Key id) throws DAOException {
		final Entity entity = this.get(id);
		
		if (entity != null) {
			this.getSession().delete(entity);
		}
	}
	
	public void update(Entity entity) throws DAOException {
		this.getSession().update(entity);
	}
	
	public Entity merge(Entity entity) throws DAOException {
		return (Entity) this.getSession().merge(entity);
	}
	
	public Entity get(Key id) {
		return (Entity) this.getSession().get(this.entityClass, id);
	}
}
