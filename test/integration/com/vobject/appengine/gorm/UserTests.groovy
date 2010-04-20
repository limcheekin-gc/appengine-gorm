package com.vobject.appengine.gorm

import com.vobject.appengine.*
import com.google.appengine.api.datastore.DatastoreServiceFactory
import com.google.appengine.api.datastore.DatastoreService
import com.google.appengine.api.datastore.Query
import com.google.appengine.api.datastore.PreparedQuery
import com.google.appengine.api.datastore.Key
import static com.google.appengine.api.datastore.FetchOptions.Builder.*

class UserTests extends LocalDatastoreTestCase {
    def transactional = false
    def dataStoreService

    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testBatchInsert() {
        def users = [new User(email:"anonymous@anonymous.com", name:"Anonymous"),
	 new User(email:"admin@vobject.com", password:"admin", name:"Administrator"),
	 new User(email:"user1@vobject.com", password:"user1", name:"User 1")]
	def keys = users.batchSave()
        Query query = new Query(User.class.getSimpleName())
	DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService()
        assertEquals(keys.size(), datastoreService.prepare(query).countEntities())
	def results = datastoreService.prepare(query).asIterator()
	if (results?.iterator().hasNext()) {
            assertEquals(1, results.iterator().next().getProperty(DataStoreService.ENTITY_VERSION_FIELD))	    
	}
    }

    void testBatchDelete() {
	def keys = [new User(email:"anonymous@anonymous.com", name:"Anonymous"),
	 new User(email:"admin@vobject.com", password:"admin", name:"Administrator"),
	 new User(email:"user1@vobject.com", password:"user1", name:"User 1")].batchSave()
        Query query = new Query(User.class.getSimpleName());
	DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
	PreparedQuery preparedQuery = datastoreService.prepare(query)
	assertEquals(keys.size(), preparedQuery.countEntities());
        def users = dataStoreService.convertEntitiesToBeans(preparedQuery.asList(withLimit(10)), User.class)
	users.eachWithIndex { bean, i ->
	   bean.id = keys[i]
	   println bean
	}
	users?.batchDelete()
	assertEquals(0, preparedQuery.countEntities());
    }

    void testGetKeyShouldFail() {
        try {
            dataStoreService.getKey(new DoubleIdClass(id: new Double(1.0)))
	    fail ("This should throw an exception")
	} catch (Exception e) {
	    println "e.getMessage() = " + e.getMessage()
	    assertTrue (e.getMessage().indexOf("Invalid type of id!") > -1)
	}
    }

    void testGetKeyShouldPass() {
        assertTrue(dataStoreService.getKey(new LongIdClass(id: 1000)) instanceof Key)
    }

    void testAsteriskOperator() {
            def list = [new DoubleIdClass(id: new Double(1.0)), 
	     new DoubleIdClass(),
	     new DoubleIdClass(id: new Double(1.0)),
	     new DoubleIdClass(id: new Double(1.0))]
	    assertEquals (3, list*.id.findAll{it != null}.size())
    }

    void testBatchInsertAndUpdateShouldPass() {
        def users = [new User(email:"anonymous@anonymous.com", name:"Anonymous"),
	 new User(email:"admin@vobject.com", password:"admin", name:"Administrator"),
	 new User(email:"user1@vobject.com", password:"user1", name:"User 1")]
	def keys = users.batchSave()
	keys.eachWithIndex { key, i ->
	    users[i].id = key
	}
	users[2].name = "Modified User 1"
        users <<  new User(email:"user2@vobject.com", password:"user2", name:"User 2")
	users <<  new User(email:"user3@vobject.com", password:"user3", name:"User 3")
	println users
	println "keys of users: " + users.batchSave()
        Query query = new Query(User.class.getSimpleName());
	DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService()
        assertEquals(users.size(), datastoreService.prepare(query).countEntities())
	assertEquals(users[2].name, datastoreService.get(users[2].id).getProperty("name"))
    }

    void testBatchUpdateShouldThrowException() {
        [new User(email:"anonymous@anonymous.com", name:"Anonymous", version: 1),
	 new User(email:"admin@vobject.com", password:"admin", name:"Administrator", version: 1),
	 new User(email:"user1@vobject.com", password:"user1", name:"User 1", version: 1)].batchSave()
        Query query = new Query(User.class.getSimpleName());
	DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();
	PreparedQuery preparedQuery = datastoreService.prepare(query)
        def users = dataStoreService.convertEntitiesToBeans(preparedQuery.asList(withLimit(10)), User.class)	
        users[2].name = "Modified User 1"
        users[2].version = users[2].version - 1
	println users
	try {
	    users.batchSave()
	    fail ("This should throw an OptimisticLockingFailureException")
	} catch (Exception e) {
	    println "e.getMessage() = " + e.getMessage()
	    assertTrue (e.getMessage().indexOf("Another user has updated") > -1)
	}
    }
}

    

    class DoubleIdClass {
         Double id
    }

    class LongIdClass {
         Long id
    }

    class StringIdClass {
         String id
    }
