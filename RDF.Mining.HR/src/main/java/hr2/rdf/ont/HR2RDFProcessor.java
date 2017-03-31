package hr2.rdf.ont;

/*
 * Date: 
 * 	- dateIndeed.ttl 			[from dateDePeIndeed.xls]
 * Ontologii(part): 
 * 	- JobSeeker.ttl 			[JobSeeker, Ocupation, Education, WorkExperince, Candidacy]
 * 	- CompetenceOntology.ttl 	[Competence] 
 * 	- SkillOntology.ttl 		[Skill]
 * 
 * https://jena.apache.org/documentation/notes/sse.html
 * 
 * Query-classes
 * 	- education: Education_Title
 * 	- experience: Years_Experience, Position_held_count, Years_to_position
 * 	- skills: skillDescription
 * 	- positions: positions_help, last_position(?)
 * 
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.ModelFactory;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.Prediction;
import weka.classifiers.rules.DecisionTable;
import weka.classifiers.rules.OneR;
import weka.classifiers.rules.PART;
import weka.classifiers.trees.DecisionStump;
import weka.classifiers.trees.J48;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

public class HR2RDFProcessor {
	private static String WEKA_DATASET_FILE = "hr2.rdf.ont/hr_veka_data.arff";
	private static String WEKA_HEADER_FILE = "hr2.rdf.ont/hr_weka_header_definition.txt";
	private static String SPARQL_DATASET_FILE = "hr2.rdf.ont/hr_veka_raw_data.csv";

	public static void main(String[] args) throws Exception {
		/*
		queryOWLData(
				// "hr2.rdf.ont/hr_ont_query1.rq",
				
				// SKILLS
				// "hr2.rdf.ont/hr_ont_query_skills.rq",
				
				// POSITIONS
				// "hr2.rdf.ont/hr_ont_query_positions.rq", // ???
				
				// Experience_YEARS
			    //"hr2.rdf.ont/hr_ont_query_experience_years.rq", // ??? COUNT
				
				// EDUCATION
				// "hr2.rdf.ont/hr_ont_query_education.rq", // ???

				// AGGREGATE
				"hr2.rdf.ont/hr_ont_query_joins.rq",
				new String[] { 
						"hr2.rdf.ont/JobSeeker.ttl",
						"hr2.rdf.ont/CompetenceOntology.ttl",
						"hr2.rdf.ont/SkillOntology.ttl", 
						"hr2.rdf.ont/dateIndeed11.ttl" },
						FORMAT_CSV // to be used in runWekaJ48 workflow
						//null
				);
		*/
		
		prepareWekaFiles(WEKA_HEADER_FILE, // "hr2.rdf.ont/hr_weka_header_definition.txt",
				SPARQL_DATASET_FILE, // "hr2.rdf.ont/hr_veka_raw_data.csv",
				WEKA_DATASET_FILE // "hr2.rdf.ont/hr_veka_data.arff"
		);		
		
		// runWekaJ48_Simplex("3,4,8,13,16,17");
		runWekaJ48_Simplex2("3,4,5,8,13,16,17");
		// runWekaJ48_CrossValidation("3,4,8,13,16,17");
	}

	/* OWL Processing ------------------------------------------------- */
	// +
	private static String readQuery(String inputQueryFileName) throws Exception {
		// read q1.rq
		BufferedReader reader = new BufferedReader(new FileReader(inputQueryFileName));
		StringBuilder builder = new StringBuilder();
		reader.lines().forEach(line -> builder.append(line + "\n"));
		String queryString = builder.toString();

		System.out.println("==================================================");
		System.out.println("Query String: " + queryString);
		reader.close();

		return queryString;
	}

	// +
	private static OntModel readOWLData(String... rdfInputFileNames) {
		OntModel base = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);

		for (String rdfInputFileName : rdfInputFileNames)
			base.read(rdfInputFileName, "TTL");

		OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF, base);

		// write it to standard out
		// model.write(System.out);

		// System.out.println("... end readOWLData:: " + rdfInputFileNames);
		// System.out.println("------------------------------------------");

		return model;
	}

	// +
	private static final String FORMAT_CSV = "CSV";

	private static void queryOWLData(String inputQueryFileName, String[] rdfInputFileNames, String format)
			throws Exception {
		OntModel model = readOWLData(rdfInputFileNames);
		String queryString = readQuery(inputQueryFileName);

		// Execute the query and obtain results
		Query query = QueryFactory.create(queryString);
		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet results = qe.execSelect();

		// Output query results
		if (FORMAT_CSV.equals(format)) {
			System.out.println("-------------------------------------------------------------");
			ResultSetFormatter.outputAsCSV(System.out, results);
			qe.close();
			System.out.println("-------------------------------------------------------------");
			// Save as CSV
			FileOutputStream wekaFileOut = new FileOutputStream("hr2.rdf.ont/hr_veka_raw_data.csv");
			qe = QueryExecutionFactory.create(query, model);
			results = qe.execSelect();
			ResultSetFormatter.outputAsCSV(wekaFileOut, results);
			wekaFileOut.close();
			// Prepare Weka file
			prepareWekaFiles(WEKA_HEADER_FILE, // "hr2.rdf.ont/hr_weka_header_definition.txt",
					SPARQL_DATASET_FILE, // "hr2.rdf.ont/hr_veka_raw_data.csv",
					WEKA_DATASET_FILE // "hr2.rdf.ont/hr_veka_data.arff"
			);
		} else
			ResultSetFormatter.out(System.out, results, query);
		// Important - free up resources used running the query
		qe.close();
		System.out.println("... end queryOWLData multiple data files :: [with RDFS files] "
		// + queryString
		);
		System.out.println("-------------------------------------------------------------- ");
	}

	// Utils
	public static void prepareWekaFiles(String sourceFile1Path, String sourceFile2Path, String mergedFilePath) {
		File[] files = new File[2];
		files[0] = new File(sourceFile1Path);
		files[1] = new File(sourceFile2Path);

		File mergedFile = new File(mergedFilePath);
		if (mergedFile.exists())
			mergedFile.delete();

		mergeFiles(files, mergedFile);
	}

	public static void mergeFiles(File[] files, File mergedFile) {

		FileWriter fstream = null;
		BufferedWriter out = null;
		try {
			fstream = new FileWriter(mergedFile, true);
			out = new BufferedWriter(fstream);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		int fcount = 0;
		for (File f : files) {
			fcount++;
			System.out.println("merging: " + f.getName());
			FileInputStream fis;
			try {
				fis = new FileInputStream(f);
				BufferedReader in = new BufferedReader(new InputStreamReader(fis));

				String aLine;
				int lcount = 0;
				while ((aLine = in.readLine()) != null) {
					lcount++;
					// omit first line from data (second file)
					if (fcount == 2 && lcount == 1) {
						System.out.println(">>> Skip csv header line: " + aLine);
						continue;
					}
					out.write(aLine);
					out.newLine();
				}

				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		try {
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/* WEKA integration Simplex --------------------------------------- */
	// Training with all set. Test 1 instance
	public static void runWekaJ48_Simplex(String attrIdxPrm) throws Exception {
		System.out.println("------------------------------------------------------");
		System.out.println("Run Weka J48 Simplex ----------------------------------");

		// Read dataset
		BufferedReader datafile = readDataFile(WEKA_DATASET_FILE);
		Instances data = new Instances(datafile);
		// Select attributes
		String attrIndexes = "3,4,8,13,16,17";
		if (attrIdxPrm != null)
			attrIndexes = attrIdxPrm;
		
		data = configureDataSet(data, attrIndexes);
		// Set target class as last attribute
		data.setClassIndex(data.numAttributes() - 1);
		// Build J48 Tree Model
		Classifier model = new J48();
		model.buildClassifier(data);
		// Apply J48 Model
		Instance testInstance = data.get(2);
		double ClassLabel = 100;
		ClassLabel = model.classifyInstance(testInstance);
		System.out.println("------------------------------------------------------------------------");
		System.out.println("Classified: " + ClassLabel);

	}
	// Select training set and testing set from original DataSet
	public static void runWekaJ48_Simplex2(String attrIdxPrm) throws Exception {
		System.out.println("------------------------------------------------------");
		System.out.println("Run Weka J48 Simplex2 ----------------------------------");

		// Read dataset
		BufferedReader datafile = readDataFile(WEKA_DATASET_FILE);
		Instances data = new Instances(datafile);
		
		// Select attributes
		String attrIndexes = "3,4,8,13,16,17";
		if (attrIdxPrm != null)
			attrIndexes = attrIdxPrm;
		data = configureDataSet(data, attrIndexes);
		
		// Set target class as last attribute
		data.setClassIndex(data.numAttributes() - 1);
		// Build J48 Tree Model
		Classifier model = new J48();

		// training set
		Instances trainingSet =  new Instances(data, 4, 3);
		Evaluation validation = new Evaluation(trainingSet);
		model.buildClassifier(data);
		//
		Instances testingSet = new Instances(data, 0, 4);
		validation.evaluateModel(model, testingSet);
		//
		// Just to print summary
		System.out.println("==================================================================");
		System.out.println(validation.toClassDetailsString());
		System.out.println(validation.toCumulativeMarginDistributionString());
		System.out.println(validation.toMatrixString());
		System.out.println("TOTAL COST: " + validation.totalCost());
		System.out.println(validation.toSummaryString());
		System.out.println("==================================================================");		
	}

	private static Instances configureDataSet(Instances inst, String attrIndexes) throws Exception {
		Instances instNew;
		Remove remove;
		remove = new Remove();
		remove.setAttributeIndices(attrIndexes);
		remove.setInvertSelection(new Boolean("true").booleanValue());
		remove.setInputFormat(inst);
		instNew = Filter.useFilter(inst, remove);
		System.out.println("-------------------------------------------------------------");
		System.out.println("------ PREPARED WEKA DATASET --------------------------------");
		System.out.println(instNew);
		System.out.println("-------------------------------------------------------------");
		return instNew;
	}

	private static BufferedReader readDataFile(String filename) {
		BufferedReader inputReader = null;

		try {
			inputReader = new BufferedReader(new FileReader(filename));
		} catch (FileNotFoundException ex) {
			System.err.println("File not found: " + filename);
		}

		return inputReader;
	}

	/*
	 * WEKA integration Complex: Cross Validation: J48 vs...
	 * ---------------------------------------
	 */
	public static void runWekaJ48_CrossValidation(String attrIdxPrm) throws Exception {
		System.out.println("------------------------------------------------------");
		System.out.println("Run Weka J48 CrossValidation ----------------------------------");

		// Read dataset
		BufferedReader datafile = readDataFile(WEKA_DATASET_FILE);
		Instances data = new Instances(datafile);
		// Select attributes
		String attrIndexes = "3,4,8,13,16,17";
		if (attrIdxPrm != null)
			attrIndexes = attrIdxPrm;
		data = configureDataSet(data, attrIndexes);
		// Set target class as last attribute
		data.setClassIndex(data.numAttributes() - 1);

		// Choose a type of validation split !!!
		// Test DataSet Size: default 10
		Instances[][] split = crossValidationSplit(data, 4);

		// Separate split into training and testing arrays
		Instances[] trainingSplits = split[0];
		Instances[] testingSplits = split[1];

		// Choose a set of classifiers
		Classifier[] models = { new J48(), new PART(), new DecisionTable(), new OneR(), new DecisionStump() };

		// Run for each classifier model
		for (int j = 0; j < models.length; j++) {
			// Collect every group of predictions for current model in a
			// FastVector
			ArrayList<Prediction> predictionsArrayList = new ArrayList<Prediction>();

			Evaluation[] evaluations = new Evaluation[trainingSplits.length];

			// For each training-testing split pair, train and test the
			// classifier
			for (int i = 0; i < trainingSplits.length; i++) {
				evaluations[i] = simpleClassify(models[j], trainingSplits[i], testingSplits[i]);
				predictionsArrayList.addAll(evaluations[i].predictions());

				// Uncomment to see the summary for each training-testing pair.
				// System.out.println(">>>>>
				// ------------------------------------------------------");
				// System.out.println(models[j].toString());
				// System.out.println(">>>>>
				// ------------------------------------------------------");

			}

			// Calculate overall accuracy of current classifier on all splits
			double accuracy = calculateAccuracyWithPredictions(predictionsArrayList);
			// Print current classifier's name and accuracy in a complicated,
			// but nice-looking way.
			System.out.println("->" + models[j].getClass().getSimpleName() + ": " + String.format("%.2f%%", accuracy)
			// +
			// "\n--------------------------------------------------------------------"
			);

			// Just to print summary
			System.out.println("==================================================================");
			System.out.println(evaluations[0].toClassDetailsString());
			System.out.println(evaluations[0].toCumulativeMarginDistributionString());
			System.out.println(evaluations[0].toMatrixString());
			System.out.println("TOTAL COST: " + evaluations[0].totalCost());
			System.out.println(evaluations[0].toSummaryString());
			System.out.println("==================================================================");
		}

	}

	// Utils
	public static Evaluation simpleClassify(Classifier model, Instances trainingSet, Instances testingSet)
			throws Exception {
		Evaluation validation = new Evaluation(trainingSet);

		model.buildClassifier(trainingSet);
		validation.evaluateModel(model, testingSet);

		return validation;
	}

	public static double calculateAccuracyWithPredictions(ArrayList<Prediction> predictions) {
		double correct = 0;

		for (int i = 0; i < predictions.size(); i++) {
			Prediction np = predictions.get(i);
			if (np.predicted() == np.actual()) {
				correct++;
			}
		}

		return 100 * correct / predictions.size();
	}

	public static Instances[][] crossValidationSplit(Instances data, int numberOfFolds) {
		Instances[][] split = new Instances[2][numberOfFolds];

		for (int i = 0; i < numberOfFolds; i++) {
			split[0][i] = data.trainCV(numberOfFolds, i);
			split[1][i] = data.testCV(numberOfFolds, i);
		}

		return split;
	}

}
/*
 * BIND(CONCAT(?Job_Seeker, ",", ?last_name, ",", ?Education_Title, ",",
 * ?Years_to_position, ",", ?skillDescription, ",", ?Positions_held) AS
 * ?csv_format)
 */

/*
 * @RELATION Job_Seeker_Data
 * 
 * @ATTRIBUTE Job_Seeker string
 * 
 * @ATTRIBUTE Education_Title string
 * 
 * @ATTRIBUTE Education_Title_Degree {Master, Bachelor, Other}
 * 
 * @ATTRIBUTE Education_Title_Spec {Computer Science, Technology, Other}
 * 
 * @ATTRIBUTE Java_Programming_Skills {true, false}
 * 
 * @ATTRIBUTE SQL_Programming_Skills {true, false}
 * 
 * @ATTRIBUTE NOSQL_Programming_Skills {true, false}
 * 
 * @ATTRIBUTE UML_Skills {true, false}
 * 
 * @ATTRIBUTE SOA_Developer_Skills {true, false}
 * 
 * @ATTRIBUTE Java_Web_Developer_Skills {true, false}
 * 
 * @ATTRIBUTE Web_Developer_Skills {true, false}
 * 
 * @ATTRIBUTE DB_Developer_Skills {true, false}
 * 
 * @ATTRIBUTE Java_Persistence_Skills {true, false}
 * 
 * @ATTRIBUTE Years_Experience numeric
 * 
 * @ATTRIBUTE Position_held_count numeric
 * 
 * @ATTRIBUTE Years_to_position numeric
 * 
 * @ATTRIBUTE Positions_held string
 * 
 * @ATTRIBUTE class {Java-developer,Software-engineer,J2EE-developer}
 * 
 * @DATA
 */
