import groovy.sql.Sql

@Grapes([
        @Grab(group='com.h2database', module='h2', version='1.4.181'),
        @Grab(group='mysql', module='mysql-connector-java', version='5.1.32'),
        @GrabConfig(systemClassLoader = true)
])

mysql = Sql.newInstance('jdbc:mysql://localhost/floh', 'root', 'root', 'com.mysql.jdbc.Driver')

h2 = Sql.newInstance('jdbc:h2:./floh', 'sa', '', 'org.h2.Driver')

standard_columns = [id: 'int primary key', created: 'datetime', modified: 'datetime']
events_columns = standard_columns + [name: 'varchar(100)', date: 'datetime']
categories_columns = standard_columns + [name: 'varchar(50)']
reservations_columns = standard_columns + [event_id: 'int', seller_id: 'int', number: 'int']
sellers_columns = standard_columns + [first_name: 'varchar(50)', last_name: 'varchar(50)',
                                      street: 'varchar(100)',
                                      zip_code: 'varchar(5)', city: 'varchar(50)',
                                      email: 'varchar(128)', phone: 'varchar(50)']
reserved_items_columns = standard_columns + [reservation_id: 'int', item_id: 'int', number: 'int', code: 'varchar(12)']
items_columns = standard_columns + [seller_id: 'int', category_id: 'int', description: 'varchar(50)', size: 'varchar(10)', price: 'decimal(5,1)']


def drop(table) { h2.execute("drop table if exists " + table) }
def create(table, columns) { h2.execute('create table ' + table + '(' + columns.collect{k,v -> "${k} ${v}"}.join(',') + ')') }
def insert(table, columns, values) { values.each { h2.execute('insert into ' + table + ' values(' + ('?,' * (columns.size()-1)) + '?)', it.values().asList()) } }
def result(table) { println "" + h2.firstRow("select count(*) from " + table).values() + " row(s) inserted into $table table." }
def copy (table, columns, db, closure) {
    drop(table)
    create(table, columns)
    result = db.rows(closure(table, columns))
    insert(table, columns, result)
    result(table)
}

copy("events", events_columns, mysql) { t,c -> 'select ' + c.keySet().join(',') + ' from ' + t + ' where current=true' }
event_id = h2.firstRow('select id from events').id
copy("categories", categories_columns, mysql) { t,c -> 'select ' + c.keySet().join(',') + ' from ' + t }
copy("reservations", reservations_columns, mysql) { t,c -> 'select ' + c.keySet().join(',') + ' from ' + t + ' where event_id=' + event_id }
copy("sellers", sellers_columns, mysql) {
    t,c -> 'select ' + c.keySet().collect{"x.$it"}.join(',') +
            ' from ' + t + ' x join reservations r on r.seller_id=x.id and r.event_id=' + event_id }
copy("reserved_items", reserved_items_columns, mysql) {
    t,c -> 'select ' + c.keySet().collect{"x.$it"}.join(',') +
            ' from ' + t + ' x join reservations r on r.id=x.reservation_id and r.event_id=' + event_id }
copy("items", items_columns, mysql) {
    t,c -> 'select ' + c.keySet().collect{"x.$it"}.join(',') +
            ' from ' + t + ' x join reserved_items ri on ri.item_id=x.id join reservations r on r.id=ri.reservation_id and r.event_id=' + event_id }
