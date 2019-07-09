package it.polito.tdp.tesi.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import it.polito.tdp.tesi.controller.HomeController;
import it.polito.tdp.tesi.db.StatisticheDAO;

public class Model {

	private StatisticheDAO dao;
	private Map<Integer,CalciatoreStatistiche> stat20162017;
	private Map<Integer,CalciatoreStatistiche> stat20172018;
	private Map<Integer,CalciatoreStatistiche> stat20182019;
	
	private List<CalciatoreStatistiche> media;
	private List<PunteggioCalciatore> punteggi;
	
	private int budgetTotale;
	private int budgetRimanente;
	private int budgetPortieri;
	private int budgetDifensori;
	private int budgetCentrocampisti;
	private int budgetAttaccanti;
	private int spesi;
	
	private Map<Integer,Quotazione> quotazioni;
	private List<CalciatoreStatistiche> portieri;
	private List<CalciatoreStatistiche> difensori;
	private List<CalciatoreStatistiche> centrocampisti;
	private List<CalciatoreStatistiche> attaccanti;
	
	private List<CalciatoreStatistiche> calciatori;
	private Map<Integer,CalciatoreStatistiche> tuaRosa;

	private List<PunteggioCalciatore> parzialeP;
	private List<PunteggioCalciatore> ottima;
	private List<PunteggioCalciatore> parzialeD;
	
	private List<PunteggioCalciatore> parzialeC;
	
	private List<PunteggioCalciatore> parzialeA;
	
	private List<PunteggioCalciatore> punt;
	private boolean portieriStessaSquadra;
	
	public Map<Integer, CalciatoreStatistiche> getTuaRosa() {
		return tuaRosa;
	}

	public Model() {
		 dao = new StatisticheDAO();
		 quotazioni = dao.getQuotazioni();
		 this.calcolaMedia();
		 this.calcolaPunteggio();
		 parzialeP = new ArrayList<PunteggioCalciatore>();
			parzialeD = new ArrayList<PunteggioCalciatore>();
			parzialeC = new ArrayList<PunteggioCalciatore>();
			parzialeA = new ArrayList<PunteggioCalciatore>();
	}
	
	public void calcolaMedia() {
		stat20162017 = dao.getCalciatori20162017();
		stat20172018 = dao.getCalciatori20172018();
		stat20182019 = dao.getCalciatori20182019();
		
		
		media = new ArrayList<CalciatoreStatistiche>();
		media.addAll(dao.getCalciatori20162017().values());
		media.addAll(dao.getCalciatori20172018().values());
		media.addAll(dao.getCalciatori20182019().values());
		media.addAll(dao.getMedia2016_2017_2018().values());
		media.addAll(dao.getMedia2016_2018_2019().values());
		media.addAll(dao.getMedia2017_2018_2019().values());
		media.addAll(dao.getMediaSolo20162017().values());
		media.addAll(dao.getMediaSolo2016_2017_2018().values());
		media.addAll(dao.getMediaSolo2016_2018_2019().values());
		media.addAll(dao.getMediaSolo20172018().values());
		media.addAll(dao.getMediaSolo2017_2018_2019().values());
		media.addAll(dao.getMediaSolo20182019().values());
		media.addAll(dao.getMediaTreAnni().values());
							
	}
	
	public List<CalciatoreStatistiche> getMedia(){
		return media;
	}

	public void calcolaPunteggio() {
		
		punteggi = new ArrayList<PunteggioCalciatore>();
		double punteggio;
		for(CalciatoreStatistiche c : this.media) {
			punteggio=0;
			//punteggio depotenziato per giocatori provenienti dalla Serie B
			if(c.getSquadra().equals("Lecce")||c.getSquadra().equals("Brescia")||c.getSquadra().equals("Verona")) {
				punteggio+= 0.40*(2*c.getPartiteGiocate()+2*c.getMediaFanta()+2*c.getMediaVoto()+2*c.getAssist()+3*c.getRigoriSegnati()-3*c.getRigoriSbagliati()+3*c.getRigoriParati()+3*c.getGolFatti()-c.getGolSubiti()-2*c.getAmmonizioni()-3*c.getEspulsioni()-3*c.getAutogol());
			}
			else {
				punteggio+= 2*c.getPartiteGiocate()+2*c.getMediaFanta()+2*c.getMediaVoto()+2*c.getAssist()+3*c.getRigoriSegnati()-3*c.getRigoriSbagliati()+3*c.getRigoriParati()+3*c.getGolFatti()-c.getGolSubiti()-2*c.getAmmonizioni()-3*c.getEspulsioni()-3*c.getAutogol();
			}
			PunteggioCalciatore p = new PunteggioCalciatore(c.getId(),c.getRuolo(), c.getNome(), c.getSquadra(), c.getQuotazione(), punteggio);
			punteggi.add(p);
			
		}
		//se esistono calciatori con 0 statistiche presenti nelle valutazioni setto il punteggio a 0
		for(Quotazione q: this.quotazioni.values()) {
			for(CalciatoreStatistiche c: this.media) {
				if(c.getId()!=q.getId()) {
					PunteggioCalciatore p = new PunteggioCalciatore(q.getId(),q.getRuolo(),q.getNome(),q.getSquadra(),q.getQuotazione(),0.0);
					if(!punteggi.contains(p)) {
						punteggi.add(p);
					}
				}
			}
			
		}
		
	}
	
	public List<PunteggioCalciatore> getListaPunteggi(){
		Collections.sort(punteggi,new Comparator<PunteggioCalciatore>() {

			@Override
			public int compare(PunteggioCalciatore o1, PunteggioCalciatore o2) {
				// TODO Auto-generated method stub
				return -Double.compare(o1.getPunteggio(), o2.getPunteggio());
			}
		
		});
		return this.punteggi;
	}


	public List<PunteggioCalciatore> calcolaMigliorRosa() {
		
		ottima=null;
		ottima = new ArrayList<PunteggioCalciatore>();
		punt = this.getListaPunteggi();

		ricorsione(this.getParzialeP(),3,"P",this.getBudgetPortieri());
		ricorsione(this.getParzialeD(),8,"D",this.getBudgetDifensori());
		ricorsione(this.getParzialeC(),8,"C",this.getBudgetCentrocampisti());
		ricorsione(this.getParzialeA(),6,"A",this.getBudgetAttaccanti());
		String res="";
		spesi=0;
		for(PunteggioCalciatore c: ottima) {
			res+= c.toStringQuotaz()+"\n";
			spesi+=c.getQuotazione();
			System.out.println(res);
		}
		return ottima;
	}
	
	
	private void ricorsione(List<PunteggioCalciatore> parziale,int i,String ruolo, int budget) {
		
		//condizione di terminazione
		if(parziale.size()>=i) {
			this.ottima.addAll(parziale);
			return;
		}
		//se viene selezionato il bottone "Portieri stessa squadra"
		if(portieriStessaSquadra==true) {
			if(ruolo.equals("P")) {
				if(parziale.size()>0 && parziale.size()<i ) {
					String squadra = parziale.get(0).getSquadra();
					System.out.println(squadra);
					for(PunteggioCalciatore c:this.punt) {
						if(!parziale.contains(c)) {
							if(c.getSquadra().equals(squadra) &&c.getRuolo().equals("P")) {
								parziale.add(c);
						//		System.out.println(c.toStringNomeQuota());
								ricorsione(parziale,i,ruolo,budget-c.getQuotazione());
							}
						}	
					
					}
				}
			}
		}
			
		//lavoro nella lista gi� ordinata per punteggi decrescenti
		
		for(PunteggioCalciatore c:this.punt) {
			if(parziale.size()<i) {
				if(!parziale.contains(c)) {
					if(c.getRuolo().equals(ruolo)){
						
						
						if(c.getQuotazione()+(i-parziale.size()-1)<budget) {
							parziale.add(c);
					//		System.out.println("Ruolo: "+c.getRuolo()+ " Nome : "+c.getNome()+" budget: "+budget+" speso: "+c.getQuotazione());
							ricorsione(parziale,i,ruolo,budget-c.getQuotazione());
						}
						
					}
				}
						
			
			}
			
		}
		
	}

	public void selezionaPortieri() {
		this.calcolaMedia();
		portieri = new ArrayList<CalciatoreStatistiche>();
		for(CalciatoreStatistiche c: this.media) {
			if(c.getRuolo().equals("P")) {
				portieri.add(c);
			}
		}
		
	}
	public void selezionaDifensori() {
		difensori = new ArrayList<CalciatoreStatistiche>();
		for(CalciatoreStatistiche c: this.media) {
			if(c.getRuolo().equals("D")) {
				difensori.add(c);
			}
		}
	}
	public void selezionaCentrocampisti() {
		centrocampisti = new ArrayList<CalciatoreStatistiche>();
		for(CalciatoreStatistiche c: this.media) {
			if(c.getRuolo().equals("C")) {
				centrocampisti.add(c);
			}
		}
	}
	public void selezionaAttaccanti() {
			attaccanti = new ArrayList<CalciatoreStatistiche>();
			for(CalciatoreStatistiche c: this.media) {
				if(c.getRuolo().equals("A")) {
					attaccanti.add(c);
				}
			}
		}

	public List<CalciatoreStatistiche> getPortieriRetiSubite() {
		
		Collections.sort(portieri, new Comparator<CalciatoreStatistiche>() {

			@Override
			public int compare(CalciatoreStatistiche o1, CalciatoreStatistiche o2) {
				// TODO Auto-generated method stub
				return Double.compare((o1.getGolSubiti()/o1.getPartiteGiocate()),(o2.getGolSubiti()/o2.getPartiteGiocate()));
				}
			
		});

		return portieri;
	}


	public List<CalciatoreStatistiche> getPortieriRigoriParati() {
		Collections.sort(this.portieri, new Comparator<CalciatoreStatistiche>() {

			@Override
			public int compare(CalciatoreStatistiche o1, CalciatoreStatistiche o2) {
				// TODO Auto-generated method stub
				return -Double.compare(o1.getRigoriParati(),o2.getRigoriParati());
				}
			
		});

		return portieri;
	}


	
	
	public List<CalciatoreStatistiche> getMediaVoto(String ruolo) {
	
	calciatori = new ArrayList<CalciatoreStatistiche>();
	for(CalciatoreStatistiche c: this.media) {
		if(c.getRuolo().equals(ruolo)) {
			calciatori.add(c);
		}
	}	
	Collections.sort(calciatori, new Comparator<CalciatoreStatistiche>() {

			@Override
			public int compare(CalciatoreStatistiche o1, CalciatoreStatistiche o2) {
				// TODO Auto-generated method stub
				return -Double.compare(o1.getMediaVoto(),o2.getMediaVoto());
				}
			
	});

	return calciatori;
	}
	
	
	
	public List<CalciatoreStatistiche> getQuotazioni(String ruolo) {

		calciatori = new ArrayList<CalciatoreStatistiche>();
		for(CalciatoreStatistiche c: this.media) {
			if(c.getRuolo().equals(ruolo)) {
				calciatori.add(c);
			}
		}	
		Collections.sort(calciatori, new Comparator<CalciatoreStatistiche>() {

				@Override
				public int compare(CalciatoreStatistiche o1, CalciatoreStatistiche o2) {
					// TODO Auto-generated method stub
					return -Double.compare(o1.getQuotazione(),o2.getQuotazione());
					}
				
		});
		return calciatori;
	}
	
	
	public List<CalciatoreStatistiche> getPunteggio(String ruolo) {

		calciatori = new ArrayList<CalciatoreStatistiche>();
		for(CalciatoreStatistiche c: this.media) {
			if(c.getRuolo().equals(ruolo)) {
				calciatori.add(c);
			}
		}
		List<PunteggioCalciatore> punt = new ArrayList<PunteggioCalciatore>();
		for(CalciatoreStatistiche c: this.calciatori) {
			for(PunteggioCalciatore s: this.punteggi) {
				if(c.getId()==s.getId()) {
					punt.add(s);
				}
			}
		}
		Collections.sort(punt, new Comparator<PunteggioCalciatore>() {

			@Override
			public int compare(PunteggioCalciatore o1, PunteggioCalciatore o2) {
				// TODO Auto-generated method stub
				return -Double.compare(o1.getPunteggio(), o2.getPunteggio());
			}
		
		});
		List<CalciatoreStatistiche> risultato = new ArrayList<CalciatoreStatistiche>();
		for(PunteggioCalciatore s: punt) {
			for(CalciatoreStatistiche c: this.calciatori) {
				if(c.getId()==s.getId()) {
					risultato.add(c);
				}
			}
		}
		
		return risultato;
		
	}


	public List<CalciatoreStatistiche> getGoleador(String ruolo) {

		calciatori = new ArrayList<CalciatoreStatistiche>();
		for(CalciatoreStatistiche c: this.media) {
			if(c.getRuolo().equals(ruolo)) {
				calciatori.add(c);
			}
		}	
		Collections.sort(calciatori, new Comparator<CalciatoreStatistiche>() {

				@Override
				public int compare(CalciatoreStatistiche o1, CalciatoreStatistiche o2) {
					// TODO Auto-generated method stub
					return -Double.compare(o1.getGolFatti()+o1.getRigoriSegnati(),o2.getGolFatti()+o2.getRigoriSegnati());
					}
				
		});	
		return calciatori;
		}

	public List<CalciatoreStatistiche> getAssistman(String ruolo) {
		calciatori = new ArrayList<CalciatoreStatistiche>();
		for(CalciatoreStatistiche c: this.media) {
			if(c.getRuolo().equals(ruolo)) {
				calciatori.add(c);
			}
		}	
		Collections.sort(calciatori, new Comparator<CalciatoreStatistiche>() {

				@Override
				public int compare(CalciatoreStatistiche o1, CalciatoreStatistiche o2) {
					// TODO Auto-generated method stub
					return -Double.compare(o1.getAssist(),o2.getAssist());
					}
				
		});
		return calciatori;

	}

	public List<CalciatoreStatistiche> getCartellini(String ruolo) {
		calciatori = new ArrayList<CalciatoreStatistiche>();
		for(CalciatoreStatistiche c: this.media) {
			if(c.getRuolo().equals(ruolo)) {
				calciatori.add(c);
			}
		}	
		Collections.sort(calciatori, new Comparator<CalciatoreStatistiche>() {

				@Override
				public int compare(CalciatoreStatistiche o1, CalciatoreStatistiche o2) {
					// TODO Auto-generated method stub
					return -Double.compare((o1.getAmmonizioni()+o1.getEspulsioni()),(o2.getAmmonizioni()+o2.getEspulsioni()));
					}
				
		});
		
		return calciatori;
	}

	public List<CalciatoreStatistiche> getRigoristi(String ruolo) {
	
		calciatori = new ArrayList<CalciatoreStatistiche>();
		for(CalciatoreStatistiche c: this.media) {
			if(c.getRuolo().equals(ruolo)) {
				calciatori.add(c);
			}
		}	
		Collections.sort(calciatori, new Comparator<CalciatoreStatistiche>() {

				@Override
				public int compare(CalciatoreStatistiche o1, CalciatoreStatistiche o2) {
					// TODO Auto-generated method stub
					return -Double.compare(o1.getRigoriSegnati(),o2.getRigoriSegnati());
					}
				
		});
		return calciatori;
	}

	public void setPortieriStessaSquadra(boolean b) {
		this.portieriStessaSquadra=b;
	}
	
	public List<PunteggioCalciatore> getParzialeP() {
		return parzialeP;
	}

	public List<PunteggioCalciatore> getParzialeD() {
		return parzialeD;
	}

	

	public List<PunteggioCalciatore> getParzialeC() {
		return parzialeC;
	}

	public List<PunteggioCalciatore> getParzialeA() {
		return parzialeA;	}

	public void addParzialeP(List<CalciatoreStatistiche> lista) {
		for(CalciatoreStatistiche c: lista) {
			PunteggioCalciatore d = this.getSimile(c);
			if(!this.parzialeP.contains(d)) {
				this.parzialeP.add(d);
				budgetRimanente= budgetRimanente-d.getQuotazione();
				budgetPortieri = budgetPortieri-d.getQuotazione();
				}
			}
	}

	public void addParzialeD(List<CalciatoreStatistiche> lista) {
		for(CalciatoreStatistiche c: lista) {
			PunteggioCalciatore d = this.getSimile(c);
			if(!this.parzialeD.contains(d)) {
				this.parzialeD.add(d);
				budgetRimanente= budgetRimanente-d.getQuotazione();
				budgetDifensori = budgetDifensori-d.getQuotazione();
			}
		}
		
	}

	public void addParzialeC(List<CalciatoreStatistiche> lista) {
		for(CalciatoreStatistiche c: lista) {
			PunteggioCalciatore d = this.getSimile(c);
			if(!this.parzialeC.contains(d)) {
				this.parzialeC.add(d);
				budgetRimanente= budgetRimanente-d.getQuotazione();
				budgetCentrocampisti = budgetCentrocampisti-d.getQuotazione();
			}
		}
	}
	public void addParzialeA(List<CalciatoreStatistiche> lista) {
		for(CalciatoreStatistiche c: lista) {
			PunteggioCalciatore d = this.getSimile(c);
			if(!this.parzialeA.contains(d)) {
				this.parzialeA.add(d);
				budgetRimanente= budgetRimanente-d.getQuotazione();
				budgetAttaccanti=budgetAttaccanti-d.getQuotazione();
			}
		}
	}
	

	public void resetParzialeP() {
		this.parzialeP.removeAll(parzialeP);
	}

	public void resetParzialeD() {
		this.parzialeD.removeAll(parzialeD);
	}

	public void resetParzialeC() {
		this.parzialeC.removeAll(parzialeC);
	}

	public void resetParzialeA() {
		this.parzialeA.removeAll(parzialeA);
	}
	public void resetOttima() {
		this.ottima.removeAll(ottima);
	}

	public PunteggioCalciatore getSimile(CalciatoreStatistiche c) {
		for(PunteggioCalciatore p:punteggi) {
			if(p.getId()==c.getId())
				return p;
			
		}
		return null;
		
	}
	

	public int getBudgetTotale() {
		return budgetTotale;
	}


	public void setBudgetTotale(int budgetTotale) {
		this.budgetTotale = budgetTotale;
	}


	public int getBudgetRimanente() {
		return budgetTotale-spesi;
	}


	public void setBudgetRimanente(int budgetRimanente) {
		this.budgetRimanente = budgetRimanente;
	}


	public int getBudgetPortieri() {
		return budgetPortieri;
	}


	public void setBudgetPortieri(int budgetPortieri) {
		this.budgetPortieri = budgetPortieri;
	}


	public int getBudgetDifensori() {
		return budgetDifensori;
	}

	
	public void setBudgetDifensori(int budgetDifensori) {
		this.budgetDifensori = budgetDifensori;
	}


	public int getBudgetCentrocampisti() {
		return budgetCentrocampisti;
	}


	public void setBudgetCentrocampisti(int budgetCentrocampisti) {
		this.budgetCentrocampisti = budgetCentrocampisti;
	}


	public int getBudgetAttaccanti() {
		return budgetAttaccanti;
	}


	public void setBudgetAttaccanti(int budgetAttaccanti) {
		this.budgetAttaccanti = budgetAttaccanti;
	}



}
