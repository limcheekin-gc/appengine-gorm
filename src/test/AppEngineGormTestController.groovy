import com.google.appengine.api.datastore.KeyFactory
import com.vobject.appengine.gorm.*

class AppEngineGormTestController {

    private final static String PASS = '<span style="font-weight:bold; color:green">PASS</span>'
    private final static String FAIL = '<span style="font-weight:bold; color:red">FAIL</span>'

    def clearData = {    
         def list = new ArrayList(User.list()) 
	 list.addAll(Role.list())
	 list.addAll(Permission.list())
	 list.batchDelete()
    }
    
    private void shouldBeEquals(String message, Object expectedValue, Object actualValue) {
        boolean isEquals = expectedValue.equals(actualValue)

	render "<p>${message}...: " 
	render isEquals? PASS: "${FAIL} (Expected value is ${expectedValue}, but actual value is ${actualValue}.)"
	render "</p>"
    }

    private void setUp() {
        clearData()
    }

    private void tearDown() {
        clearData()
    }


    def index = {
      testNewBatchInsert()
      testAppendBatchInsert()
      testBatchDelete()	
      testBatchUpdate()
      testBatchUpdateShouldThrowException()
      renderKnownIssuesStatement()
      testBatchInsertWithTransactionShouldThrowException()
      testBatchInsertWithTransactionCommit()
      testBatchInsertWithTransactionRollback()
    }

    def renderKnownIssuesStatement () {
      render """<p>
      <span style="font-weight:bold">KNOWN ISSUE: </span>
      <span style="font-weight:bold; color:red">Test results below indicated that Transaction is not supported.</span>
      </p>""" 
    }

    def testNewBatchInsert = {
        setUp()
        def users = [new User(email:"anonymous@anonymous.com", name:"Anonymous"),
	             new User(email:"admin@vobject.com", password:"admin", name:"Administrator"),
	             new User(email:"user1@vobject.com", password:"user1", name:"User 1")]
	users.batchSave()
        shouldBeEquals("testNewBatchInsert", users.size(), User.count())
	tearDown()
    }

    def testAppendBatchInsert = {
        setUp()
        [new User(email:"anonymous@anonymous.com", name:"Anonymous"),
	 new User(email:"admin@vobject.com", password:"admin", name:"Administrator"),
	 new User(email:"user1@vobject.com", password:"user1", name:"User 1")].batchSave()
        def users = new ArrayList(User.list()) // to prevent java.lang.UnsupportedOperationException: Query result sets are not modifiable
        users <<  new User(email:"user2@vobject.com", password:"user2", name:"User 2")
	users <<  new User(email:"user3@vobject.com", password:"user3", name:"User 3")
        users.batchSave()
	shouldBeEquals("testAppendBatchInsert", users.size(), User.count())
	tearDown()
    }

    def testBatchDelete = {
        setUp()
        [new User(email:"anonymous@anonymous.com", name:"Anonymous"),
	 new User(email:"admin@vobject.com", password:"admin", name:"Administrator"),
	 new User(email:"user1@vobject.com", password:"user1", name:"User 1")].batchSave()
        User.list().batchDelete()
        shouldBeEquals("testBatchDelete", 0, User.count())
	tearDown()
    }

    def testBatchUpdate = {
        setUp() 
        [new User(email:"anonymous@anonymous.com", name:"Anonymous"),
	 new User(email:"admin@vobject.com", password:"admin", name:"Administrator"),
	 new User(email:"user1@vobject.com", password:"user1", name:"User 1")].batchSave()
	def users = User.list() 
	// render "testBatchUpdate. Get users after first insert: <br />"
	// renderUsers(users)
	users[2].name = "Modified User 1"
	def keys = users.batchSave()
	shouldBeEquals ("testBatchUpdate - Number of keys return should be 3", 3, keys.size())
	// render "testBatchUpdate. Get users after batch update: <br />"
	printUsers (User.list()) // BUG? After execute this statement only the "version should equals to 2" test case will PASS 
	def user1 = User.get(users[2].id)
        shouldBeEquals ("testBatchUpdate - user name updated", users[2].name, user1.name)
	shouldBeEquals ("testBatchUpdate - version should be equals to 2", new Long(2), user1.version)
	tearDown()
    }

    def testBatchUpdateShouldThrowException = {
        setUp()
	def users = [new User(email:"anonymous@anonymous.com", name:"Anonymous"),
	 new User(email:"admin@vobject.com", password:"admin", name:"Administrator"),
	 new User(email:"user1@vobject.com", password:"user1", name:"User 1")]
	 users.each { user ->
	   user.version = 2   
	 }
	users.batchSave()
	users.clear()
	users = User.list()
	// render("testBatchUpdateShouldThrowException. Get users after first insert: <br />")
	// renderUsers(users)
	users[2].name = "Modified User 1"
        users[2].version = users[2].version - 1
	try {
	    users.batchSave()
	    throw new RuntimeException("This should throw an OptimisticLockingFailureException")
	} catch (Exception e) {
	    println "EXCEPTION: ${e.getMessage()}"
	    String expectedValue = "Another user has updated"
	    shouldBeEquals ("testBatchUpdateShouldThrowException", expectedValue, e.getMessage().substring(0, expectedValue.length()))
	}
	tearDown()
    }

    def testBatchInsertWithTransactionShouldThrowException = {
        setUp()   
        def role = new Role(name:"Test User")
	try {
	    Role.withTransaction {
	        if (role.save(flush: true)) {
	            println "testBatchInsertWithTransactionCommit. role.id = ${role.id}"
	          [new Permission(roleId:role.id, controller:"user", action:"show"),
	           new Permission(roleId:role.id, controller:"user", action:"list"),
	           new Permission(roleId:role.id, controller:"user", action:"index"),
	           new Permission(roleId:role.id, controller:"role", action:"show"),
	           new Permission(roleId:role.id, controller:"role", action:"list"),
	           new Permission(roleId:role.id, controller:"role", action:"index")].batchSave()
	        }
	    }
	    throw new RuntimeException("This should throw an IllegalArgumentException")
	} catch (Exception e) {
	    println "EXCEPTION: ${e.getMessage()}"
	    String expectedValue = "can't operate on multiple entity groups in a single transaction."
	    shouldBeEquals ("testBatchInsertWithTransactionShouldThrowException", expectedValue, e.getMessage().substring(0, expectedValue.length()))
	}
	tearDown()
    }

    def testBatchInsertWithTransactionCommit = {
        setUp()   
	def roles = new ArrayList(5)
        Role.withTransaction {
  	    for (int i = 0; i < 5; i++) {
	        roles << new Role(name: "Role ${i}")
	    }
	    roles.batchSave()
	}
        shouldBeEquals ("testBatchInsertWithTransactionCommit - number of Role should be ${roles.size()}", 
	                roles.size(), Role.count())
	tearDown()
    }

    def testBatchInsertWithTransactionRollback = {
        setUp()   
	def roles = new ArrayList(5)
        Role.withTransaction { status ->
  	    for (int i = 0; i < 5; i++) {
	        roles << new Role(name: "Role ${i}")
	    }
	    roles.batchSave()
	    status.setRollbackOnly()
	}
        shouldBeEquals ("testBatchInsertWithTransactionRollback - number of Role should be 0", 
	                0, Role.count())
	tearDown()
    }

    private void renderUsers(List users) {
        users.each { bean ->
	    render "${bean.id?KeyFactory.stringToKey(bean.id):"NULL"}, ${bean}<br />" 
	}
    }

    private void printUsers(List users) {
        users.each { bean ->
	    println "${bean.id?KeyFactory.stringToKey(bean.id):"NULL"}, ${bean}" 
	}
    }
}