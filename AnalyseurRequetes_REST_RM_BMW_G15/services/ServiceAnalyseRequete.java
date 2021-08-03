package services;

import analyseur.*;
import javax.ws.rs.*;
import javax.ws.rs.core.Response.*;
import javax.ws.rs.core.*;

@Path("/")
public class ServiceAnalyseRequete
{
	@GET
	@Path("ActionBegin/{vocal}")
	@Produces(MediaType.APPLICATION_JSON)
	public ReponseAnalyseur action(@PathParam("vocal") String vocal)
	{
		Analyseur analyse = new Analyseur(vocal);
		String[] req = analyse.analyse();
		
		if(req != null)
			return new ReponseAnalyseur(req[0], req[1]);
		else 
			return new ReponseAnalyseur("Reessayer");
	}
}