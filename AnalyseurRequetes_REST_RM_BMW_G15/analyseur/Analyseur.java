package analyseur;

import java.util.*;
import java.util.regex.Pattern;

public class Analyseur {
	
	private String[] requeteDecompose; 
	
	public Analyseur(String requete) {
		requeteDecompose = requete.split(" ");
	}

	private List<String> playlist = Arrays.asList("union" , "libert�" , "esp�rance" , "dkr" , "dior" , "international",
		"musique", "video", "vid�o");
	
	private String analyseAction()
	{ 
		String choixAction ="";
		for(String requete : requeteDecompose)
		{
			if(Pattern.compile("^a(ff|f){1}ich(e|er)*$").matcher(requete.toLowerCase()).matches() || Pattern.compile("^voir(e)*$").matcher(requete.toLowerCase()).matches())
			{
				if (!choixAction.isEmpty() && !choixAction.equals(Actions.afficher.toString()))
					return null;
				choixAction = Actions.afficher.toString();
			}
			else if(Pattern.compile("^l(ectur|ir){1}(e)*$").matcher(requete.toLowerCase()).matches() || Pattern.compile("^jou(e|�|er)*$").matcher(requete.toLowerCase()).matches() || Pattern.compile("^play(e)*$").matcher(requete.toLowerCase()).matches() || Pattern.compile("^(e|�){1}cout(e|er)*$").matcher(requete.toLowerCase()).matches())
			{
				if (!choixAction.isEmpty() && !choixAction.equals(Actions.lire.toString()))
					return null;
				choixAction = Actions.lire.toString();
			}
			else if(Pattern.compile("^reprend(re)*$").matcher(requete.toLowerCase()).matches() || Pattern.compile("^repris(e)*$").matcher(requete.toLowerCase()).matches())
			{
				if (!choixAction.isEmpty() && !choixAction.equals(Actions.reprendre.toString()))
					return null;
				choixAction = Actions.reprendre.toString();
			}
			else if(Pattern.compile("^paus(e|�|er)*$").matcher(requete.toLowerCase()).matches())
			{
				if (!choixAction.isEmpty() && !choixAction.equals(Actions.pause.toString()))
					return null;
				choixAction = Actions.pause.toString();
			}
			else if(Pattern.compile("^arr(�|e)t(e|�|er)*$").matcher(requete.toLowerCase()).matches() || Pattern.compile("^stop(e|er)*$").matcher(requete.toLowerCase()).matches())
			{
				if (!choixAction.isEmpty() && !choixAction.equals(Actions.arreter.toString()))
					return null;
				choixAction = Actions.arreter.toString();
			}
			else if(Pattern.compile("^augment(e|�|er)*$").matcher(requete.toLowerCase()).matches())
			{
				if (!choixAction.isEmpty() && !choixAction.equals(Actions.augmenter.toString()))
					return null;
				choixAction = Actions.augmenter.toString();
			}
			else if(Pattern.compile("^r(�|e){1}duir(e|)*$").matcher(requete.toLowerCase()).matches() || Pattern.compile("^d(i|�){1}m(i|�){1}nu(�|e|er)*$").matcher(requete.toLowerCase()).matches())
			{
				if (!choixAction.isEmpty() && !choixAction.equals(Actions.reduire.toString()))
					return null;
				choixAction = Actions.reduire.toString();
			}
			else if(Pattern.compile("^quitt(�|e|er)*$").matcher(requete.toLowerCase()).matches())
			{
				if (!choixAction.isEmpty() && !choixAction.equals(Actions.quitter.toString()))
					return null;
				choixAction = Actions.quitter.toString();
			}		
		}	
		return !choixAction.isEmpty() ? choixAction : null;
	}
	
	private String analysePlaylist()
	{
		String choixPlaylist = "";
		for(String requete : requeteDecompose)
		{	
			for(String piste : this.playlist)
			{
				if((requete.toLowerCase().contains(piste) || piste.toLowerCase().contains(requete)))
				{
					if(!choixPlaylist.isEmpty() && !(requete.toLowerCase().contains(choixPlaylist) || choixPlaylist.contains(requete.toLowerCase()))) // Deux titre dans une requete ou autre
						return null;
					else 
						choixPlaylist = piste.toLowerCase();
				}
			}	
		}
		
		return !choixPlaylist.isEmpty() ? choixPlaylist : null;
	}
	
	public String[] analyse()
	{
		String playlist = this.analysePlaylist();
		String action = this.analyseAction();
		
		if(playlist == null && action == null) 
			return null; 
		else 
		{
			String [] s = new String[2]; 
			s[1] = playlist != null ? playlist : "rien" ; 
			s[0] = action != null ? action : "rien"; 	
			return s;
		}
	}	
}