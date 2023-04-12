package me.juge.nsf2c;

public class test02 {
	public static String nsf_db_server = null;
	public static String nsf_db_path = "dev/nsf2c.nsf"; //"dev/nsf2c.nsf"; //"dev/esdemo.nsf"; //"dev/smartdev_nsf";
	public static String cloudant_username = "admin";
	public static String cloudant_password = "P@ssw0rd";

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		App app = new App();
		app.setNSF( nsf_db_server, nsf_db_path );
		app.setCloudant( cloudant_username,  cloudant_password, "http://192.168.0.105:5984/" );

		String repid = app.ExportDB();
		System.out.println( "\nrepid = " + repid );
	}

}
