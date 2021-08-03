package com.example.app_android_v1.Model;

import android.os.Environment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class DirectoryManager {

    // -------------------- SINGLETON ---------------------------
    private DirectoryManager() {}

    private static DirectoryManager INSTANCE = null;

    public static synchronized DirectoryManager getInstance()
    {
        if (INSTANCE == null) { INSTANCE = new DirectoryManager(); }
        return INSTANCE;
    }

    // Listes des repertoires
    public static final String OUTPUT_DIRECTORY = Environment.getExternalStorageDirectory() + "/ProjetStreaming";
    public static final String OUTPUT_ENR= Environment.getExternalStorageDirectory() + "/ProjetStreaming/Enregistrements";
    public static final String OUTPUT_LIST = Environment.getExternalStorageDirectory() + "/ProjetStreaming/ListeMuisiques";


    public void initProject()
    {
        createInternalDirectory();
        createFolderInAppFolder("Enregistrements");
        createFolderInAppFolder("ListeMuisiques");
    }

    // Créer le dossier de notre application sur l'appareil Android
    private void createInternalDirectory()
    {
        File file = new File(OUTPUT_DIRECTORY);
        if (!file.exists())
            file.mkdir();
    }

    // Créer un dossier à l'intérieur du dossier de l'application
    public void createFolderInAppFolder(String directoryName)
    {
        File file = new File(OUTPUT_DIRECTORY + "/" + directoryName);
        if (!file.exists())
            file.mkdir();
    }

        public File[] getListFiles(String pathDirectory){
        File dir = new File(pathDirectory);

        File[] dirs = dir.listFiles();

        return dirs;
        }




}
