package qa.qcri.qf.annotators;



import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.descriptor.TypeCapability;
import org.uimafit.factory.AnalysisEngineFactory;
import org.uimafit.util.JCasUtil;

import qa.qcri.qf.type.QuestionClass;
import qa.qcri.qf.type.QuestionID;

import com.google.common.base.Charsets;
import com.google.common.io.Files;


public class FromFileQuestionClassifier extends JCasAnnotator_ImplBase {
	private final Logger logger = LoggerFactory.getLogger(FromFileQuestionClassifier.class);
	

	
	public static final String CATEGORIES_FILE = "categoriesFile";
	@ConfigurationParameter(name = CATEGORIES_FILE, mandatory = true)
	private String categoriesFile;

	private Map<String, String> questionIdToClass;
	
	
	public static AnalysisEngineDescription getDescription(String categoriesFile) {
		AnalysisEngineDescription ann = null;
		try {
			ann = AnalysisEngineFactory.createPrimitiveDescription(FromFileQuestionClassifier.class, CATEGORIES_FILE);
		} catch (ResourceInitializationException e) {
			e.printStackTrace();
		}
		return ann;
	}
	
	private void readQuestionCategories(String fname) throws IOException {
		for (String line : Files.readLines(new File(fname), Charsets.UTF_8)) {
			String[] data = line.trim().split(" ");
			questionIdToClass.put(data[0].trim(), data[1].trim());
		}
	}
	
	@Override
	public void initialize(UimaContext aContext) throws ResourceInitializationException {		
		super.initialize(aContext);
		this.questionIdToClass = new HashMap<>();
		try {
			logger.debug("reading trainCategories from {}, testCategories from {}", categoriesFile);
			readQuestionCategories(categoriesFile);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void process(JCas cas) throws AnalysisEngineProcessException {
	
		String questionId = JCasUtil.selectSingle(cas, QuestionID.class).getQuestionID();
		
		//questionId = questionId.split("-")[1];
		
		
		String goldClass = questionIdToClass.get(questionId);
		
		logger.info("Question category: {} for question: {}", goldClass, questionId);
		
		
		
		if (JCasUtil.select(cas, QuestionClass.class).size()>0){
			Collection<QuestionClass> qClasses = JCasUtil.select(cas, QuestionClass.class);
			
			for (QuestionClass qclass : qClasses){
				cas.removeFsFromIndexes(qclass);
			}
			
		}
		
		QuestionClass questionClass = new QuestionClass(cas);
		questionClass.setBegin(0);
		questionClass.setEnd(cas.getDocumentText().length());
		questionClass.setQuestionClass(goldClass);
		questionClass.addToIndexes();
	}
}
