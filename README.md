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
$ ant -f ant.xml
```

#### Usage syntax

```
Usage: -l conn_info -q query_sql -o bcp_fnm [-d delimiter] [-c charset] [-w wrk_num] [-s split_lines] [-v verbosity] [-t]
Usage: -L login_str -q query_sql -o bcp_fnm [-d delimiter] [-c charset] [-w wrk_num] [-s split_lines] [-v verbosity] [-t]
Usage: -F login_cfg -q query_sql -o bcp_fnm [-d delimiter] [-c charset] [-w wrk_num] [-s split_lines] [-v verbosity] [-t]
eg   :        -l usr@sid:127.0.0.1:1521 -q "select * from table_name" -o uld.bcp
eg   : -L usr/passwd@sid:127.0.0.1:1521 -q "select * from table_name" -o uld.bcp
eg   : -F $HOME/etc/mytest_db_login.cfg -q "select * from table_name" -o uld.bcp
     : -l/-L : defalt host is 127.0.0.1, default port is 1521
     : -l : an interactive prompt will ask for password
     : -L : password is provided via command line args directly, bad guys may peek by list processes
     : -F : login_str is stored in the config file
     : -o : bcp_fnm --> bulk copy output file name
     : -d : default delimiter is pipe char '|'
     : -c : default using JVM default encoding, support GB18030/UTF-8 etc.
     : -w : default 2, worker thread number, should between 1 and 4
     : -s : default 0 (no split), open a new bcp file every split_lines, bcp files will be sequential numbered with '_%09d'
     : -v : default 3, 0:ERO, 1:WRN, 2:INF, 3:DBG, 4:TRC
     : -t : default no trim for CHAR type
Usage: -l/L/F login -q query_sql -O ctl_fnm [-d delimiter] [-c charset] [-v verbosity]
eg   : -L usr/passwd@sid:dbhost -q "select x,y,z from some_view" -O table_name.ctl
     : -O : ctl_fnm --> sqlldr control file name, file_name without .ctl is the table_name
```

#### Usage examples

```
$ java -cp orauld.jar:../lib/ojdbc14.jar github.peihanw.orauld.OrauldMain \
> -L scott/tiger@orcl:192.168.200.88 \
> -q "select * from rqst_log where yyyymm=201601" -o rqst_log_201601.bcp -v 1
```

#### Memo

- Develop environment is NetBeans 8.x.
- Please download ojdbc14.jar yourself.
- Oracle jdbc driver 'ojdbc6.jar & orai18n.jar' are not supported.
- Test environment is Oracle 11g (11.2.0.4.0).

