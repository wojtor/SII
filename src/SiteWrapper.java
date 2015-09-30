import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import org.jsoup.Jsoup;


public class SiteWrapper {

	/**
	 * @param args
	 * @return 
	 * @throws IOException 
	 */
	public static String getTextSite(String uri) throws IOException {
		//URL url = new URL("http://www.harvard.edu/");
		System.out.println("CMR> Trying to wrap the given WebSite");
		URL url = new URL(uri);
        InputStream in =url.openStream();
        StringBuffer sb = new StringBuffer();
        
        
        byte [] buffer = new byte[1024];

        while(true){
            int byteRead = in.read(buffer);
            if(byteRead == -1)
                break;
            for(int i = 0; i < byteRead; i++){
                sb.append((char)buffer[i]);
                if(i%32768==0)
                	System.out.print(". ");
            }
        }
        System.out.print("\n");
        String text = sb.toString();
        //System.out.println(text);
        text = text.replaceAll("&nbsp;", " ");
        text = text.replaceAll("\\s\\s*", " ");
        text = text.replaceAll("\\s[a-zA-Z][a-zA-Z]\\s", " ");
		text=text.replaceAll("\\s[a-zA-Z]\\s", " ");
		text=text.replaceAll("<span>", "");
		text=text.replaceAll("</span>", "");
        //System.out.println("_____________\n"+cleanHTML(text));
        return cleanHTML(text);
	}
	
	private static String cleanHTML(String htmlText) {
		return Jsoup.parse(htmlText).text();
	}

}
