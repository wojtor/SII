import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayesMultinomialUpdateable;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import com.echonest.api.v4.Artist;
import com.echonest.api.v4.EchoNestAPI;

/**
 * Java program for classifying short text messages into two classes.
 * <p/>
 * See also wiki article <a href="http://weka.wiki.sourceforge.net/MessageClassifier">MessageClassifier</a>.
 * 
 * @author Eibe Frank (eibe@cs.waikato.ac.nz)
 * @version $Revision$
 */
public class MusicClassifier implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String console = "CMR> ";
	/* The training data gathered so far. */
	private Instances m_Data = null;

	/* The filter used to generate the word counts. */
	private StringToWordVector m_Filter = new StringToWordVector();

	/* The actual classifier. */
	private Classifier m_Classifier = new NaiveBayesMultinomialUpdateable();

	/* Whether the model is up to date. */
	private boolean m_UpToDate;

	/**
	 * Constructs empty training dataset.
	 * @param num_inst 
	 */
	public MusicClassifier(int num_inst) throws Exception {

		String nameOfDataset = "MusicClassificationProblem";

		// Create vector of attributes.
		FastVector attributes = new FastVector(2);

		// Add attribute for holding web site text.
		attributes.addElement(new Attribute("Text", (FastVector)null));

		// Add class attribute.
		Path currentRelativePath = Paths.get("");
		String classFile = currentRelativePath.toAbsolutePath().toString()+"\\artists.txt";
		Scanner sc = new Scanner(new File(classFile));
		FastVector classValues = new FastVector(2);
		
		int size = 0;
		while(sc.hasNextLine() && size<num_inst){
			classValues.addElement(sc.nextLine());
			size++;
		}
		System.out.println(console+"Size of the set is :"+size);
		sc.close();
		attributes.addElement(new Attribute("Class", classValues));

		// Create dataset with initial capacity of 100, and set index of class.
		m_Data = new Instances(nameOfDataset, attributes, size);
		m_Data.setClassIndex(m_Data.numAttributes() - 1);
	}

	/**
	 * Updates model using the given training message.
	 */
	public void updateData(String message, String classValue) throws Exception {

		// Make text into instance.
		Instance instance = makeInstance(message, m_Data);

		// Set class value for instance.
		if(classValue.codePointAt(0)!=32993){	//necessary in case of "???" = unknown artist
			instance.setClassValue(classValue);
			
			// Add instance to training data.
			m_Data.add(instance);
			m_UpToDate = false;
		}
	}

	/**
	 * Classifies a given text.
	 */
	public void classifyMessage(String message) throws Exception {
		// Check whether classifier has been built.
		if (m_Data.numInstances() == 0) {
			throw new Exception("No classifier available.");
		}

		// Check whether classifier and filter are up to date.
		if (!m_UpToDate) {

			// Initialize filter and tell it about the input format.
			m_Filter.setInputFormat(m_Data);

			// Generate word counts from the training data.
			Instances filteredData  = Filter.useFilter(m_Data, m_Filter);
			System.out.println("\n"+console+filteredData+"\n");
			// Rebuild classifier.
			m_Classifier.buildClassifier(filteredData);
			m_UpToDate = true;
			
		}

		// Make separate little test set so that text
		// does not get added to string attribute in m_Data.
		Instances testset = m_Data.stringFreeStructure();

		// Make text into test instance.
		Instance instance = makeInstance(message, testset);

		// Filter instance.
		m_Filter.input(instance);
		Instance filteredInstance = m_Filter.output();

		// Get index of predicted class value.
		System.out.println();
		double predictedBest = m_Classifier.classifyInstance(filteredInstance);
		double[] predicted = m_Classifier.distributionForInstance(filteredInstance);// .classifyInstance(filteredInstance);
		int secondPredicted = getSecondBest(predicted, predictedBest);

		System.out.println();

		// Output class value.
		String artist1 = m_Data.classAttribute().value((int)predictedBest);
		String artist2 = m_Data.classAttribute().value(secondPredicted);
		
		System.err.println("_______________________________________________________________");
		System.err.println(console+"The WebSite is similar to artists like:");
		System.err.println(console+artist1+" , "+artist2+"\n");
		System.err.println(console+"Similar artists:");
		
		//similar artists
		EchoNestAPI echoNest = new EchoNestAPI("LXDFSCNCNP8N662AW");
		
		//similar to 1st artist
		List<Artist> artists = echoNest.searchArtists(artist1);
	    Artist similar = artists.get(0);
	    System.err.print(console);
	    for (Artist artist : similar.getSimilar(5)) {
	    	System.err.print(artist.getName()+" , ");
	    }
	    
	    //similar to 2nd artist
	    artists = echoNest.searchArtists(artist2);
	    similar = artists.get(0);
	    System.err.print("\n"+console);
	    for (Artist artist : similar.getSimilar(5)) {
	    	System.err.print(artist.getName()+" , ");
	    }
	    System.err.println("\n_______________________________________________________________\n");
	    
	    /*
	     * NaiveBayesMultinomialUpdateable: Cannot handle string attributes!
	    //testing the evaluation
		Evaluation eval = new Evaluation(m_Data);
		eval.crossValidateModel(m_Classifier, m_Data, 4, new Random(1));
		System.out.println("EVAL__:"+eval.toSummaryString());
		System.out.println("END_____EVAL");
	*/}
	
	private int getSecondBest(double[] predicted, double predictedBest){
		double tmp=Double.MIN_VALUE;
		//double tmpS=Double.MIN_VALUE;
		int i=0;
		int best =0;
		int second =0;
		for(double d: predicted){
			if(d>tmp){
				//tmpS=tmp;
				tmp=d;
				second=best;
				best=i;
				//System.out.println(second+":"+tmpS+" "+best+":"+tmp);
			}
			i++;
		}
		return second;
	}

	/**
	 * Method that converts a text into an instance.
	 */
	private Instance makeInstance(String text, Instances data) {

		// Create instance of length two.
		Instance instance = new Instance(2);

		// Set value for text attribute
		Attribute messageAtt = data.attribute("Text");
		instance.setValue(messageAtt, messageAtt.addStringValue(text));

		// Give instance access to attribute information from the dataset.
		instance.setDataset(data);
		return instance;
	}

	/**
	 * Main method.
	 * @param i 
	 */
	public static void run(String dr_text, String dr_artist, int num_inst) {
		try {

			String message = dr_text;
			// Check if class value is given.
			String classValue = dr_artist;

			// If model file exists, read it, otherwise create new one.
			Path currentRelativePath = Paths.get("");
			String modelName = currentRelativePath.toAbsolutePath().toString()+"\\musicClassifier.model";
			if (modelName.length() == 0) {
				throw new Exception("Must provide name of model file.");
			}
			MusicClassifier messageCl = null;
			try {
				ObjectInputStream modelInObjectFile = new ObjectInputStream(new FileInputStream(modelName));
				messageCl = (MusicClassifier) modelInObjectFile.readObject();
				modelInObjectFile.close();
			} catch (FileNotFoundException e) {
				messageCl = new MusicClassifier(num_inst);
			} catch (java.io.EOFException e) {
				System.out.println("CMR> "+"Error reading MusicClassifierModel, please delete musicClassifier.model and restart the program, .model is too big to handle it");
				File model = new File(modelName);
				if(model.isFile()){
					File rename = new File("C:\\test\\model2cancel.model");
					rename.createNewFile();
					model.renameTo(rename);
					model.delete();
					model.deleteOnExit();
					rename.delete();
					rename.deleteOnExit();
				}
				System.exit(0);
			}

			// Process text.
			if (classValue.length() != 0) {
				messageCl.updateData(message.toString(), classValue);
			} else {
				messageCl.classifyMessage(message.toString());
			}

			// Save message classifier object.
			ObjectOutputStream modelOutObjectFile = new ObjectOutputStream(new FileOutputStream(modelName));
			modelOutObjectFile.writeObject(messageCl);
			modelOutObjectFile.close();
		} catch (Exception e) {
			e.printStackTrace();   
		}
	}
}
