package calliope;
import java.io.FileInputStream;
import java.io.File;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.dom.DOMSource;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import org.w3c.dom.Document;

public class AeseTransformer
{
    private static String readFile( String file )
	{
		try
		{
			File f = new File( file );
			int len = (int)f.length();
			byte[] data = new byte[len];
			FileInputStream fis = new FileInputStream(f);
			fis.read( data );
			fis.close();
			return new String( data, "UTF-8");
		}
		catch ( Exception e )
		{
			return "";
		}
	}
    /**
     * Perform an XSLT transform using pure Java
     * @param xsl the contents of the XSLT file
     * @param xml the contents of the XML file
     * @return the transformed XML
     */
	public String transform( String xsl, String xml )
	{
		try
		{
			long start = System.currentTimeMillis();
            ByteArrayInputStream xslBais = new ByteArrayInputStream( 
				xsl.getBytes("UTF-8") );
			ByteArrayInputStream xmlBais = new ByteArrayInputStream( 
				xml.getBytes("UTF-8") );
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
		    Document document = builder.parse(xmlBais);
		    // Use a Transformer for output
		    TransformerFactory tFactory = TransformerFactory.newInstance();
		    StreamSource stylesource = new StreamSource(xslBais);
		    Transformer transformer = tFactory.newTransformer(stylesource);
	        DOMSource source = new DOMSource(document);
			StringWriter sw = new StringWriter();
	        StreamResult result = new StreamResult(sw);
	        transformer.transform(source, result);
			return sw.toString();
		}
		catch ( Exception e )
		{
			e.printStackTrace( System.out );
			return "<html><body>"+e.getMessage()+"</body></html>";
		}
	}
	public static void main( String[] args )
	{
		if ( args.length==2 )
		{
			String xsl = readFile( args[0] );
			String xml = readFile( args[1] );
			String html = new AeseTransformer().transform( xsl, xml );
			System.out.println( html );
		}
		else
			System.out.println("usage: java AeseTransformer xsl-file xml-file");
	}
}
