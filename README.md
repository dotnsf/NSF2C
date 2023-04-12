# NSF2C

## Overview

Database converter stand-alone-application from NSF to Cloudant.

## Pre-requisite

- Create Cloudant instance, and get username and password.

- (Option)Place dblinkicon.png, viewlinkicon.png, and doclinkicon.png in same directory of application java file for custom link icons.

- IBM Notes or Domino need to be installed.

- Notes.jar need to be inclueded in CLASSPATH

- Following JARs need to be stored in same directory of NSF2C.jar, and included in CLASSPATH:

    - Apache Commons Codec

        - https://commons.apache.org/proper/commons-codec/

            - commons-codec-1.4.jar

    - Apache Commons Logging    
    
        - https://commons.apache.org/proper/commons-logging/

            - commons-logging-1.1.1.jar

    - Apache HttpComponents
    
        - https://hc.apache.org/
        
            - httpclient-4.5.1.jar
    
            - httpclient-cache-4.5.1.jar
    
            - httpcore-4.4.3.jar
    
            - httpmime-4.5.1.jar
    
    - JSON Simple
    
        - https://code.google.com/archive/p/json-simple/
    
            - json-simple-1.1.1.jar

- IBM Java should be used as JRE

## How to use App class in NSF2C.jar

`public class test01 {
	public static String nsf_db_server = null;
	public static String nsf_db_path = "dev/abc.nsf";
	public static String cloudant_username = "(username)";
	public static String cloudant_password = "(password)";
	
	public static void main( String[] args ) {
		App app = new App();
		app.setNSF( nsf_db_server, nsf_db_path );
		app.setCloudant( cloudant_username, cloudant_password );

		String repid = app.ExportDB();
		System.out.println( "repid = " + repid );
	}
}`


## Copyright

2017 K.Kimura @ Juge.Me all rights reserved.

