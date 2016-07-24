package org.alfresco.tool.contentStoreGenerator;


import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;


public class ContentTool {
	
	private static String operation = "check";
    private static String alfPropertiesFileName = "";
    private static String dbType = "postgresql";
    private static String dbUser = "alfresco";
    private static String dbPass = "alfresco";
    private static String dbHost = "localhost";
    private static String dbPort = null;
    private static String dbSchema = "alfresco";
    private static String dbUrl = "";
    private static String dbDriver = "org.postgresql.Driver";
    private static String DEBUG = "PART";
    private static String alfData = "./alf_data";
    private static String contentStoreName = "contentstore";
    private static String dummyDir;
    private static String mimeMapFile;
    private static String defaultFilename = "";
    private static Properties mimeMap = null;
    private static Properties alfProps = null;
    private static final String defaultDefaultFilename = "a.txt";
    private static final String sql1 = "SELECT substring(string_value, 19) FROM alf_node_properties WHERE string_value like 'contentUrl=store:%'";
    private static final String sql2 = "SELECT substr(concat(cu.content_url ,'|mimetype=', m.mimetype_str,'|size=',cu.content_size),8) FROM alf_content_data cd, alf_mimetype m, alf_content_url cu WHERE m.id = cd.content_mimetype_id and cd.content_url_id = cu.id";

    static 
    {
        dummyDir = "./data";
        mimeMapFile = (new StringBuilder(String.valueOf(dummyDir))).append("/mimemap.txt").toString();
    }

    public ContentTool() {
    }

    public static void main(String args[]) {
        checkArgs(args);
        System.out.println("Arguments checked OK");
        printArgs();
        Connection con = getConnection();
        System.out.println("Connection created");
        ArrayList<String> contentUrls = getContentUrlsFromDb(con);
        System.out.println((new StringBuilder(String.valueOf(contentUrls.size()))).append(" Urls retrieved").toString());
        if(operation.equals("check")) {
            ContentStoreChecker.doWork(contentUrls, mimeMap, alfData, contentStoreName, dummyDir, defaultFilename, DEBUG);
        } else if(operation.equals("fill")) {
            ContentStoreFiller.doWork(contentUrls, mimeMap, alfData, contentStoreName, dummyDir, defaultFilename, DEBUG);
        }
    }

    private static void checkArgs(String args[]) {
        if(args.length < 1) {
            usage();
        }
        try {
            for(int i = 0; i < args.length; i++) {
                if(!args[i].toLowerCase().equals("-f")) {
                    continue;
                }
                i++;
                alfPropertiesFileName = args[i];
                readAlfProps(alfPropertiesFileName);
                break;
            }

            for(int i = 0; i < args.length; i++) {
                if(args[i].toLowerCase().equals("check")) {
                    operation = "check";
                } else if(args[i].toLowerCase().equals("fill")) {
                    operation = "fill";
                } else {
                    if(args[i].toLowerCase().equals("-f")) {
                        i++;
                    }
                    if(args[i].toLowerCase().equals("-d")) {
                        i++;
                        dbType = args[i].toLowerCase();
                        if(!dbType.equals("mysql") && !dbType.equals("oracle") && !dbType.equals("postgresql") && !dbType.equals("sqlserver")) {
                            System.out.println("Only mysql, oracle, sqlserver and postgresql are supported at the moment.");
                            usage();
                        }
                    } else if(args[i].toLowerCase().equals("-h")) {
                        i++;
                        dbHost = args[i].toLowerCase();
                    } else if(args[i].toLowerCase().equals("-port")) {
                        i++;
                        dbPort = args[i].toLowerCase();
                    } else if(args[i].toLowerCase().equals("-s")) {
                        i++;
                        dbSchema = args[i].toLowerCase();
                    } else if(args[i].toLowerCase().equals("-u")) {
                        i++;
                        dbUser = args[i].toLowerCase();
                    } else if(args[i].toLowerCase().equals("-p")) {
                        i++;
                        dbPass = args[i].toLowerCase();
                    } else if(args[i].toLowerCase().equals("-dbd")) {
                        i++;
                        dbDriver = args[i];
                    } else if(args[i].toLowerCase().equals("-a")) {
                        i++;
                        alfData = args[i];
                    } else if(args[i].toLowerCase().equals("-c")) {
                        i++;
                        contentStoreName = args[i];
                    } else if(args[i].toLowerCase().equals("-dd")) {
                        i++;
                        dummyDir = args[i];
                    } else if(args[i].toLowerCase().equals("-m")) {
                        i++;
                        mimeMapFile = args[i];
                    } else if(args[i].toLowerCase().equals("-df")) {
                        i++;
                        defaultFilename = args[i];
                    } else if(args[i].toLowerCase().equals("-debug"))
                        DEBUG = "FULL";
                }
            }
        }
        catch(Exception e) {
            usage();
        }
        mimeMap = fillHashMap(mimeMapFile);
        if(defaultFilename.equals("")) {
            defaultFilename = mimeMap.getProperty("default");
        }
        if(defaultFilename.equals("")) {
            defaultFilename = defaultDefaultFilename;
        }
    }

    private static void readAlfProps(String alfPropertiesFileName) {
        alfProps = new Properties();
        try {
            alfProps.load(new FileInputStream(alfPropertiesFileName));
        } catch(IOException e) {
            System.out.println("Unable to read Alf properties file.");
            e.getMessage();
            System.exit(0);
        } if(alfProps.containsKey("db.url")) {
            dbUrl = alfProps.getProperty("db.url");
            dbType = dbUrl.toLowerCase().split(":")[1];
            if(dbType.equals("mysql")) {
                dbHost = dbUrl.toLowerCase().split("/")[2];
                if (dbHost.indexOf(":") != -1) {
                	dbHost = dbHost.toLowerCase().split(":")[0];
                	dbPort = dbHost.toLowerCase().split(":")[1];
                }
                if (dbPort == null) {
                	dbPort = "3306";
                }
                dbSchema = dbUrl.toLowerCase().split("/")[3];
            } else if(dbType.equals("oracle")) {
                dbHost = dbUrl.toLowerCase().split(":")[3];
                dbPort = dbUrl.toLowerCase().split(":")[4];
                dbSchema = dbUrl.toLowerCase().split(":")[5];
            } else if(dbType.equals("postgresql")) {
            	dbHost = dbUrl.toLowerCase().split("/")[2];
                if (dbHost.indexOf(":") != -1) {
                	dbHost = dbHost.toLowerCase().split(":")[0];
                	dbPort = dbHost.toLowerCase().split(":")[1];
                }
                if (dbPort == null) {
                	dbPort = "5432";
                }
            	dbSchema = dbUrl.toLowerCase().split("/")[3];
            } else if(dbType.equals("sqlserver")) {
            	dbHost = dbUrl.toLowerCase().split("/")[2];
                if (dbHost.indexOf(":") != -1) {
                	dbHost = dbHost.toLowerCase().split(":")[0];
                	dbPort = dbHost.toLowerCase().split(":")[1];
                }
                if (dbPort == null) {
                	dbPort = "1433";
                }
            	dbSchema = dbUrl.toLowerCase().split("/")[3];
            }  else {
                System.out.println("Only mysql, oracle, sqlserver and postgresql are supported at the moment.");
                System.exit(0);
            }
        }
        dbUser = alfProps.getProperty("db.username", dbUser);
        dbPass = alfProps.getProperty("db.password", dbPass);
        dbHost = alfProps.getProperty("db.host", dbHost);
        dbPort = alfProps.getProperty("db.port", dbPort);
        dbDriver = alfProps.getProperty("db.driver", dbDriver);
        alfData = alfProps.getProperty("dir.root", alfData);
    }

    private static void usage() {
        System.out.println("Usage: java -jar ContentStoreGenerator.jar <operation> [-f <properties_filename> ] ");
        System.out.println("     [-d <db_type>]           [-h <db_hostname>]       [-port <db_port>] ");
        System.out.println("     [-s <db_schema>]         [-u <db_user>]           [-p <db_pass>] ");
        System.out.println("     [-dbd <db_driver>]         ");
        System.out.println("     [-a <alfdata_directory>] [-c <contentstore>]      [-dd <dummydata_dir>] ");
        System.out.println("     [-m <mimetype_map_file>] [-df <default_filename>] [-debug] ");
        System.out.println("");
        System.out.println("  You can either specify a standard Alfresco repository.properties file with the -f switch,");
        System.out.println("  or you can specify all the standard Alfresco individual properties separately. ");
        System.out.println("  Those properties which are not standard Alfresco ones, can't be specified in the properties file.");
        System.out.println("");
        System.out.println("  All properties are optional.");
        System.out.println("  Values specified on the command line will over-ride values in the properties file.");
        System.out.println("");
        System.out.println(" <operation>                - Whether to check the existing ContentStore, or fill a new one - default: check ");
        System.out.println("-f <properties_filename>    - the properties file which holds the relevant information ");
        System.out.println("");
        System.out.println("-d <db_type>                - currently only mysql, oracle, sqlserver and postgresql are supported   - default:postgresql");
        System.out.println("-h <db_hostname>            - Where the DB is                     - default:localhost");
        System.out.println("-port <db_port>             ");
        System.out.println("-s <db_schema>              - default:alfresco");
        System.out.println("-u <db_user>                - default:alfresco");
        System.out.println("-p <db_pass>                - default:alfresco");
        System.out.println("-dbd <db_driver>            - database driver");
        System.out.println("                              org.gjt.mm.mysql.Driver for mysql");
        System.out.println("                              oracle.jdbc.OracleDriver for oracle");
        System.out.println("                              org.postgresql.Driver for postgresql (default)");
        System.out.println("                              com.microsoft.sqlserver.jdbc.SQLServerDriver for sqlserver");
        System.out.println("");
        System.out.println("-a <alf_data_directory>     - Directory underneath which the contentstore directory will be");
        System.out.println("                               The does not have to exist yet.");
        System.out.println("                              default: ./alf_data");
        System.out.println("-c <contentstore dir>       - default: contentstore");
        System.out.println("");
        System.out.println("-dd <dummydata_dir>         - Directory which currently holds the sample files");
        System.out.println("                              default:./data");
        System.out.println("-m <mimetype_map_file>      - properties file holding mimetype to filename mapping");
        System.out.println("                              default:<dummydata_dir>/mimemap.txt");
        System.out.println("-df <default_filename>      - file in <dummydata_dir to be used for unrecognised mimetypes");
        System.out.println("                              default:a.txt");
        System.out.println("");
        System.out.println("-debug                      - turn on debug logging");
        System.out.println("");
        System.exit(0);
    }

    private static void printArgs() {
        System.out.println("=============Properties=============");
        if(!alfPropertiesFileName.equals("")) {
            System.out.println((new StringBuilder("Alfresco Properties file being used: ")).append(alfPropertiesFileName).toString());
        }
        System.out.println((new StringBuilder("DB Host: ")).append(dbHost).toString());
        System.out.println((new StringBuilder("DB Port: ")).append(dbPort).toString());
        System.out.println((new StringBuilder("DB Schema: ")).append(dbSchema).toString());
        System.out.println((new StringBuilder("DB User: ")).append(dbUser).toString());
        System.out.println((new StringBuilder("DB Pass: ")).append(dbPass).toString());
        System.out.println((new StringBuilder("DB Type: ")).append(dbType).toString());
        System.out.println((new StringBuilder("DB Url: ")).append(dbUrl).toString());
        System.out.println((new StringBuilder("DB Driver: ")).append(dbDriver).toString());
        System.out.println((new StringBuilder("alf_data: ")).append(alfData).toString());
        System.out.println((new StringBuilder("contentstore: ")).append(contentStoreName).toString());
        System.out.println((new StringBuilder("dummy dir: ")).append(dummyDir).toString());
        System.out.println((new StringBuilder("Mimetype maping: ")).append(mimeMapFile).toString());
        System.out.println((new StringBuilder("default filename: ")).append(defaultFilename).toString());
        if(DEBUG.equals("FULL")) {
            System.out.println("Debug is turned on");
        } else {
            System.out.println("Debug is turned off");
        }
        System.out.println("========End of Properties==========");
    }

    private static ArrayList<String> getContentUrlsFromDb(Connection con) {
        Statement stmnt = null;
        ResultSet rs = null;
        ArrayList<String> urls = new ArrayList<String>();
        try {
            stmnt = con.createStatement();
            rs = stmnt.executeQuery(sql1);
            if(rs.first()) {
                do {
                    urls.add(rs.getString(1));
                } while(rs.next());
            }
            rs.close();
            stmnt.close();
            try {
                stmnt = con.createStatement();
                rs = stmnt.executeQuery(sql2);
                if(rs.first()) {
                    do {
                        urls.add(rs.getString(1));
                    } while(rs.next());
                }
                rs.close();
                stmnt.close();
            } catch(MySQLSyntaxErrorException mysqlsyntaxerrorexception) {
            	
            }
        } catch(SQLException e) {
            System.out.println("Unable to retrieve Urls");
            e.printStackTrace();
            System.exit(0);
        }
        return urls;
    }

    private static Connection getConnection() {
        Connection con = null;
        try {
            Class.forName(dbDriver);
            if(dbUrl.equals("")) {
                if(dbType.equals("mysql")) {
                    dbUrl = (new StringBuilder("jdbc:mysql://")).append(dbHost).append(":").append(dbPort).append("/").append(dbSchema).toString();
                }
                if(dbType.equals("oracle")) {
                    dbUrl = (new StringBuilder("jdbc:oracle:thin:@")).append(dbHost).append(":").append(dbPort).append(":").append(dbSchema).toString();
                }
                if(dbType.equals("postgresql")) {
                    dbUrl = (new StringBuilder("jdbc:postgresql://")).append(dbHost).append(":").append(dbPort).append("/").append(dbSchema).toString();
                }
                if(dbType.equals("sqlserver")) {
                    dbUrl = (new StringBuilder("jdbc:jtds:sqlserver://")).append(dbHost).append(":").append(dbPort).append("/").append(dbSchema).toString();
                }
            }
            con = DriverManager.getConnection(dbUrl, dbUser, dbPass);
        }
        catch(ClassNotFoundException e) {
            System.out.println("Can't load jdbc driver ");
            e.printStackTrace();
            System.exit(0);
        }
        catch(SQLException e) {
            System.out.println("Can't connect to DB");
            e.printStackTrace();
            System.exit(0);
        }
        return con;
    }

    private static Properties fillHashMap(String mimeMapFile) {
        Properties mimeTypes = new Properties();
        try {
            mimeTypes.load(new FileInputStream(mimeMapFile));
        } catch(IOException e) {
            System.out.println("Unable to read mime map file.");
            e.getMessage();
            System.exit(0);
        }
        return mimeTypes;
    }
}
