module Demo
{   
    sequence<string> arrayString;
    interface StreamingApp
    {
        void printString(string s);
        ["java:type:java.util.ArrayList<String>"]arrayString getAllSong(string type);
        string playStream(string song);
        void pauseStream();
        void stopStream();
        void resumeStream();
    }
}
