// Código adaptado de NER-to-portuguese.zip/Categorizador_entidades/src/Main.java,
// desenvolvido por Gabriel C. Chiele, Evandro Fonseca e Renata Vieira e disponível em
// http://www.inf.pucrs.br/linatural/Recursos/

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.util.Logger;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;


//bibliotecas OpenNLP
import opennlp.tools.cmdline.namefind.TokenNameFinderConverterTool;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.NameSample;
import opennlp.tools.namefind.NameSampleDataStream;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.Span;


public class NERAgent extends Agent {
		
	protected void setup() {
	// Printout a welcome message
	String nickname = "NER";
	AID id = new AID(nickname, AID.ISLOCALNAME);
	//System.out.println("Hello! Buyer-agent "+getAID().getName()+" is ready.");
	NERBehaviour NERResponse = new  NERBehaviour();
	addBehaviour(NERResponse);
	}

	private class NERBehaviour extends CyclicBehaviour {
		public void action() {
			ACLMessage  msg = myAgent.receive();
			if (msg != null){
				//ACLMessage reply = msg.createReply();
				//String content = msg.getContent();
				//reply.setContent("Echo: " + content);
				//reply.setPerformative(ACLMessage.INFORM);
				//send(reply);


				ACLMessage reply = msg.createReply();
				String content = msg.getContent();
				String response = "";

				//openNLP part
				try{
					response = RUN(content);				
				}
				catch(IOException e){
					e.printStackTrace();
				}

				reply.setContent(response);
				reply.setPerformative(ACLMessage.INFORM);
				send(reply);
			}
			else{
				block();
			}
		}
	}

	public static String RUN(String texto) throws IOException
	{	
		String finalPrint = "";
		String name;
		ArrayList<Entidade> entities = new ArrayList<Entidade>();	// array que vai conter as entidades coletadas no texto
		//System.out.println("Iniciando a aplicação: \n");
		
		//declaração do path dos modelos
		//System.out.println("Carregando path dos modelos");
		String sentModelPATH = "Categorizador_entidades/Modelos/pt-sent.bin";
		String tokenModelPATH = "Categorizador_entidades/Modelos/pt-token.bin";
		String nfpModelPATH = "Categorizador_entidades/Modelos/pt-ner-multi.bin";
	
		//declaração do path do texto a ser analisado		
		//System.out.println("Carregando path do texto para analise \n");
		
		//System.out.print("Lendo arquivo ... ");
		//String texto = Txt(pathTxt);	// concatena todas string do texto em uma string unica
		//System.out.println("Pronto!");
		
		//System.out.print("Dividindo em sentenças ... ");
		ArrayList<String> sentences = sentenceDetectorAPI(sentModelPATH,texto);	// separa o texto em sentenças
		//System.out.println("Pronto!\n" );
		
		//System.out.println("Iniciando criação dos tokens:");
		//System.out.println("---------------------------------------------------------------------------------");
		ArrayList<String[]> tokens = new ArrayList<String[]>();	// cada posição do vetor contém um array, este que conterá os tokens de uma sentença do texto
		for(int i=0;i<sentences.size();i++)
		{
			//System.out.print("Tokenizando ...");
			//System.out.println(" Iteração n°: " + i);
			tokens.add(tokenizerAPI(tokenModelPATH,sentences.get(i))); // preenche o array de tokens
		}
		//System.out.println("---------------------------------------------------------------------------------" + "\nFinalizado\n");
		
		//System.out.println("Iniciando o localizador de entidades nomeadas:");
		//System.out.println("---------------------------------------------------------------------------------");
		ArrayList<Span[]> nameSpans = new ArrayList<Span[]>();
		int flag = 0; // marca a ultima posição de nameSpans avaliada, no 'for' de concatenação
		for(int i=0; i<tokens.size();i++)
		{
			//System.out.print("Localizando ...");
			//System.out.println(" Iteração n°: " + i);
			// localiza as entidades nomeadas da sentença que está sobre analize
			nameSpans.add(nameFinderAPI(nfpModelPATH,tokens.get(i)));
			// concatena os tokens que se referem a mesma entidade
			if(!nameSpans.isEmpty() && flag < nameSpans.size())
			{
				for(int j=flag;j<nameSpans.size();j++)
				{
					for(int k=0;k<nameSpans.get(j).length;k++)
					{
						int start = nameSpans.get(j)[k].getStart();
						int end = nameSpans.get(j)[k].getEnd()-1;
						String type = nameSpans.get(j)[k].getType();
						if(start != end)
						{
							name = tokens.get(i)[start]+" "+tokens.get(i)[end];
						}
						else name = tokens.get(i)[start];
						
						entities.add(new Entidade(name,type,i,start,end));	// criação e armazenamento da entidade localizada
					}
				}
				flag = nameSpans.size();	// evita que entidades já inseridas em 'entities' sejam re-adicionadas
			}
		}
		//System.out.println("---------------------------------------------------------------------------------" + "\nFinalizado\n");
		//System.out.println(pathTxt);
		//System.out.println("Lista de entidades encontradas:");
		for(int i=0; i<entities.size();i++)	// percorre o array de entidades e as imprime na tela
			//System.out.println(entities.get(i).nome+"<"+entities.get(i).tipo+">");
			finalPrint = finalPrint + entities.get(i).nome+"<"+entities.get(i).tipo+">"+"\n";
		return finalPrint;
	}


	//public static String Txt(String path) throws IOException	// percorre o texto e concatena todas suas linhas
	//{
	//	String txt = "";
	//	String aux = "";
	//	BufferedReader buffin = new BufferedReader( new FileReader(path));
	//	while (true) // percorre o arquivo de entrada
	//	{
	//		if (aux != null)
	//		{			
	//			txt = txt + aux + "\n"; // concatenas todas as linhas do texto.
	//		}
	//		else break;
	//		aux = buffin.readLine();	// le a proxima linha
	//	}
	//	buffin.close(); // fecha o buffer de entrada
	//	return txt; // retorna o texto
	//}


	public static ArrayList<String> sentenceDetectorAPI(String path, String text) throws FileNotFoundException	// divide o texto em sentenças.
	{
		ArrayList<String> sent = new ArrayList<String>();	// lista que armazenará todas as sentenças do texto
		InputStream modelIn = new FileInputStream(path);
		try 
		{
		  SentenceModel model = new SentenceModel(modelIn); // instanciação do modelo do detector de sentenças
		  SentenceDetectorME sentenceDetector = new SentenceDetectorME(model); // instanciação do detector de sentenças
		  String sentences[] = sentenceDetector.sentDetect(text);	// chamada da função de detecção
		  sent = arrayTolist(sentences);	// converção de vetor para lista
		}
		catch (IOException e){e.printStackTrace();}
		finally 
		{
		  if (modelIn != null) 
		  {
		    try 
		    {
		      modelIn.close();
		    }
		    catch (IOException e){}
		  }
		}
		return sent;
	}
	public static ArrayList<String> arrayTolist(String[] s1)	// transforma um vetor em uma lista
	{
		ArrayList<String> s2 = new ArrayList<String>();	// instanciação da lista de retorno
		for(int i=0;i<s1.length;i++)
		{
			s2.add(s1[i]);	// adiciona na lista o valor armazenado na posição 'i' do vetor
		}
		return s2;	// retorna a lista
	}
	public static String[] tokenizerAPI(String path, String sentence) throws FileNotFoundException	// divide sentenças em tokens
	{
		String tokens[] = null;
		InputStream modelIn = new FileInputStream(path);
		try 
		{
		  TokenizerModel model = new TokenizerModel(modelIn);	// instanciação do modelo do tokenizador
		  Tokenizer tokenizer = new TokenizerME(model);	// instanciação do tokenizador
		  tokens = tokenizer.tokenize(sentence);	// chamada da função de tokenização
		}
		catch (IOException e) {e.printStackTrace();}
		finally 
		{
		  if (modelIn != null) 
		  {
		    try 
		    {
		      modelIn.close();
		    }
		    catch (IOException e) {}
		  }
		}
		return tokens; 
	}
	public static Span[] nameFinderAPI(String path, String[] sentence) throws FileNotFoundException	// localiza as entidades nomeadas na sentença
	{
		Span nameSpans[] = null;
		InputStream modelIn = new FileInputStream(path);
		try 
		{
		  TokenNameFinderModel model = new TokenNameFinderModel(modelIn);	// instanciação do modelo do namefinder
		  NameFinderME nameFinder = new NameFinderME(model);	// instanciação do namefinder
		  nameSpans = nameFinder.find(sentence);	// chamada da função de localização de entidades
		}
		catch (IOException e) {e.printStackTrace();}
		finally 
		{
		  if (modelIn != null) 
		  {
		    try 
		    {
		      modelIn.close();
		    }
		    catch (IOException e) {}
		  }
		}
		return nameSpans;
	}
}