'orauld' : unload/export Oracle SQL result to CSV files
-------------------------------------------------------

#### Prerequisites

- jdk 1.6 +
- Oracle jdbc driver ojdbc14.jar
- apache Ant for build from source

#### Build

```
$ git clone https://github.com/peihanw/orauld.git
$ cd orauld/
$ mkdir lib
$ cp /some/where/ojdbc14.jar ./lib/
$ ant jar
```

#### Usage syntax

```
$ java -cp orauld.jar:../lib/ojdbc14.jar github.peihanw.orauld.OrauldMain
Usage: -l conn_info -q query_sql -o output_fnm [-d delimiter] [-c charset] [-w wrk_num] [-v verbose] [-t]
Usage: -L login_str -q query_sql -o output_fnm [-d delimiter] [-c charset] [-w wrk_num] [-v verbose] [-t]
eg   :        -l usr@sid:127.0.0.1:1521 -q "select * from table_name" -o uld.bcp
eg   : -L usr/passwd@sid:127.0.0.1:1521 -q "select * from table_name" -o uld.bcp
     : -l/-L : defalt host is 127.0.0.1, default port is 1521
     : -l : an interactive prompt will ask for password
     : -L : password is provided via command line args directly, bad guys may peek by list processes
     : -d : default delimiter is pipe char '|'
     : -c : default using JVM default encoding, support GB18030/UTF-8 etc.
     : -w : default 2, worker thread number, should between 1 and 4
     : -v : default 3, 0:ERO, 1:WRN, 2:INF, 3:DBG, 4:TRC
     : -t : default no trim for CHAR type
```

#### Usage examples

```
$ java -cp orauld.jar:../lib/ojdbc14.jar github.peihanw.orauld.OrauldMain \
> -l scott@orcl:192.168.200.88 \
> -q "select * from rqst_log where yyyymm=201601" -o rqst_log_201601.bcp -v 1
Please input password
```

#### Memo

- Develop environment is NetBeans 8.x.
- Please download ojdbc14.jar yourself.
- Oracle jdbc driver 'ojdbc6.jar & orai18n.jar' are not supported.
- Test environment is Oracle 11g (11.2.0.4.0).

