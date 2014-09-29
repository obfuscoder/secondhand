import groovy.sql.Sql

@Grapes([
        @Grab(group='com.h2database', module='h2', version='1.4.181'),
        @Grab(group='mysql', module='mysql-connector-java', version='5.1.32'),
        @GrabConfig(systemClassLoader = true)
])

mysql = Sql.newInstance('jdbc:mysql://localhost/floh', 'root', 'root', 'com.mysql.jdbc.Driver')
h2 = Sql.newInstance('jdbc:h2:./floh', 'sa', '', 'org.h2.Driver')

failed = []
h2.eachRow('select id,sold,code from reserved_items where sold is not null') {
    result = mysql.executeUpdate("update reserved_items set sold=$it.sold where id=$it.id") == 1
    print result ? "." : "F"
    if (!result) failed << it.code
}
println "Done"

println "Failed:"
println failed
