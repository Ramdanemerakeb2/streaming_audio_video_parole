package com.example.app_android_v1.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.example.app_android_v1.Model.Actions;
import com.example.app_android_v1.Model.AudioSender;
import com.example.app_android_v1.Model.DirectoryManager;
import com.example.app_android_v1.Model.Recorder;
import com.example.app_android_v1.Model.ReponseAnalyseur;
import com.example.app_android_v1.R;
import com.owlike.genson.Genson;
import com.zeroc.Ice.Util;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.util.VLCVideoLayout;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import Demo.StreamingAppPrx;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class MusiqueActivity extends AppCompatActivity {

    com.zeroc.Ice.Communicator communicator;
    com.zeroc.Ice.ObjectPrx obj;
    StreamingAppPrx printer ;

    private ImageButton btn_enregistrement;
    private TextView titre_playlist;
    private VLCVideoLayout videoView ;

    private ListView playlist;
    private ArrayList<String> listMusique;
    private ArrayAdapter<String> adapter;

    private boolean isRecording = false, isListening = false;
    private Recorder record;
    android.media.MediaPlayer mp;
    private String wavLocation = "";
    private String urlStreaming;

    private LibVLC mLibVLC = null;
    private org.videolan.libvlc.MediaPlayer mMediaPlayer = null;
    private static final boolean USE_TEXTURE_VIEW = false;
    private static final boolean ENABLE_SUBTITLES = true;

    String responseGoogleSpeech=null;

    private AudioManager audioManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_musique);

        //pour la gestion de son (augmenter et reduire le son)
        audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

        //initialisation des composants (bouttons, listview ...)
        initComposant();

        // Implmentation Ice
        try {
            communicator = Util.initialize();
            obj = communicator.stringToProxy("StreamingApp:default -h 192.168.1.19 -p 10000");
            printer = StreamingAppPrx.checkedCast(obj);
            printer.printString("test android");

            //initialiser la listView avec la liste des musiques disponible sur le serveur de streaming (ICE)
            //initPlayList(printer.getAllSong());

        }catch (Exception e){
            e.printStackTrace();
            SweetAlertDialog sDialog = new SweetAlertDialog(MusiqueActivity.this, SweetAlertDialog.ERROR_TYPE);
            sDialog.setTitleText("Oups ...");
            sDialog.setContentText("Erreur de connexion au serveur ICE");
            sDialog.setCancelable(true);
            sDialog.show();
        }

        btn_enregistrement.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(!isRecording)
                {
                    //btn_enregistrement.setImageResource(R.drawable.stop);
                    btn_enregistrement.setBackgroundResource(R.drawable.stop);
                    isRecording = true;
                    record.startRecording();
                }else{
                    btn_enregistrement.setBackgroundResource(R.drawable.mic);
                    isRecording = false;

                    wavLocation = record.stopRecording("test.wav");

                    //a chaque fin d'enregistrement on envoie le vocal vers le serveur NodeJs a fin qu'il soit traiter par GoogleSpeech
                    uploadGoogleSpeech(wavLocation);

                }
            }
        });
    }

    private void initComposant(){
        // Créer l'architecture dossier de l'application (si cela n'est pas déjà fait)
        DirectoryManager.getInstance().initProject();

        //Inistialiser le recorder
        record = new Recorder(DirectoryManager.OUTPUT_ENR);

        playlist = findViewById(R.id.list_playlist);
        btn_enregistrement = findViewById(R.id.btn_enregistrement);
        titre_playlist = findViewById(R.id.titre_playlist);
        videoView = findViewById(R.id.videoVlcView);
    }

    private void initPlayList(ArrayList<String> res){
        listMusique =  res;

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,listMusique);
        playlist.setAdapter(adapter);

        // When the user clicks on the ListItem
        playlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                //playAudio(position);
            }
        });
    }

    public void uploadGoogleSpeech(String outputFile) {

        AudioSender sender=new AudioSender(outputFile);
        FutureTask<String> uploadTask=new FutureTask<String>(sender);
        ExecutorService executorService= Executors.newFixedThreadPool(1);
        executorService.execute(uploadTask);
        while(true){
            if(uploadTask.isDone()){
                try{
                    responseGoogleSpeech = uploadTask.get();
                    if(responseGoogleSpeech != null){
                        Log.i("Response Node",responseGoogleSpeech);
                        //envoie de la phrase retourner de google speech a l'analyseur Rest
                        analyseRequete(responseGoogleSpeech);
                    }else{
                        SweetAlertDialog sDialog = new SweetAlertDialog(MusiqueActivity.this, SweetAlertDialog.ERROR_TYPE);
                        sDialog.setTitleText("Oups ...");
                        sDialog.setContentText("Veillez exprimer votre requte");
                        sDialog.setCancelable(true);
                        sDialog.show();
                    }




                    break;
                }catch(InterruptedException| ExecutionException e){
                    e.printStackTrace();
                    Log.e("Upload","Exception",e.getCause());
                    SweetAlertDialog sDialog = new SweetAlertDialog(MusiqueActivity.this, SweetAlertDialog.ERROR_TYPE);
                    sDialog.setTitleText("Oups ...");
                    sDialog.setContentText("Erreur de connexion au serveur de transcription automatique");
                    sDialog.setCancelable(true);
                    sDialog.show();
                }
            }
        }

    }


    public void analyseRequete(String requete){

        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                HttpURLConnection urlConnection = null ;
                try {
                    URL url = new URL("http://192.168.1.20:8080/analyseur/ActionBegin/"+ requete);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");

                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    Scanner scanner = new Scanner(in);
                    final ReponseAnalyseur reponse = new Genson().deserialize(scanner.nextLine(), ReponseAnalyseur.class);
                    //Toast.makeText(MainActivity.this, "Le numero du client est :"+ contact.getNumero(),Toast.LENGTH_LONG).show();
                    Log.i("Reponse Analyseur req","Result = "+reponse);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            //traitement de la reponse de l'analyseur
                            if(reponse.getInfos().equals("Reessayer") || reponse.getAction().equals("rien")){
                                SweetAlertDialog sDialog = new SweetAlertDialog(MusiqueActivity.this, SweetAlertDialog.ERROR_TYPE);
                                sDialog.setTitleText("Oups ...");
                                sDialog.setContentText("Ressayer");
                                sDialog.setCancelable(true);
                                sDialog.show();
                            }else{
                                if(reponse.getAction().equals(Actions.lire.toString())){
                                    if(!reponse.getObjet().equals("rien")) {
                                        jouerStreaming(reponse.getObjet());
                                    }else{
                                        SweetAlertDialog sDialog = new SweetAlertDialog(MusiqueActivity.this, SweetAlertDialog.ERROR_TYPE);
                                        sDialog.setTitleText("Oups ...");
                                        sDialog.setContentText("Ressayer");
                                        sDialog.setCancelable(true);
                                        sDialog.show();
                                    }
                                }else if(reponse.getAction().equals(Actions.pause.toString())){
                                    pauseStreaming();
                                }else if(reponse.getAction().equals(Actions.reprendre.toString())){
                                    reprendreStreaming();
                                }else if(reponse.getAction().equals(Actions.arreter.toString())){
                                    stopStreaming();
                                }else if(reponse.getAction().equals(Actions.afficher.toString())){
                                    if(reponse.getObjet().equals("vidéo")){
                                        titre_playlist.setText("PlayList Video");
                                        initPlayList((ArrayList<String>) printer.getAllSong("video"));
                                    }else if(reponse.getObjet().equals("musique")){
                                        titre_playlist.setText("PlayList Musique");
                                        initPlayList((ArrayList<String>) printer.getAllSong("musique"));
                                    }

                                }else if(reponse.getAction().equals(Actions.quitter.toString())){
                                    onBackPressed();
                                }else if(reponse.getAction().equals(Actions.augmenter.toString())){
                                    //Pour augmenter le volume
                                    audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
                                }else if(reponse.getAction().equals(Actions.reduire.toString())){
                                    //Pour reduire le volume
                                    audioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
                                }
                            }

                            //Looper.loop();
                        }
                    });




                    in.close();
                }catch (Exception e){
                    Log.e("Result JSON", "Cannot found http server",e);
                    SweetAlertDialog sDialog = new SweetAlertDialog(MusiqueActivity.this, SweetAlertDialog.ERROR_TYPE);
                    sDialog.setTitleText("Oups ...");
                    sDialog.setContentText("Erreur de connexion au serveur d'analyseur de requtes");
                    sDialog.setCancelable(true);
                    sDialog.show();
                }finally {
                    if(urlConnection != null) urlConnection.disconnect();
                }
            }
        }).start();
    }

    //******************Gestion d'action streaming******************************
    private void jouerStreaming(String son){

        final ArrayList<String> args = new ArrayList<>();
        args.add("-vvv");
        mLibVLC = new LibVLC(this, args);
        mMediaPlayer = new org.videolan.libvlc.MediaPlayer(mLibVLC);

        mMediaPlayer.attachViews(videoView, null, ENABLE_SUBTITLES, USE_TEXTURE_VIEW);
        //Envoie de requete a Ice afin de lancer le Streaming
        urlStreaming = printer.playStream(son);

        //System.out.println(urlStream);
        Log.i("Url de Streaming","Result = "+urlStreaming);
        final Media media = new Media(mLibVLC, Uri.parse(urlStreaming));
        mMediaPlayer.setMedia(media);
        media.release();
        mMediaPlayer.play();
    }


    private void pauseStreaming(){
        //Envoie de requete a Ice afin de mettre en pause le Streaming
        printer.pauseStream();
        mMediaPlayer.pause();
    }

    private void reprendreStreaming(){
        //Envoie de requete a Ice afin de reprendre le Streaming
        printer.resumeStream();

        final Media media = new Media(mLibVLC, Uri.parse(urlStreaming));
        mMediaPlayer.setMedia(media);
        media.release();
        mMediaPlayer.play();
    }

    private void stopStreaming(){
        //Envoie de requete a Ice afin d'arreter le Streaming
        printer.stopStream();
        if(mMediaPlayer != null){
            mMediaPlayer.stop();
            mMediaPlayer.detachViews();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mLibVLC != null && mMediaPlayer!= null) {
            mMediaPlayer.release();
            mLibVLC.release();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mMediaPlayer != null){
            mMediaPlayer.stop();
            mMediaPlayer.detachViews();
        }

    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(false);

        new cn.pedant.SweetAlert.SweetAlertDialog(this, cn.pedant.SweetAlert.SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Êtes-vous sûr ?")
                .setContentText("Vous aller quitter l'application.")
                .setConfirmText("Quitter")
                .setConfirmClickListener(new cn.pedant.SweetAlert.SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(cn.pedant.SweetAlert.SweetAlertDialog sDialog)
                    {
                        finishAffinity();
                    }
                })
                .setCancelButton("Annuler", new cn.pedant.SweetAlert.SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(cn.pedant.SweetAlert.SweetAlertDialog sDialog) {
                        sDialog.dismissWithAnimation();
                    }
                })
                .show();
    }
}