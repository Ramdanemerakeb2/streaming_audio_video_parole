package analyseur;

public class ReponseAnalyseur
{
	private String action; 
	private String objet; 
	private String infos; 

	public ReponseAnalyseur(){};

	public ReponseAnalyseur(String action, String objet){
		this.action = action; 
		this.objet = objet;
		this.infos = "Ok";
	}

	public ReponseAnalyseur(String infos){
		this.infos = infos;
	}

	public void setAction(String action)
	{
		this.action = action; 
	}

	public void setObjet(String objet)
	{
		this.objet = objet;
	}

	public void setInfos(String infos)
	{
		this.infos = infos;
	}

	public String getAction()
	{
		return this.action ; 
	}
	
	public String getObjet()
	{
		return this.objet;
	}

	public String getInfos()
	{
		return this.infos;
	}
}
