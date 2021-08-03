

import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class StreamingAppI implements Demo.StreamingApp{

        private MediaPlayerFactory mediaPlayerFactory;
		private EmbeddedMediaPlayer mediaPlayer;
        String media ;
		private String options = formatHttpStreamVideo("192.168.1.19", 5555);
		private Thread t;
        private ArrayList<String> fichiers;
    
    public void printString(String s, com.zeroc.Ice.Current current){
        System.out.println(s);
        initSongs();
    }


    public ArrayList<String> getAllSong(String type, com.zeroc.Ice.Current current){
        System.out.println("Test getAllSong");
        ArrayList<String> songs = new ArrayList<String>();

        if(type.equals("musique")){
            for(int i = 0 ; i < fichiers.size(); i++){
                if(fichiers.get(i).contains(".mp3"))
                    songs.add(fichiers.get(i).substring(0, fichiers.get(i).length() - 4));
            } 
        }else if(type.equals("video")){
            for(int i = 0 ; i < fichiers.size(); i++){
                if(fichiers.get(i).contains(".mp4"))
                    songs.add(fichiers.get(i).substring(0, fichiers.get(i).length() - 4));
            } 
        }

        return songs;
    }

    public String playStream(String song, com.zeroc.Ice.Current current){

        System.out.println("Test playSteam");
        boolean found = new NativeDiscovery().discover();
		//System.out.println(found);
		//System.out.println(LibVlc.INSTANCE.libvlc_get_version());

		mediaPlayerFactory = new MediaPlayerFactory();
		mediaPlayer = mediaPlayerFactory.newEmbeddedMediaPlayer();
        //String media = "server\\songs\\";

        //pour recuperer le nom du fichier de la musqiue ou la video 
        for(String s: fichiers){
            if(song.equals(s.substring(0, s.length() - 4))){
                media = "server\\songs\\"+s;
            }
        }

		t = new Thread() {
			public void run() {
				mediaPlayer.playMedia(media,options);
				try {
					Thread.currentThread().join();
				} catch (InterruptedException e) {
					System.out.println("Stream stoped");
				}
			}
		};
		t.start();

        return "http://192.168.1.19:5555";

    }

    public void pauseStream(com.zeroc.Ice.Current current){

        System.out.println("Test pauseStream");
        mediaPlayer.pause();

    }

    public void stopStream(com.zeroc.Ice.Current current){

        System.out.println("Test stopStream");
        mediaPlayer.stop();
        mediaPlayerFactory.release();
        t.interrupt();

    }

    public void resumeStream(com.zeroc.Ice.Current current){

        System.out.println("Test resumeStream");
        mediaPlayer.play();

    }

    private static String formatHttpStreamVideo(String serverAddress, int serverPort) {
		StringBuilder sb = new StringBuilder(60);
		sb.append(":sout=#duplicate{dst=std{access=http,mux=ts,");
		sb.append("dst=");
		sb.append(serverAddress);
		sb.append(':');
		sb.append(serverPort);
		sb.append("}}");
		return sb.toString();
	}

    public void initSongs(){
        File dir = new File("server\\songs");
        File[] dirs = dir.listFiles();

        fichiers = new ArrayList<String>();

        for(int i = (dirs.length - 1); i >= 0; i--) {
            fichiers.add(dirs[i].getName());
        }
    }

  
}
