/*gradlew :server:build
java -jar server/build/libs/server.jar */
public class Server
{
    public static void main(String[] args)
    {
        try(com.zeroc.Ice.Communicator communicator = com.zeroc.Ice.Util.initialize(args))
        {   
            //instancier un StreamingAppI servant 
            com.zeroc.Ice.Object servant = new StreamingAppI();

            //creer un adapter qui contient le servant deja instancier
            com.zeroc.Ice.ObjectAdapter adapter = communicator.createObjectAdapterWithEndpoints("StreamingAppAdapter", "default -p 10000");
            
            //ajouter le servant a l'adapter
            adapter.add(servant, com.zeroc.Ice.Util.stringToIdentity("StreamingApp"));

            //activer l'adapter pour recevoir les requetes et les distribué aux servants
            adapter.activate();
            communicator.waitForShutdown();
        }
    }
}
