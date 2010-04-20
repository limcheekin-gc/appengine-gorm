package com.vobject.appengine.gorm

import com.google.appengine.api.datastore.DatastoreService
import com.google.appengine.api.datastore.DatastoreServiceFactory
import org.springframework.beans.BeanWrapperImpl
import com.google.appengine.api.datastore.Entity;
import java.beans.PropertyDescriptor
import java.lang.annotation.Annotation 
import javax.persistence.Transient
import com.google.appengine.api.datastore.KeyFactory
import com.google.appengine.api.datastore.Key
import org.springframework.dao.OptimisticLockingFailureException

class DataStoreService {

    boolean transactional = false
    DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService()
    static final String ENTITY_VERSION_FIELD = "VERSION"
    static final String BEAN_VERSION_FIELD = "version"

    List batchSave(Collection collection) {
        checkVersion(collection)
	// println "batchSave. getCurrentTransaction() = ${datastoreService.getCurrentTransaction(null)}"
	List keys = datastoreService.put(datastoreService.getCurrentTransaction(null),
	                                 convertBeansToEntities(collection))
	return keys
    }

    private void checkVersion(Collection collection) {
        List beansWithId = collection.findAll {it.id && it.metaClass.hasProperty(it, BEAN_VERSION_FIELD) && it.version}
	if (beansWithId) { 
	    println "checkVersion. beansWithId = ${beansWithId}"
	    List keys = getKeys(beansWithId)
	    Map entitiesMap = datastoreService.get(keys)
	    Entity entity
            beansWithId.each { bean ->
                entity = entitiesMap.get(getKey(bean))
                if (entity.getProperty(ENTITY_VERSION_FIELD) > bean.version) { 
                    throw new OptimisticLockingFailureException("Another user has updated this ${bean.class.simpleName} of id ${bean.id} while you were editing.")
                } 
	        entity = null
	    }
	}
    }


    void batchDelete(Collection collection) {
        // println "batchDelete. getCurrentTransaction() = ${datastoreService.getCurrentTransaction(null)}"
	datastoreService.delete(datastoreService.getCurrentTransaction(null), 
	                        getKeysFromBeans(collection))
    }
    
    private Collection convertBeansToEntities(Collection collection) {
	List list = new ArrayList(collection)
	for (int i = 0; i < list.size(); i ++)
	    list.set(i, convertBeanToEntity(list.get(i)))
        // println "convertBeansToEntities. convertedEntities: \n${list}"
        return list
    }

    private Key[] getKeysFromBeans(Collection collection) {
	Key[] keys = new Key[collection.size()]
	collection.eachWithIndex { bean, i ->
            keys[i] = getKey(bean)
	}
	return keys
    }

    private Key getKey(Object bean) {
        Key key
	if (bean.id instanceof Key) {
	    key = bean.id
	} else if (bean.id instanceof String) {
	    key = KeyFactory.stringToKey(bean.id) 
	} else if (bean.id instanceof Long){
	    key = KeyFactory.createKey(bean.class.simpleName, bean.id)
	} else {
	    throw new IllegalArgumentException("Invalid type of id!\nThe id must be type of com.google.appengine.api.datastore.Key or String or Long instead of ${bean.id.class}.")
	}
	return key
    }

    private Object getId(Class propertyType, Key entityKey) {
        
	if (propertyType == Key.class) {
	    return entityKey
	} else if (propertyType == String.class) {
	    return KeyFactory.keyToString(entityKey) 
	} else if (propertyType == Long.class){
	    return new Long(entityKey.id)
	} else {
	    throw new IllegalArgumentException("Invalid type of id!\nThe id must be type of com.google.appengine.api.datastore.Key or String or Long instead of ${propertyType}.")
	}
    }

    private List getKeys(Collection beans) {
        List keys = new ArrayList(beans.size())
	beans.each { bean ->
	    keys << getKey(bean)
	}
	return keys
    }

    private Entity convertBeanToEntity(Object bean) {
	Entity entity
	if (!bean.id) {
	    entity = new Entity(bean.getClass().getSimpleName()) 
	} else {
	    entity = new Entity(getKey(bean))
	}
	BeanWrapperImpl beanWrapper = new BeanWrapperImpl(bean)
	PropertyDescriptor[] propertyDescriptors = beanWrapper.getPropertyDescriptors() 
	PropertyDescriptor propertyDescriptor
	for (int i = 0; i < propertyDescriptors.length; i++) {
	    if (propertyDescriptors[i].getName().indexOf("class") == -1 && 
	        propertyDescriptors[i].getName().indexOf("metaClass") == -1 && 
		!propertyDescriptors[i].getName().equals("id")) {
		boolean isTransientField = false
	        Annotation[] annotations = bean.getClass().getDeclaredField(propertyDescriptors[i].getName()).getDeclaredAnnotations()	
	          for (int j = 0; j < annotations.length; j++) {
		      // println "annotations[${j}].annotationType() = " + annotations[j].annotationType()
		      if (annotations[j].annotationType() == javax.persistence.Transient) {
		          isTransientField = true
			  break;
		      }	
                  }
                if (!isTransientField) {
                    // println ("setProperty: " + propertyDescriptors[i].getName() + " = " + beanWrapper.getPropertyValue(propertyDescriptors[i].getName()))
		    if (!propertyDescriptors[i].getName().equals(BEAN_VERSION_FIELD)) {
		        entity.setProperty(propertyDescriptors[i].getName(), beanWrapper.getPropertyValue(propertyDescriptors[i].getName()))
                    } else {
		        Long version = beanWrapper.getPropertyValue(BEAN_VERSION_FIELD)
		        entity.setProperty(ENTITY_VERSION_FIELD, version? version + 1: 1)
		    }
		}
	    }   
	} 
	return entity

    }

    private Object convertEntityToBean(Entity entity, Class beanClass) {
	BeanWrapperImpl beanWrapper = new BeanWrapperImpl(beanClass)
        beanWrapper.setPropertyValue("id", getId(beanWrapper.getPropertyType("id"), entity.getKey()))
	entity.getProperties().keySet().each { key ->
	    if (!key.equals(ENTITY_VERSION_FIELD)) {
	        beanWrapper.setPropertyValue(key, entity.getProperty(key))
            } else { 
	        beanWrapper.setPropertyValue(BEAN_VERSION_FIELD, entity.getProperty(ENTITY_VERSION_FIELD))
	    }	
	}
	return beanWrapper.getRootInstance() 
    }

    private Collection convertEntitiesToBeans(Collection collection, Class beanClass) {
	List list = new ArrayList(collection)
	for (int i = 0; i < list.size(); i ++)
	    list.set(i, convertEntityToBean(list.get(i), beanClass))
        // println "convertEntitiesToBeans. convertedBeans: \n${list}"
        return list
    }
}
