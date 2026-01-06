### Prerequisites
JDK >= 8
MySQL
Redis

### Preparation
See configurations in ***application.properties***.<br/>

Ignore lines related to aliyun OSS since it's not used. <br/>

Set up a mysql database ***test*** and a user ***test***, grant read/write privileges to ***test***.
Or just run: <br/>
`mysql -uroot test < sql/setup.sql`

Create tables: <br/>
`mysql -uroot test < sql/tables.sql`

Create initial data: <br/>
`mysql -uroot < sql/initial_data.sql`

### Run
./gradlew run