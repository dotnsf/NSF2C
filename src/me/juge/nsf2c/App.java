package me.juge.nsf2c;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.net.ssl.HostnameVerifier;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.DxlExporter;
import lotus.domino.NotesFactory;
import lotus.domino.NotesThread;
import lotus.domino.Session;
import lotus.domino.View;
import lotus.domino.ViewEntry;
import lotus.domino.ViewEntryCollection;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

//. Cloudant REST APIs
//. https://console.bluemix.net/docs/services/Cloudant/api/database.html#databases

//. References
//. http://www.atmarkit.co.jp/ait/articles/0910/26/news097.html


public class App {
	public Session s = null;
	public String nsf_db_server = null;
	public String nsf_db_path = null;
	public String cloudant_username = null;
	public String cloudant_password = null;
	public String cloudant_url = "";
	
	public String cloudant_base_url = "";
	public String cloudant_dbname = "";
	
	public String dblink_default = "iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAYAAABzenr0AAAFzElEQVR42qVXS28bVRQOW14tLJH6hBZ1wT9gw48A2iKxREKohbjQIrWkSfyK7dixXcfxY2Y8Y6d5P+08/UhoypI0UeIq+QXsWLFh0xzOd+w7HQckT6mlq3vveHzPd875zneue3r+41Or1c6n0+nL6+vrH6+urn6ysrJyjeer1Wr1SqVSuYI1ZuxzudxVvDc+Pn6hGCm+xT9/o+d1Ps1m83y9WjtJJJKk5TUydIN0TZeBdT6Xl+fp0TTl83mKj8SpUi5TqVjEdy/4vT/5vSbPjYJRsPS8PqTlNI+maZ8bhnGuK4C9vb2L9VqNspksjaZGKZPJ0HhpnEZiIzQ7M0MjIyNkmSalHqVoc2ND3gMAvMMGZQAsZrNg2nuAZiC+rgAODg4uA8DYWIYePuynwYFBAXLr1m0qFArU2+uheDxOY+kxerqzQxl+b3lpiYqWJcYsszUjWhgKANamaQ50BXB4eHiuUa8TUoDDc9mchB0Da3iCORaNybNgcEgicNqw8h7PscYzTkOwK4Dd3d0LABCPJ8QQQMBblXuEHAMcgGfDw1E7Asq4SkHb65epyOv+rgCO9o4u1qpVMZpMJiXcOCAcDtPM9DSFQmEGkpfvq5ubNgeKVtHONcCqsDt5YRUsv5sUfIgIZPjggf4BGmAOwIin94546fHcEQICwG9Pn0qUKsvLAkB5rUAI8RzDNMygKxI26g1JgRyUf3kQjHHd2xyQFERUCl5GQHFApcROja4HugJ49uzZFaQg2dYBGBRvtDaANhHTHAHFAaUDHcb4O5V7+1le97qMQF0MJDgKUTZg6DoFAkGaeDxBoaEQH2oICesMFADLS8t2FajQq/w7Zxai7jqwv79/aavREPaDA/2sBZmxMbp392fx8qcf74ouQCd2njwRT8vMARVy7AFCrQHGVlI3AJ4/f35hq7FFj5hoyL/yyOaC1jpQfY8UAEDBQT5nGXbsNRcAoAOIQDL5yPYmy3lXXjCRZIxKL9CEjCoCztA7CakA8O+6l2Hz9+b5La4ChDjG+g/tx4/9/gBNTkzS4KBXDkWKthut91AFTgCnw69SwlLuddWMlMB4vT4KsGEc9MuDPibhY7p//4FwACT8dXtbSKiEyKn7pyOA2ZUQgYS1zSpFObTZbNY+RDiA/HP4sUaFIDWRcKSjFyjOKAAdjUkvuJDioyNpxwg/wmzn3iGzeK4a1XBkmFYqFYmAU3yU1x0lqRlBlymoig5MT03Z3axULHHPL8ldADqAQ6cmJwWEUkJnF3TeCdQz5oC7XlCv1SW3SMP16zfpyy9u0M0bX9H3t3+g/Lf95LnXS9/5vmaCxoULAACArPUdDch5KWlLsTsOAAAiMDc7J7eg+bk5Xs/KemF+3h7Yp5iQAIBI2TcfR+6dDYpvRAFXQgQOhEMR4QHqPBqNSnOCLGOPeyCuaBHOP8pSlaEKu+2xoxLa/STguh3DWCAQIF+7FHHzGeI+EOb7gM/n432QvGwc5bm0sCBGxZhh/IsLNondAAAJIUTIP7wNc5mhM4LtsViMvY6w/A7LvgUyKOkAOeGlTbhTKXCthLgRNZgDqPNEIiFEw60IIPAMM25KUEgACjIAkWLT7Cg/JwEdJPS6JiEioK5kAqY9AACGFQ/8vkCrGTnCrYw7AQmYV5FiEAwGYAyziggAYY/wKwAtKbZs9qtZgbDvhW5SIDpQrUmOMUA6yC3WmFtVERMAaMW4M7ZuRKWOG5ESIAxRSdM8ZhJ+1hXA8fHxJSghjPXzhQQNaYgrwOfzy+znGQO5R4fs63to9wKn8fb+L/bcsHTrU9f/GZu7zY9QhvDO1zYOpmMADICgLPEcpakAKClmgy/Y0x1TN7/hZnbmlf+c8oXkA47A3yyxJ6yAHWMOY3b2BGU3MzMjz1gJT1iIThjAH9xuY5ZlXet53U+5XH4zFAq9s729fXZxcfEs/z1/DzMbPbO2tvbuxsbG+5gxUqnU29gT0f/6W/4PiKnpYX2u/zgAAAAASUVORK5CYII=";
	public String viewlink_default = "iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAYAAABzenr0AAAEVElEQVR42u1XPWgUURAOIlhbC2JKUURsLP3J3QVs1EYjaExESW5/7sBKkXiX2NhpaaE5c+cVent3uxcQbExhYyWaiEn8ye1uEMFS0klcZ96+eW9290oFCw+Gt7vvzcw3876Z925o6P9v0K9S2TEy453Ozy2O5SreudGqd6FQ7Z1HOT7TOfbX/edn3bOFqhflbnejPIz4PDrbiwqxbOfveMN/GYB3XzivuMKpcI5AZmMgIxV3/I87PXmzdShXdVfA+To42ypIp5SBkRnMhitAwbfvILDO3RidW1yHuU8ouYr7WXyf9T7K8Uu+2vsMNlcLwm5vA2x9yFe6hxPODS84cLW59uZK/X2EMrmwouRy7V00AePEY5RlIZdrehQCz5fm30Xj8n1yYVno8vUo47Dm4sO30WRj7U2pHR5UAMxOcB0kMttBZLT9aOpZX4wo+C0WeHdioXcLdIrym1jLbJhybrqF0mfz8bvlBjd0Bjr+NVxQcsPI7gZiRONk0GTPaBTXmBIgrrOlHgrOx45jpwqgkwQIwEo6A+1wDCfRcdkD52AIDVO0BArXkFN8tqQOCUVttLVDitxO2MT5/oTOgNM/ZTGDFAU5JzFkSvE7gS1L5wTMks74NnHAlspUeEYBKDqbJ0wWsYikJUFIJbUlch8pIrOtI1WOWSZMxhMRBIISAIIc2wL/CCpSikhZoJXRFWXqeFpJh6KyGBCL84cRmshtuJtHdQY6/f20gFIojMiREzDBdkdXiUo9Gwk4bS3nQGmRleHU03BPmlQEQKfeV4C4A+SE2mO5nsBYcktIh/glnntf9yoA9vOPu2DyF2c0TyPPBDkoKjb76jtxxGZA0pUSbxPoN4Pd+vBbinbCxE9yiOy2WfRELsF8ZcQX/EiXJm9glIUS6xNyzXZlaWlnoh2Dwg+KgGeBlMRednV6eZOxXc0RrB5uh1JPfSIOzN/KHEZWJ/xKbC3LxZw81J6tlOH03qutkZ3VlECLLd3CzW7wPQMADK0SmwVZWjqV1A8MJ1mOCZ5QlliGLNZ6Uy15YxCA17zUSImLweZ4d6NsCE6AjemW7iW6XH1ZNX3UWc4AAGa+VKchLKJojERbTrZc1Yh470h1QXWgtTVPAMyrDABA3UukS3ZCQm2wiBTb5dlA3c+WfOEVVEzpiePY8V8MIKHf5G2TGyCipeucGhGBIWclRmJ+GNH2QYadDADb9R/wZpK4bFD7lOeCajys1apLC1aRF6rTkhNVNahu8GhQGd6liwZ1P17bcWT6NOOlZrFTzuSk7Ma84fyISza8lwFQ7AS3EpGzK9ggovEtGnQHEHcFT/cJfr5A45rLACi5m2UyOKjk+GlpsPsgv6Lxaxw5TJ+gsrNezwAod/v77E7wEAw9AqlZbf8xgJgHpTqwdgGU6pBOGMMFqIon8L0x7fQbQKg6nJTwza/BXK0I66GkG2LsBE3oeg0xh+9gB/ff8r4N///7+c/8fgOHZgvRLaKhvgAAAABJRU5ErkJggg==";
	public String doclink_default = "iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAYAAABzenr0AAAGK0lEQVR42q1X10+bVxTPW6W+5KFJIOkfFCkB2lRNG4W2GS9V36o2o7ENBoRihgMxKW2YDsNmGjzCtDFgphlpFMKwjcGx2UgkQLaUnN7f+bBBhvazUK50dM49d/185ucjR6LG+Sz70SS1+VTiLUvcWYXty3OKtpOnFaaT4NCfU3eckOTOU0nXzadYJ9bBQThzes/8vPph/Jlr9fE4k6C2fn5EbnyT3jJ7Kc9FP93ppUuCLmZ30eX8Pvoh10nJOV08T85x0sWsLkpKsUYoQWkW3EJfpdooUWVhHc5h7wWNne9LUFqzZAGIjQEcBAjwK3f7BHfR1bv9rLsidD9qe/nCq7p+1mMP5pexvrMf+7DG63xPP+7LkwVwwzDj07YFKb8jRBnNc/RX9zKlNswSdHntIdZDBt22ztO1yklSN/qo0LlEaSY/6ewLvPaH0UOKWi8T7tDYAnTdMJMrC0BR65nNbnlGua3PKL0JFy6Sqt5Hty3zfInGGiCsZz0MUKZ5jq5XTQqAXn4YQHPEOey7UT1DyjoJgM6xyGCFLA/ggsYxV2AaoaGnQRqeDAkKsswk5u6pBRqZCtHI9OKOvCD2CGIu7ZPkkLRPyEb7E/q5cFjEUfcdWQAiiPw1HeMUPT58+EDLy8v07t07evHiBW1vb9Pbt29pc3OTtra2mEP38ePHfWd7x73CIpP0vcahkweQZvMbOx7tu8Tv91NycjLZ7XbSaDR07949MplMlJ2dTfn5+VRQUEA6nY5CodC+sz1jHvq9YoK+zeyUt0BiiiVQcwCAWAZ+fTRJFvDQbxVPhQXs+fIAVBZ/ddvYoQDAOnq9nhoaGpgePHhAGxsb1Ds2Q7/qn9B3tztzZAEkKMzBwwJwOBzkcrmosrKSBgYGIvru0RmRrk9RlOTrQKLS4q1qHaVPOfoeeemXv0dgAXkACSqzLxwDRUVFVF1dTbW1tWQwGKi+vp4yMjLIbDbzL1QqlVRVVcVrIJgdAdrY2Eg9PT2sm5iYIOfIDJdpUZ61sgCSUm1BQ7vkAjxYWFhIdXV1fHlNTQ1Hf3l5OV8OHxuNxsijXV1dEVAVFRWcnhyEIgbQH0SAx2ABpdlX2SK5ALn96tUrevPmDb1+/ZovRD1AdGOOvMf6Xjk8ByEA379/L2LAw83p61RbbixBKGJAsgB+pc1mI7fbTW1tbVRcXBzxK9yAYLNarWSxWNhVLS0tXBtgDZBWqyWPxyNcMC11TZU1O5YYmDXuVEKYHI/C/HAHLkVcABjcAg4QmZmZPHc6nZSSksJuWVlZ2a2EwgU7bVreAsJMPkP7+CfNAqQhWyA1BgBJqVZvOAgPqnLRFS88R2xEy7ulGBYQMaB+qI0hCC3zey0A/8IFcAXMDfNChnsQ7dCBq1QqTj3EBjIBDWvXAtMMIElplgdwVtH8rLptFwAyAL5HcDU3N7PO5/NRX18fNyL4HnUCIBGQeXl5+6znHJXSMCmWLBC+mjfuacfBYJAWFhaY0OkCgQDLS0tLNDs7y+ter5f5+vo6PX78mBYXF7lth0eXezp2ACJaA3vbMdIQv7a3t5frPOao+TA3oh4cbsEePA53dHR00PPnz6NcILIgxXpXHkCKzWfs/Cdy+OXLl3wpzAx/p6WlsStQYjFgegCBK6BHVTzoe0D6arbGEoTmQ7fj/xoAELMFRKrMGQ/5QfJ/dQAAYmtGIggN7RKA+/fvc8NByqESwtfp6emcmmg8CoWCSktLuQxjD1yAbtnU1MTr0E1NTVHPqNQNY2pGohBFghBdDw/Dv5ABBj7Ho3gMOoACANQBBGdrayvdvHmTG1HEAiNTJHoMJSrMubH0grnoUnzQl260bu88ei1cis/capb/KFXXTwVKHT5qHJqnhsE5Mg0HqGl4XlCAmt2S3Cj0WA/rzUJv4v3zVNfv53OQsadRcL3TR+r6ScpsiuGfUUn/mr+0f43KB9cIHFQ2IPjALseafmid9IOSHNYX9a1SkWuVigUvEVTav8rnS5hWqWxwTR5A0djG0cqB5ROGsa1jevdqfMnQdlzJ0Epc2aOt4yUOwV1Lx8PzMtfW8aLuxWOYVw5snwBhXuHe/AL7IP/ZvRov3bMSp2v1fhb93r9gLqnW1tg34wAAAABJRU5ErkJggg==";

	public static int[] dbicon_bg = new int[32*32];
	public static int[] dbicon_fg = new int[32*32];
	public static Color[] dbicon_color = {
		new Color( 0, 0, 0 ),
		new Color( 255, 255, 255 ),
		new Color( 255, 0, 0 ),
		new Color( 0, 255, 0 ),
		new Color( 0, 0, 255 ),
		new Color( 255, 0, 255 ),
		new Color( 255, 255, 0 ),
		new Color( 0, 255, 255 ),
		new Color( 128, 0, 0 ),
		new Color( 0, 128, 0 ),
		new Color( 0, 0, 128 ),
		new Color( 128, 0, 128 ),
		new Color( 128, 128, 0 ),
		new Color( 0, 128, 128 ),
		new Color( 70, 70, 70 ),
		new Color( 128, 128, 128 ),
		new Color( 200, 200, 200 )  //. 背景
	};
	
	
	public App(){
	}
	
	public App( String nsf_db_server, String nsf_db_path ){
		this( nsf_db_server, nsf_db_path, "" );
	}

	public App( String nsf_db_server, String nsf_db_path, String cloudant_url ){
		this.nsf_db_server = nsf_db_server;
		this.nsf_db_path = nsf_db_path;
		this.cloudant_url = cloudant_url;
	}
	
	
	public void setNSF( String nsf_db_server, String nsf_db_path ){
		this.nsf_db_server = nsf_db_server;
		this.nsf_db_path = nsf_db_path;
	}
	
	public void setCloudant( String cloudant_username, String cloudant_password, String cloudant_url ){
		this.cloudant_username = cloudant_username;
		this.cloudant_password = cloudant_password;
		this.cloudant_url = cloudant_url;
	}
	
	public void setCloudant( String cloudant_username, String cloudant_password ){
		this.setCloudant( cloudant_username, cloudant_password, "" );
	}
	
	public void setCloudantDBName( String dbname ){
		this.cloudant_dbname = dbname;
	}
	

	public String ExportDB(){
		String repid = null;
		
		try{
			NotesThread.sinitThread();
			JSONParser parser = new JSONParser();
			
			s = NotesFactory.createSessionWithFullAccess();
			Database db = s.getDatabase( this.nsf_db_server, this.nsf_db_path );
			if( !db.isOpen() ){
				db.open();
			}
			
			//. HTTP Client
			HostnameVerifier hostnameVerifier = NoopHostnameVerifier.INSTANCE;
			CloseableHttpClient client = HttpClients.custom().setSSLHostnameVerifier( hostnameVerifier ).build(); //HttpClients.createDefault();
			
			//. Cloudant
			cloudant_base_url = "https://" + cloudant_username + ".cloudant.com/";
			if( this.cloudant_url != null && this.cloudant_url.length() > 0 ){
				cloudant_base_url = this.cloudant_url;
			}
			System.out.println( "Cloudant URL: " + cloudant_url );
			byte[] b64data = Base64.encodeBase64( ( cloudant_username + ":" + cloudant_password ).getBytes() );

			//. Cloudant DB 名称
			if( cloudant_dbname == null || cloudant_dbname.length() == 0 ){
				String tmp_nsf_path = nsf_db_path;
				tmp_nsf_path = tmp_nsf_path.replaceAll( "\\\\", "/" );
				tmp_nsf_path = tmp_nsf_path.replaceAll( "/", "_" );
				cloudant_dbname = tmp_nsf_path.replaceAll( "\\.", "_" );
			}
			
			DxlExporter exporter = s.createDxlExporter();
			exporter.setOutputDOCTYPE( false );
			exporter.setConvertNotesBitmapsToGIF( true );
			String dxl = exporter.exportDxl( db );

			Element databaseElement = getRootElement( dxl ); // <database>
			repid = databaseElement.getAttribute( "replicaid" );

			//cloudant_dbname = repid.toLowerCase(); //. must begin with letter
			
			//. Cloudant DB （削除／）作成
			System.out.println( "cloudant_dbname = " + cloudant_dbname );
			HttpDelete delete0 = new HttpDelete( cloudant_base_url + cloudant_dbname );
			delete0.setHeader( "Authorization", "Basic " + new String( b64data ) );
			CloseableHttpResponse response0 = client.execute( delete0 );
			int sc0 = response0.getStatusLine().getStatusCode();
			HttpEntity entity0 = response0.getEntity();
			String r0 = EntityUtils.toString( entity0, "UTF_8" );
			System.out.println( "delete0(" + sc0 + ") = " + r0 );
			
			HttpPut put1 = new HttpPut( cloudant_base_url + cloudant_dbname );
			put1.setHeader( "Authorization", "Basic " + new String( b64data ) );
			CloseableHttpResponse response1 = client.execute( put1 );
			int sc1 = response1.getStatusLine().getStatusCode();
			HttpEntity entity1 = response1.getEntity();
			String r1 = EntityUtils.toString( entity1, "UTF_8" );
			System.out.println( "put1(" + sc1 + ") = " + r1 );
			client.close();

			//. DB 本体の DXL そのものを保存
			Writer out1 = new OutputStreamWriter( new FileOutputStream( repid + ".xml" ), "UTF-8" );
			out1.write( dxl );
			out1.close();

			//. UTF-8 で読み込みなおし
			databaseElement = getRootElement( new File( repid + ".xml" ) ); // <database>	
			File file = new File( repid + ".xml" );
			file.deleteOnExit();

			//. DBタイトル
			String dbTitle = databaseElement.getAttribute( "title" );

			//. 最初にアイコン類
			//. DBIcon
			//. Lotus Notes 8.5.2 以降のエクスポートアイコンを探す
			String dbicon = null;
			NodeList imgresList = databaseElement.getElementsByTagName( "imageresource" );
			int nImgresList = imgresList.getLength();
			for( int i = 0; i < nImgresList; i ++ ){
				Element imgresElement = ( Element )imgresList.item( i );
				String imgresName = imgresElement.getAttribute( "name" );
				if( imgresName.equals( "$DBIcon" ) ){
					String imagename = imgresElement.getAttribute( "imagename" );
					int n = imagename.lastIndexOf( "." );
					String imageext = imagename.substring( n );  //. .gif, .jpg, .png, ..
					
					NodeList imgList = imgresElement.getElementsByTagName( "gif" );
					if( imgList.getLength() == 0 ){
						imgList = imgresElement.getElementsByTagName( "jpeg" );
					}
					
					byte[] data = null;
					if( imgList.getLength() > 0 ){
						Element imgElement = ( Element )imgList.item( 0 );
						String bitmapdata = imgElement.getFirstChild().getNodeValue();
						if( bitmapdata.length() > 0 ){
							data = Base64.decodeBase64( bitmapdata );
						}
					}
					
					if( data != null ){
						//. 一旦保存
						try{
							File f = new File( repid + imageext );
							BufferedOutputStream bos = new BufferedOutputStream( new FileOutputStream( f ) );
							bos.write( data, 0, data.length );
							bos.close();
							
							//. 32x32 を 64x64 にリサイズして読み込み
							BufferedImage srcimg = null, dstimg = null;
							try{
								srcimg = ImageIO.read( f );
							}catch( Exception e ){
								e.printStackTrace();
								srcimg = null;
							}
							
							try{
								dstimg = new BufferedImage( 64, 64, BufferedImage.TYPE_INT_ARGB );  //. NGリスト: TYPE_BYTE_BINARY
							}catch( Exception e ){
								e.printStackTrace();
								dstimg = null;
							}

							if( srcimg != null && dstimg != null ){
								try{
									int w = srcimg.getWidth();
									double sx = ( double )64.0 / w;
									int h = srcimg.getHeight();
									double sy = ( double )64.0 / h;
									AffineTransform trans = AffineTransform.getScaleInstance( sx, sy );
									
									Graphics2D g2 = ( Graphics2D )dstimg.createGraphics();
									g2.drawImage( srcimg, trans, null );
									g2.dispose();
									
									//. BufferedImage -> Base64 String
									try{
										ByteArrayOutputStream os = new ByteArrayOutputStream();
										ImageIO.write( dstimg, "gif", os );
										dbicon = Base64.encodeBase64String( os.toByteArray() );
									}catch( Exception e ){
									}
									//File outf = new File( repid + ".gif" );
									//ImageIO.write( dstimg, "gif", outf );
								}catch( Exception e ){
									e.printStackTrace();
								}
							}
						}catch( Exception e ){
							e.printStackTrace();
						}
					}
					
					break;
				}
			}
			
			if( dbicon == null ){
				//. note を処理
				NodeList noteList = databaseElement.getElementsByTagName( "note" );
				int nNoteList = noteList.getLength();
				for( int i = 0; i < nNoteList; i ++ ){
					Element noteElement = ( Element )noteList.item( i );
					String unid = getUnid( noteElement );
					
					//. アイコンを探す
					byte[] data = null;
						
					//. クラシックアイコンの調査
					NodeList itemList = noteElement.getElementsByTagName( "item" );
					int nItemList = itemList.getLength();
					for( int j = 0; j < nItemList; j ++ ){
						Element itemElement = ( Element )itemList.item( j );
						String name = itemElement.getAttribute( "name" );
						if( name.toLowerCase().equals( "iconbitmap" ) ){
							NodeList list = itemElement.getElementsByTagName( "rawitemdata" );
							Element element = ( Element )list.item( 0 );
							String bitmapdata = element.getFirstChild().getNodeValue();
							data = Base64.decodeBase64( bitmapdata );
							break;
						}
					}
							
					if( data != null ){
						//. 背景データ
						for( int j = 0x0006, k = 0; j <= 0x0085; j ++ ){
							int b = ( int )data[j];
								
							//. 背景データは１ビットずつ確認する
							for( int l = 0; l < 8; l ++ ){
								int mask = 128 >> l;
								int x = ( b & mask ) >> ( 7 - l );
								dbicon_bg[k+l] = x;
							}
							k += 8;
						}
								
						//. 前景データ
						for( int j = 0x0086, k = 0; j <= 0x0285; j ++ ){
							int b = ( int )data[j];
					
							//. 前景データは４ビットずつ確認する
							for( int l = 0; l < 2; l ++ ){
								int mask = 0xf0 >> ( l * 4 );
								int x = ( b & mask ) >> ( 4 - ( l * 4 ) );
								dbicon_fg[k+l] = x;
							}
							k += 2;
						}

						//. 可能ならば透過GIFにしたい・・・
//						BufferedImage bimg = new BufferedImage( 64, 64, BufferedImage.TYPE_BYTE_INDEXED );
						BufferedImage bimg = new BufferedImage( 64, 64, BufferedImage.TYPE_INT_BGR );
						Graphics2D g2 = bimg.createGraphics();
						//. g2 に図形を描画
						for( int j = 0; j < 32 * 32; j ++ ){
							int x = ( j % 32 ) * 2;
							int y = ( 31 - ( j / 32 ) ) * 2;
							if( dbicon_bg[j] == 1 ){
								g2.setColor( dbicon_color[16] );
							}else{
								g2.setColor( dbicon_color[dbicon_fg[j]] );
							}
									
							g2.fillRect( x, y, 2, 2 );
						}
								
						g2.drawImage( bimg, 0, 0, null );
								
						//. BufferedImage -> Base64 String
						try{
							ByteArrayOutputStream os = new ByteArrayOutputStream();
							ImageIO.write( bimg, "gif", os );
							dbicon = Base64.encodeBase64String( os.toByteArray() );
						}catch( Exception e ){
						}
					}
				}
			}
			
			if( dbicon != null ){
				//. 「アプリケーションについて」
				String unidHelpabout = null;
				NodeList helpaboutdocumentList = databaseElement.getElementsByTagName( "helpaboutdocument" );
				int nHelpaboutdocumentList = helpaboutdocumentList.getLength();
				for( int i = 0; i < nHelpaboutdocumentList; i ++ ){
					Element helpaboutdocumentElement = ( Element )helpaboutdocumentList.item( i );
					JSONObject helpaboutdocumentJSON = convertHelpaboutdocumentElementToJSON( helpaboutdocumentElement );
					unidHelpabout = ( String )helpaboutdocumentJSON.get( "_id" );
					//System.out.println( "\nunidHelpabout = " + unidHelpabout );
					
					String helpaboutdocumentStr = helpaboutdocumentJSON.toJSONString();

//					System.out.println( "\nhelpaboutdocumentStr[" + i + "] = " + helpaboutdocumentStr );

					client = HttpClients.custom().setSSLHostnameVerifier( hostnameVerifier ).build(); //HttpClients.createDefault();
					HttpPost post2 = new HttpPost( cloudant_base_url + cloudant_dbname );
					post2.setHeader( "Authorization", "Basic " + new String( b64data ) );
					post2.setHeader( "Content-Type", "application/json" );
					StringEntity body_entity2 = new StringEntity( helpaboutdocumentStr, "UTF-8" ); //. "UTF-8" 必須
					post2.setEntity( body_entity2 );

					CloseableHttpResponse response2 = client.execute( post2 );
					int sc2 = response2.getStatusLine().getStatusCode();
					HttpEntity entity2 = response2.getEntity();
					String r2 = EntityUtils.toString( entity2, "UTF_8" );
					System.out.println( "post2(" + sc2 + ") = " + r2 );
					client.close();
				}

				//. 「アプリケーションの使い方」
				String unidHelpusing = null;
				NodeList helpusingdocumentList = databaseElement.getElementsByTagName( "helpusingdocument" );
				int nHelpusingdocumentList = helpusingdocumentList.getLength();
				for( int i = 0; i < nHelpusingdocumentList; i ++ ){
					Element helpusingdocumentElement = ( Element )helpusingdocumentList.item( i );
					JSONObject helpusingdocumentJSON = convertHelpusingdocumentElementToJSON( helpusingdocumentElement );
					unidHelpusing = ( String )helpusingdocumentJSON.get( "_id" );
					//System.out.println( "\nunidHelpusing = " + unidHelpusing );
					
					String helpusingdocumentStr = helpusingdocumentJSON.toJSONString();

//					System.out.println( "\nhelpusingdocumentStr[" + i + "] = " + helpusingdocumentStr );

					client = HttpClients.custom().setSSLHostnameVerifier( hostnameVerifier ).build(); //HttpClients.createDefault();
					HttpPost post2 = new HttpPost( cloudant_base_url + cloudant_dbname );
					post2.setHeader( "Authorization", "Basic " + new String( b64data ) );
					post2.setHeader( "Content-Type", "application/json" );
					StringEntity body_entity2 = new StringEntity( helpusingdocumentStr, "UTF-8" ); //. "UTF-8" 必須
					post2.setEntity( body_entity2 );

					CloseableHttpResponse response2 = client.execute( post2 );
					int sc2 = response2.getStatusLine().getStatusCode();
					HttpEntity entity2 = response2.getEntity();
					String r2 = EntityUtils.toString( entity2, "UTF_8" );
					System.out.println( "post2(" + sc2 + ") = " + r2 );
					client.close();
				}

				//. データベーストップページとそのデータをアップロード
				
				//. DBIcon
				dbicon = dbicon.replaceAll( "\r", "" );  //. \r\n を無視
				dbicon = dbicon.replaceAll( "\n", "" );  //. \r\n を無視
				String dbimgs_doc = "{\"_id\":\"" + repid + "\",\"_attachments\":{\"dbicon.gif\":{\"content_type\":\"image/gif\",\"data\":\"" + dbicon + "\"}";
				
				//. DBLink, ViewLink, DocLink
				String dblink = dblink_default, viewlink = viewlink_default, doclink = doclink_default;

				try{
					//. DBLink
					try{
						File fdb = new File( "./dblinkicon.png" );
						long f_size = fdb.length();
						byte[] dbimg = new byte[( int )f_size];

						RandomAccessFile raf = new RandomAccessFile( fdb, "r" );
						try{
							raf.readFully( dbimg );
						}finally{
							raf.close();
						}
					
						BufferedImage image = ImageIO.read( new ByteArrayInputStream( dbimg ) );
						ByteArrayOutputStream os = new ByteArrayOutputStream();
						ImageIO.write( image, "png", os );
						dblink = Base64.encodeBase64String( os.toByteArray() );
						dblink = dblink.replaceAll( "\r", "" );
						dblink = dblink.replaceAll( "\n", "" );
					}catch( Exception e ){
					}
					dbimgs_doc += ",\"dblink.png\":{\"content_type\":\"image/png\",\"data\":\"" + dblink + "\"}";
					
					//. ViewLink
					try{
						File fview = new File( "./viewlinkicon.png" );
						long f_size = fview.length();
						byte[] viewimg = new byte[( int )f_size];

						RandomAccessFile raf = new RandomAccessFile( fview, "r" );
						try{
							raf.readFully( viewimg );
						}finally{
							raf.close();
						}
					
						BufferedImage image = ImageIO.read( new ByteArrayInputStream( viewimg ) );
						ByteArrayOutputStream os = new ByteArrayOutputStream();
						ImageIO.write( image, "png", os );
						viewlink = Base64.encodeBase64String( os.toByteArray() );
						viewlink = viewlink.replaceAll( "\r", "" );
						viewlink = viewlink.replaceAll( "\n", "" );
					}catch( Exception e ){
					}
					dbimgs_doc += ",\"viewlink.png\":{\"content_type\":\"image/png\",\"data\":\"" + viewlink + "\"}";
					
					//. DocLink
					try{
						File fdoc = new File( "./doclinkicon.png" );
						long f_size = fdoc.length();
						byte[] docimg = new byte[( int )f_size];

						RandomAccessFile raf = new RandomAccessFile( fdoc, "r" );
						try{
							raf.readFully( docimg );
						}finally{
							raf.close();
						}
					
						BufferedImage image = ImageIO.read( new ByteArrayInputStream( docimg ) );
						ByteArrayOutputStream os = new ByteArrayOutputStream();
						ImageIO.write( image, "png", os );
						doclink = Base64.encodeBase64String( os.toByteArray() );
						doclink = doclink.replaceAll( "\r", "" );
						doclink = doclink.replaceAll( "\n", "" );
					}catch( Exception e ){
					}
					dbimgs_doc += ",\"doclink.png\":{\"content_type\":\"image/png\",\"data\":\"" + doclink + "\"}";
				}catch( Exception e ){
				}
				
				dbimgs_doc += "}}";
				//System.out.println( "\ndbimgs_doc = " + dbimgs_doc );

				client = HttpClients.custom().setSSLHostnameVerifier( hostnameVerifier ).build(); //HttpClients.createDefault();
				HttpPost post3 = new HttpPost( cloudant_base_url + cloudant_dbname );
				post3.setHeader( "Authorization", "Basic " + new String( b64data ) );
				post3.setHeader( "Content-Type", "application/json" );
				StringEntity body_entity3 = new StringEntity( dbimgs_doc, "UTF-8" ); //. "UTF-8" 必須
				post3.setEntity( body_entity3 );

				CloseableHttpResponse response3 = client.execute( post3 );
				int sc3 = response3.getStatusLine().getStatusCode();
				HttpEntity entity3 = response3.getEntity();
				String r3 = EntityUtils.toString( entity3, "UTF_8" );
				System.out.println( "post3(" + sc3 + ") = " + r3 );
				client.close();
				
				//. REPID view
				String postdata = "{\"language\":\"javascript\",\"views\":{\"" + repid + "\":{\"map\":\"function(doc){ if( doc.nsfview ){ emit( doc._id, doc.nsfview ); } }\" } }, \"lists\": { \"" + repid + "\": \"function( head, row ){ start( { 'headers': { 'content-type': 'text/html' } } ); send( '<h1>" + dbTitle + "</h1><br/><img src=\\\"../../../../" + repid + "/dbicon.gif\\\"/><br/><ul>' ); var row; while( row = getRow() ){ var vname = row.value.name[0]; var url = '../../../' + vname + '/_list/' + vname + '/' + vname; send( '<li><a href=\\\"' + url + '\\\">' + vname + '</a></li>' ); } send( '</ul><div align=\\\"right\\\">' );";
				//. K.Kimura（文書は入るが、それを HTML 表示するための汎用的な Show Design Document が必要）
				if( unidHelpabout != null ){
					postdata += " send( '<a href=\\\"../../_show/view/" + unidHelpabout + "\\\">アプリケーションについて</a><br/>' );";
				}
				if( unidHelpusing != null ){
					postdata += " send( '<a href=\\\"../../_show/view/" + unidHelpusing + "\\\">アプリケーションの使い方</a><br/>' );";
				}
				postdata += " send( '</div>' ); }\" }";
				postdata += ", \"shows\":{\"view\": \"(function( doc, req ){ if( doc && doc.nsfhelpdocument ){ return doc.nsfhelpdocument.richtext; }else{ return 'empty'; } } )\"}";
				postdata += "}";
				//System.out.println( "\npostdata = " + postdata );
				
				client = HttpClients.custom().setSSLHostnameVerifier( hostnameVerifier ).build(); //HttpClients.createDefault();
				HttpPut put4 = new HttpPut( cloudant_base_url + cloudant_dbname + "/_design/" + repid );
				put4.setHeader( "Authorization", "Basic " + new String( b64data ) );
				put4.setHeader( "Content-Type", "application/json" );
				StringEntity body_entity4 = new StringEntity( postdata, "UTF-8" ); //. "UTF-8" 必須
				put4.setEntity( body_entity4 );

				CloseableHttpResponse response4 = client.execute( put4 );
				int sc4 = response4.getStatusLine().getStatusCode();
				HttpEntity entity4 = response4.getEntity();
				String r4 = EntityUtils.toString( entity4, "UTF_8" );
				System.out.println( "put4(" + sc4 + ") = " + r4 );
				client.close();
			}
			
			//. SharedField を処理
			NodeList sharedfieldList = databaseElement.getElementsByTagName( "sharedfield" );
			int nSharedfieldList = sharedfieldList.getLength();
			String[] sharedfieldStr = new String[nSharedfieldList];
			for( int i = 0; i < nSharedfieldList; i ++ ){
				Element sharedfieldElement = ( Element )sharedfieldList.item( i );

				JSONObject sharedfieldJSON = convertSharedfieldElementToJSON( sharedfieldElement );
				sharedfieldStr[i] = sharedfieldJSON.toJSONString();
				//sharedfieldStr[i] = sharedfieldStr[i].replaceAll( "\\\\/", "/" );
				//System.out.println( "\nsharedfieldStr[" + i + "] = " + sharedfieldStr[i] );
			}
			
			//. SubForm を処理 
			NodeList subformList = databaseElement.getElementsByTagName( "subform" );
			int nSubformList = subformList.getLength();
			String[] subformStr = new String[nSubformList];
			for( int i = 0; i < nSubformList; i ++ ){
				Element subformElement = ( Element )subformList.item( i );
				
				//. 共有要素をマージ
				JSONObject subformJSON = convertSubformElementToJSON( subformElement );
				subformStr[i] = subformJSON.toJSONString();
				subformStr[i] = mergeSharedfieldsIntoSubform( sharedfieldStr, subformStr[i] );
				subformStr[i] = subformStr[i].replaceAll( "\\\\/", "/" );
				//subformStr[i] = subformStr[i].replaceAll( "\\n", "\\\\n" );
				//System.out.println( "\nsubformStr[" + i + "] = " + subformStr[i] );
			}

			//. Form を処理
			NodeList formList = databaseElement.getElementsByTagName( "form" );
			int nFormList = formList.getLength();
			String[] formStr = new String[nFormList];
			for( int i = 0; i < nFormList; i ++ ){
				Element formElement = ( Element )formList.item( i );
				
				String tmp = ElementToXML( formElement );
				//System.out.println( "\nformElement[" + i + "] = " + tmp );
				
				//. 共有要素をマージ
				JSONObject formJSON = convertFormElementToJSON( formElement );
				formStr[i] = formJSON.toJSONString();
				formStr[i] = mergeSharedfieldsIntoForm( sharedfieldStr, formStr[i] );
				formStr[i] = mergeSubformsIntoForm( subformStr, formStr[i] );
				formStr[i] = formStr[i].replaceAll( "\\\\/", "/" );
				//System.out.println( "\nformStr[" + i + "] = " + formStr[i] );

				client = HttpClients.custom().setSSLHostnameVerifier( hostnameVerifier ).build(); //HttpClients.createDefault();
				HttpPost post2 = new HttpPost( cloudant_base_url + cloudant_dbname );
				post2.setHeader( "Authorization", "Basic " + new String( b64data ) );
				post2.setHeader( "Content-Type", "application/json" );
				StringEntity body_entity2 = new StringEntity( formStr[i], "UTF-8" ); //. "UTF-8" 必須
				post2.setEntity( body_entity2 );

				CloseableHttpResponse response2 = client.execute( post2 );
				int sc2 = response2.getStatusLine().getStatusCode();
				HttpEntity entity2 = response2.getEntity();
				String r2 = EntityUtils.toString( entity2, "UTF_8" );
				System.out.println( "post2(" + sc2 + ") = " + r2 );
				client.close();
			}
			
/*
			//. SharedColumn を処理
			NodeList sharedcolumnList = databaseElement.getElementsByTagName( "sharedcolumn" );
			int nSharedcolumnList = sharedcolumnList.getLength();
			String[] sharedcolumnStr = new String[nSharedcolumnList];
			for( int i = 0; i < nSharedcolumnList; i ++ ){
				Element sharedcolumnElement = ( Element )sharedcolumnList.item( i );

				JSONObject sharedcolumnJSON = convertSharedcolumnElementToJSON( sharedcolumnElement );
				sharedcolumnStr[i] = sharedcolumnJSON.toJSONString();
				//sharedcolumnStr[i] = sharedcolumnStr[i].replaceAll( "\\\\/", "/" );
				System.out.println( "\nsharedcolumnStr[" + i + "] = " + sharedcolumnStr[i] );
			}
*/
			
			//. View を処理 
			NodeList viewList = databaseElement.getElementsByTagName( "view" );
			int nViewList = viewList.getLength();
			String[] viewStr = new String[nViewList];
			for( int i = 0; i < nViewList; i ++ ){
				Element viewElement = ( Element )viewList.item( i );

				JSONObject viewJSON = convertViewElementToJSON( viewElement );
				viewStr[i] = viewJSON.toJSONString();
				
				//. データとしてポスト
				//System.out.println( "\nviewStr[" + i + "] = " + viewStr[i] );
				client = HttpClients.custom().setSSLHostnameVerifier( hostnameVerifier ).build(); //HttpClients.createDefault();
				HttpPost post2 = new HttpPost( cloudant_base_url + cloudant_dbname );
				post2.setHeader( "Authorization", "Basic " + new String( b64data ) );
				post2.setHeader( "Content-Type", "application/json" );
				StringEntity body_entity2 = new StringEntity( viewStr[i], "UTF-8" ); //. "UTF-8" 必須
				post2.setEntity( body_entity2 );

				CloseableHttpResponse response2 = client.execute( post2 );
				int sc2 = response2.getStatusLine().getStatusCode();
				HttpEntity entity2 = response2.getEntity();
				String r2 = EntityUtils.toString( entity2, "UTF_8" );
				System.out.println( "post2(" + sc2 + ") = " + r2 );
				client.close();

				//. この時点で共有要素をマージ済み
				
				//. 選択式
				String formname = "";
				JSONArray viewnames = ( JSONArray )( ( JSONObject )viewJSON.get( "nsfview" ) ).get( "name" );
				String viewname = ( String )( viewnames.get( 0 ) );
				String selection_formula = ( String )( ( JSONObject )( ( JSONObject )viewJSON.get( "nsfview" ) ).get( "selection" ) ).get( "formula" );
				//System.out.println( "selection_formula = " + selection_formula );
				String ifstring = "doc.nsfdocument";
				if( !selection_formula.equalsIgnoreCase( "SELECT @All" ) ){
					if( selection_formula.startsWith( "SELECT " ) ){
						String str1 = selection_formula.substring( 7 );
						str1 = str1.trim();
						if( str1.toLowerCase().startsWith( "form" ) ){
							String tmp0 = str1.substring( 4 ); //. Form = 'Form1'
							str1 = "form" + tmp0;
							
							//. フォーム名
							String[] stmp1 = tmp0.split( "=" );
							for( int j = 0; j < stmp1.length; j ++ ){
								String tmp2 = stmp1[j].trim();
								if( !tmp2.toLowerCase().equals( "form" ) ){
									tmp2 = tmp2.replaceAll( "'", "" );
									tmp2 = tmp2.replaceAll( "\"", "" );
									if( tmp2.length() > 0 && tmp2.indexOf( " " ) == -1 ){
										formname = tmp2;
									}
								}
							}
						}
						ifstring += ( " && doc.nsfdocument." + str1 );
					}
				}
				
				//. view 時用
				String strv = "\\\"\\\"; switch( doc.nsfdocument.form ){ ";
				for( int j = 0; j < nFormList; j ++ ){
					JSONObject obj = ( JSONObject )( parser.parse( formStr[j] ) );
					JSONObject nsfform = ( JSONObject )( obj.get( "nsfform" ) );
					JSONArray names = ( JSONArray )( nsfform.get( "name" ) );
					for( int k = 0; k < names.size(); k ++ ){
						String name = ( String )( names.get( k ) );

						//. UI 定義
						String richtext = ( String )( nsfform.get( "richtext" ) );
						
						richtext = richtext.replaceAll( "\n", "" );
						richtext = richtext.replaceAll( "'", "\\'" );
						//richtext = "\\\"" + richtext + "\\\"";
						
						String[] tmpa = null;
						do{
							tmpa = TrimTextNext( richtext, new String[]{ "<field " }, "</field>" );
							if( tmpa != null && tmpa.length == 3 ){
								//. 以下は view 用
								String fieldkind = TrimText( tmpa[1], new String[]{ "kind='" }, "'" );
								String fieldname = TrimText( tmpa[1], new String[]{ "name='" }, "'" );
								if( fieldname != null && fieldname.length() > 0 ){
									if( fieldkind.equals( "editable" ) ){
//										richtext = tmpa[0] + "\\\" + doc.nsfdocument.items." + fieldname + ".values[0] + \\\"" + tmpa[2];
//										richtext = tmpa[0] + "\\\" + doc.nsfdocument.items." + fieldname + ".values + \\\"" + tmpa[2]; //. 複数値対応
										richtext = tmpa[0] + "\\\" + ( doc.nsfdocument.items." + fieldname + " ? doc.nsfdocument.items." + fieldname + ".values : \\\"(????)\\\" ) + \\\"" + tmpa[2]; //. 値がなかった時の対応
									}else if( fieldkind.indexOf( "computed" ) > -1 ){
										String code = TrimText( tmpa[1], new String[]{ "<code ", ">" }, "</code>" );
										
										richtext = tmpa[0] + code + tmpa[2];
									}else{
										richtext = tmpa[0] + tmpa[2];
									}
								}else{
									richtext = tmpa[0] + tmpa[2];
								}
							}
						}while( tmpa != null && tmpa.length == 3 );
						
						String cs = "case \\\"" + name + "\\\": str =\\\"" + richtext + "\\\"; break;";
						strv += cs;
					}
				}
				strv += " }";

				
				//. edit 時用
				String stre = "\\\"\\\"; switch( doc.nsfdocument.form ){ ";
				for( int j = 0; j < nFormList; j ++ ){
					JSONObject obj = ( JSONObject )( parser.parse( formStr[j] ) );
					JSONObject nsfform = ( JSONObject )( obj.get( "nsfform" ) );
					JSONArray names = ( JSONArray )( nsfform.get( "name" ) );
					for( int k = 0; k < names.size(); k ++ ){
						String name = ( String )( names.get( k ) );

						//. UI 定義
						String richtext = ( String )( nsfform.get( "richtext" ) );
						
						richtext = richtext.replaceAll( "\n", "" );
						richtext = richtext.replaceAll( "'", "\\'" );
						
						String[] tmpa = null;
						do{
							tmpa = TrimTextNext( richtext, new String[]{ "<field " }, "</field>" );
							if( tmpa != null && tmpa.length == 3 ){
								//. 以下は edit 用
								String fieldkind = TrimText( tmpa[1], new String[]{ "kind='" }, "'" );
								String fieldname = TrimText( tmpa[1], new String[]{ "name='" }, "'" );
								String fieldtype = TrimText( tmpa[1], new String[]{ "type='" }, "'" );
								
								//richtext = tmpa[0] + "<input type='text' name='" + fieldname + "' value='\\\" + doc.nsfdocument.items." + fieldname + ".values + \\\" '/>" + tmpa[2];
								if( fieldname != null && fieldname.length() > 0 ){
									if( fieldkind.equals( "editable" ) ){
										//. <input> か <textarea> か
										if( fieldtype != null && fieldtype.equals( "richtext" ) ){
											richtext = tmpa[0] + "<textarea cols='100' rows='10' name='" + fieldname + "'>\\\" + ( '' + doc.nsfdocument.items." + fieldname + ".values ).split( \\\"'\\\" ).join( \\\"&#39;\\\" ) + \\\"</textarea>" + tmpa[2];
										}else{
											richtext = tmpa[0] + "<input type='text' name='" + fieldname + "' value='\\\" + ( '' + doc.nsfdocument.items." + fieldname + ".values ).split( \\\"'\\\" ).join( \\\"&#39;\\\" ) + \\\"'/>" + tmpa[2];
										}
									}else if( fieldkind.indexOf( "computed" ) > -1 ){
										String code = TrimText( tmpa[1], new String[]{ "<code ", ">" }, "</code>" );
										
										richtext = tmpa[0] + code + tmpa[2];
									}else{
										richtext = tmpa[0] + tmpa[2];
									}
								}else{
									richtext = tmpa[0] + tmpa[2];
								}
							}
						}while( tmpa != null && tmpa.length == 3 );
						
						String cs = "case \\\"" + name + "\\\": str =\\\"" + richtext + "\\\"; break;";
						stre += cs;
					}
				}
				stre += " }";
				
				ifstring = ifstring.replaceAll( "\"", "'" );
				ifstring = ifstring.replaceAll( "=", "==" );
				String functionstr = "function(doc){ if( " + ifstring + " ){ emit( doc._id, doc.nsfdocument ); } }";
				//System.out.println( functionstr );
				String postdata = "{ \"language\": \"javascript\", \"views\": { \"" + viewname + "\": { \"map\": \"" + functionstr + "\" } }";
				
				//. この "lists" 部分は Cloudant dashboard からは操作できない
				//. https://qiita.com/usagi/items/ffe7b2cde9f2f8b1b7f4
				String lists = ", \"lists\": { \"" + viewname + "\": \"function( head, row ){";
				lists += " start( { 'headers': { 'content-type': 'text/html; charset=utf8' } } );";
				lists += " send( '<h1>" + viewname + "</h1><hr/><table border=\\\"1\\\">' );";
				
				lists += "  send( ' <tr>";
				JSONObject nsfview = ( JSONObject )viewJSON.get( "nsfview" );
				JSONArray columns = ( JSONArray )nsfview.get( "columns" );
				for( int j = 0; j < columns.size(); j ++ ){
					JSONObject column = ( JSONObject )columns.get( j );
					lists += "<th bgcolor=\\\"gray\\\" width=\\\"" + ( String )column.get( "width" ) + "\\\">" + ( String )column.get( "title" ) + "</th>";
				}
				lists += "</tr>' );";
				
				
				lists += " var row;";
				lists += " while( row = getRow() ){";
				lists += "  var url = '../../_show/view/';";
//				lists += "  send( ' <li><a href=\\\"' + url + row.id + '\\\">' + row.value.unid + '</a></li>' );";
				lists += "  send( ' <tr>";
				for( int j = 0; j < columns.size(); j ++ ){
					JSONObject column = ( JSONObject )columns.get( j );
					lists += "<td><a href=\\\"' + url + row.id + '\\\">' + ( row.value.items." + ( String )column.get( "itemname" ) + " ? row.value.items." + ( String )column.get( "itemname" ) + ".values : \\\"(????)\\\" ) + '</a></td>";
				}
				lists += "</tr>' );";

				lists += " }";
				
				lists += " send( '</table>' );";
				lists += "}\" }";
				
				//System.out.println( "lists = " + lists ); //. /(DB)/_design/View1/_list/View1/View1  /　（/(DB)/_design/View11）
				postdata += lists;

				//. この "shows" 部分は Cloudant dashboard からは操作できない
				String shows = ", \"shows\": { \"view\": \"(function( doc, req ){";
				shows += " if( doc && doc.nsfdocument ){";

				//shows += "  var str = '<h2>' + doc.nsfdocument.unid + '</h2><h3>' + doc.nsfdocument.noteid + '</h3>';";
				shows += "  var str = " + strv + ";";

				shows += "  return str;";
				shows += " }else{";
				shows += "  return 'empty';";
				shows += " }";
				shows += "})\"";  //. end of shows.view
				
				shows += ", \"edit\": \"(function( doc, req ){ if( doc && doc.nsfdocument ){ var str = " + stre + "; return str; }else{ return 'empty'; } })\" }"; //. end of edit

				//System.out.println( "shows = " + shows );
				postdata += shows;

				postdata += " }";
				//System.out.println( "postdata = " + postdata );
				
				//. https://236667f7-2726-49fe-acaa-02b956984020-bluemix.cloudant.com/dev_nsf2c_nsf/_design/View1/_list/View1/View1
				//. -> compilation_error("Expression does not eval to a function. (function( head, row ){ start( { 'headers': { 'content-type': 'text/html' } } ); send( '<ul>\n' ); var row; while( row = getRow() ){  send( ' <li>' + row.value.unid + '\n' ); } send( '</ul>\n' );})")

				client = HttpClients.custom().setSSLHostnameVerifier( hostnameVerifier ).build(); //HttpClients.createDefault();
				HttpPut put4 = new HttpPut( cloudant_base_url + cloudant_dbname + "/_design/" + viewname );
				put4.setHeader( "Authorization", "Basic " + new String( b64data ) );
				put4.setHeader( "Content-Type", "application/json" );
				StringEntity body_entity4 = new StringEntity( postdata, "UTF-8" ); //. "UTF-8" 必須
				put4.setEntity( body_entity4 );

				CloseableHttpResponse response4 = client.execute( put4 );
				int sc4 = response4.getStatusLine().getStatusCode();
				HttpEntity entity4 = response4.getEntity();
				String r4 = EntityUtils.toString( entity4, "UTF_8" );
				System.out.println( "put4(" + sc4 + ") = " + r4 );
				client.close();
			}
			
			//. Folder を処理 
			NodeList folderList = databaseElement.getElementsByTagName( "folder" );
			int nFolderList = folderList.getLength();
			String[] folderStr = new String[nFolderList];
			for( int i = 0; i < nFolderList; i ++ ){
				Element folderElement = ( Element )folderList.item( i );

				JSONObject folderJSON = convertViewElementToJSON( folderElement );
				folderStr[i] = folderJSON.toJSONString();

				//. データとしてポスト
				//System.out.println( "\nfolderStr[" + i + "] = " + folderStr[i] );
				client = HttpClients.custom().setSSLHostnameVerifier( hostnameVerifier ).build(); //HttpClients.createDefault();
				HttpPost post2 = new HttpPost( cloudant_base_url + cloudant_dbname );
				post2.setHeader( "Authorization", "Basic " + new String( b64data ) );
				post2.setHeader( "Content-Type", "application/json" );
				StringEntity body_entity2 = new StringEntity( folderStr[i], "UTF-8" ); //. "UTF-8" 必須
				post2.setEntity( body_entity2 );

				CloseableHttpResponse response2 = client.execute( post2 );
				int sc2 = response2.getStatusLine().getStatusCode();
				HttpEntity entity2 = response2.getEntity();
				String r2 = EntityUtils.toString( entity2, "UTF_8" );
				System.out.println( "post2(" + sc2 + ") = " + r2 );
				client.close();

				//. この時点で共有要素をマージ済み
				
				//. フォルダに含まれる文書一覧を取得
				List<String> list = new ArrayList<String>();
				JSONObject obj0 = ( JSONObject )parser.parse( folderStr[i] );
				JSONObject nsffolder0 = ( JSONObject )obj0.get( "nsfview" );
				String folder0_unid = ( String )nsffolder0.get( "unid" );
				JSONArray folder0_name = ( JSONArray )nsffolder0.get( "name" );
				String foldername = ( String )folder0_name.get( 0 );
				View folder = db.getView( foldername );
				ViewEntryCollection vec = folder.getAllEntries();
				ViewEntry entry = vec.getFirstEntry();
				while( entry != null ){
					Document doc = entry.getDocument();
					list.add( doc.getUniversalID() );
					entry = vec.getNextEntry( entry );
				}
				String[] unids = ( String[] )list.toArray( new String[0] );

				//. 選択式
				String ifstring = "doc.nsfdocument && [";
				for( int j = 0; j < unids.length; j ++ ){
					if( j > 0 ){ ifstring += ","; }
					ifstring += ("'" + unids[j] + "'");
				}
				ifstring += "].indexOf( doc.nsfdocument.unid ) > -1";
				
				//. view 時用
				String strv = "\\\"\\\"; switch( doc.nsfdocument.form ){ ";
				for( int j = 0; j < nFormList; j ++ ){
					JSONObject obj = ( JSONObject )( parser.parse( formStr[j] ) );
					JSONObject nsfform = ( JSONObject )( obj.get( "nsfform" ) );
					JSONArray names = ( JSONArray )( nsfform.get( "name" ) );
					for( int k = 0; k < names.size(); k ++ ){
						String name = ( String )( names.get( k ) );

						//. UI 定義
						String richtext = ( String )( nsfform.get( "richtext" ) );
								
						richtext = richtext.replaceAll( "\n", "" );
						richtext = richtext.replaceAll( "'", "\\'" );
						//richtext = "\\\"" + richtext + "\\\"";
								
						String[] tmpa = null;
						do{
							tmpa = TrimTextNext( richtext, new String[]{ "<field " }, "</field>" );
							if( tmpa != null && tmpa.length == 3 ){
								String fieldkind = TrimText( tmpa[1], new String[]{ "kind='" }, "'" );
								String fieldname = TrimText( tmpa[1], new String[]{ "name='" }, "'" );
								if( fieldname != null && fieldname.length() > 0 ){
									if( fieldkind.equals( "editable" ) ){
//										richtext = tmpa[0] + "\\\" + doc.nsfdocument.items." + fieldname + ".values[0] + \\\"" + tmpa[2];
//										richtext = tmpa[0] + "\\\" + doc.nsfdocument.items." + fieldname + ".values + \\\"" + tmpa[2]; //. 複数値対応
										richtext = tmpa[0] + "\\\" + ( doc.nsfdocument.items." + fieldname + " ? doc.nsfdocument.items." + fieldname + ".values : \\\"(????)\\\" ) + \\\"" + tmpa[2]; //. 値がなかった時の対応
									}else if( fieldkind.indexOf( "computed" ) > -1 ){
										String code = TrimText( tmpa[1], new String[]{ "<code ", ">" }, "</code>" );
												
										richtext = tmpa[0] + code + tmpa[2];
									}else{
										richtext = tmpa[0] + tmpa[2];
									}
								}else{
									richtext = tmpa[0] + tmpa[2];
								}
							}
						}while( tmpa != null && tmpa.length == 3 );
						
						String cs = "case \\\"" + name + "\\\": str =\\\"" + richtext + "\\\"; break;";
						strv += cs;
					}
				}
				strv += " }";
				
				//. edit 時用
				String stre = "\\\"\\\"; switch( doc.nsfdocument.form ){ ";
				for( int j = 0; j < nFormList; j ++ ){
					JSONObject obj = ( JSONObject )( parser.parse( formStr[j] ) );
					JSONObject nsfform = ( JSONObject )( obj.get( "nsfform" ) );
					JSONArray names = ( JSONArray )( nsfform.get( "name" ) );
					for( int k = 0; k < names.size(); k ++ ){
						String name = ( String )( names.get( k ) );

						//. UI 定義
						String richtext = ( String )( nsfform.get( "richtext" ) );
						
						richtext = richtext.replaceAll( "\n", "" );
						richtext = richtext.replaceAll( "'", "\\'" );
						
						String[] tmpa = null;
						do{
							tmpa = TrimTextNext( richtext, new String[]{ "<field " }, "</field>" );
							if( tmpa != null && tmpa.length == 3 ){
								//. 以下は edit 用
								String fieldkind = TrimText( tmpa[1], new String[]{ "kind='" }, "'" );
								String fieldname = TrimText( tmpa[1], new String[]{ "name='" }, "'" );
								String fieldtype = TrimText( tmpa[1], new String[]{ "type='" }, "'" );
								
								//richtext = tmpa[0] + "<input type='text' name='" + fieldname + "' value='\\\" + doc.nsfdocument.items." + fieldname + ".values + \\\" '/>" + tmpa[2];
								if( fieldname != null && fieldname.length() > 0 ){
									if( fieldkind.equals( "editable" ) ){
										//. <input> か <textarea> か
										if( fieldtype != null && fieldtype.equals( "richtext" ) ){
											richtext = tmpa[0] + "<textarea cols='100' rows='10' name='" + fieldname + "'>\\\" + ( '' + doc.nsfdocument.items." + fieldname + ".values ).split( \\\"'\\\" ).join( \\\"&#39;\\\" ) + \\\"</textarea>" + tmpa[2];
										}else{
											richtext = tmpa[0] + "<input type='text' name='" + fieldname + "' value='\\\" + ( '' + doc.nsfdocument.items." + fieldname + ".values ).split( \\\"'\\\" ).join( \\\"&#39;\\\" ) + \\\"'/>" + tmpa[2];
										}
									}else if( fieldkind.indexOf( "computed" ) > -1 ){
										String code = TrimText( tmpa[1], new String[]{ "<code ", ">" }, "</code>" );
										
										richtext = tmpa[0] + code + tmpa[2];
									}else{
										richtext = tmpa[0] + tmpa[2];
									}
								}else{
									richtext = tmpa[0] + tmpa[2];
								}
							}
						}while( tmpa != null && tmpa.length == 3 );
						
						String cs = "case \\\"" + name + "\\\": str =\\\"" + richtext + "\\\"; break;";
						stre += cs;
					}
				}
				stre += " }";

				ifstring = ifstring.replaceAll( "\"", "'" );
				ifstring = ifstring.replaceAll( "=", "==" );
				String functionstr = "function(doc){ if( " + ifstring + " ){ emit( doc._id, doc.nsfdocument ); } }";
				//System.out.println( functionstr );
				String postdata = "{ \"language\": \"javascript\", \"views\": { \"" + foldername + "\": { \"map\": \"" + functionstr + "\" } }";
				
				//. この "lists" 部分は Cloudant dashboard からは操作できない
				//. https://qiita.com/usagi/items/ffe7b2cde9f2f8b1b7f4
				String lists = ", \"lists\": { \"" + foldername + "\": \"function( head, row ){";
				lists += " start( { 'headers': { 'content-type': 'text/html; charset=utf8' } } );";
				lists += " send( '<h1>" + foldername + "</h1><hr/><table border=\\\"1\\\">' );";
				
				lists += "  send( ' <tr>";
				JSONObject nsffolder = ( JSONObject )folderJSON.get( "nsfview" ); //( JSONObject )folderJSON.get( "nsffolder" );
				JSONArray columns = ( JSONArray )nsffolder.get( "columns" );
				for( int j = 0; j < columns.size(); j ++ ){
					JSONObject column = ( JSONObject )columns.get( j );
					lists += "<th bgcolor=\\\"gray\\\" width=\\\"" + ( String )column.get( "width" ) + "\\\">" + ( String )column.get( "title" ) + "</th>";
				}
				lists += "</tr>' );";
				
				
				lists += " var row;";
				lists += " while( row = getRow() ){";
				lists += "  var url = '../../_show/view/';";
//				lists += "  send( ' <li><a href=\\\"' + url + row.id + '\\\">' + row.value.unid + '</a></li>' );";
				lists += "  send( ' <tr>";
				for( int j = 0; j < columns.size(); j ++ ){
					JSONObject column = ( JSONObject )columns.get( j );
					lists += "<td><a href=\\\"' + url + row.id + '\\\">' + ( row.value.items." + ( String )column.get( "itemname" ) + " ? row.value.items." + ( String )column.get( "itemname" ) + ".values : \\\"(????)\\\" ) + '</a></td>";
				}
				lists += "</tr>' );";

				lists += " }";
				
				lists += " send( '</table>' );";
				lists += "}\" }";
				
				//System.out.println( "lists = " + lists ); //. /(DB)/_design/View1/_list/View1/View1  /　（/(DB)/_design/View11）
				postdata += lists;

				//. この "shows" 部分は Cloudant dashboard からは操作できない
				String shows = ", \"shows\": { \"view\": \"(function( doc, req ){";
				shows += " if( doc && doc.nsfdocument ){";

				//shows += "  var str = '<h2>' + doc.nsfdocument.unid + '</h2><h3>' + doc.nsfdocument.noteid + '</h3>';";
				shows += "  var str = " + strv + ";";

				shows += "  return str;";
				shows += " }else{";
				shows += "  return 'empty';";
				shows += " }";
				shows += "})\"";  //. end of shows.view
				
				shows += ", \"edit\": \"(function( doc, req ){ if( doc && doc.nsfdocument ){ var str = " + stre + "; return str; }else{ return 'empty'; } })\" }"; //. end of edit

				//System.out.println( "shows = " + shows );
				postdata += shows;

				postdata += " }";
				//System.out.println( "postdata = " + postdata );
				
				//. https://236667f7-2726-49fe-acaa-02b956984020-bluemix.cloudant.com/dev_nsf2c_nsf/_design/View1/_list/View1/View1
				//. -> compilation_error("Expression does not eval to a function. (function( head, row ){ start( { 'headers': { 'content-type': 'text/html' } } ); send( '<ul>\n' ); var row; while( row = getRow() ){  send( ' <li>' + row.value.unid + '\n' ); } send( '</ul>\n' );})")

				client = HttpClients.custom().setSSLHostnameVerifier( hostnameVerifier ).build(); //HttpClients.createDefault();
				HttpPut put4 = new HttpPut( cloudant_base_url + cloudant_dbname + "/_design/" + foldername );
				put4.setHeader( "Authorization", "Basic " + new String( b64data ) );
				put4.setHeader( "Content-Type", "application/json" );
				StringEntity body_entity4 = new StringEntity( postdata, "UTF-8" ); //. "UTF-8" 必須
				put4.setEntity( body_entity4 );

				CloseableHttpResponse response4 = client.execute( put4 );
				int sc4 = response4.getStatusLine().getStatusCode();
				HttpEntity entity4 = response4.getEntity();
				String r4 = EntityUtils.toString( entity4, "UTF_8" );
				System.out.println( "put4(" + sc4 + ") = " + r4 );
				client.close();
			}

			/*
			//. ImageResource を処理
			NodeList imageresourceList = databaseElement.getElementsByTagName( "imageresource" );
			int nImageresourceList = imageresourceList.getLength();
			for( int i = 0; i < nImageresourceList; i ++ ){
				Element imageresourceElement = ( Element )imageresourceList.item( i );
				String imageresource_unid = getUnid( imageresourceElement );
				String imageresourceStr = ElementToXML( imageresourceElement );
				System.out.println( "imageresourceStr[" + i + "] = " + imageresourceStr );
				
				//. バイナリを取り出して保存する
				String name = imageresourceElement.getAttribute( "name" ); //. ファイル名
				if( name == null || name.length() == 0 ){
					name = imageresourceElement.getAttribute( "alias" );
				}
				if( name != null && name.length() > 0 ){
					int n = name.lastIndexOf( "." );
					if( n > -1 ){
						String ext = name.substring( n + 1 ).toLowerCase();
						try{
							NodeList imgList = imageresourceElement.getElementsByTagName( ext );
							if( imgList.getLength() > 0 ){
								Element imgElement = ( Element )imgList.item( 0 );
								String b64img = imgElement.getFirstChild().getNodeValue(); //. Base64 エンコード済みイメージ
//								String imgurl = imageresourceUpload( r, b64img, name );
							}
						}catch( Exception e ){
							e.printStackTrace();
						}
					}
				}
			}

			//. FileResource を処理
			NodeList fileresourceList = databaseElement.getElementsByTagName( "fileresource" );
			int nFileresourceList = fileresourceList.getLength();
			for( int i = 0; i < nFileresourceList; i ++ ){
				Element fileresourceElement = ( Element )fileresourceList.item( i );
				String fileresource_unid = getUnid( fileresourceElement );
//				String filename = repid + "/" + fileresource_folder + "/" + unid + ".xml";
//				WriteElementToXmlFile( filename, fileresourceElement );
			}
			*/

			//. Document を処理
			NodeList documentList = databaseElement.getElementsByTagName( "document" );
			int nDocumentList = documentList.getLength();
			for( int i = 0; i < nDocumentList; i ++ ){
				Element documentElement = ( Element )documentList.item( i );
				
				//String tmp = ElementToXML( documentElement );
				//System.out.println( "\ntmp[" + i + "] = " + tmp );
				
				JSONObject documentJSON = convertDocumentElementToJSON( documentElement );
				
				String documentStr = documentJSON.toJSONString();

				//System.out.println( "\ndocumentStr[" + i + "] = " + documentStr );

				client = HttpClients.custom().setSSLHostnameVerifier( hostnameVerifier ).build(); //HttpClients.createDefault();
				HttpPost post2 = new HttpPost( cloudant_base_url + cloudant_dbname );
				post2.setHeader( "Authorization", "Basic " + new String( b64data ) );
				post2.setHeader( "Content-Type", "application/json" );
				StringEntity body_entity2 = new StringEntity( documentStr, "UTF-8" ); //. "UTF-8" 必須
				post2.setEntity( body_entity2 );

				CloseableHttpResponse response2 = client.execute( post2 );
				int sc2 = response2.getStatusLine().getStatusCode();
				HttpEntity entity2 = response2.getEntity();
				String r2 = EntityUtils.toString( entity2, "UTF_8" );
				System.out.println( "post2(" + sc2 + ") = " + r2 );
				client.close();
			}
			
			System.out.println( "homeurl = " + cloudant_base_url + cloudant_dbname + "/_design/" + repid + "/_list/" + repid + "/" + repid );
			System.out.println( "baseurl = " + cloudant_base_url + cloudant_dbname );
		}catch( Exception e ){
			e.printStackTrace();
			repid = "" + e;
		}finally{
			NotesThread.stermThread();
		}
		
		return repid;
	}

	private String getDbnameByRepid( String dbrepid ){
		String dbname = null;
		
		try{
			//. Cloudant DB から取得する
			HostnameVerifier hostnameVerifier = NoopHostnameVerifier.INSTANCE;
			CloseableHttpClient client = HttpClients.custom().setSSLHostnameVerifier( hostnameVerifier ).build(); //HttpClients.createDefault();

			HttpGet get0 = new HttpGet( cloudant_base_url + "_all_dbs" );
			byte[] b64data = Base64.encodeBase64( ( cloudant_username + ":" + cloudant_password ).getBytes() );
			get0.setHeader( "Authorization", "Basic " + new String( b64data ) );
			CloseableHttpResponse response0 = client.execute( get0 );
			int sc0 = response0.getStatusLine().getStatusCode();
			HttpEntity entity0 = response0.getEntity();
			String r0 = EntityUtils.toString( entity0, "UTF_8" );
			//System.out.println( "get0(" + sc0 + ") = " + r0 );
			client.close();
			
			JSONParser parser = new JSONParser();
			JSONArray obj0 = ( JSONArray )parser.parse( r0 );
			boolean b = false;
			for( int i = 0; i < obj0.size() && !b; i ++ ){
				String db_name = ( String )obj0.get( i );
				
				client = HttpClients.custom().setSSLHostnameVerifier( hostnameVerifier ).build(); //HttpClients.createDefault();
				HttpGet get1 = new HttpGet( cloudant_base_url + db_name + "/" + dbrepid );
				get1.setHeader( "Authorization", "Basic " + new String( b64data ) );
				CloseableHttpResponse response1 = client.execute( get1 );
				int sc1 = response1.getStatusLine().getStatusCode();
				client.close();

				if( 200 <= sc1 && sc1 < 300 ){
					b = true;
					dbname = db_name;
				}
			}
		}catch( Exception e ){
			e.printStackTrace();
		}
		
		return dbname;
	}

	private String getViewname( String dbrepid, String viewid ){
		String viewname = "";
		
		try{
			String dbname = getDbnameByRepid( dbrepid );
			if( dbname != null ){
				//. Cloudant DB から取得する
				HostnameVerifier hostnameVerifier = NoopHostnameVerifier.INSTANCE;
				CloseableHttpClient client = HttpClients.custom().setSSLHostnameVerifier( hostnameVerifier ).build(); //HttpClients.createDefault();
				HttpGet get0 = new HttpGet( cloudant_base_url + dbname + "/" + viewid );
				byte[] b64data = Base64.encodeBase64( ( cloudant_username + ":" + cloudant_password ).getBytes() );
				get0.setHeader( "Authorization", "Basic " + new String( b64data ) );
				CloseableHttpResponse response0 = client.execute( get0 );
				int sc0 = response0.getStatusLine().getStatusCode();
				HttpEntity entity0 = response0.getEntity();
				String r0 = EntityUtils.toString( entity0, "UTF_8" );
				//System.out.println( "get0(" + sc0 + ") = " + r0 );
				client.close();
				
				JSONParser parser = new JSONParser();
				JSONObject obj0 = ( JSONObject )parser.parse( r0 );
				JSONObject nsfview = ( JSONObject )obj0.get( "nsfview" );
				if( nsfview != null ){
					JSONArray name = ( JSONArray )nsfview.get( "name" );
					viewname = ( String )name.get( 0 );
					//System.out.println( " viewname = " + viewname );
				}
			}
		}catch( Exception e ){
			e.printStackTrace();
		}
		
		return viewname;
	}

	private String sanitize( String s ){
		String r = s;
		
		try{
			r = r.replaceAll( "&", "&amp;" );
			r = r.replaceAll( "<", "&lt;" );
			r = r.replaceAll( ">", "&gt;" );
			r = r.replaceAll( "\"", "&quot;" );
			r = r.replaceAll( "'", "&#39;" );
			
			if( !r.equals( s ) ){
				s = s + "";
			}
		}catch( Exception e ){
			e.printStackTrace();
		}
		
		return r;
	}
	
	private String unsanitize( String s ){
		String r = s;
		
		try{
			r = r.replaceAll( "&#39;", "'" );
			r = r.replaceAll( "&quot;", "\"" );
			r = r.replaceAll( "&gt;", ">" );
			r = r.replaceAll( "&lt;", "<" );
			r = r.replaceAll( "&amp;", "&" );
		}catch( Exception e ){
			e.printStackTrace();
		}
		
		return r;
	}
	
	public String getUnid( Element element ){
		String unid = "";
		
		try{
			NodeList noteinfoList = element.getElementsByTagName( "noteinfo" );
			Element noteinfoElement = ( Element )noteinfoList.item( 0 );
			unid = noteinfoElement.getAttribute( "unid" );
		}catch( Exception e ){
		}

		if( unid == null || unid.length() == 0 ){
			unid = "(nounid)";
		}
		
		return unid;
	}

	public Element getRootElement( String xml ){
		Element root = null;
		
		try{
			DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = dbfactory.newDocumentBuilder();
			org.w3c.dom.Document xdoc = builder.parse( new InputSource( new StringReader( xml ) ) );
			root = xdoc.getDocumentElement();
		}catch( Exception e ){
			e.printStackTrace();
		}
		
		return root;
	}

	public Element getRootElement( File file ){
		Element root = null;
		
		try{
			
			DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = dbfactory.newDocumentBuilder();
			org.w3c.dom.Document xdoc = builder.parse( file );
			root = xdoc.getDocumentElement();
		}catch( Exception e ){
			e.printStackTrace();
		}
		
		return root;
	}

	private String ElementToXML( Element element ){
		String r = "";
		
		try{
			r = "<?xml version='1.0' ?>\n";
			r = traceWriteElement( r, element, true );
		}catch( Exception e ){
			e.printStackTrace();
		}
		
		return r;
	}

	private String traceWriteElement( String str, Node node, boolean isTop ){
		try{
			int type = node.getNodeType();  //. sharedaction の時、ここで NullPointerException（まあいいか・・）
			if( type == Node.ELEMENT_NODE ){
				//. 自分自身
				String tagname = ( ( Element )node ).getTagName();
				str += ( "<" + tagname );

				//. 属性
				NamedNodeMap attrs = node.getAttributes();
				if( attrs != null ){
					for( int i = 0; i < attrs.getLength(); i ++ ){
						Node attr = attrs.item( i );
						String attrName = attr.getNodeName();
						String attrValue = sanitize( attr.getNodeValue() );
						str += ( " " + attrName + "='" + attrValue + "'" );
					}
				}
				
				str += ( ">" );

				//. 子
				if( node.hasChildNodes() ){
					Node child = node.getFirstChild();
					str = traceWriteElement( str, child, false ) ;
				}
				
				if( type == Node.ELEMENT_NODE ){
					str += ( "</" + tagname + ">" );
				}
			}else if( type == Node.TEXT_NODE ){
				try{
					String text = node.getNodeValue();
					if( text != null ){
						str += ( sanitize( text ) );
					}
				}catch( Exception e ){
					e.printStackTrace();
				}
			}			
			
			//. 兄弟
			if( !isTop ){
				Node sib = node.getNextSibling();
				if( sib != null ){
					str = traceWriteElement( str, sib, false );
				}
			}
		}catch( Exception e ){
			e.printStackTrace();
		}
		
		return str;
	}
	
	private JSONObject convertDocumentElementToJSON( Element documentElement ){
		JSONObject json = null;
		
		try{
			JSONParser parser = new JSONParser();
			json = ( JSONObject )( parser.parse( "{}" ) );
			JSONObject nsfdocument = ( JSONObject )( parser.parse( "{}" ) );
			JSONObject attachments = ( JSONObject )( parser.parse( "{}" ) );
			int attachments_cnt = 0;

			//. form
			NamedNodeMap attrs = documentElement.getAttributes();
			if( attrs != null ){
				for( int i = 0; i < attrs.getLength(); i ++ ){
					Node attr = attrs.item( i );
					String attrName = attr.getNodeName();
					if( attrName.equals( "form" ) ){
						String form = attr.getNodeValue();
						nsfdocument.put( "form", form );
					}
				}
			}

			//. noteid, unid
			NodeList noteinfoList = documentElement.getElementsByTagName( "noteinfo" );
			Element noteinfoElement = ( Element )noteinfoList.item( 0 );
			attrs = noteinfoElement.getAttributes();
			if( attrs != null ){
				for( int i = 0; i < attrs.getLength(); i ++ ){
					Node attr = attrs.item( i );
					String attrName = attr.getNodeName();
					if( attrName.equals( "noteid" ) ){
						String noteid = attr.getNodeValue();
						nsfdocument.put( "noteid", noteid );
					}else if( attrName.equals( "unid" ) ){
						String unid = attr.getNodeValue();
						nsfdocument.put( "unid", unid );
						json.put( "_id", unid );
					}
				}
			}
			
			//. updatedby
			NodeList updatedbyList = documentElement.getElementsByTagName( "updatedby" );
			Element updatedbyElement = ( Element )updatedbyList.item( 0 );
			String updatedby = updatedbyElement.getTextContent();
			nsfdocument.put( "updatedby", updatedby );

			//. items
			JSONObject items = ( JSONObject )( parser.parse( "{}" ) );
			NodeList itemList = documentElement.getElementsByTagName( "item" );
			int n = itemList.getLength();
			for( int i = 0; i < n; i ++ ){
				Element itemElement = ( Element )itemList.item( i );
				
				JSONObject item = ( JSONObject )( parser.parse( "{}" ) );
				String itemname = "";
				attrs = itemElement.getAttributes();
				if( attrs != null ){
					for( int j = 0; j < attrs.getLength(); j ++ ){
						Node attr = attrs.item( j );
						String attrName = attr.getNodeName();
						if( attrName.equals( "name" ) ){
							itemname = attr.getNodeValue();
							//item.put( "name", itemname );
						}
					}
				}
				
				//. フィールド種類
				JSONArray values = ( JSONArray )( parser.parse( "[]" ) );
				NodeList child1List = itemElement.getChildNodes();
				Element child1Element = ( Element )child1List.item( 0 );
				String tagname1 = child1Element.getTagName();
				if( tagname1.endsWith( "list" ) ){
					//. 配列値
					NodeList child2List = child1Element.getChildNodes();
					int m = child2List.getLength();
					String itemtype = "";
					for( int j = 0; j < m; j ++ ){
						Element child2Element = ( Element )child2List.item( j );
						//String tagname2 = child2Element.getTagName();
						itemtype = child2Element.getTagName();

						JSONObject value = ( JSONObject )( parser.parse( "{}" ) );

						//. 添付ファイルのみ別扱いにする
						if( itemname.equals( "$FILE" ) ){
							NodeList fileList = child2Element.getElementsByTagName( "file" );
							Element fileElement = ( Element )fileList.item( 0 );
							String filename = fileElement.getAttribute( "name" );
							NodeList filedataList = fileElement.getElementsByTagName( "filedata" );
							Element filedataElement = ( Element )filedataList.item( 0 );
							String filedata = filedataElement.getFirstChild().getNodeValue();

							value.put( "content_type", "application/octet-stream" );  //. K.Kimura（ここをどうする？）
							value.put( "data", filedata );
							attachments.put( filename, value );
							attachments_cnt ++;
						}else{
							String kindValue = child2Element.getTextContent();
							if( itemtype.startsWith( "richtext" ) ){
								kindValue = convertRichtextToHTML( traceWriteElement( "", child2Element, true ) );
							}

							values.add( kindValue );
						}
					}
					
					item.put( "type",  itemtype );
					item.put( "values", values );
					items.put( itemname, item );
				}else{
					//. 単値
					JSONObject value = ( JSONObject )( parser.parse( "{}" ) );
					
					//. 添付ファイルのみ別扱いにする
					if( itemname.equals( "$FILE" ) ){
						NodeList fileList = child1Element.getElementsByTagName( "file" );
						Element fileElement = ( Element )fileList.item( 0 );
						String filename = fileElement.getAttribute( "name" );
						NodeList filedataList = fileElement.getElementsByTagName( "filedata" );
						Element filedataElement = ( Element )filedataList.item( 0 );
						String filedata = filedataElement.getFirstChild().getNodeValue();

						value.put( "content_type", "application/octet-stream" );  //. K.Kimura（ここをどうする？）
						value.put( "data", filedata );
						attachments.put( filename, value );
						attachments_cnt ++;
					}else{
						String kindValue = child1Element.getTextContent();
						if( tagname1.startsWith( "richtext" ) ){
							kindValue = convertRichtextToHTML( traceWriteElement( "", child1Element, true ) );
						}

						values.add( kindValue );

						item.put( "type",  tagname1 );
						item.put( "values", values );
						items.put( itemname, item );
					}
				}
			}
			
			nsfdocument.put( "items", items );
			json.put( "nsfdocument", nsfdocument );
			
			if( attachments_cnt > 0 ){
				json.put( "_attachments", attachments );
			}
		}catch( Exception e ){
			e.printStackTrace();
		}
		
		return json;
	}
	
	private JSONObject convertHelpaboutdocumentElementToJSON( Element helpaboutdocumentElement ){
		JSONObject json = null;
		
		try{
			JSONParser parser = new JSONParser();
			json = ( JSONObject )( parser.parse( "{}" ) );
			JSONObject nsfhelpaboutdocument = ( JSONObject )( parser.parse( "{}" ) );
			JSONObject atthelpaboutachments = ( JSONObject )( parser.parse( "{}" ) );
			int attachments_cnt = 0;

			//. form
			String bgcolor = "";
			NamedNodeMap attrs = helpaboutdocumentElement.getAttributes();
			if( attrs != null ){
				for( int i = 0; i < attrs.getLength(); i ++ ){
					Node attr = attrs.item( i );
					String attrName = attr.getNodeName();
					if( attrName.equals( "bgcolor" ) ){
						bgcolor = attr.getNodeValue();
					}
				}
			}

			//. noteid, unid
			NodeList noteinfoList = helpaboutdocumentElement.getElementsByTagName( "noteinfo" );
			Element noteinfoElement = ( Element )noteinfoList.item( 0 );
			attrs = noteinfoElement.getAttributes();
			if( attrs != null ){
				for( int i = 0; i < attrs.getLength(); i ++ ){
					Node attr = attrs.item( i );
					String attrName = attr.getNodeName();
					if( attrName.equals( "noteid" ) ){
						String noteid = attr.getNodeValue();
						nsfhelpaboutdocument.put( "noteid", noteid );
					}else if( attrName.equals( "unid" ) ){
						String unid = attr.getNodeValue();
						nsfhelpaboutdocument.put( "unid", unid );
						json.put( "_id", unid );
					}
				}
			}
			
			//. updatedby
			NodeList updatedbyList = helpaboutdocumentElement.getElementsByTagName( "updatedby" );
			Element updatedbyElement = ( Element )updatedbyList.item( 0 );
			String updatedby = updatedbyElement.getTextContent();
			nsfhelpaboutdocument.put( "updatedby", updatedby );

			//. body/richtext
			NodeList bodyList = helpaboutdocumentElement.getElementsByTagName( "body" );
			Element bodyElement = ( Element )bodyList.item( 0 );
			NodeList richtextList = bodyElement.getElementsByTagName( "richtext" );
			Element richtextElement = ( Element )richtextList.item( 0 );

//			String richtextValue = convertRichtextToHTML( traceWriteElement( "", richtextElement, true ) );
			String richtextstr = traceWriteElement( "", richtextElement, true );
			String tmp = TrimText( richtextstr, new String[]{ "<richtext", ">" }, "</richtext>" );
			if( tmp != null && tmp.length() > 0 ){
				String body = "<body";
				if( bgcolor != null && bgcolor.length() > 0 ){
					body += ( " bgcolor='" + bgcolor + "'" );
				}
				body += ">" + tmp + "</body>";
				richtextstr = body;
			}
			String richtextValue = convertRichtextToHTML( richtextstr );
			
			//. 添付ファイル表示用
			String script = "\n<script>function showAttachment( attachmentname ){ var t1 = location.href; var t2 = t1.split('/');  var docid = t2.splice(t2.length - 1,1)[0]; docid = docid.substring( 0, docid.length - 1 ); location = '../../../../' + docid + '/' + attachmentname;  }</script>\n";
			richtextValue += script;
			
			nsfhelpaboutdocument.put( "richtext", richtextValue );
			
			json.put( "nsfhelpdocument", nsfhelpaboutdocument );
		}catch( Exception e ){
			e.printStackTrace();
		}
		
		return json;
	}

	private JSONObject convertHelpusingdocumentElementToJSON( Element helpusingdocumentElement ){
		JSONObject json = null;
		
		try{
			JSONParser parser = new JSONParser();
			json = ( JSONObject )( parser.parse( "{}" ) );
			JSONObject nsfhelpusingdocument = ( JSONObject )( parser.parse( "{}" ) );
			JSONObject atthelpusingachments = ( JSONObject )( parser.parse( "{}" ) );
			int attachments_cnt = 0;

			//. form
			String bgcolor = "";
			NamedNodeMap attrs = helpusingdocumentElement.getAttributes();
			if( attrs != null ){
				for( int i = 0; i < attrs.getLength(); i ++ ){
					Node attr = attrs.item( i );
					String attrName = attr.getNodeName();
					if( attrName.equals( "bgcolor" ) ){
						bgcolor = attr.getNodeValue();
					}
				}
			}

			//. noteid, unid
			NodeList noteinfoList = helpusingdocumentElement.getElementsByTagName( "noteinfo" );
			Element noteinfoElement = ( Element )noteinfoList.item( 0 );
			attrs = noteinfoElement.getAttributes();
			if( attrs != null ){
				for( int i = 0; i < attrs.getLength(); i ++ ){
					Node attr = attrs.item( i );
					String attrName = attr.getNodeName();
					if( attrName.equals( "noteid" ) ){
						String noteid = attr.getNodeValue();
						nsfhelpusingdocument.put( "noteid", noteid );
					}else if( attrName.equals( "unid" ) ){
						String unid = attr.getNodeValue();
						nsfhelpusingdocument.put( "unid", unid );
						json.put( "_id", unid );
					}
				}
			}
			
			//. updatedby
			NodeList updatedbyList = helpusingdocumentElement.getElementsByTagName( "updatedby" );
			Element updatedbyElement = ( Element )updatedbyList.item( 0 );
			String updatedby = updatedbyElement.getTextContent();
			nsfhelpusingdocument.put( "updatedby", updatedby );

			//. body/richtext
			NodeList bodyList = helpusingdocumentElement.getElementsByTagName( "body" );
			Element bodyElement = ( Element )bodyList.item( 0 );
			NodeList richtextList = bodyElement.getElementsByTagName( "richtext" );
			Element richtextElement = ( Element )richtextList.item( 0 );

//			String richtextValue = convertRichtextToHTML( traceWriteElement( "", richtextElement, true ) );
			String richtextstr = traceWriteElement( "", richtextElement, true );
			String tmp = TrimText( richtextstr, new String[]{ "<richtext", ">" }, "</richtext>" );
			if( tmp != null && tmp.length() > 0 ){
				String body = "<body";
				if( bgcolor != null && bgcolor.length() > 0 ){
					body += ( " bgcolor='" + bgcolor + "'" );
				}
				body += ">" + tmp + "</body>";
				richtextstr = body;
			}
			String richtextValue = convertRichtextToHTML( richtextstr );
			
			//. 添付ファイル表示用
			String script = "\n<script>function showAttachment( attachmentname ){ var t1 = location.href; var t2 = t1.split('/');  var docid = t2.splice(t2.length - 1,1)[0]; docid = docid.substring( 0, docid.length - 1 ); location = '../../../../' + docid + '/' + attachmentname;  }</script>\n";
			richtextValue += script;
			
			nsfhelpusingdocument.put( "richtext", richtextValue );
			
			json.put( "nsfhelpdocument", nsfhelpusingdocument );
		}catch( Exception e ){
			e.printStackTrace();
		}
		
		return json;
	}

	private JSONObject convertViewElementToJSON( Element viewElement ){
		JSONObject json = null;
		
		try{
			JSONParser parser = new JSONParser();
			json = ( JSONObject )( parser.parse( "{}" ) );
			JSONObject nsfview = ( JSONObject )( parser.parse( "{}" ) );

			//. name/alias
			JSONArray viewnames = ( JSONArray )( parser.parse( "[]" ) );
			NamedNodeMap attrs = viewElement.getAttributes();
			if( attrs != null ){
				for( int i = 0; i < attrs.getLength(); i ++ ){
					Node attr = attrs.item( i );
					String attrName = attr.getNodeName();
					if( attrName.equals( "name" ) ){
						String name = attr.getNodeValue();
						viewnames.add( name );
					}else if( attrName.equals( "alias" ) ){
						String[] alias = attr.getNodeValue().split( "\\|" );
						for( int j = 0; j < alias.length; j ++ ){
							viewnames.add( alias[j].trim() );
						}
					}
				}
			}
			nsfview.put( "name", viewnames );

			//. noteid, unid
			NodeList noteinfoList = viewElement.getElementsByTagName( "noteinfo" );
			Element noteinfoElement = ( Element )noteinfoList.item( 0 );
			attrs = noteinfoElement.getAttributes();
			if( attrs != null ){
				for( int i = 0; i < attrs.getLength(); i ++ ){
					Node attr = attrs.item( i );
					String attrName = attr.getNodeName();
					if( attrName.equals( "noteid" ) ){
						String noteid = attr.getNodeValue();
						nsfview.put( "noteid", noteid );
					}else if( attrName.equals( "unid" ) ){
						String unid = attr.getNodeValue();
						nsfview.put( "unid", unid );
						json.put( "_id", unid );
					}
				}
			}
			
			//. updatedby
			NodeList updatedbyList = viewElement.getElementsByTagName( "updatedby" );
			Element updatedbyElement = ( Element )updatedbyList.item( 0 );
			String updatedby = updatedbyElement.getTextContent();
			nsfview.put( "updatedby", updatedby );
			
			//. selection formula
			JSONObject viewselection = ( JSONObject )( parser.parse( "{}" ) );
			NodeList codeList = viewElement.getElementsByTagName( "code" );
			Element codeElement = ( Element )codeList.item( 0 );
			NodeList childList = codeElement.getChildNodes();
			Element childElement = ( Element )childList.item( 0 );
			String formula = childElement.getTagName();
			String formulaValue = childElement.getTextContent(); //. "SELECT Form = \"Form1\""
			viewselection.put( formula, formulaValue );
			nsfview.put( "selection", viewselection );

			//. columns, sharedcolumns
			JSONArray columns = ( JSONArray )( parser.parse( "[]" ) );
			childList = viewElement.getChildNodes();
			int n = childList.getLength();
			for( int i = 0; i < n; i ++ ){
				try{
					childElement = ( Element )childList.item( i );
					String tagname = childElement.getTagName();
					if( tagname.equals( "column" ) || tagname.equals( "sharedcolumnref" ) ){
						if( tagname.equals( "sharedcolumnref" ) ){
							NodeList child1List = childElement.getElementsByTagName( "column" );
							childElement = ( Element )child1List.item( 0 );
						}
						
						JSONObject column = ( JSONObject )( parser.parse( "{}" ) );
						attrs = childElement.getAttributes();
						if( attrs != null ){
							for( int j = 0; j < attrs.getLength(); j ++ ){
								Node attr = attrs.item( j );
								String attrName = attr.getNodeName();
								if( attrName.equals( "itemname" ) ){
									String itemname = attr.getNodeValue();
									column.put( "itemname", itemname );
								}else if( attrName.equals( "width" ) ){
									String width = attr.getNodeValue();
									column.put( "width", width );
								}
							}
						}
						
						NodeList columnheaderList = childElement.getElementsByTagName( "columnheader" );
						Element columnheaderElement = ( Element )columnheaderList.item( 0 );
						attrs = columnheaderElement.getAttributes();
						if( attrs != null ){
							for( int j = 0; j < attrs.getLength(); j ++ ){
								Node attr = attrs.item( j );
								String attrName = attr.getNodeName();
								if( attrName.equals( "title" ) ){
									String title = attr.getNodeValue();
									column.put( "title", title );
								}
							}
						}
						
						NodeList code1List = childElement.getElementsByTagName( "code" );
						if( code1List.getLength() > 0 ){
							Element code1Element = ( Element )code1List.item( 0 );
							NodeList formulaList = code1Element.getElementsByTagName( "formula" );
							Element formulaElement = ( Element )formulaList.item( 0 );
							String codeformula = formulaElement.getTextContent();
							column.put( "formula", codeformula );
						}
						
						columns.add( column );
					}
				}catch( Exception e ){
				}
			}
			
			nsfview.put( "columns", columns );
			json.put( "nsfview", nsfview );
		}catch( Exception e ){
			e.printStackTrace();
		}
		
		return json;
	}

	private JSONObject convertSharedcolumnElementToJSON( Element sharedcolumnElement ){
		JSONObject json = null;
		
		try{
			JSONParser parser = new JSONParser();
			json = ( JSONObject )( parser.parse( "{}" ) );
			JSONObject nsfsharedcolumn = ( JSONObject )( parser.parse( "{}" ) );

			//. name / alias
			JSONArray sharedcolumnnames = ( JSONArray )( parser.parse( "[]" ) );
			NamedNodeMap attrs = sharedcolumnElement.getAttributes();
			if( attrs != null ){
				for( int i = 0; i < attrs.getLength(); i ++ ){
					Node attr = attrs.item( i );
					String attrName = attr.getNodeName();
					if( attrName.equals( "name" ) ){
						String name = attr.getNodeValue();
						sharedcolumnnames.add( name );
					}else if( attrName.equals( "alias" ) ){
						String[] alias = attr.getNodeValue().split( "\\|" );
						for( int j = 0; j < alias.length; j ++ ){
							sharedcolumnnames.add( alias[j].trim() );
						}
					}
				}
			}
			nsfsharedcolumn.put( "name", sharedcolumnnames );

			//. noteid, unid
			NodeList noteinfoList = sharedcolumnElement.getElementsByTagName( "noteinfo" );
			Element noteinfoElement = ( Element )noteinfoList.item( 0 );
			attrs = noteinfoElement.getAttributes();
			if( attrs != null ){
				for( int i = 0; i < attrs.getLength(); i ++ ){
					Node attr = attrs.item( i );
					String attrName = attr.getNodeName();
					if( attrName.equals( "noteid" ) ){
						String noteid = attr.getNodeValue();
						nsfsharedcolumn.put( "noteid", noteid );
					}else if( attrName.equals( "unid" ) ){
						String unid = attr.getNodeValue();
						nsfsharedcolumn.put( "unid", unid );
						json.put( "_id", unid );
					}
				}
			}
			
			//. updatedby
			NodeList updatedbyList = sharedcolumnElement.getElementsByTagName( "updatedby" );
			Element updatedbyElement = ( Element )updatedbyList.item( 0 );
			String updatedby = updatedbyElement.getTextContent();
			nsfsharedcolumn.put( "updatedby", updatedby );
			
			//. column
			JSONArray columns = ( JSONArray )( parser.parse( "[]" ) );
			NodeList childList = sharedcolumnElement.getChildNodes();
			int n = childList.getLength();
			for( int i = 0; i < n; i ++ ){
				try{
					Element childElement = ( Element )childList.item( i );
					String tagname = childElement.getTagName();
					if( tagname.equals( "sharedcolumnref" ) ){
						NodeList child1List = childElement.getElementsByTagName( "column" );
						childElement = ( Element )child1List.item( 0 );
						
						JSONObject column = ( JSONObject )( parser.parse( "{}" ) );
						attrs = childElement.getAttributes();
						if( attrs != null ){
							for( int j = 0; j < attrs.getLength(); j ++ ){
								Node attr = attrs.item( j );
								String attrName = attr.getNodeName();
								if( attrName.equals( "itemname" ) ){
									String itemname = attr.getNodeValue();
									column.put( "itemname", itemname );
								}else if( attrName.equals( "width" ) ){
									String width = attr.getNodeValue();
									column.put( "width", width );
								}
							}
						}
						
						NodeList columnheaderList = childElement.getElementsByTagName( "columnheader" );
						Element columnheaderElement = ( Element )columnheaderList.item( 0 );
						attrs = columnheaderElement.getAttributes();
						if( attrs != null ){
							for( int j = 0; j < attrs.getLength(); j ++ ){
								Node attr = attrs.item( j );
								String attrName = attr.getNodeName();
								if( attrName.equals( "title" ) ){
									String title = attr.getNodeValue();
									column.put( "title", title );
								}
							}
						}
						
						NodeList code1List = childElement.getElementsByTagName( "code" );
						if( code1List.getLength() > 0 ){
							Element code1Element = ( Element )code1List.item( 0 );
							NodeList formulaList = code1Element.getElementsByTagName( "formula" );
							Element formulaElement = ( Element )formulaList.item( 0 );
							String codeformula = formulaElement.getTextContent();
							column.put( "formula", codeformula );
						}
						
						columns.add( column );
					}
				}catch( Exception e ){
				}
			}
			
			nsfsharedcolumn.put( "columns", columns );
			json.put( "nsfsharedcolumn", nsfsharedcolumn );
		}catch( Exception e ){
			e.printStackTrace();
		}
		
		return json;
	}

	private JSONObject convertSharedfieldElementToJSON( Element sharedfieldElement ){
		JSONObject json = null;
		
		try{
			JSONParser parser = new JSONParser();
			json = ( JSONObject )( parser.parse( "{}" ) );
			JSONObject nsfsharedfield = ( JSONObject )( parser.parse( "{}" ) );

			//. name / alias
			JSONArray sharedfieldnames = ( JSONArray )( parser.parse( "[]" ) );
			NamedNodeMap attrs = sharedfieldElement.getAttributes();
			if( attrs != null ){
				for( int i = 0; i < attrs.getLength(); i ++ ){
					Node attr = attrs.item( i );
					String attrName = attr.getNodeName();
					if( attrName.equals( "name" ) ){
						String name = attr.getNodeValue();
						sharedfieldnames.add( name );
					}else if( attrName.equals( "alias" ) ){
						String[] alias = attr.getNodeValue().split( "\\|" );
						for( int j = 0; j < alias.length; j ++ ){
							sharedfieldnames.add( alias[j].trim() );
						}
					}
				}
			}
			nsfsharedfield.put( "name", sharedfieldnames );

			//. noteid, unid
			NodeList noteinfoList = sharedfieldElement.getElementsByTagName( "noteinfo" );
			Element noteinfoElement = ( Element )noteinfoList.item( 0 );
			attrs = noteinfoElement.getAttributes();
			if( attrs != null ){
				for( int i = 0; i < attrs.getLength(); i ++ ){
					Node attr = attrs.item( i );
					String attrName = attr.getNodeName();
					if( attrName.equals( "noteid" ) ){
						String noteid = attr.getNodeValue();
						nsfsharedfield.put( "noteid", noteid );
					}else if( attrName.equals( "unid" ) ){
						String unid = attr.getNodeValue();
						nsfsharedfield.put( "unid", unid );
						json.put( "_id", unid );
					}
				}
			}
			
			//. updatedby
			NodeList updatedbyList = sharedfieldElement.getElementsByTagName( "updatedby" );
			Element updatedbyElement = ( Element )updatedbyList.item( 0 );
			String updatedby = updatedbyElement.getTextContent();
			nsfsharedfield.put( "updatedby", updatedby );
			
			//. field
			NodeList fieldList = sharedfieldElement.getElementsByTagName( "field" );
			Element fieldElement = ( Element )fieldList.item( 0 );
			
			//. name 属性と type 属性を取り出す
			JSONObject field = ( JSONObject )( parser.parse( "{}" ) );
			attrs = fieldElement.getAttributes();
			if( attrs != null ){
				for( int i = 0; i < attrs.getLength(); i ++ ){
					Node attr = attrs.item( i );
					String attrName = attr.getNodeName();
					if( attrName.equals( "name" ) ){
						String fieldname = attr.getNodeValue();
						field.put( "name", fieldname );
					}else if( attrName.equals( "type" ) ){
						String fieldtype = attr.getNodeValue();
						field.put( "type", fieldtype );
					}else if( attrName.equals( "kind" ) ){
						String fieldkind = attr.getNodeValue();
						field.put( "kind", fieldkind );
					}
				}
			}
			nsfsharedfield.put( "field", field );
			
			json.put( "nsfsharedfield", nsfsharedfield );
		}catch( Exception e ){
			e.printStackTrace();
		}
		
		return json;
	}

	private JSONObject convertSubformElementToJSON( Element subformElement ){
		JSONObject json = null;
		
		try{
			JSONParser parser = new JSONParser();
			json = ( JSONObject )( parser.parse( "{}" ) );
			JSONObject nsfsubform = ( JSONObject )( parser.parse( "{}" ) );

			//. name / alias
			JSONArray subformnames = ( JSONArray )( parser.parse( "[]" ) );
			NamedNodeMap attrs = subformElement.getAttributes();
			if( attrs != null ){
				for( int i = 0; i < attrs.getLength(); i ++ ){
					Node attr = attrs.item( i );
					String attrName = attr.getNodeName();
					if( attrName.equals( "name" ) ){
						String name = attr.getNodeValue();
						subformnames.add( name );
					}else if( attrName.equals( "alias" ) ){
						String[] alias = attr.getNodeValue().split( "\\|" );
						for( int j = 0; j < alias.length; j ++ ){
							subformnames.add( alias[j].trim() );
						}
					}
				}
			}
			nsfsubform.put( "name", subformnames );

			//. noteid, unid
			NodeList noteinfoList = subformElement.getElementsByTagName( "noteinfo" );
			Element noteinfoElement = ( Element )noteinfoList.item( 0 );
			attrs = noteinfoElement.getAttributes();
			if( attrs != null ){
				for( int i = 0; i < attrs.getLength(); i ++ ){
					Node attr = attrs.item( i );
					String attrName = attr.getNodeName();
					if( attrName.equals( "noteid" ) ){
						String noteid = attr.getNodeValue();
						nsfsubform.put( "noteid", noteid );
					}else if( attrName.equals( "unid" ) ){
						String unid = attr.getNodeValue();
						nsfsubform.put( "unid", unid );
						json.put( "_id", unid );
					}
				}
			}
			
			//. updatedby
			NodeList updatedbyList = subformElement.getElementsByTagName( "updatedby" );
			Element updatedbyElement = ( Element )updatedbyList.item( 0 );
			String updatedby = updatedbyElement.getTextContent();
			nsfsubform.put( "updatedby", updatedby );
			
			//. body/richtext
			NodeList bodyList = subformElement.getElementsByTagName( "body" );
			Element bodyElement = ( Element )bodyList.item( 0 );
			NodeList richtextList = bodyElement.getElementsByTagName( "richtext" );
			Element richtextElement = ( Element )richtextList.item( 0 );
//			String richtextValue = richtextElement.getTextContent();
			String richtextValue = convertRichtextToHTML( traceWriteElement( "", richtextElement, true ) );
			nsfsubform.put( "richtext", richtextValue );
			
			json.put( "nsfsubform", nsfsubform );
		}catch( Exception e ){
			e.printStackTrace();
		}
		
		return json;
	}

	private JSONObject convertFormElementToJSON( Element formElement ){
		JSONObject json = null;
		
		try{
			JSONParser parser = new JSONParser();
			json = ( JSONObject )( parser.parse( "{}" ) );
			JSONObject nsfform = ( JSONObject )( parser.parse( "{}" ) );

			//. name / alias / bgcolor
			JSONArray formnames = ( JSONArray )( parser.parse( "[]" ) );
			NamedNodeMap attrs = formElement.getAttributes();
			String bgcolor = "";
			if( attrs != null ){
				for( int i = 0; i < attrs.getLength(); i ++ ){
					Node attr = attrs.item( i );
					String attrName = attr.getNodeName();
					if( attrName.equals( "name" ) ){
						String name = attr.getNodeValue();
						formnames.add( name );
					}else if( attrName.equals( "alias" ) ){
						String[] alias = attr.getNodeValue().split( "\\|" );
						for( int j = 0; j < alias.length; j ++ ){
							formnames.add( alias[j].trim() );
						}
					}else if( attrName.equals( "bgcolor" ) ){
						bgcolor = attr.getNodeValue();
					}
				}
			}
			nsfform.put( "name", formnames );

			//. noteid, unid
			NodeList noteinfoList = formElement.getElementsByTagName( "noteinfo" );
			Element noteinfoElement = ( Element )noteinfoList.item( 0 );
			attrs = noteinfoElement.getAttributes();
			if( attrs != null ){
				for( int i = 0; i < attrs.getLength(); i ++ ){
					Node attr = attrs.item( i );
					String attrName = attr.getNodeName();
					if( attrName.equals( "noteid" ) ){
						String noteid = attr.getNodeValue();
						nsfform.put( "noteid", noteid );
					}else if( attrName.equals( "unid" ) ){
						String unid = attr.getNodeValue();
						nsfform.put( "unid", unid );
						json.put( "_id", unid );
					}
				}
			}
			
			//. updatedby
			NodeList updatedbyList = formElement.getElementsByTagName( "updatedby" );
			Element updatedbyElement = ( Element )updatedbyList.item( 0 );
			String updatedby = updatedbyElement.getTextContent();
			nsfform.put( "updatedby", updatedby );
			
			//. body/richtext
			NodeList bodyList = formElement.getElementsByTagName( "body" );
			Element bodyElement = ( Element )bodyList.item( 0 );
			NodeList richtextList = bodyElement.getElementsByTagName( "richtext" );
			Element richtextElement = ( Element )richtextList.item( 0 );

//			String richtextValue = convertRichtextToHTML( traceWriteElement( "", richtextElement, true ) );
			String richtextstr = traceWriteElement( "", richtextElement, true );
			String tmp = TrimText( richtextstr, new String[]{ "<richtext", ">" }, "</richtext>" );
			if( tmp != null && tmp.length() > 0 ){
				String body = "<body";
				if( bgcolor != null && bgcolor.length() > 0 ){
					body += ( " bgcolor='" + bgcolor + "'" );
				}
				body += ">" + tmp + "</body>";
				richtextstr = body;
			}
			String richtextValue = convertRichtextToHTML( richtextstr );
			
			//. 添付ファイル表示用
			String script = "\n<script>function showAttachment( attachmentname ){ var t1 = location.href; var t2 = t1.split('/');  var docid = t2.splice(t2.length - 1,1)[0]; docid = docid.substring( 0, docid.length - 1 ); location = '../../../../' + docid + '/' + attachmentname;  }</script>\n";
			richtextValue += script;
			
			nsfform.put( "richtext", richtextValue );
			
			json.put( "nsfform", nsfform );
		}catch( Exception e ){
			e.printStackTrace();
		}
		
		return json;
	}
	
	private String convertRichtextToHTML( String richtext ){
		String r = richtext;
		String[] tmpa = null;
		int n1 = -1, n2 = -1;
		
		try{
			//. <pardef id='idx' align='xxx'>**</pardef> => <p xx>**</p>  （id と align 情報を記憶した上で無視）
			
			Map<String,String> pardefMap = new HashMap<String,String>();
			do{
				tmpa = TrimTextNext( r, new String[]{ "<pardef ", ">" }, "</pardef>" );
				if( tmpa != null && tmpa.length == 3 ){
					String pardef_attrs = TrimText( r, new String[]{ "<pardef "}, ">" );
					String pardef_id = TrimText( pardef_attrs, new String[]{ "id='" }, "'" );
					if( pardef_id != null && pardef_id.length() > 0 ){
						String pardef_align = TrimText( pardef_attrs, new String[]{ "align='" }, "'" );
						pardefMap.put( pardef_id, pardef_align );
					}

					r = tmpa[0] + tmpa[2];
				}
			}while( tmpa != null && tmpa.length == 3 );
			
			//. <par> => <p>
			do{
				tmpa = TrimTextNext( r, new String[]{ "<par " }, ">" );
				if( tmpa != null && tmpa.length == 3 ){
//					r = tmpa[0] + "<p>" + tmpa[2];
					String p = "<p";
					String par_attrs = tmpa[1];
					String par_def = TrimText( par_attrs, new String[]{ "def='" }, "'" );
					if( par_def != null && par_def.length() > 0 ){
						String align = ( String )pardefMap.get( par_def );
						if( align != null && align.length() > 0 ){
							p += ( " align='" + align + "'" );
						}
					}
					p += ">";
					r = tmpa[0] + p + tmpa[2];
				}
			}while( tmpa != null && tmpa.length == 3 );
			r = r.replaceAll( "</par>", "</p>" );

			//. <run><font xx></font>***</run> => <span><font xx>***</font></span>
			do{
				tmpa = TrimTextNext( r, new String[]{ "<run>" }, "</run>" );
				if( tmpa != null && tmpa.length == 3 ){
					//. tmpa[1] = <font xx></font>***
					String t = tmpa[1];
					n1 = t.indexOf( "<font " );
					if( n1 > -1 ){
						n2 = t.indexOf( "></font>", n1 + 1 );
						if( n2 > n1 ){
							tmpa[1] = t.substring( 0, n2 + 1 ) + t.substring( n2 + 8 ) + "</font>";
						}
					}
					r = tmpa[0] + "<span class='run'>" + tmpa[1] + "</span>" + tmpa[2];
				}
			}while( tmpa != null && tmpa.length == 3 );

			//. <font **** style='xx'> => <font *** style='font-weight: xx'>
			do{
				tmpa = TrimTextNext( r, new String[]{ "<font " }, ">" );
				if( tmpa != null && tmpa.length == 3 ){
					String _font = "";
					String[] tmpb = tmpa[1].split( " " );
					for( int i = 0; i < tmpb.length; i ++ ){
						if( tmpb[i].length() > 0 ){
							String[] tmpc = tmpb[i].split( "=" ); //. tmpc[0]: "size", "style", "color"
							if( _font.length() > 0 ){
								_font += " ";
							}
							if( tmpc[0].equals( "style" ) ){
								//. tmpc[1]: "'bold'", "'italic'", "'underline'", "'superscript'", "'strikethrough'", "'shadow'", "'emboss'"
								if( tmpc[1].equals( "'bold'" ) || tmpc[1].equals( "'normal'" ) ){
									_font += ( tmpc[0] + "='font-weight: " + tmpc[1].substring( 1 ) );
								}else if( tmpc[1].equals( "'italic'" ) || tmpc[1].equals( "'oblique'" ) ){
									_font += ( tmpc[0] + "='font-style: " + tmpc[1].substring( 1 ) );
								}else if( tmpc[1].equals( "'underline'" ) || tmpc[1].equals( "'overline'" ) || tmpc[1].equals( "'strikethrough'" ) ){
									if( tmpc[1].equals( "'strikethrough'" ) ){
										tmpc[1] = "'line-through'";
									}
									_font += ( tmpc[0] + "='text-decoration: " + tmpc[1].substring( 1 ) );
								}else if( tmpc[1].equals( "'superscript'" ) ){
									_font += ( tmpc[0] + "='vertical-align: super;'" );
								}else if( tmpc[1].equals( "'shadow'" ) ){
									_font += ( tmpc[0] + "='text-shadow: 2px 2px 1px #999999;'" );
								}else{
									_font += ( tmpc[0] + "='font-unknown: " + tmpc[1].substring( 1 ) );
								}
							}else{
								_font += ( tmpc[0] + "=" + tmpc[1] );
							}
						}else{
						}
					}
					r = tmpa[0] + "<xfont " + _font + ">" + tmpa[2];
				}
			}while( tmpa != null && tmpa.length == 3 );
			r = r.replaceAll( "<xfont ", "<font " );

			//. Tab
			/*
			do{
				tmpa = TrimTextNext( r, new String[]{ "<table " }, "</table>" );
				if( tmpa != null && tmpa.length == 3 ){
					String tmp = tmpa[1];
					if( tmp.indexOf( "rowdisplay='tabs'" ) > -1 ){
					}else{
					}
					
					r = tmpa[0] + "<xtable " + tmp + "</table>";
				}
			}while( tmpa != null && tmpa.length == 3 );
			r = r.replaceAll( "<xtable ", "<table " );
			*/

			//. <table>
			do{
				tmpa = TrimTextNext( r, new String[]{ "<table " }, "</table>" );
				if( tmpa != null && tmpa.length == 3 ){
					String tmp1 = tmpa[1];
					
					//. border
					if( tmp1.startsWith( "refwidth=" ) ){
						tmp1 = "border=" + tmp1.substring( 9 );
					}
					
					//. <tablecolumn>
					ArrayList<String> tablecolumnList = new ArrayList<String>();
					String[] tmpb = null;
					do{
						tmpb = TrimTextNext( tmp1, new String[]{ "<tablecolumn ", ">" }, "</tablecolumn>" );
						if( tmpb != null && tmpb.length == 3 ){
							String tablecolumn_attrs = TrimText( tmp1, new String[]{ "<tablecolumn "}, ">" );
							String tablecolumn_width = TrimText( tablecolumn_attrs, new String[]{ "width='" }, "'" );
							if( tablecolumn_width != null && tablecolumn_width.length() > 0 ){
								tablecolumnList.add( tablecolumn_width );
							}

							tmp1 = tmpb[0] + tmpb[2];
						}
					}while( tmpb != null && tmpb.length == 3 );
					String[] td_widths = ( String[] )tablecolumnList.toArray( new String[0] );
					
					do{
						tmpb = TrimTextNext( tmp1, new String[]{ "<tablerow" }, "</tablerow>" );
						if( tmpb != null && tmpb.length == 3 ){
							String tmp2 = tmpb[1];
							int idx = 0;
							String[] tmpc = null;
							do{
								tmpc = TrimTextNext( tmp2, new String[]{ "<tablecell" }, "</tablecell>" );
								if( tmpc != null && tmpc.length == 3 ){
									tmp2 = tmpc[0] + "<td style='width:" + td_widths[idx++] + ";'" + tmpc[1] + "</td>" + tmpc[2];
								}
							}while( tmpc != null && tmpc.length == 3 );
							
							tmp1 = tmpb[0] + "<tr" + tmp2 + "</tr>" + tmpb[2];
						}
					}while( tmpb != null && tmpb.length == 3 );
					
					r = tmpa[0] + "<xtable " + tmp1 + "</table>" + tmpa[2];
				}
			}while( tmpa != null && tmpa.length == 3 );
			r = r.replaceAll( "<xtable ", "<table " );

			//. 各種リンク
			//. DBリンク
			do{
				tmpa = TrimTextNext( r, new String[]{ "<databaselink " }, "</databaselink>" );
				if( tmpa != null && tmpa.length == 3 ){
					String dbrepid = TrimText( tmpa[1], new String[]{ "database='"}, "'" );
					if( dbrepid != null && dbrepid.length() > 0 ){
						String desc = TrimText( tmpa[1], new String[]{ "description='"}, "'" );
						String dbname = getDbnameByRepid( dbrepid ); //cloudant_dbname;
						if( dbname == null ){ dbname = cloudant_dbname; }
						String dbimg = "<img src='../../../../../" + dbname + "/" + dbrepid + "/dblink.png'/>";
						String a = "<a target='_blank' title='" + desc + "' href='/" + dbname + "/_design/" + dbrepid + "/_list/" + dbrepid + "/" + dbrepid + "'/>" + dbimg + "</a>";
						r = tmpa[0] + a + tmpa[2];
					}else{
						r = tmpa[0] + tmpa[2];
					}
				}
			}while( tmpa != null && tmpa.length == 3 );

			//. Viewリンク
			do{
				tmpa = TrimTextNext( r, new String[]{ "<viewlink " }, "</viewlink>" );
				if( tmpa != null && tmpa.length == 3 ){
					String dbrepid = TrimText( tmpa[1], new String[]{ "database='"}, "'" );
					if( dbrepid != null && dbrepid.length() > 0 ){
						String desc = TrimText( tmpa[1], new String[]{ "description='"}, "'" );
						String view_id = TrimText( tmpa[1], new String[]{ "view='"}, "'" );
						String dbname = getDbnameByRepid( dbrepid ); //cloudant_dbname;
						if( dbname == null ){ dbname = cloudant_dbname; }
						String viewname = getViewname( dbrepid, view_id );
						String viewimg = "<img src='../../../../../" + dbname + "/" + dbrepid + "/viewlink.png'/>";
						String a = "<a target='_blank' title='" + viewname + "' href='/" + dbname + "/_design/" + viewname + "/_list/" + viewname + "/" + viewname + "'/>" + viewimg + "</a>";
						r = tmpa[0] + a + tmpa[2];
					}else{
						r = tmpa[0] + tmpa[2];
					}
				}
			}while( tmpa != null && tmpa.length == 3 );

			//. Docリンク
			do{
				tmpa = TrimTextNext( r, new String[]{ "<doclink " }, "</doclink>" );
				if( tmpa != null && tmpa.length == 3 ){
					String dbrepid = TrimText( tmpa[1], new String[]{ "database='"}, "'" );
					if( dbrepid != null && dbrepid.length() > 0 ){
						String desc = TrimText( tmpa[1], new String[]{ "description='"}, "'" );
						String view_id = TrimText( tmpa[1], new String[]{ "view='"}, "'" );
						String doc_id = TrimText( tmpa[1], new String[]{ "document='"}, "'" );
						String dbname = getDbnameByRepid( dbrepid ); //cloudant_dbname;
						if( dbname == null ){ dbname = cloudant_dbname; }
						String viewname = getViewname( dbrepid, view_id );
						String docimg = "<img src='../../../../../" + dbname + "/" + dbrepid + "/doclink.png'/>";
						String a = "<a target='_blank' title='" + desc + "' href='/" + dbname + "/_design/" + viewname + "/_show/view/" + doc_id + "'/>" + docimg + "</a>";
						r = tmpa[0] + a + tmpa[2];
					}else{
						r = tmpa[0] + tmpa[2];
					}
				}
			}while( tmpa != null && tmpa.length == 3 );

			//. URLリンク
			do{
				tmpa = TrimTextNext( r, new String[]{ "<urllink " }, "</urllink>" );
				if( tmpa != null && tmpa.length == 3 ){
					String href = TrimText( tmpa[1], new String[]{ "href='"}, "'" );
					if( href != null && href.length() > 0 ){
						int n = tmpa[1].indexOf( ">" );
						String desc = tmpa[1].substring( n + 1 );
						String a = "<a target='_blank' href='" + href + "'/>" + desc + "</a>";
						r = tmpa[0] + a + tmpa[2];
					}else{
						r = tmpa[0] + tmpa[2];
					}
				}
			}while( tmpa != null && tmpa.length == 3 );

			//. <compositedata ></compositedata> => ''
			do{
				tmpa = TrimTextNext( r, new String[]{ "<compositedata ", ">" }, "</compositedata>" );
				if( tmpa != null && tmpa.length == 3 ){
					r = tmpa[0] + tmpa[2];
				}
			}while( tmpa != null && tmpa.length == 3 );

			//. <picture> => base64 inline image
			do{
				tmpa = TrimTextNext( r, new String[]{ "<picture " }, "</picture>" );
				if( tmpa != null && tmpa.length == 3 ){
					//. tmpa[1] : width, height, picturedata
					String width = TrimText( tmpa[1], new String[]{ "width='" }, "'" );
					String height = TrimText( tmpa[1], new String[]{ "height='" }, "'" );

//					String imagetype = TrimText( tmpa[1], new String[]{ "<" }, ">" );
					String imagetype = null;
					String imagetype_ = TrimText( tmpa[1], new String[]{ "<jpeg" }, "</jpeg>" );
					if( imagetype_ != null && imagetype_.length() > 0 ){
						imagetype = "jpeg";
					}else{
						imagetype_ = TrimText( tmpa[1], new String[]{ "<gif" }, "</gif>" );
						if( imagetype_ != null && imagetype_.length() > 0 ){
							imagetype = "gif";
						}else{
							imagetype_ = TrimText( tmpa[1], new String[]{ "<png" }, "</png>" );
							if( imagetype_ != null && imagetype_.length() > 0 ){
								imagetype = "png";
							}
						}
					}

					if( imagetype != null ){
						String picturedata = TrimText( tmpa[1], new String[]{ "<" + imagetype, ">" }, "<" );
						picturedata = picturedata.replaceAll( "\n", "" ); //. K.Kimura
						if( picturedata != null && picturedata.length() > 0 ){
							String img = "<img src='data:image/" + imagetype + ";base64," + picturedata + "'";
							if( width != null && width.length() > 0 ){
								img += ( " width='" + width + "'" );
							}
							if( height != null && height.length() > 0 ){
								img += ( " height='" + height + "'" );
							}
							img += "/>";
							r = tmpa[0] + img + tmpa[2];
						}else{
							r = tmpa[0] + tmpa[2];
						}
					}else{
						r = tmpa[0] + tmpa[2];
					}
				}
			}while( tmpa != null && tmpa.length == 3 );
			
			//. <attachmentref name='xxx'></attachmentref> => <a href='../../../../(doc.id)/xxx'>xxx</a>
			do{
				tmpa = TrimTextNext( r, new String[]{ "<attachmentref ", ">" }, "</attachmentref>" );
				if( tmpa != null && tmpa.length == 3 ){
					//. 上の <picture> の処理で添付ファイルとして表示されているアイコン画像は tmpa[1] に入っている
					String iconimg = tmpa[1];
					iconimg = iconimg.replace( "<img ", "<img border='1' " ); //. 添付ファイル画像アイコンにはボーダーを付ける
					
					String tmpb = TrimText( r, new String[]{ "<attachmentref " }, ">" );
					String attachmentname = TrimText( tmpb, new String[]{ "name='" }, "'" );
					if( attachmentname != null && attachmentname.length() > 0 ){
						String a = "<a title='" + attachmentname + "' href='#' onClick='showAttachment( \"" + attachmentname + "\" )'>" + iconimg + "<br/>" + attachmentname + "</a>";
						r = tmpa[0] + a + tmpa[2];
					}else{
						r = tmpa[0] + tmpa[2];
					}
				}
			}while( tmpa != null && tmpa.length == 3 );
		}catch( Exception e ){
			e.printStackTrace();
		}
		
		return r;
	}
	
	private String mergeSharedfieldsIntoSubform( String[] sharedfieldJSONs, String subformJSON ){
		String r = subformJSON;
		String[] tmpa = null;
		
		try{
			JSONParser parser = new JSONParser();
			do{
				tmpa = TrimTextNext( r, new String[]{ "<sharedfieldref " }, "<\\/sharedfieldref>" );
				if( tmpa != null && tmpa.length == 3 ){
					String name1 = TrimText( tmpa[1], new String[]{ "name='" }, "'" );
					if( name1 != null && name1.length() > 0 ){
						boolean b = false;
						for( int i = 0; i < sharedfieldJSONs.length && !b; i ++ ){
							JSONObject obj0 = ( JSONObject )parser.parse( sharedfieldJSONs[i] );
							JSONObject obj1 = ( JSONObject )obj0.get( "nsfsharedfield" );
							JSONArray obj2 = ( JSONArray )obj1.get( "name" );
							int m = obj2.size();
							for( int j = 0; j < m && !b; j ++ ){
								String name2 = ( String )obj2.get( j );
								if( name1.equals( name2 ) ){
									JSONObject obj3 = ( JSONObject )obj1.get( "field" );
									String fieldtype = ( String )obj3.get( "type" );
									String field = "<field kind='" + ( String )obj3.get( "kind" ) + "' name='" + name1 + "'><" + fieldtype + "><\\/" + fieldtype + "><\\/field>";
									r = tmpa[0] + field + tmpa[2];
									b = true;
								}
							}
						}
					}else{
						r = tmpa[0] + tmpa[2];
					}
				}
			}while( tmpa != null && tmpa.length == 3 );
		}catch( Exception e ){
			e.printStackTrace();
		}
		
		return r;
	}

	private String mergeSubformsIntoForm( String[] subformJSONs, String formJSON ){
		String r = formJSON;
		String[] tmpa = null;
		
		try{
			JSONParser parser = new JSONParser();
			do{
				tmpa = TrimTextNext( r, new String[]{ "<subformref " }, "<\\/subformref>" );
				if( tmpa != null && tmpa.length == 3 ){
					String name1s = TrimText( tmpa[1], new String[]{ "name='" }, "'" );  //. 'aaa | bbbb'
					if( name1s != null && name1s.length() > 0 ){
						boolean b = false;
						String[] name1 = name1s.split( "\\|" );
						for( int k = 0; k < name1.length; k ++ ){
							for( int i = 0; i < subformJSONs.length && !b; i ++ ){
								JSONObject obj0 = ( JSONObject )parser.parse( subformJSONs[i] );
								JSONObject obj1 = ( JSONObject )obj0.get( "nsfsubform" );
								JSONArray obj2 = ( JSONArray )obj1.get( "name" );
								int m = obj2.size();
								for( int j = 0; j < m && !b; j ++ ){
									String name2 = ( String )obj2.get( j );
									if( name1[k].trim().equals( name2 ) ){
										String richtext = ( String )obj1.get( "richtext" );
										richtext = richtext.replaceAll( "\\n", "\\\\n" ); //. K.Kimura
										r = tmpa[0] + richtext + tmpa[2];
										b = true;
									}
								}
							}
						}
					}else{
						r = tmpa[0] + tmpa[2];
					}
				}
			}while( tmpa != null && tmpa.length == 3 );
		}catch( Exception e ){
			e.printStackTrace();
		}
		
		return r;
	}

	private String mergeSharedfieldsIntoForm( String[] sharedfieldJSONs, String formJSON ){
		String r = formJSON;
		String[] tmpa = null;
		
		try{
			JSONParser parser = new JSONParser();
			do{
				tmpa = TrimTextNext( r, new String[]{ "<sharedfieldref " }, "<\\/sharedfieldref>" );
				if( tmpa != null && tmpa.length == 3 ){
					String name1 = TrimText( tmpa[1], new String[]{ "name='" }, "'" );
					if( name1 != null && name1.length() > 0 ){
						boolean b = false;
						for( int i = 0; i < sharedfieldJSONs.length && !b; i ++ ){
							JSONObject obj0 = ( JSONObject )parser.parse( sharedfieldJSONs[i] );
							JSONObject obj1 = ( JSONObject )obj0.get( "nsfsharedfield" );
							JSONArray obj2 = ( JSONArray )obj1.get( "name" );
							int m = obj2.size();
							for( int j = 0; j < m && !b; j ++ ){
								String name2 = ( String )obj2.get( j );
								if( name1.equals( name2 ) ){
									JSONObject obj3 = ( JSONObject )obj1.get( "field" );
									String fieldtype = ( String )obj3.get( "type" );
									String field = "<field kind='" + ( String )obj3.get( "kind" ) + "' name='" + name1 + "'><" + fieldtype + "><\\/" + fieldtype + "><\\/field>";
									r = tmpa[0] + field + tmpa[2];
									b = true;
								}
							}
						}
					}else{
						r = tmpa[0] + tmpa[2];
					}
				}
			}while( tmpa != null && tmpa.length == 3 );
		}catch( Exception e ){
			e.printStackTrace();
		}
		
		return r;
	}
	
	
	private String TrimText( String src, String[] s1, String s2 ){
		String r = src;
		
		try{
			int m = 0;
			int l = s1.length;
			for( int i = 0; i < l && m >= 0; i ++ ){
				int n = src.indexOf( s1[i], m );
				if( n < 0 ){
					m = n;
				}else{
					m = n + s1[i].length();
				}
			}
			if( m > 0 ){
				int n = src.indexOf( s2, m );
				if( n > m ){
					r = src.substring( m, n );
				}else{
					r = "";
				}
			}else{
				r = "";
			}
		}catch( Exception e ){
			e.printStackTrace();
			r = "";
		}
		
		return r;
	}
	
	private String[] TrimTextNext( String src, String[] s1, String s2 ){
		String[] r = null;
		
		try{
			int m = 0;
			int l = s1.length;
			for( int i = 0; i < l && m >= 0; i ++ ){
				int n = src.indexOf( s1[i], m );
				if( n < 0 ){
					m = n;
				}else{
					m = n + s1[i].length();
				}
			}
			if( m > 0 ){
				int n = src.indexOf( s2, m );
				if( n >= m ){
					r = new String[3];
					int m0 = src.indexOf( s1[0] );
					r[0] = src.substring( 0, m0 );
					r[1] = src.substring( m, n );
					r[2] = src.substring( n + s2.length() );
				}
			}
		}catch( Exception e ){
			e.printStackTrace();
		}
		
		return r;
	}
}
