import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import com.echonest.api.v4.Artist;
import com.echonest.api.v4.EchoNestAPI;

public class ContextualMusicRecommendation {
	private final static String console = "CMR> ";
	private final static String welcome = console+"Welcome!\n" +
			console+"This java project (Contextual Music Recommendation) is the result of work\n" +
			console+"on the project assign during the course of\n" +
			console+"Sistemi Intelligenti per Internet of Roma3 University\n" +
			console+"made by Tomasz Wojtowicz, 421494\n" +
			console+"__________________________________________________________________________";
	/**
	 * @param args
	 * @throws Exception 
	 */
	
	
	
	public static void main(String[] args) throws Exception {
		System.err.println(welcome);
		Path currentRelativePath = Paths.get("");
		String model = currentRelativePath.toAbsolutePath().toString()+"\\musicClassifier.model";
		File modelFile = new File(model);
		
		InputStreamReader tastiera = new InputStreamReader(System.in);
		BufferedReader buffer = new BufferedReader(tastiera);
		
		//artists file
		boolean f = new File(currentRelativePath.toAbsolutePath().toString()+"\\artists.txt").isFile();
		//data raw file
		boolean g = new File(currentRelativePath.toAbsolutePath().toString()+"\\data.txt").isFile();
		
		int instance_number =0;
		if(!modelFile.isFile()){
			System.out.println(console+"Classifier Model doesnt exist");
			System.out.println(console+"Specify number of instances you want to use");
			System.out.println(console+"Max number 969, min number 2");
			System.out.println(console+"Recommended number is at least 200");
			instance_number = Integer.valueOf(buffer.readLine());
		}
		if(instance_number<=1)
			instance_number=2;
		if(instance_number>=967)
			instance_number=969;
		
		
		/*	Build Classifier directly from Spotify echonest api
		*	it may take time... max 20 accesses per minute
		*/
		fromSpotifyBuilder(modelFile, f, g, instance_number);
		
		
		/*	Build Classifier directly from data.txt and artists.txt
		*	without using of Spotify echonest api
		*/
		fromPrecompiledDocsBuilder(currentRelativePath, modelFile, f, g,
				instance_number);
		
		
		//String page_to_classify="The talk and talks about snoop dogg and drop it song";
		/*User interaction: write url site, exit 
		*/
		talkWithMe(modelFile, buffer);
	}

	private static void talkWithMe(File modelFile, BufferedReader buffer)
			throws IOException {
		boolean end = false;
		while(!end){
			boolean problem = false;
			System.out.println(console+"write the URL of site to classify");
			String page_to_classify= buffer.readLine();
			try{
			page_to_classify = SiteWrapper.getTextSite(page_to_classify);
			System.out.println(console+"Web Site has :" +page_to_classify.split(" ").length+" tokens");
			} catch (java.net.UnknownHostException e){
				problem = true;
				System.err.println(console+"Unknown Host Exception, retry please");
			}
			catch (java.net.MalformedURLException e){
				problem = true;
				System.err.println(console+"Malformed Url, be sure to use http or https protocol");
			}
			catch (java.io.IOException e){
				problem = true;
				System.err.println(console+"This WebSite cant be wrapped, internal protection");
			}
			if(page_to_classify.length()<1 || problem){
				System.out.println(console+"Sorry, impossible to get information abuot this website");
				problem = false;
			} else {
			System.out.println(console+"Classifing. . .");
			MusicClassifier.run(page_to_classify.toLowerCase(), "",-1);
			System.out.println(console+"write 'exit' to exit");
			}
			if(page_to_classify.equals("exit")){
				end=true;
				System.out.println(console+"Bye Bye ;)");
				modelFile.deleteOnExit();
			}
		}
	}

	private static void fromPrecompiledDocsBuilder(Path currentRelativePath,
			File modelFile, boolean f, boolean g, int instance_number)
			throws FileNotFoundException {
		if(!modelFile.isFile() && f && g){
			List<DataRaw> ldr = getListFromDatas(currentRelativePath);
			Iterator<DataRaw> it = ldr.iterator();
			int j = 1;
			while(it.hasNext()){
				DataRaw training = (DataRaw) it.next();
				if(j%10==0)
					System.out.println(console+" ...updated training set with "+j +" istances");
				MusicClassifier.run(training.getTextPrecompiled().toLowerCase(), training.getArtist(),instance_number);
				j++;
				if(j>instance_number)
					break;
			}
			System.out.println(console+"End of training set");
		}
	}

	private static void fromSpotifyBuilder(File modelFile, boolean f,
			boolean g, int instance_number) throws Exception {
		if(!modelFile.isFile() && !f && !g){
			List<DataRaw> ldr = init(instance_number);
			Iterator<DataRaw> it = ldr.iterator();
			int j = 1;
			while(it.hasNext()){
				DataRaw training = (DataRaw) it.next();
				System.out.println(console+j+" ...updating training set");
				MusicClassifier.run(training.getText().toLowerCase(), training.getArtist(),instance_number);
				j++;
			}
			System.out.println(console+"End of training set");
		}
	}

	private static List<DataRaw> getListFromDatas(Path path) throws FileNotFoundException {
		List<DataRaw> ldr = new ArrayList<DataRaw>();
		File data = new File(path.toAbsolutePath().toString()+"\\data.txt");
		File art = new File(path.toAbsolutePath().toString()+"\\artists.txt");
		Scanner d = new Scanner(data);
		Scanner a = new Scanner(art);
		while(d.hasNextLine() && a.hasNextLine()){
			DataRaw tmp = new DataRaw();
			tmp.setText(d.nextLine());
			tmp.setArtist(a.nextLine());
			ldr.add(tmp);
		}
		d.close();
		a.close();
		return ldr;
	}

	private static List<DataRaw> init(int instance_number) throws Exception{
		List<DataRaw> ldr = new ArrayList<DataRaw>();
		EchoNestAPI echoNest = new EchoNestAPI("LXDFSCNCNP8N662AW");
		List<Artist> artists = echoNest.topHotArtists(instance_number);
		int i = 1;
		String tmp= "";
		if (artists.size() > 0) {
			for (Artist artist : artists) {
				DataRaw dr = new DataRaw(); 
				dr.setArtist(artist.getName());
				dr.setNews(artist.getNews());
				dr.setSongs(artist.getSongs());
				dr.setBiog(artist.getBiographies());
				dr.setText(dr.getText());
				ldr.add(dr);
				writeClassFile(dr.getArtist());
				if(i==1)
					tmp=tmp+dr.getText().toLowerCase();
				if(i!=1)
					tmp=tmp+"\n"+dr.getText().toLowerCase();
				System.out.println(i);
				i++;
				if(i%6==0)
					Thread.sleep(59000);	//max 20 requests per minute
			}
			
		}
		writeDataFile(tmp);
		return ldr;
	}
	
	private static void writeDataFile(String text) throws IOException{
		Path currentRelativePath = Paths.get("");
		String data = currentRelativePath.toAbsolutePath().toString()+"\\data.txt";
		File art = new File(data);
		if(!art.isFile())
			art.createNewFile();
		/*if(art.isFile()){
			art.delete();
			art.createNewFile();
		}*/
		try {
			PrintWriter writer = new PrintWriter(
				new BufferedWriter(
					new FileWriter(data,
						false)));
			
				writer.print(text);
				writer.flush();
				writer.close();
				
	    }
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	private static void writeClassFile(String artists) throws IOException{
		Path currentRelativePath = Paths.get("");
		String data = currentRelativePath.toAbsolutePath().toString()+"\\artists.txt";
		String tmp="";
		File art = new File(data);
		if(!art.isFile())
			art.createNewFile();
		/*if(art.isFile()){
			art.delete();
			art.createNewFile();
		}*/
		Scanner sc = new Scanner(new File(data));
		while(sc.hasNextLine()){
			tmp=tmp+sc.nextLine()+"\n";
		}
		tmp=tmp+artists;
		sc.close();
		
		try {
			PrintWriter writer = new PrintWriter(
				new BufferedWriter(
					new FileWriter(data,
						false)));
			
				writer.print(tmp);
				writer.flush();
				writer.close();
				
	    }
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
