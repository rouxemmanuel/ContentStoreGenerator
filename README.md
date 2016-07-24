# ContentStoreGenerator

## About
This tools allows to generate a ContentStore structure from an existing database.

This tools is useful when you have to analyse bugs for a customer's Alfresco environment but your customer can't give you his datas.

## Compatibiity
This tools is compatible for :
- Alfresco 3.0 to 5.1
- Alfresco Enterprise or Community
- PostgresSQL, MySQL, Oracle or SqlServer database


## Usage
```
Usage: java -jar ContentStoreGenerator.jar <operation> [-f <properties_filename> ]
     [-d <db_type>]           [-h <db_hostname>]       [-port <db_port>]
     [-s <db_schema>]         [-u <db_user>]           [-p <db_pass>]
     [-dbd <db_driver>]
     [-a <alfdata_directory>] [-c <contentstore>]      [-dd <dummydata_dir>]
     [-m <mimetype_map_file>] [-df <default_filename>] [-debug]

  You can either specify a standard Alfresco repository.properties file with the -f switch, or you can specify all the standard Alfresco individual properties separately.
  Those properties which are not standard Alfresco ones, can't be specified in the properties file.

  All properties are optional.
  Values specified on the command line will over-ride values in the properties file.

 <operation>                - Whether to check the existing ContentStore, or fill a new one - default: check
-f <properties_filename>    - the properties file which holds the relevant information

-d <db_type>                - currently only mysql, oracle, sqlserver and postgresql are supported, default:postgresql
-h <db_hostname>            - Where the DB is, default:localhost
-port <db_port>
-s <db_schema>              - default:alfresco
-u <db_user>                - default:alfresco
-p <db_pass>                - default:alfresco
-dbd <db_driver>            - database driver
                              org.gjt.mm.mysql.Driver for mysql
                              oracle.jdbc.OracleDriver for oracle
                              org.postgresql.Driver for postgresql (default)
                              com.microsoft.sqlserver.jdbc.SQLServerDriver for sqlserver

-a <alf_data_directory>     - Directory underneath which the contentstore directory will be The does not have to exist yet, default: ./alf_data
-c <contentstore dir>       - default: contentstore

-dd <dummydata_dir>         - Directory which currently holds the sample files, default:./data
-m <mimetype_map_file>      - properties file holding mimetype to filename mapping, default:<dummydata_dir>/mimemap.txt
-df <default_filename>      - file in <dummydata_dir to be used for unrecognised mimetypes, default:a.txt

-debug                      - turn on debug logging
```
