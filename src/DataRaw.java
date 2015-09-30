import java.util.List;

import com.echonest.api.v4.Biography;
import com.echonest.api.v4.News;
import com.echonest.api.v4.Song;

public class DataRaw {
	private String text;
	private String artist;
	private List<Song> songs;
	private List<News> news;
	private List<Biography> biog;
	
	public String getTextPrecompiled() {
		this.text = this.text.replaceAll("\\s[a-zA-Z][a-zA-Z]\\s", " ");
		this.text = this.text.replaceAll("\\s[a-zA-Z]\\s", " ");
		this.text = this.text.replaceAll("<span>", "");
		this.text = this.text.replaceAll("</span>", "");
		return text;
	}
	
	public String getText() {
		String newS = "";
		String songS = "";
		String biography = "";
		if(news.size()>0 && news!=null){
			int newSize = news.size();
			if(newSize<=1)
			newS = news.get(news.size()-1).getSummary();
			if(newSize>1)
				newS = news.get(news.size()-1).getSummary()+" "+news.get(news.size()-2).getSummary();
		}
		if(songs.size()>0 && songs!=null){
			int songSize = songs.size();
			if(songSize>2)
				songS = songs.get(0).getTitle()+" "+songs.get(1).getTitle()+" "+songs.get(2).getTitle();
			if(songSize<2)
				songS = songs.get(0).getTitle();
		}
		if(biog.size()>0 && biog!=null){
			biography = biog.get(biog.size()-1).getText().replaceAll("\n", " ");
		}
		this.text=artist+" songs of "+artist+" :"+songS+" "+newS+" "+biography+" "+artist;
		this.text=text.replaceAll("\\s[a-zA-Z][a-zA-Z]\\s", " ");
		this.text=text.replaceAll("\\s[a-zA-Z]\\s", " ");
		this.text = this.text.replaceAll("<span>", "");
		this.text = this.text.replaceAll("</span>", "");
		return text;
	}
	
	
	public List<Biography> getBiog() {
		return biog;
	}

	public void setBiog(List<Biography> biog) {
		this.biog = biog;
	}

	public void setText(String text) {
		this.text = text;
	}
	public String getArtist() {
		return artist;
	}
	public void setArtist(String artist) {
		this.artist = artist;
	}
	public List<Song> getSongs() {
		return songs;
	}
	public void setSongs(List<Song> songs) {
		this.songs = songs;
	}
	public List<News> getNews() {
		return news;
	}
	public void setNews(List<News> news) {
		this.news = news;
	}

}
