'orauld' : unload/export Oracle SQL result to CSV files
-------------------------------------------------------

#### Prerequisites

- Oracle jdbc driver ojdbc14.jar
- For branch v-0.1: jdk 1.6 + & apache Ant
- For branch v-0.2: jdk 1.8 + & Gradle (shadow fat-jar)

#### Build

```
$ git clone https://github.com/peihanw/orauld.git
$ cd orauld/
$ mkdir lib
$ cp /some/where/ojdbc14.jar ./lib/

# for branch v-0.1 (Ant manually build)
$ git checkout v-0.1
$ ant -f ant.xml

# for branch v-0.2 (Gradle build)
$ git checkout v-0.2
$ gradle build
```

#### Usage syntax

```
Usage: -l conn_info -q query_sql -o bcp_fnm [-d delimiter] [-D eor_str] [-c charset] [-w wrk_num] [-s split_lines] [-v verbosity] [-h] [-t]
Usage: -L login_str -q query_sql -o bcp_fnm [-d delimiter] [-D eor_str] [-c charset] [-w wrk_num] [-s split_lines] [-v verbosity] [-h] [-t]
Usage: -F login_cfg -q query_sql -o bcp_fnm [-d delimiter] [-D eor_str] [-c charset] [-w wrk_num] [-s split_lines] [-v verbosity] [-h] [-t]
eg   :        -l usr@sid:127.0.0.1:1521 -q "select * from table_name" -o uld.bcp
eg   : -L usr/passwd@sid:127.0.0.1:1521 -q "select * from table_name" -o uld.bcp
eg   : -F $HOME/etc/mytest_db_login.cfg -q "select * from table_name" -o uld.bcp
     : -l/-L : defalt host is 127.0.0.1, default port is 1521
     : -l : an interactive prompt will ask for password
     : -L : password is provided via command line args directly, bad guys may peek by list processes
     : -F : login_str is stored in the config file
     : -o : bcp_fnm --> bulk copy output file name
     : -d : default field delimiter is pipe char '|'
     : -D : default record delimiter is %n, should be used for dealing with embeded CR/LF
     : -c : default using JVM default encoding, support GB18030/UTF-8 etc.
     : -w : default 2, worker thread number, should between 1 and 4
     : -s : default 0 (no split), open a new bcp file every split_lines, bcp files will be sequential numbered with '_%09d'
     : -v : default 3, 0:ERO, 1:WRN, 2:INF, 3:DBG, 4:TRC
     : -h : default no header line
     : -t : default no trim for CHAR type
Usage: -l/L/F login -q query_sql -O ctl_fnm [-d delimiter] [-D eor_str] [-c charset] [-v verbosity] [-h]
eg   : -L usr/passwd@sid:dbhost -q "select x,y,z from some_view" -D #EOR# -O table_name.ctl
     : -O : ctl_fnm --> sqlldr control file name, file_name without .ctl is the table_name
```

#### Usage examples

##### simple dump (most common usage)

```
## for branch v-0.1
$ java -cp orauld.jar:../lib/ojdbc14.jar \
## for branch v-0.2 (Gradle shadow fat-jar)
$ java -cp orauld.jar \
> github.peihanw.orauld.OrauldMain \
> -L scott/tiger@orcl:192.168.200.88 \
> -q "select * from rqst_log where yyyymm=201601" \
> -o rqst_log_201601.bcp -v 1
```

##### generate a control file according to the table schema

```
## for branch v-0.1
$ java -cp orauld.jar:../lib/ojdbc14.jar \
## for branch v-0.2 (Gradle shadow fat-jar)
$ java -cp orauld.jar \
> github.peihanw.orauld.OrauldMain \
> -L scott/tiger@orcl:192.168.200.88 \
> -q "select * from rqst_log" \
> -O rqst_log.ctl
```

#### Memo

- Please download ojdbc14.jar yourself.
- Oracle jdbc drivers 'ojdbc6.jar & orai18n.jar' are not recommended. Test results show that 'ojdbc6.jar' may cause performance dropping down about 30-50%.
- Test environment is Oracle 11g (11.2.0.4.0).

#### TODO

- Replace JDK LinkedBlockingQueue with com.conversantmedia:disruptor to impove performance. (implemented in branch v-0.2)
- Support DATE/TIMESTAMP WITH TIMEZONE.

